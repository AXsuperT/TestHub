package com.testhub.controller;

import com.testhub.common.PageResult;
import com.testhub.common.Result;
import com.testhub.entity.ApiCase;
import com.testhub.service.ApiCaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "接口用例管理")
@RestController
@RequestMapping("/api-cases")
@RequiredArgsConstructor
public class ApiCaseController {

    private final ApiCaseService apiCaseService;

    @Operation(summary = "分页查询接口用例")
    @GetMapping("/page")
    public Result<PageResult<ApiCase>> page(@RequestParam(defaultValue = "1") Long current,
                                            @RequestParam(defaultValue = "10") Long size,
                                            @RequestParam(required = false) Long projectId,
                                            @RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) String method) {
        return Result.success(apiCaseService.page(current, size, projectId, keyword, method));
    }

    @Operation(summary = "获取接口用例详情")
    @GetMapping("/{id}")
    public Result<ApiCase> getById(@PathVariable Long id) {
        return Result.success(apiCaseService.getById(id));
    }

    @Operation(summary = "新增/更新接口用例")
    @PostMapping
    public Result<Void> save(@RequestBody ApiCase apiCase) {
        apiCaseService.save(apiCase);
        return Result.success();
    }

    @Operation(summary = "删除接口用例")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        apiCaseService.delete(id);
        return Result.success();
    }
}
