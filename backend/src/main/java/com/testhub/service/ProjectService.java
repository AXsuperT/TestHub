package com.testhub.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.testhub.common.PageResult;
import com.testhub.entity.Project;
import com.testhub.mapper.ProjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectMapper projectMapper;

    public PageResult<Project> page(Long current, Long size, String keyword) {
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(keyword != null && !keyword.isEmpty(), Project::getName, keyword)
               .orderByDesc(Project::getCreateTime);
        Page<Project> page = projectMapper.selectPage(new Page<>(current, size), wrapper);
        return PageResult.of(page);
    }

    public List<Project> listAll() {
        return projectMapper.selectList(null);
    }

    public Project getById(Long id) {
        return projectMapper.selectById(id);
    }

    public void save(Project project) {
        if (project.getId() == null) {
            projectMapper.insert(project);
        } else {
            projectMapper.updateById(project);
        }
    }

    public void delete(Long id) {
        projectMapper.deleteById(id);
    }
}
