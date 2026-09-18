package com.testhub.controller;

import com.testhub.common.PageResult;
import com.testhub.common.Result;
import com.testhub.entity.TestCase;
import com.testhub.service.TestCaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "测试用例管理")
@RestController
@RequestMapping("/test-cases")
@RequiredArgsConstructor
public class TestCaseController {

    private final TestCaseService testCaseService;

    @Operation(summary = "分页查询测试用例")
    @GetMapping("/page")
    public Result<PageResult<TestCase>> page(@RequestParam(defaultValue = "1") Long current,
                                             @RequestParam(defaultValue = "10") Long size,
                                             @RequestParam(required = false) Long projectId,
                                             @RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) String caseType,
                                             @RequestParam(required = false) String priority) {
        return Result.success(testCaseService.page(current, size, projectId, keyword, caseType, priority));
    }

    @Operation(summary = "获取用例详情")
    @GetMapping("/{id}")
    public Result<TestCase> getById(@PathVariable Long id) {
        return Result.success(testCaseService.getById(id));
    }

    @Operation(summary = "新增/更新用例")
    @PostMapping
    public Result<Void> save(@RequestBody TestCase testCase) {
        testCaseService.save(testCase);
        return Result.success();
    }

    @Operation(summary = "删除用例")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        testCaseService.delete(id);
        return Result.success();
    }
}
