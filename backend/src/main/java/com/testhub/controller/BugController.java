package com.testhub.controller;

import com.testhub.common.PageResult;
import com.testhub.common.Result;
import com.testhub.entity.Bug;
import com.testhub.service.BugService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "缺陷管理")
@RestController
@RequestMapping("/bugs")
@RequiredArgsConstructor
public class BugController {

    private final BugService bugService;

    @Operation(summary = "分页查询缺陷")
    @GetMapping("/page")
    public Result<PageResult<Bug>> page(@RequestParam(defaultValue = "1") Long current,
                                        @RequestParam(defaultValue = "10") Long size,
                                        @RequestParam(required = false) Long projectId,
                                        @RequestParam(required = false) String status,
                                        @RequestParam(required = false) String severity,
                                        @RequestParam(required = false) String keyword) {
        return Result.success(bugService.page(current, size, projectId, status, severity, keyword));
    }

    @Operation(summary = "获取缺陷详情")
    @GetMapping("/{id}")
    public Result<Bug> getById(@PathVariable Long id) {
        return Result.success(bugService.getById(id));
    }

    @Operation(summary = "新增/更新缺陷")
    @PostMapping
    public Result<Void> save(@RequestBody Bug bug) {
        bugService.save(bug);
        return Result.success();
    }

    @Operation(summary = "删除缺陷")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        bugService.delete(id);
        return Result.success();
    }

    @Operation(summary = "缺陷状态流转")
    @PutMapping("/{id}/status")
    public Result<Void> changeStatus(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        String status = (String) params.get("status");
        Long assigneeId = params.get("assigneeId") != null ? Long.valueOf(params.get("assigneeId").toString()) : null;
        bugService.changeStatus(id, status, assigneeId);
        return Result.success();
    }
}
