package com.testhub.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.testhub.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("test_case_module")
public class TestCaseModule extends BaseEntity {

    private String name;
    private Long parentId;
    private Long projectId;
    private Integer sort;
}
