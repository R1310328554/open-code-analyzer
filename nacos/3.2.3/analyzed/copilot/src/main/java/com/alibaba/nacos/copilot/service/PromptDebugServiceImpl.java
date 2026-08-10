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
import com.alibaba.nacos.copilot.config.CopilotAgentManager;
import com.alibaba.nacos.copilot.model.PromptDebugRequest;
import com.alibaba.nacos.copilot.model.PromptDebugResponse;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.EventType;
import io.agentscope.core.agent.StreamOptions;
import io.agentscope.core.message.Msg;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Prompt 调试服务实现：校验参数与 Copilot 可用性后，创建 Agent 并以流式方式返回调试结果。
 * Prompt debug service implementation.
 *
 * @author nacos
 */
@Service
public class PromptDebugServiceImpl implements PromptDebugService {
    
    /** Copilot Agent 管理器，负责创建与配置 ReActAgent。 */
    private final CopilotAgentManager agentManager;
    
    /** 注入 Agent 管理器。 */
    @Autowired
    public PromptDebugServiceImpl(CopilotAgentManager agentManager) {
        this.agentManager = agentManager;
    }
    
    @Override
    public void debugPromptStream(PromptDebugRequest request,
        StreamResponseCallback<PromptDebugResponse> callback) {
        // 1. 校验请求参数
        if (StringUtils.isBlank(request.getPrompt())) {
            callback
                .onError(new NacosException(NacosException.INVALID_PARAM, "Prompt is required"));
            return;
        }
        
        if (StringUtils.isBlank(request.getUserInput())) {
            callback.onError(
                new NacosException(NacosException.INVALID_PARAM, "User input is required"));
            return;
        }
        
        // 2. 检查 Copilot 是否已启用
        if (!agentManager.isEnabled()) {
            callback.onError(new NacosException(NacosException.INVALID_PARAM,
                "AI 功能未启用：请配置 Copilot API Key。请设置 nacos.copilot.llm.apiKey 或环境变量 COPILOT_API_KEY"));
            return;
        }
        
        // 3. 使用用户 Prompt 作为系统提示词
        String systemPrompt = request.getPrompt();
        
        // 4. 基于系统提示词创建 Agent
        ReActAgent agent = agentManager.createAgent(systemPrompt);
        if (agent == null) {
            callback.onError(new NacosException(NacosException.INVALID_PARAM,
                "Failed to create Copilot agent. Please check configuration."));
            return;
        }
        
        // 5. 配置流式选项（推理与工具结果事件）
        StreamOptions streamOptions = StreamOptions.builder()
            .eventTypes(EventType.REASONING, EventType.TOOL_RESULT)
            .incremental(true)
            .build();
        
        // 6. 构造用户消息
        Msg userMsg = Msg.builder()
            .textContent(request.getUserInput())
            .build();
        
        // 7. 调用 Agent 流式接口；调试场景保留 THINKING 分片供前端展示
        Flux<io.agentscope.core.agent.Event> eventFlux = agent.stream(userMsg, streamOptions)
            .subscribeOn(Schedulers.boundedElastic());
        
        eventFlux.subscribe(StreamEventProcessor.createSubscriber(
            (type, content, done) -> {
                // 调试模式保留所有类型（含 THINKING）
                PromptDebugResponse response = new PromptDebugResponse();
                response.setType(type);
                response.setChunk(content);
                response.setDone(done);
                return response;
            },
            callback));
    }
}
