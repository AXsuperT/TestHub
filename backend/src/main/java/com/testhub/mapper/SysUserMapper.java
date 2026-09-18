package com.testhub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.testhub.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
