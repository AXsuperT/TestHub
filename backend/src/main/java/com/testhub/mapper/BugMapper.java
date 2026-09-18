package com.testhub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.testhub.entity.Bug;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BugMapper extends BaseMapper<Bug> {
}
