package com.testhub.engine;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.testhub.entity.ApiCase;
import com.testhub.entity.ApiTestResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * HTTP 接口测试执行引擎
 * 负责发送HTTP请求、断言验证、变量提取
 */
@Slf4j
@Component
public class HttpTestEngine {

    @Value("${testhub.api.base-url:http://localhost:8088/api}")
    private String baseUrl;

    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    /**
     * 执行单个接口测试用例
     */
    public ApiTestResult execute(ApiCase apiCase, Map<String, String> contextVars) {
        ApiTestResult result = new ApiTestResult();
        result.setCaseId(apiCase.getId());
        result.setCaseName(apiCase.getName());
        result.setRequestMethod(apiCase.getMethod());

        long startTime = System.currentTimeMillis();
        try {
            // 1. 变量替换
            String url = replaceVariables(apiCase.getUrl(), contextVars);
            // 如果是相对路径，拼接 baseUrl
            if (!url.startsWith("http")) {
                url = baseUrl + (url.startsWith("/") ? url : "/" + url);
            }
            result.setRequestUrl(url);

            // 2. 构建请求
            HttpUriRequestBase request = buildRequest(apiCase, url, contextVars);

            // 3. 执行请求
            try (var response = httpClient.execute(request)) {
                int statusCode = response.getCode();
                result.setResponseStatus(statusCode);

                HttpEntity entity = response.getEntity();
                String responseBody = entity != null ? EntityUtils.toString(entity, StandardCharsets.UTF_8) : "";
                result.setResponseBody(responseBody);

                // 收集响应头
                Map<String, String> respHeaders = new HashMap<>();
                for (org.apache.hc.core5.http.Header h : response.getHeaders()) {
                    respHeaders.put(h.getName(), h.getValue());
                }
                result.setResponseHeaders(JSON.toJSONString(respHeaders));

                long endTime = System.currentTimeMillis();
                result.setResponseTime(endTime - startTime);

                // 4. 执行断言
                List<Map<String, Object>> assertionDetails = new ArrayList<>();
                boolean allPassed = executeAssertions(apiCase.getAssertions(), statusCode, responseBody, respHeaders, assertionDetails);
                result.setAssertions(JSON.toJSONString(assertionDetails));
                result.setResult(allPassed ? "PASS" : "FAIL");

                if (!allPassed) {
                    result.setErrorMessage("断言失败");
                }

                // 5. 提取变量
                if (apiCase.getExtractVars() != null && !apiCase.getExtractVars().isEmpty()) {
                    extractVariables(apiCase.getExtractVars(), responseBody, contextVars);
                }
            }
        } catch (Exception e) {
            log.error("接口执行异常: case={}, error={}", apiCase.getName(), e.getMessage(), e);
            result.setResult("FAIL");
            result.setErrorMessage(e.getMessage());
            result.setResponseTime(System.currentTimeMillis() - startTime);
        }

        return result;
    }

    /**
     * 构建HTTP请求
     */
    private HttpUriRequestBase buildRequest(ApiCase apiCase, String url, Map<String, String> contextVars) {
        String method = apiCase.getMethod().toUpperCase();
        HttpUriRequestBase request;

        // 处理Query参数
        if (apiCase.getQueryParams() != null && !apiCase.getQueryParams().isEmpty()) {
            url = appendQueryParams(url, apiCase.getQueryParams(), contextVars);
        }

        switch (method) {
            case "GET" -> request = new HttpGet(url);
            case "POST" -> request = new HttpPost(url);
            case "PUT" -> request = new HttpPut(url);
            case "DELETE" -> request = new HttpDelete(url);
            case "PATCH" -> request = new HttpPatch(url);
            default -> throw new IllegalArgumentException("不支持的请求方法: " + method);
        }

        // 设置请求头
        if (apiCase.getHeaders() != null && !apiCase.getHeaders().isEmpty()) {
            JSONObject headers = JSON.parseObject(apiCase.getHeaders());
            headers.forEach((k, v) -> request.setHeader(k, replaceVariables(v.toString(), contextVars)));
        }

        // 设置请求体
        if (apiCase.getBody() != null && !apiCase.getBody().isEmpty()) {
            String body = replaceVariables(apiCase.getBody(), contextVars);
            String bodyType = apiCase.getBodyType() != null ? apiCase.getBodyType().toUpperCase() : "NONE";
            ContentType contentType = switch (bodyType) {
                case "JSON" -> ContentType.APPLICATION_JSON;
                case "FORM" -> ContentType.APPLICATION_FORM_URLENCODED;
                default -> ContentType.TEXT_PLAIN;
            };
            request.setEntity(new StringEntity(body, contentType));
        }

        return request;
    }

    /**
     * 追加Query参数
     */
    private String appendQueryParams(String url, String queryParamsJson, Map<String, String> contextVars) {
        JSONObject params = JSON.parseObject(queryParamsJson);
        StringBuilder sb = new StringBuilder();
        params.forEach((k, v) -> {
            if (!sb.isEmpty()) sb.append("&");
            sb.append(URLEncoder.encode(k, StandardCharsets.UTF_8))
              .append("=")
              .append(URLEncoder.encode(replaceVariables(v.toString(), contextVars), StandardCharsets.UTF_8));
        });
        return url + (url.contains("?") ? "&" : "?") + sb;
    }

    /**
     * 执行断言
     * 支持的断言类型:
     * - statusCode: 状态码断言
     * - responseTime: 响应时间断言
     * - jsonPath: JSON路径断言
     * - contains: 包含断言
     * - equals: 相等断言
     * - regex: 正则匹配
     */
    private boolean executeAssertions(String assertionsJson, int statusCode, String responseBody,
                                      Map<String, String> responseHeaders, List<Map<String, Object>> details) {
        if (assertionsJson == null || assertionsJson.isEmpty()) {
            // 默认为状态码200断言
            Map<String, Object> d = new HashMap<>();
            d.put("field", "statusCode");
            d.put("operator", "equals");
            d.put("expected", 200);
            d.put("actual", statusCode);
            d.put("passed", statusCode == 200);
            details.add(d);
            return statusCode == 200;
        }

        boolean allPassed = true;
        try {
            JSONArray assertions = JSON.parseArray(assertionsJson);
            for (int i = 0; i < assertions.size(); i++) {
                JSONObject assertion = assertions.getJSONObject(i);
                String field = assertion.getString("field");
                String operator = assertion.getString("operator");
                Object expected = assertion.get("expected");

                Object actual = extractActualValue(field, statusCode, responseBody, responseHeaders);
                boolean passed = evaluateAssertion(operator, actual, expected);

                Map<String, Object> d = new HashMap<>();
                d.put("field", field);
                d.put("operator", operator);
                d.put("expected", expected);
                d.put("actual", actual);
                d.put("passed", passed);
                details.add(d);

                if (!passed) allPassed = false;
            }
        } catch (Exception e) {
            log.error("断言解析异常: {}", e.getMessage());
            allPassed = false;
        }
        return allPassed;
    }

    /**
     * 提取实际值
     */
    private Object extractActualValue(String field, int statusCode, String responseBody, Map<String, String> headers) {
        return switch (field) {
            case "statusCode" -> statusCode;
            case "responseBody" -> responseBody;
            case "responseTime" -> null; // 由调用方处理
            default -> {
                // JSONPath 支持: $.data.id, $.data.name
                if (field.startsWith("$.")) {
                    try {
                        JSONObject json = JSON.parseObject(responseBody);
                        yield getValueByJsonPath(json, field.substring(2));
                    } catch (Exception e) {
                        yield null;
                    }
                }
                // 尝试从响应JSON根层级提取字段
                try {
                    JSONObject json = JSON.parseObject(responseBody);
                    if (json != null && json.containsKey(field)) {
                        yield json.get(field);
                    }
                } catch (Exception ignored) {}
                // Header
                if (headers.containsKey(field)) {
                    yield headers.get(field);
                }
                yield null;
            }
        };
    }

    /**
     * 按JSON路径获取值
     */
    private Object getValueByJsonPath(JSONObject json, String path) {
        String[] keys = path.split("\\.");
        Object current = json;
        for (String key : keys) {
            if (current instanceof JSONObject obj) {
                current = obj.get(key);
            } else if (current instanceof JSONArray arr) {
                try {
                    int idx = Integer.parseInt(key);
                    current = arr.get(idx);
                } catch (NumberFormatException e) {
                    return null;
                }
            } else {
                return null;
            }
        }
        return current;
    }

    /**
     * 评估断言
     */
    private boolean evaluateAssertion(String operator, Object actual, Object expected) {
        if (actual == null) return false;
        try {
            return switch (operator) {
                case "equals", "eq" -> actual.toString().equals(expected.toString());
                case "notEquals", "ne" -> !actual.toString().equals(expected.toString());
                case "contains" -> actual.toString().contains(expected.toString());
                case "notContains" -> !actual.toString().contains(expected.toString());
                case "greaterThan", "gt" -> Double.parseDouble(actual.toString()) > Double.parseDouble(expected.toString());
                case "lessThan", "lt" -> Double.parseDouble(actual.toString()) < Double.parseDouble(expected.toString());
                case "greaterThanOrEqual", "gte" -> Double.parseDouble(actual.toString()) >= Double.parseDouble(expected.toString());
                case "lessThanOrEqual", "lte" -> Double.parseDouble(actual.toString()) <= Double.parseDouble(expected.toString());
                case "notNull" -> actual != null && !actual.toString().isEmpty();
                case "isNull" -> actual == null || actual.toString().isEmpty();
                case "regex" -> actual.toString().matches(expected.toString());
                default -> false;
            };
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 提取变量到上下文
     */
    private void extractVariables(String extractVarsJson, String responseBody, Map<String, String> contextVars) {
        try {
            JSONArray extractVars = JSON.parseArray(extractVarsJson);
            for (int i = 0; i < extractVars.size(); i++) {
                JSONObject ev = extractVars.getJSONObject(i);
                String varName = ev.getString("name");
                String path = ev.getString("path");
                JSONObject json = JSON.parseObject(responseBody);
                Object value = getValueByJsonPath(json, path.substring(2));
                if (value != null) {
                    contextVars.put(varName, value.toString());
                }
            }
        } catch (Exception e) {
            log.warn("变量提取失败: {}", e.getMessage());
        }
    }

    /**
     * 变量替换: 将 {{varName}} 替换为实际值
     */
    private String replaceVariables(String text, Map<String, String> contextVars) {
        if (text == null || contextVars == null || contextVars.isEmpty()) {
            return text;
        }
        String result = text;
        for (Map.Entry<String, String> entry : contextVars.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return result;
    }
}
