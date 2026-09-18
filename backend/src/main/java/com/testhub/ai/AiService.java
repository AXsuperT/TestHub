package com.testhub.ai;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * AI 大模型服务
 * 支持测试用例生成、缺陷分析、测试报告总结
 */
@Slf4j
@Service
public class AiService {

    @Value("${ai.api-key}")
    private String apiKey;

    @Value("${ai.base-url}")
    private String baseUrl;

    @Value("${ai.model}")
    private String model;

    @Value("${ai.timeout:30000}")
    private int timeout;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * 通用对话接口
     */
    public String chat(String systemPrompt, String userPrompt) {
        // Mock模式：当API Key为占位符时返回模拟响应，方便演示
        if (apiKey == null || apiKey.startsWith("sk-your")) {
            return generateMockResponse(userPrompt);
        }
        try {
            JSONArray messages = new JSONArray();
            messages.add(buildMessage("system", systemPrompt));
            messages.add(buildMessage("user", userPrompt));

            return callOpenAI(messages);
        } catch (Exception e) {
            log.error("AI调用失败: {}", e.getMessage(), e);
            return "AI服务暂时不可用: " + e.getMessage();
        }
    }

    /**
     * 生成模拟AI响应（用于无API Key时的演示）
     */
    private String generateMockResponse(String userPrompt) {
        String lower = userPrompt.toLowerCase();
        if (lower.contains("登录") || lower.contains("login")) {
            return """
                    ## 📋 用户登录功能测试用例

                    ### 用例1: 正常登录-账号密码正确
                    - **前置条件**: 用户已注册
                    - **测试步骤**: 输入正确用户名和密码，点击登录
                    - **预期结果**: 登录成功，跳转至首页
                    - **优先级**: P0 | **类型**: 功能

                    ### 用例2: 密码错误登录
                    - **前置条件**: 用户已注册
                    - **测试步骤**: 输入正确用户名和错误密码，点击登录
                    - **预期结果**: 提示"密码错误"，停留在登录页
                    - **优先级**: P0 | **类型**: 异常

                    ### 用例3: 用户名为空
                    - **测试步骤**: 不输入用户名，点击登录
                    - **预期结果**: 提示"请输入用户名"
                    - **优先级**: P1 | **类型**: 边界

                    ### 用例4: 密码为空
                    - **测试步骤**: 输入用户名，不输入密码，点击登录
                    - **预期结果**: 提示"请输入密码"
                    - **优先级**: P1 | **类型**: 边界

                    ### 用例5: SQL注入测试
                    - **测试步骤**: 用户名输入 `' OR 1=1 --`
                    - **预期结果**: 登录失败，系统安全拦截
                    - **优先级**: P0 | **类型**: 安全

                    ### 用例6: 连续错误登录锁定
                    - **测试步骤**: 连续5次输入错误密码
                    - **预期结果**: 账号锁定15分钟
                    - **优先级**: P1 | **类型**: 安全
                    """;
        }
        if (lower.contains("缺陷") || lower.contains("bug") || lower.contains("分析")) {
            return """
                    ## 🔍 缺陷根因分析

                    ### 1. 缺陷根因分析
                    该缺陷可能由以下原因导致：
                    - 前端表单未做输入长度校验
                    - 后端未对请求参数进行边界检查
                    - 数据库字段长度设置不合理

                    ### 2. 影响范围
                    - 所有涉及该字段的表单提交接口
                    - 可能导致数据截断或数据库异常

                    ### 3. 修复建议
                    - 前端：添加 maxlength 属性和实时校验
                    - 后端：使用 @Size 注解校验参数长度
                    - 数据库：合理设置字段长度并添加索引

                    ### 4. 回归测试建议
                    - 测试边界值（刚好等于限制长度、超过限制长度）
                    - 测试特殊字符和SQL注入
                    - 测试并发提交场景

                    ### 5. 预防措施
                    - 代码审查时关注参数校验
                    - 单元测试覆盖边界场景
                    - 集成 SonarQube 静态代码扫描
                    """;
        }
        if (lower.contains("接口") || lower.contains("api")) {
            return """
                    ## 🔌 接口测试用例

                    | 用例名称 | 方法 | URL | 参数 | 预期结果 |
                    |---------|------|-----|------|---------|
                    | 正常请求 | POST | /api/auth/login | {username,password} | 200, 返回token |
                    | 密码为空 | POST | /api/auth/login | {username} | 400, 参数错误 |
                    | 用户不存在 | POST | /api/auth/login | {username:xxx} | 401, 用户不存在 |
                    | 密码错误 | POST | /api/auth/login | {username,wrongpwd} | 401, 密码错误 |
                    | 无Token访问 | GET | /api/users | - | 401, 未授权 |
                    """;
        }
        return """
                ## 🤖 TestHub AI助手（演示模式）

                这是AI助手的模拟响应。当配置有效的AI API Key后，将获得真实的智能分析结果。

                **支持的功能：**
                1. 📝 生成测试用例 - 输入需求描述即可生成详细测试用例
                2. 🐛 缺陷分析 - 输入缺陷信息进行根因分析
                3. 🔌 生成接口用例 - 根据API文档生成接口测试用例
                4. 📊 报告总结 - 总结测试报告并给出质量评估

                **配置真实AI服务：**
                在 `application.yml` 中配置 `ai.api-key` 和 `ai.base-url` 即可启用真实AI能力。
                """;
    }

    /**
     * 根据需求描述生成测试用例
     */
    public String generateTestCases(String requirement, String module) {
        String systemPrompt = """
                你是一名资深测试工程师，擅长根据需求描述编写详细的测试用例。
                请按照以下格式输出测试用例：
                1. 用例标题
                2. 前置条件
                3. 测试步骤
                4. 预期结果
                5. 优先级(P0-P3)
                6. 用例类型(功能/接口/边界/异常)
                
                请覆盖正常流程、异常流程、边界值、兼容性等场景。
                """;
        String userPrompt = String.format("需求模块: %s\n需求描述: %s\n\n请生成详细的测试用例。", module, requirement);
        return chat(systemPrompt, userPrompt);
    }

    /**
     * 缺陷分析 - 根据缺陷信息分析根因和修复建议
     */
    public String analyzeBug(String title, String description, String errorMessage) {
        String systemPrompt = """
                你是一名资深质量保障专家，擅长分析软件缺陷的根本原因并给出修复建议。
                请从以下维度分析：
                1. 缺陷根因分析
                2. 可能的影响范围
                3. 修复建议
                4. 回归测试建议
                5. 预防措施
                """;
        String userPrompt = String.format("缺陷标题: %s\n缺陷描述: %s\n错误信息: %s", title, description, errorMessage);
        return chat(systemPrompt, userPrompt);
    }

    /**
     * 测试报告总结
     */
    public String summarizeReport(int total, int pass, int fail, String details) {
        String systemPrompt = """
                你是一名测试经理，擅长总结测试报告并给出质量评估。
                请输出：
                1. 测试概览
                2. 通过率分析
                3. 主要风险点
                4. 改进建议
                5. 上线建议
                """;
        String userPrompt = String.format("总用例数: %d, 通过: %d, 失败: %d\n执行详情: %s", total, pass, fail, details);
        return chat(systemPrompt, userPrompt);
    }

    /**
     * 生成接口测试用例
     */
    public String generateApiCases(String apiDoc) {
        String systemPrompt = """
                你是一名接口测试专家，根据API文档生成接口测试用例。
                请覆盖：
                1. 正常参数请求
                2. 参数缺失/为空
                3. 参数类型错误
                4. 边界值
                5. 权限校验
                6. 并发/重复请求
                输出格式：用例名称 | 请求方法 | 请求URL | 请求参数 | 预期结果
                """;
        return chat(systemPrompt, "API文档:\n" + apiDoc);
    }

    private JSONObject buildMessage(String role, String content) {
        JSONObject msg = new JSONObject();
        msg.put("role", role);
        msg.put("content", content);
        return msg;
    }

    private String callOpenAI(JSONArray messages) throws IOException, InterruptedException {
        JSONObject body = new JSONObject();
        body.put("model", model);
        body.put("messages", messages);
        body.put("temperature", 0.7);
        body.put("max_tokens", 2000);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .timeout(Duration.ofMillis(timeout))
                .POST(HttpRequest.BodyPublishers.ofString(body.toJSONString(), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        if (response.statusCode() != 200) {
            throw new RuntimeException("API返回错误: " + response.statusCode() + " " + response.body());
        }

        JSONObject result = JSON.parseObject(response.body());
        JSONArray choices = result.getJSONArray("choices");
        if (choices != null && !choices.isEmpty()) {
            return choices.getJSONObject(0).getJSONObject("message").getString("content");
        }
        return "无返回内容";
    }
}
