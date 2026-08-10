/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.console.controller.v3.ai;

import com.alibaba.nacos.api.annotation.Since;
import com.alibaba.nacos.api.ai.model.skills.Skill;
import com.alibaba.nacos.api.ai.model.skills.SkillResource;
import com.alibaba.nacos.api.annotation.NacosApi;
import com.alibaba.nacos.api.common.ApiType;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.copilot.adapter.StreamResponseCallback;
import com.alibaba.nacos.copilot.constant.CopilotConstants;
import com.alibaba.nacos.copilot.form.PromptDebugForm;
import com.alibaba.nacos.copilot.form.PromptOptimizationForm;
import com.alibaba.nacos.copilot.form.SkillGenerationForm;
import com.alibaba.nacos.copilot.form.SkillOptimizationForm;
import com.alibaba.nacos.copilot.model.PromptDebugRequest;
import com.alibaba.nacos.copilot.model.PromptDebugResponse;
import com.alibaba.nacos.copilot.model.PromptOptimizationRequest;
import com.alibaba.nacos.copilot.model.PromptOptimizationResponse;
import com.alibaba.nacos.copilot.model.SkillGenerationRequest;
import com.alibaba.nacos.copilot.model.SkillGenerationResponse;
import com.alibaba.nacos.copilot.model.SkillOptimizationRequest;
import com.alibaba.nacos.copilot.model.SkillOptimizationResponse;
import com.alibaba.nacos.copilot.service.PromptDebugService;
import com.alibaba.nacos.copilot.service.PromptOptimizationService;
import com.alibaba.nacos.copilot.service.SkillGenerationService;
import com.alibaba.nacos.copilot.service.SkillOptimizationService;
import com.alibaba.nacos.core.paramcheck.ExtractorManager;
import com.alibaba.nacos.plugin.auth.constant.ActionTypes;
import com.alibaba.nacos.plugin.auth.constant.SignType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 控制台 Copilot AI 辅助 REST 控制器，提供 Skill/Prompt 优化、生成与调试的 SSE 流式接口。
 * 映射 {@link com.alibaba.nacos.copilot.constant.CopilotConstants#COPILOT_CONSOLE_PATH}，
 * 使用 {@link CopilotHttpParamExtractor} 提取权限校验参数。
 *
 * Console Copilot controller.
 *
 * @author nacos
 */
@NacosApi
@RestController
@RequestMapping(CopilotConstants.COPILOT_CONSOLE_PATH)
@ExtractorManager.Extractor(httpExtractor = CopilotHttpParamExtractor.class)
public class ConsoleCopilotController {
    
    /** 日志记录器。 */
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleCopilotController.class);
    
    /** Skill 优化流式服务。 */
    private final SkillOptimizationService skillOptimizationService;
    
    /** Skill 生成流式服务。 */
    private final SkillGenerationService skillGenerationService;
    
    /** Prompt 优化流式服务。 */
    private final PromptOptimizationService promptOptimizationService;
    
    /** Prompt 调试流式服务。 */
    private final PromptDebugService promptDebugService;
    
    @Autowired
    public ConsoleCopilotController(SkillOptimizationService skillOptimizationService,
        SkillGenerationService skillGenerationService,
        PromptOptimizationService promptOptimizationService,
        PromptDebugService promptDebugService) {
        this.skillOptimizationService = skillOptimizationService;
        this.skillGenerationService = skillGenerationService;
        this.promptOptimizationService = promptOptimizationService;
        this.promptDebugService = promptDebugService;
    }
    
    /**
      * 流式优化 Skill（SSE）。
     * Optimize skill with stream response (SSE).
     *
     * @param form skill optimization form
     * @return SSE emitter for stream response
     * @throws NacosException if validation fails
     */
    @Since("3.2.0")
    @PostMapping(value = CopilotConstants.SKILL_OPTIMIZE_PATH,
        produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @SuppressWarnings("PMD.MethodTooLongRule")
    public SseEmitter optimizeSkillStream(
        @RequestBody(required = false) SkillOptimizationForm form) {
        // 创建 5 分钟超时的 SSE 发射器
        SseEmitter emitter = new SseEmitter(300000L);
        
        // 处理空请求体
        if (form == null) {
            try {
                SkillOptimizationResponse errorResponse = new SkillOptimizationResponse();
                errorResponse.setDone(true);
                errorResponse.setExplanation("请求体不能为空");
                emitter.send(
                    SseEmitter.event().data(JacksonUtils.toJson(errorResponse)).name("error"));
                emitter.complete();
            } catch (IOException ioException) {
                LOGGER.error("Failed to send error SSE event", ioException);
                emitter.completeWithError(ioException);
            }
            return emitter;
        }
        
        try {
            form.validate();
        } catch (Exception e) {
            LOGGER.error("Form validation failed", e);
            try {
                SkillOptimizationResponse errorResponse = new SkillOptimizationResponse();
                errorResponse.setDone(true);
                errorResponse.setExplanation("请求验证失败：" + e.getMessage());
                emitter.send(
                    SseEmitter.event().data(JacksonUtils.toJson(errorResponse)).name("error"));
                emitter.complete();
            } catch (IOException ioException) {
                LOGGER.error("Failed to send validation error SSE event", ioException);
                emitter.complete();
            }
            return emitter;
        }
        
        // 组装后端请求对象
        SkillOptimizationRequest request = new SkillOptimizationRequest();
        request.setSkill(form.getSkill());
        request.setOptimizationGoal(form.getOptimizationGoal());
        request.setConversationHistory(form.getConversationHistory());
        request.setTargetFileName(form.getTargetFileName());
        
        // 若指定 MCP 工具则写入请求参数
        if (form.getSelectedMcpTools() != null && !form.getSelectedMcpTools().isEmpty()) {
            java.util.Map<String, Object> params = new java.util.HashMap<>();
            params.put("selectedMcpTools", form.getSelectedMcpTools());
            request.setParams(params);
        }
        
        // 调用优化服务并通过流式回调推送 SSE 事件
        skillOptimizationService.optimizeSkillStream(request,
            new StreamResponseCallback<SkillOptimizationResponse>() {
                
                /** 流式回调：推送下一条 SSE 消息。 */
                @Override
                public void onNext(SkillOptimizationResponse response) {
                    try {
                        // 向前端推送前过滤 SKILL.md 资源
                        if (response != null && response.getOptimizedSkill() != null) {
                            Skill optimizedSkill = response.getOptimizedSkill();
                            if (optimizedSkill.getResource() != null
                                && !optimizedSkill.getResource().isEmpty()) {
                                Map<String, SkillResource> filteredResources = new HashMap<>(
                                    optimizedSkill.getResource().size());
                                boolean hasFiltered = false;
                                
                                for (Map.Entry<String, SkillResource> entry : optimizedSkill
                                    .getResource().entrySet()) {
                                    String key = entry.getKey();
                                    SkillResource resource = entry.getValue();
                                    
                                    // 判断资源名或键是否为 SKILL.md（忽略大小写）
                                    String resourceName =
                                        resource != null && resource.getName() != null
                                            ? resource.getName() : "";
                                    String resourceKey = key != null ? key : "";
                                    
                                    boolean isSkillMd =
                                        "SKILL.MD".equalsIgnoreCase(resourceName)
                                            || "SKILL.MD".equalsIgnoreCase(
                                                resourceKey)
                                            || resourceName.toUpperCase().contains("SKILL.MD")
                                            || resourceKey.toUpperCase().contains("SKILL.MD");
                                    
                                    if (isSkillMd) {
                                        hasFiltered = true;
                                        LOGGER.warn(
                                            "Filtered out SKILL.md resource: key={}, name={}", key,
                                            resourceName);
                                        continue;
                                    }
                                    
                                    filteredResources.put(key, resource);
                                }
                                
                                if (hasFiltered) {
                                    optimizedSkill.setResource(filteredResources);
                                    response.setOptimizedSkill(optimizedSkill);
                                }
                            }
                        }
                        
                        // 发送 SSE 消息事件
                        emitter.send(
                            SseEmitter.event().data(JacksonUtils.toJson(response)).name("message"));
                    } catch (IOException e) {
                        LOGGER.error("Failed to send SSE event", e);
                        try {
                            SkillOptimizationResponse errorResponse =
                                new SkillOptimizationResponse();
                            errorResponse.setDone(true);
                            errorResponse.setExplanation("流式响应发送失败：" + e.getMessage());
                            emitter.send(SseEmitter.event().data(JacksonUtils.toJson(errorResponse))
                                .name("error"));
                            emitter.complete();
                        } catch (IOException ioException) {
                            LOGGER.error("Failed to send error SSE event", ioException);
                            emitter.complete();
                        }
                    }
                }
                
                /** 流式回调：处理错误并发送 error 事件。 */
                @Override
                public void onError(Throwable t) {
                    LOGGER.error("Error in skill optimization stream", t);
                    try {
                        // 发送错误响应事件
                        SkillOptimizationResponse errorResponse = new SkillOptimizationResponse();
                        errorResponse.setDone(true);
                        errorResponse.setExplanation("优化失败：" + t.getMessage());
                        emitter.send(SseEmitter.event().data(JacksonUtils.toJson(errorResponse))
                            .name("error"));
                        emitter.complete();
                    } catch (IOException e) {
                        LOGGER.error("Failed to send error SSE event", e);
                        emitter.complete();
                    }
                }
                
                /** 流式回调：完成 SSE 流。 */
                @Override
                public void onComplete() {
                    emitter.complete();
                }
            });
        
        return emitter;
    }
    
    /**
      * 根据背景信息流式生成 Skill（SSE）。
     * Generate skill from background information with stream response (SSE).
     *
     * @param form skill generation form
     * @return SSE emitter for stream response
     * @throws NacosException if validation fails
     */
    @Since("3.2.0")
    @PostMapping(value = CopilotConstants.SKILL_GENERATE_PATH,
        produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @SuppressWarnings("PMD.MethodTooLongRule")
    public SseEmitter generateSkillStream(@RequestBody(required = false) SkillGenerationForm form) {
        // Create SSE emitter with 5 minutes timeout
        SseEmitter emitter = new SseEmitter(300000L);
        
        // Handle null form or missing request body
        if (form == null) {
            try {
                SkillGenerationResponse errorResponse = new SkillGenerationResponse();
                errorResponse.setDone(true);
                errorResponse.setExplanation("请求体不能为空");
                emitter.send(
                    SseEmitter.event().data(JacksonUtils.toJson(errorResponse)).name("error"));
                emitter.complete();
            } catch (IOException ioException) {
                LOGGER.error("Failed to send error SSE event", ioException);
                emitter.completeWithError(ioException);
            }
            return emitter;
        }
        
        try {
            form.validate();
        } catch (Exception e) {
            LOGGER.error("Form validation failed", e);
            try {
                SkillGenerationResponse errorResponse = new SkillGenerationResponse();
                errorResponse.setDone(true);
                errorResponse.setExplanation("请求验证失败：" + e.getMessage());
                emitter.send(
                    SseEmitter.event().data(JacksonUtils.toJson(errorResponse)).name("error"));
                emitter.complete();
            } catch (IOException ioException) {
                LOGGER.error("Failed to send validation error SSE event", ioException);
                emitter.complete();
            }
            return emitter;
        }
        
        // Build request
        SkillGenerationRequest request = new SkillGenerationRequest();
        request.setBackgroundInfo(form.getBackgroundInfo());
        request.setSelectedMcpTools(form.getSelectedMcpTools());
        request.setConversationHistory(form.getConversationHistory());
        
        // 调用生成服务并通过流式回调推送 SSE 事件
        skillGenerationService.generateSkillStream(request,
            new StreamResponseCallback<SkillGenerationResponse>() {
                
                /** 流式回调：推送下一条 SSE 消息。 */
                @Override
                public void onNext(SkillGenerationResponse response) {
                    try {
                        // Send SSE event
                        emitter.send(
                            SseEmitter.event().data(JacksonUtils.toJson(response)).name("message"));
                    } catch (IOException e) {
                        LOGGER.error("Failed to send SSE event", e);
                        try {
                            SkillGenerationResponse errorResponse = new SkillGenerationResponse();
                            errorResponse.setDone(true);
                            errorResponse.setExplanation("流式响应发送失败：" + e.getMessage());
                            emitter.send(SseEmitter.event().data(JacksonUtils.toJson(errorResponse))
                                .name("error"));
                            emitter.complete();
                        } catch (IOException ioException) {
                            LOGGER.error("Failed to send error SSE event", ioException);
                            emitter.complete();
                        }
                    }
                }
                
                /** 流式回调：处理错误并发送 error 事件。 */
                @Override
                public void onError(Throwable t) {
                    LOGGER.error("Error in skill generation stream", t);
                    try {
                        // Send error response
                        SkillGenerationResponse errorResponse = new SkillGenerationResponse();
                        errorResponse.setDone(true);
                        errorResponse.setExplanation("生成失败：" + t.getMessage());
                        emitter.send(SseEmitter.event().data(JacksonUtils.toJson(errorResponse))
                            .name("error"));
                        emitter.complete();
                    } catch (IOException e) {
                        LOGGER.error("Failed to send error SSE event", e);
                        emitter.complete();
                    }
                }
                
                /** 流式回调：完成 SSE 流。 */
                @Override
                public void onComplete() {
                    emitter.complete();
                }
            });
        
        return emitter;
    }
    
    /**
      * 流式优化 Prompt（SSE）。
     * Optimize prompt with stream response (SSE).
     *
     * @param form prompt optimization form
     * @return SSE emitter for stream response
     * @throws NacosException if validation fails
     */
    @Since("3.2.0")
    @PostMapping(value = CopilotConstants.PROMPT_OPTIMIZE_PATH,
        produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @SuppressWarnings("PMD.MethodTooLongRule")
    public SseEmitter optimizePromptStream(
        @RequestBody(required = false) PromptOptimizationForm form) {
        // Create SSE emitter with 5 minutes timeout
        SseEmitter emitter = new SseEmitter(300000L);
        
        // Handle null form or missing request body
        if (form == null) {
            try {
                PromptOptimizationResponse errorResponse = new PromptOptimizationResponse();
                errorResponse.setDone(true);
                errorResponse.setExplanation("请求体不能为空");
                emitter.send(
                    SseEmitter.event().data(JacksonUtils.toJson(errorResponse)).name("error"));
                emitter.complete();
            } catch (IOException ioException) {
                LOGGER.error("Failed to send error SSE event", ioException);
                emitter.completeWithError(ioException);
            }
            return emitter;
        }
        
        try {
            form.validate();
        } catch (Exception e) {
            LOGGER.error("Form validation failed", e);
            try {
                PromptOptimizationResponse errorResponse = new PromptOptimizationResponse();
                errorResponse.setDone(true);
                errorResponse.setExplanation("请求验证失败：" + e.getMessage());
                emitter.send(
                    SseEmitter.event().data(JacksonUtils.toJson(errorResponse)).name("error"));
                emitter.complete();
            } catch (IOException ioException) {
                LOGGER.error("Failed to send validation error SSE event", ioException);
                emitter.complete();
            }
            return emitter;
        }
        
        // Build request
        PromptOptimizationRequest request = new PromptOptimizationRequest();
        request.setPrompt(form.getPrompt());
        request.setOptimizationGoal(form.getOptimizationGoal());
        
        // 调用 Prompt 优化服务并通过流式回调推送 SSE 事件
        promptOptimizationService.optimizePromptStream(request,
            new StreamResponseCallback<PromptOptimizationResponse>() {
                
                /** 流式回调：推送下一条 SSE 消息。 */
                @Override
                public void onNext(PromptOptimizationResponse response) {
                    try {
                        // Send SSE event
                        emitter.send(
                            SseEmitter.event().data(JacksonUtils.toJson(response)).name("message"));
                    } catch (IOException e) {
                        LOGGER.error("Failed to send SSE event", e);
                        try {
                            PromptOptimizationResponse errorResponse =
                                new PromptOptimizationResponse();
                            errorResponse.setDone(true);
                            errorResponse.setExplanation("流式响应发送失败：" + e.getMessage());
                            emitter.send(SseEmitter.event().data(JacksonUtils.toJson(errorResponse))
                                .name("error"));
                            emitter.complete();
                        } catch (IOException ioException) {
                            LOGGER.error("Failed to send error SSE event", ioException);
                            emitter.complete();
                        }
                    }
                }
                
                /** 流式回调：处理错误并发送 error 事件。 */
                @Override
                public void onError(Throwable t) {
                    LOGGER.error("Error in prompt optimization stream", t);
                    try {
                        // Send error response
                        PromptOptimizationResponse errorResponse = new PromptOptimizationResponse();
                        errorResponse.setDone(true);
                        errorResponse.setExplanation("优化失败：" + t.getMessage());
                        emitter.send(SseEmitter.event().data(JacksonUtils.toJson(errorResponse))
                            .name("error"));
                        emitter.complete();
                    } catch (IOException e) {
                        LOGGER.error("Failed to send error SSE event", e);
                        emitter.complete();
                    }
                }
                
                /** 流式回调：完成 SSE 流。 */
                @Override
                public void onComplete() {
                    emitter.complete();
                }
            });
        
        return emitter;
    }
    
    /**
      * 流式调试 Prompt（SSE），传入用户输入并返回模型响应含思考过程。
     * Debug prompt with stream response (SSE). This allows testing a prompt with user input and returns the model's
     * response including thinking.
     *
     * @param form prompt debug form containing prompt and user input
     * @return SSE emitter for stream response
     * @throws NacosException if validation fails
     */
    @Since("3.2.0")
    @PostMapping(value = CopilotConstants.PROMPT_DEBUG_PATH,
        produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Secured(action = ActionTypes.WRITE, signType = SignType.AI, apiType = ApiType.CONSOLE_API)
    @SuppressWarnings("PMD.MethodTooLongRule")
    public SseEmitter debugPromptStream(@RequestBody(required = false) PromptDebugForm form) {
        // Create SSE emitter with 5 minutes timeout
        SseEmitter emitter = new SseEmitter(300000L);
        
        // Handle null form or missing request body
        if (form == null) {
            try {
                PromptDebugResponse errorResponse = new PromptDebugResponse();
                errorResponse.setDone(true);
                emitter.send(
                    SseEmitter.event().data(JacksonUtils.toJson(errorResponse)).name("error"));
                emitter.complete();
            } catch (IOException ioException) {
                LOGGER.error("Failed to send error SSE event", ioException);
                emitter.completeWithError(ioException);
            }
            return emitter;
        }
        
        try {
            form.validate();
        } catch (Exception e) {
            LOGGER.error("Form validation failed", e);
            try {
                PromptDebugResponse errorResponse = new PromptDebugResponse();
                errorResponse.setDone(true);
                emitter.send(
                    SseEmitter.event().data(JacksonUtils.toJson(errorResponse)).name("error"));
                emitter.complete();
            } catch (IOException ioException) {
                LOGGER.error("Failed to send validation error SSE event", ioException);
                emitter.complete();
            }
            return emitter;
        }
        
        // Build request
        PromptDebugRequest request = new PromptDebugRequest();
        request.setPrompt(form.getPrompt());
        request.setUserInput(form.getUserInput());
        
        // 调用 Prompt 调试服务并通过流式回调推送 SSE 事件
        promptDebugService.debugPromptStream(request,
            new StreamResponseCallback<PromptDebugResponse>() {
                
                /** 流式回调：推送下一条 SSE 消息。 */
                @Override
                public void onNext(PromptDebugResponse response) {
                    try {
                        // Send SSE event
                        emitter.send(
                            SseEmitter.event().data(JacksonUtils.toJson(response)).name("message"));
                    } catch (IOException e) {
                        LOGGER.error("Failed to send SSE event", e);
                        try {
                            PromptDebugResponse errorResponse = new PromptDebugResponse();
                            errorResponse.setDone(true);
                            emitter.send(SseEmitter.event().data(JacksonUtils.toJson(errorResponse))
                                .name("error"));
                            emitter.complete();
                        } catch (IOException ioException) {
                            LOGGER.error("Failed to send error SSE event", ioException);
                            emitter.complete();
                        }
                    }
                }
                
                /** 流式回调：处理错误并发送 error 事件。 */
                @Override
                public void onError(Throwable t) {
                    LOGGER.error("Error in prompt debug stream", t);
                    try {
                        // Send error response
                        PromptDebugResponse errorResponse = new PromptDebugResponse();
                        errorResponse.setDone(true);
                        emitter.send(SseEmitter.event().data(JacksonUtils.toJson(errorResponse))
                            .name("error"));
                        emitter.complete();
                    } catch (IOException e) {
                        LOGGER.error("Failed to send error SSE event", e);
                        emitter.complete();
                    }
                }
                
                /** 流式回调：完成 SSE 流。 */
                @Override
                public void onComplete() {
                    emitter.complete();
                }
            });
        
        return emitter;
    }
    
}
