package com.testhub.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.testhub.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("test_case")
public class TestCase extends BaseEntity {

    private String caseNo;
    private String title;
    private Long moduleId;
    private Long projectId;
    private String caseType;
    private String priority;
    private String precondition;
    private String steps;
    private String expected;
    private String status;
    private String tags;
    private Long creatorId;
}
