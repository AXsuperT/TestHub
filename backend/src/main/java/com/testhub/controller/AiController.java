package com.testhub.controller;

import com.testhub.ai.AiService;
import com.testhub.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "AI智能助手")
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @Operation(summary = "通用AI对话")
    @PostMapping("/chat")
    public Result<String> chat(@RequestBody Map<String, String> params) {
        String message = params.get("message");
        String response = aiService.chat("你是TestHub测试平台的AI助手，帮助用户解决测试相关问题。", message);
        return Result.success(response);
    }

    @Operation(summary = "AI生成测试用例")
    @PostMapping("/generate-cases")
    public Result<String> generateTestCases(@RequestBody Map<String, String> params) {
        String requirement = params.get("requirement");
        String module = params.getOrDefault("module", "未指定");
        String result = aiService.generateTestCases(requirement, module);
        return Result.success(result);
    }

    @Operation(summary = "AI缺陷分析")
    @PostMapping("/analyze-bug")
    public Result<String> analyzeBug(@RequestBody Map<String, String> params) {
        String title = params.getOrDefault("title", "");
        String description = params.getOrDefault("description", "");
        String errorMessage = params.getOrDefault("errorMessage", "");
        String result = aiService.analyzeBug(title, description, errorMessage);
        return Result.success(result);
    }

    @Operation(summary = "AI生成接口测试用例")
    @PostMapping("/generate-api-cases")
    public Result<String> generateApiCases(@RequestBody Map<String, String> params) {
        String apiDoc = params.get("apiDoc");
        String result = aiService.generateApiCases(apiDoc);
        return Result.success(result);
    }

    @Operation(summary = "AI测试报告总结")
    @PostMapping("/summarize-report")
    public Result<String> summarizeReport(@RequestBody Map<String, Object> params) {
        int total = Integer.parseInt(params.getOrDefault("total", "0").toString());
        int pass = Integer.parseInt(params.getOrDefault("pass", "0").toString());
        int fail = Integer.parseInt(params.getOrDefault("fail", "0").toString());
        String details = params.getOrDefault("details", "").toString();
        String result = aiService.summarizeReport(total, pass, fail, details);
        return Result.success(result);
    }
}
