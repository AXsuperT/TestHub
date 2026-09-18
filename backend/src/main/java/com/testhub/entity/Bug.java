package com.testhub.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.testhub.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bug")
public class Bug extends BaseEntity {

    private String bugNo;
    private String title;
    private Long projectId;
    private Long moduleId;
    private String severity;
    private String priority;
    private String status;
    private String bugType;
    private String description;
    private String reproduceSteps;
    private String expectedResult;
    private String actualResult;
    private String attachments;
    private Long reporterId;
    private Long assigneeId;
    private String fixVersion;
    private String foundVersion;
}
