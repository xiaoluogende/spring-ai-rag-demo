package com.simpleai.demo.entity;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;
@Data
@TableName("chat_record")
public class ChatRecord {
    @TableId
    private Long id;
    private String question;
    private String answer;
    private LocalDateTime createTime;
}
