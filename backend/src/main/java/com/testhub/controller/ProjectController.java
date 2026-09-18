package com.testhub.controller;

import com.testhub.common.PageResult;
import com.testhub.common.Result;
import com.testhub.entity.Project;
import com.testhub.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "项目管理")
@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @Operation(summary = "分页查询项目")
    @GetMapping("/page")
    public Result<PageResult<Project>> page(@RequestParam(defaultValue = "1") Long current,
                                            @RequestParam(defaultValue = "10") Long size,
                                            @RequestParam(required = false) String keyword) {
        return Result.success(projectService.page(current, size, keyword));
    }

    @Operation(summary = "查询所有项目")
    @GetMapping("/list")
    public Result<List<Project>> list() {
        return Result.success(projectService.listAll());
    }

    @Operation(summary = "获取项目详情")
    @GetMapping("/{id}")
    public Result<Project> getById(@PathVariable Long id) {
        return Result.success(projectService.getById(id));
    }

    @Operation(summary = "新增/更新项目")
    @PostMapping
    public Result<Void> save(@RequestBody Project project) {
        projectService.save(project);
        return Result.success();
    }

    @Operation(summary = "删除项目")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        projectService.delete(id);
        return Result.success();
    }
}
