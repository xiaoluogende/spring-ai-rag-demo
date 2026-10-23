package com.simpleai.demo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.simpleai.demo.entity.ChatRecord;
import com.simpleai.demo.mapper.ChatRecordMapper;
import com.simpleai.demo.service.ChatRecordService;
import org.springframework.stereotype.Service;

@Service
public class ChatRecordServiceImpl extends ServiceImpl<ChatRecordMapper, ChatRecord>
        implements ChatRecordService {
}
