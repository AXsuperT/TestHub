package com.testhub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("api_test_result")
public class ApiTestResult implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long recordId;
    private Long caseId;
    private String caseName;
    private String requestMethod;
    private String requestUrl;
    private String requestHeaders;
    private String requestBody;
    private Integer responseStatus;
    private String responseHeaders;
    private String responseBody;
    private Long responseTime;
    private String result;
    private String errorMessage;
    private String assertions;
    private LocalDateTime createTime;
}
