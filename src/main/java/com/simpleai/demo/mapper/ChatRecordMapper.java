package com.simpleai.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simpleai.demo.entity.ChatRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatRecordMapper extends BaseMapper<ChatRecord> {
}
