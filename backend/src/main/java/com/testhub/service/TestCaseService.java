package com.testhub.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.testhub.common.PageResult;
import com.testhub.entity.TestCase;
import com.testhub.mapper.TestCaseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 测试用例管理服务
 */
@Service
@RequiredArgsConstructor
public class TestCaseService {

    private final TestCaseMapper testCaseMapper;

    public PageResult<TestCase> page(Long current, Long size, Long projectId, String keyword, String caseType, String priority) {
        LambdaQueryWrapper<TestCase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(projectId != null, TestCase::getProjectId, projectId)
               .like(keyword != null && !keyword.isEmpty(), TestCase::getTitle, keyword)
               .eq(caseType != null && !caseType.isEmpty(), TestCase::getCaseType, caseType)
               .eq(priority != null && !priority.isEmpty(), TestCase::getPriority, priority)
               .orderByDesc(TestCase::getCreateTime);
        Page<TestCase> page = testCaseMapper.selectPage(new Page<>(current, size), wrapper);
        return PageResult.of(page);
    }

    public TestCase getById(Long id) {
        return testCaseMapper.selectById(id);
    }

    public void save(TestCase testCase) {
        if (testCase.getId() == null) {
            // 生成用例编号
            testCase.setCaseNo(generateCaseNo());
            testCaseMapper.insert(testCase);
        } else {
            testCaseMapper.updateById(testCase);
        }
    }

    public void delete(Long id) {
        testCaseMapper.deleteById(id);
    }

    public List<TestCase> listByProject(Long projectId) {
        return testCaseMapper.selectList(
            new LambdaQueryWrapper<TestCase>().eq(TestCase::getProjectId, projectId)
        );
    }

    private synchronized String generateCaseNo() {
        Long count = testCaseMapper.selectCount(null);
        return String.format("TC-%05d", count + 1);
    }
}
