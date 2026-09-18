package com.testhub.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.testhub.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ui_test_case")
public class UiTestCase extends BaseEntity {

    private String name;
    private Long projectId;
    private String browser;
    private String baseUrl;
    private String steps;
    private String assertions;
    private String tags;
    private String status;
}
