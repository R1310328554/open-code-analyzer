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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.copilot.service;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.copilot.adapter.StreamResponseCallback;
import com.alibaba.nacos.copilot.capability.prompt.PromptOptimizationPrompt;
import com.alibaba.nacos.copilot.config.CopilotAgentManager;
import com.alibaba.nacos.copilot.model.PromptOptimizationRequest;
import com.alibaba.nacos.copilot.model.PromptOptimizationResponse;
import com.alibaba.nacos.copilot.model.StreamResponseType;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.EventType;
import io.agentscope.core.agent.StreamOptions;
import io.agentscope.core.message.Msg;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Prompt 优化服务实现：组装优化提示词、调用 Agent 流式推理，并过滤 THINKING 分片。
 * Prompt optimization service implementation.
 *
 * @author nacos
 */
@Service
public class PromptOptimizationServiceImpl implements PromptOptimizationService {
    
    /** Copilot Agent 管理器。 */
    private final CopilotAgentManager agentManager;
    
    /** 注入 Agent 管理器。 */
    @Autowired
    public PromptOptimizationServiceImpl(CopilotAgentManager agentManager) {
        this.agentManager = agentManager;
    }
    
    @Override
    public void optimizePromptStream(PromptOptimizationRequest request,
        StreamResponseCallback<PromptOptimizationResponse> callback) {
        // 1. 校验请求
        if (StringUtils.isBlank(request.getPrompt())) {
            callback
                .onError(new NacosException(NacosException.INVALID_PARAM, "Prompt is required"));
            return;
        }
        
        // 2. 检查 Copilot 是否启用
        if (!agentManager.isEnabled()) {
            callback.onError(new NacosException(NacosException.INVALID_PARAM,
                "AI 功能未启用：请配置 Copilot API Key。请设置 nacos.copilot.llm.apiKey 或环境变量 COPILOT_API_KEY"));
            return;
        }
        
        // 3. 加载优化系统提示词
        String systemPrompt = PromptOptimizationPrompt.SYSTEM_PROMPT;
        
        // 4. 组装用户消息
        String userMessage = buildUserMessage(request);
        
        // 5. 创建 Agent
        ReActAgent agent = agentManager.createAgent(systemPrompt);
        if (agent == null) {
            callback.onError(new NacosException(NacosException.INVALID_PARAM,
                "Failed to create Copilot agent. Please check configuration."));
            return;
        }
        
        // 6. 配置流式选项
        StreamOptions streamOptions = StreamOptions.builder()
            .eventTypes(EventType.REASONING, EventType.TOOL_RESULT)
            .incremental(true)
            .build();
        
        // 7. 构造 Msg 用户消息
        Msg userMsg = Msg.builder()
            .textContent(userMessage)
            .build();
        
        // 8. 流式调用 Agent；前端自行累积并解析内容
        Flux<io.agentscope.core.agent.Event> eventFlux = agent.stream(userMsg, streamOptions)
            .subscribeOn(Schedulers.boundedElastic());
        
        eventFlux.subscribe(StreamEventProcessor.createSubscriber(
            (type, content, done) -> {
                // 过滤 THINKING 分片，不向用户暴露推理过程
                if (type == StreamResponseType.THINKING) {
                    return null;
                }
                PromptOptimizationResponse response = new PromptOptimizationResponse();
                response.setType(type);
                response.setChunk(content);
                response.setDone(done);
                return response;
            },
            callback));
    }
    
    /**
     * 为 Prompt 优化组装用户消息，包含原始 Prompt 与可选优化目标。
     *
     * @param request 优化请求
     * @return 格式化后的用户消息文本
     */
    private String buildUserMessage(PromptOptimizationRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("请优化以下 Prompt：\n\n");
        sb.append("【原始 Prompt】\n");
        sb.append(request.getPrompt());
        
        if (StringUtils.isNotBlank(request.getOptimizationGoal())) {
            sb.append("\n\n【优化目标】\n");
            sb.append(request.getOptimizationGoal());
        }
        
        return sb.toString();
    }
}
