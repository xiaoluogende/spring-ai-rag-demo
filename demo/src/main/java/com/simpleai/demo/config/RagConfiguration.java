package com.simpleai.demo.config;

import com.simpleai.demo.service.ChatRecordService;
import com.simpleai.demo.service.DocumentService;
import com.simpleai.demo.service.RagService;
import com.simpleai.demo.service.impl.DocumentServiceImpl;
import com.simpleai.demo.service.impl.RagServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chroma.vectorstore.ChromaApi;
import org.springframework.ai.chroma.vectorstore.ChromaVectorStore;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

/**
 * RAG 模块装配。
 *
 * <p>由 {@code app.rag.enabled} 控制整个模块的启停。关闭时不会创建
 * EmbeddingModel / VectorStore / RagService / DocumentService，
 * 因此也不要求 Chroma 和百炼 Key 存在——这正是「可低侵入移植」的技术实现。
 */
@Configuration
@ConditionalOnProperty(name = "app.rag.enabled", havingValue = "true", matchIfMissing = true)
public class RagConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RagConfiguration.class);

    /**
     * 向量化模型：走阿里百炼，与对话模型（DeepSeek）分离。
     * 两者 base-url 不同，所以不能用 Spring AI 的自动配置。
     */
    @Bean
    public EmbeddingModel embeddingModel(
            @Value("${app.embedding.base-url}") String baseUrl,
            @Value("${app.embedding.api-key}") String apiKey,
            @Value("${app.embedding.model}") String model) {

        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .build();

        return new OpenAiEmbeddingModel(
                openAiApi,
                MetadataMode.EMBED,
                OpenAiEmbeddingOptions.builder().model(model).build());
    }

    @Bean
    public VectorStore vectorStore(
            EmbeddingModel embeddingModel,
            @Value("${spring.ai.vectorstore.chroma.client.host:http://localhost}") String host,
            @Value("${spring.ai.vectorstore.chroma.client.port:8000}") int port,
            @Value("${spring.ai.vectorstore.chroma.tenant-name:SpringAiTenant}") String tenantName,
            @Value("${spring.ai.vectorstore.chroma.database-name:SpringAiDatabase}") String databaseName,
            @Value("${spring.ai.vectorstore.chroma.collection-name:simple-ai-docs}") String collectionName) {

        ChromaApi chromaApi = ChromaApi.builder()
                .baseUrl(host + ":" + port)
                .build();

        ensureSchema(chromaApi, tenantName, databaseName, collectionName);

        return ChromaVectorStore.builder(chromaApi, embeddingModel)
                .tenantName(tenantName)
                .databaseName(databaseName)
                .collectionName(collectionName)
                // schema 已在上面手动建好，不再交给 Spring AI 判断
                .initializeSchema(false)
                .build();
    }

    @Bean
    public RagService ragService(ChatClient.Builder builder,
                                 VectorStore vectorStore,
                                 ChatRecordService chatRecordService,
                                 @Value("classpath:prompts/rag-system.st") Resource ragSystemPrompt,
                                 @Value("${app.rag.top-k:4}") int topK,
                                 @Value("${app.rag.similarity-threshold:0.6}") double similarityThreshold) {
        return new RagServiceImpl(builder, vectorStore, chatRecordService,
                ragSystemPrompt, topK, similarityThreshold);
    }

    @Bean
    public DocumentService documentService(VectorStore vectorStore) {
        return new DocumentServiceImpl(vectorStore);
    }

    /**
     * 预创建 tenant / database / collection。
     *
     * <p>为什么不交给 Spring AI 的 initializeSchema：1.1.0 的
     * {@code ChromaApi.getCollection} 用硬编码文案 {@code "Collection [x] does not exists"}
     * （多一个 s）判断「不存在」，而 Chroma 1.0 返回的是 {@code "does not exist"}，
     * 字符串比对失败导致异常直接抛出，建表逻辑根本没机会执行。
     *
     * <p>这里改成「直接创建 + 忽略已存在」，绕开所有查询判断，同时把度量方式
     * 显式指定为 cosine（Chroma 默认 L2，会让相似度分数语义变得反直觉）。
     */
    private void ensureSchema(ChromaApi api, String tenant, String database, String collection) {
        createIgnoringExists("tenant", tenant, () -> api.createTenant(tenant));
        createIgnoringExists("database", database, () -> api.createDatabase(tenant, database));
        // 用单参数构造器：它内部默认带 metadata {"hnsw:space": "cosine"}。
        // 不能用双参数版本——那个参数是 metadata 而非 configuration，
        // 传嵌套 Map 会被 Chroma 拒绝（422 MetadataValue）。
        createIgnoringExists("collection", collection,
                () -> api.createCollection(tenant, database, new ChromaApi.CreateCollectionRequest(collection)));

        log.info("Chroma 就绪: {}/{}/{}", tenant, database, collection);
    }

    private void createIgnoringExists(String type, String name, Runnable action) {
        try {
            action.run();
            log.info("已创建 Chroma {}: {}", type, name);
        } catch (Exception e) {
            String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
            // 已存在不是错误；其余异常（如连不上 Chroma）必须抛出，避免带病启动
            if (msg.contains("exist") || msg.contains("conflict")) {
                log.debug("Chroma {} 已存在，跳过: {}", type, name);
                return;
            }
            throw new IllegalStateException("初始化 Chroma " + type + " 失败: " + msg, e);
        }
    }
}
