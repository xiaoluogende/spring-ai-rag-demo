package com.simpleai.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simpleai.demo.entity.DemoEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DemoMapper extends BaseMapper<DemoEntity> {
}