package com.simpleai.demo.auth;

import cn.dev33.satoken.stp.StpInterface;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 权限数据源。
 *
 * <p>演示用内存实现。真实项目里这里应该查数据库或缓存，
 * 对接现有权限体系时只需替换这个类，业务代码不用改。
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    private static final Map<String, List<String>> PERMISSIONS = Map.of(
            // 管理员：可问答、可上传文档
            "admin", List.of("rag:ask", "doc:upload"),
            // 访客：只能问答，不能上传
            "guest", List.of("rag:ask"));

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return PERMISSIONS.getOrDefault(String.valueOf(loginId), List.of());
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return List.of();
    }
}
