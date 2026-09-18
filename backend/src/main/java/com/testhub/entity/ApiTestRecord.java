package com.testhub.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.testhub.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("api_test_record")
public class ApiTestRecord extends BaseEntity {

    private Long suiteId;
    private Long caseId;
    private Long projectId;
    private String execType;
    private Integer totalCount;
    private Integer passCount;
    private Integer failCount;
    private Integer skipCount;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long durationMs;
    private String triggerType;
    private Long operatorId;
    private String reportPath;
}
