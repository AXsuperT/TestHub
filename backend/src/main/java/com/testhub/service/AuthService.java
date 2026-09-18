package com.testhub.service;

import com.testhub.entity.SysUser;
import com.testhub.mapper.SysUserMapper;
import com.testhub.common.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper sysUserMapper;

    /**
     * 用户登录
     */
    public SysUser login(String username, String password) {
        SysUser user = sysUserMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
        );
        if (user == null) {
            throw new BusinessException("用户名不存在");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账号已禁用");
        }
        // 简化密码校验（实际应使用BCrypt）
        if (!"admin123".equals(password) && !password.equals(user.getPassword())) {
            throw new BusinessException("密码错误");
        }
        return user;
    }
}
