package com.testhub.controller;

import com.testhub.common.Result;
import com.testhub.entity.ApiTestRecord;
import com.testhub.service.ApiTestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "接口测试执行")
@RestController
@RequestMapping("/api-tests")
@RequiredArgsConstructor
public class ApiTestController {

    private final ApiTestService apiTestService;

    @Operation(summary = "执行单个接口用例")
    @PostMapping("/execute/{caseId}")
    public Result<ApiTestRecord> executeSingle(@PathVariable Long caseId,
                                               @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        ApiTestRecord record = apiTestService.executeSingle(caseId, userId);
        return Result.success("执行完成", record);
    }

    @Operation(summary = "执行测试套件")
    @PostMapping("/execute-suite")
    public Result<ApiTestRecord> executeSuite(@RequestBody Map<String, Object> params,
                                              @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        Long suiteId = params.get("suiteId") != null ? Long.valueOf(params.get("suiteId").toString()) : null;
        String caseIds = (String) params.get("caseIds");
        Long projectId = Long.valueOf(params.get("projectId").toString());
        ApiTestRecord record = apiTestService.executeSuite(suiteId, caseIds, projectId, userId);
        return Result.success("执行完成", record);
    }

    @Operation(summary = "异步执行测试套件")
    @PostMapping("/execute-suite/async")
    public Result<String> executeSuiteAsync(@RequestBody Map<String, Object> params,
                                          @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        Long suiteId = params.get("suiteId") != null ? Long.valueOf(params.get("suiteId").toString()) : null;
        String caseIds = (String) params.get("caseIds");
        Long projectId = Long.valueOf(params.get("projectId").toString());
        apiTestService.executeSuiteAsync(suiteId, caseIds, projectId, userId);
        return Result.success("已提交异步执行");
    }

    @Operation(summary = "获取执行记录详情")
    @GetMapping("/records/{recordId}")
    public Result<Map<String, Object>> getRecordDetail(@PathVariable Long recordId) {
        return Result.success(apiTestService.getRecordDetail(recordId));
    }
}
