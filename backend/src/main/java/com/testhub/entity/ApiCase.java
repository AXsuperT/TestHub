package com.testhub.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.testhub.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("api_case")
public class ApiCase extends BaseEntity {

    private String name;
    private Long moduleId;
    private Long projectId;
    private Long envId;
    private String method;
    private String url;
    private String headers;
    private String queryParams;
    private String bodyType;
    private String body;
    private String assertions;
    private String extractVars;
    private Integer timeout;
    private String tags;
    private String status;
}
