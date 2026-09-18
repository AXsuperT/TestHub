package com.testhub.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.testhub.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("api_environment")
public class ApiEnvironment extends BaseEntity {

    private String name;
    private String baseUrl;
    private String headers;
    private String variables;
    private Long projectId;
}
