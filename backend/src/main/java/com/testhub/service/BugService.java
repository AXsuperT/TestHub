package com.testhub.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.testhub.common.PageResult;
import com.testhub.entity.Bug;
import com.testhub.mapper.BugMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 缺陷管理服务
 */
@Service
@RequiredArgsConstructor
public class BugService {

    private final BugMapper bugMapper;

    public PageResult<Bug> page(Long current, Long size, Long projectId, String status, String severity, String keyword) {
        LambdaQueryWrapper<Bug> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(projectId != null, Bug::getProjectId, projectId)
               .eq(status != null && !status.isEmpty(), Bug::getStatus, status)
               .eq(severity != null && !severity.isEmpty(), Bug::getSeverity, severity)
               .like(keyword != null && !keyword.isEmpty(), Bug::getTitle, keyword)
               .orderByDesc(Bug::getCreateTime);
        Page<Bug> page = bugMapper.selectPage(new Page<>(current, size), wrapper);
        return PageResult.of(page);
    }

    public Bug getById(Long id) {
        return bugMapper.selectById(id);
    }

    public void save(Bug bug) {
        if (bug.getId() == null) {
            bug.setBugNo(generateBugNo());
            if (bug.getStatus() == null) bug.setStatus("OPEN");
            bugMapper.insert(bug);
        } else {
            bugMapper.updateById(bug);
        }
    }

    public void delete(Long id) {
        bugMapper.deleteById(id);
    }

    /**
     * 状态流转: OPEN -> ASSIGNED -> FIXED -> VERIFIED -> CLOSED
     */
    public void changeStatus(Long id, String status, Long assigneeId) {
        Bug bug = bugMapper.selectById(id);
        if (bug == null) throw new com.testhub.common.BusinessException("缺陷不存在");
        bug.setStatus(status);
        if (assigneeId != null) bug.setAssigneeId(assigneeId);
        bugMapper.updateById(bug);
    }

    private synchronized String generateBugNo() {
        Long count = bugMapper.selectCount(null);
        return String.format("BUG-%05d", count + 1);
    }
}
