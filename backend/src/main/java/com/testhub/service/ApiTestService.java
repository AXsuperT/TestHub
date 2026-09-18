package com.testhub.service;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.testhub.common.BusinessException;
import com.testhub.entity.ApiCase;
import com.testhub.entity.ApiTestRecord;
import com.testhub.entity.ApiTestResult;
import com.testhub.engine.HttpTestEngine;
import com.testhub.mapper.ApiCaseMapper;
import com.testhub.mapper.ApiTestRecordMapper;
import com.testhub.mapper.ApiTestResultMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 接口测试执行服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiTestService {

    private final HttpTestEngine httpTestEngine;
    private final ApiCaseMapper apiCaseMapper;
    private final ApiTestRecordMapper apiTestRecordMapper;
    private final ApiTestResultMapper apiTestResultMapper;

    /**
     * 执行单个接口用例
     */
    public ApiTestRecord executeSingle(Long caseId, Long operatorId) {
        ApiCase apiCase = apiCaseMapper.selectById(caseId);
        if (apiCase == null) {
            throw new BusinessException("用例不存在");
        }

        // 创建执行记录
        ApiTestRecord record = new ApiTestRecord();
        record.setCaseId(caseId);
        record.setProjectId(apiCase.getProjectId());
        record.setExecType("SINGLE");
        record.setTriggerType("MANUAL");
        record.setOperatorId(operatorId);
        record.setStatus("RUNNING");
        record.setStartTime(LocalDateTime.now());
        apiTestRecordMapper.insert(record);

        // 执行
        Map<String, String> contextVars = new HashMap<>();
        ApiTestResult result = httpTestEngine.execute(apiCase, contextVars);
        result.setRecordId(record.getId());
        apiTestResultMapper.insert(result);

        // 更新记录
        record.setTotalCount(1);
        record.setPassCount("PASS".equals(result.getResult()) ? 1 : 0);
        record.setFailCount("FAIL".equals(result.getResult()) ? 1 : 0);
        record.setStatus("PASS".equals(result.getResult()) ? "PASS" : "FAIL");
        record.setEndTime(LocalDateTime.now());
        record.setDurationMs(result.getResponseTime());
        apiTestRecordMapper.updateById(record);

        return record;
    }

    /**
     * 执行测试套件（多个用例）
     */
    public ApiTestRecord executeSuite(Long suiteId, String caseIdsStr, Long projectId, Long operatorId) {
        List<Long> caseIds = parseCaseIds(caseIdsStr);
        if (caseIds.isEmpty()) {
            throw new BusinessException("用例列表为空");
        }

        // 创建执行记录
        ApiTestRecord record = new ApiTestRecord();
        record.setSuiteId(suiteId);
        record.setProjectId(projectId);
        record.setExecType("SUITE");
        record.setTriggerType("MANUAL");
        record.setOperatorId(operatorId);
        record.setStatus("RUNNING");
        record.setStartTime(LocalDateTime.now());
        record.setTotalCount(caseIds.size());
        apiTestRecordMapper.insert(record);

        Map<String, String> contextVars = new HashMap<>();
        int passCount = 0;
        int failCount = 0;

        for (Long caseId : caseIds) {
            ApiCase apiCase = apiCaseMapper.selectById(caseId);
            if (apiCase == null) continue;

            ApiTestResult result = httpTestEngine.execute(apiCase, contextVars);
            result.setRecordId(record.getId());
            apiTestResultMapper.insert(result);

            if ("PASS".equals(result.getResult())) {
                passCount++;
            } else {
                failCount++;
            }
        }

        record.setPassCount(passCount);
        record.setFailCount(failCount);
        record.setStatus(failCount == 0 ? "PASS" : "FAIL");
        record.setEndTime(LocalDateTime.now());
        record.setDurationMs(java.time.Duration.between(record.getStartTime(), record.getEndTime()).toMillis());
        apiTestRecordMapper.updateById(record);

        return record;
    }

    /**
     * 异步执行套件
     */
    @Async
    public void executeSuiteAsync(Long suiteId, String caseIdsStr, Long projectId, Long operatorId) {
        try {
            executeSuite(suiteId, caseIdsStr, projectId, operatorId);
        } catch (Exception e) {
            log.error("异步执行套件失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取执行记录详情（含结果列表）
     */
    public Map<String, Object> getRecordDetail(Long recordId) {
        ApiTestRecord record = apiTestRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("记录不存在");
        }
        List<ApiTestResult> results = apiTestResultMapper.selectList(
            new LambdaQueryWrapper<ApiTestResult>().eq(ApiTestResult::getRecordId, recordId)
        );
        Map<String, Object> detail = new HashMap<>();
        detail.put("record", record);
        detail.put("results", results);
        return detail;
    }

    private List<Long> parseCaseIds(String caseIdsStr) {
        List<Long> ids = new ArrayList<>();
        if (caseIdsStr == null || caseIdsStr.isEmpty()) return ids;
        for (String id : caseIdsStr.split(",")) {
            try {
                ids.add(Long.parseLong(id.trim()));
            } catch (NumberFormatException ignored) {}
        }
        return ids;
    }
}
