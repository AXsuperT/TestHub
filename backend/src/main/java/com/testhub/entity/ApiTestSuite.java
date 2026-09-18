package com.testhub.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.testhub.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("api_test_suite")
public class ApiTestSuite extends BaseEntity {

    private String name;
    private Long projectId;
    private String description;
    private String caseIds;
    private String runOrder;
}
