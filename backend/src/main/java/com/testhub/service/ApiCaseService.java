package com.testhub.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.testhub.common.PageResult;
import com.testhub.entity.ApiCase;
import com.testhub.mapper.ApiCaseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 接口用例管理服务
 */
@Service
@RequiredArgsConstructor
public class ApiCaseService {

    private final ApiCaseMapper apiCaseMapper;

    public PageResult<ApiCase> page(Long current, Long size, Long projectId, String keyword, String method) {
        LambdaQueryWrapper<ApiCase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(projectId != null, ApiCase::getProjectId, projectId)
               .like(keyword != null && !keyword.isEmpty(), ApiCase::getName, keyword)
               .eq(method != null && !method.isEmpty(), ApiCase::getMethod, method)
               .orderByDesc(ApiCase::getCreateTime);
        Page<ApiCase> page = apiCaseMapper.selectPage(new Page<>(current, size), wrapper);
        return PageResult.of(page);
    }

    public ApiCase getById(Long id) {
        return apiCaseMapper.selectById(id);
    }

    public void save(ApiCase apiCase) {
        if (apiCase.getId() == null) {
            apiCaseMapper.insert(apiCase);
        } else {
            apiCaseMapper.updateById(apiCase);
        }
    }

    public void delete(Long id) {
        apiCaseMapper.deleteById(id);
    }
}
