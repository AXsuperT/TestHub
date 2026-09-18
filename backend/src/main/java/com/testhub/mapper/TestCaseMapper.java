package com.testhub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.testhub.entity.TestCase;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TestCaseMapper extends BaseMapper<TestCase> {
}
