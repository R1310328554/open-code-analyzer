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

package com.alibaba.nacos.copilot.model;

import java.io.Serializable;
import java.util.List;

/**
 * LLM 聊天请求：封装会话 ID、用户消息、上下文、历史与流式选项等参数。
 * Chat request for LLM.
 *
 * @author nacos
 */
public class ChatRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 会话 ID，标识一次对话会话。
     */
    private String sessionId;
    
    /**
     * 用户当前输入消息。
     */
    private String message;
    
    /**
     * 上下文信息（可选），补充业务背景。
     */
    private String context;
    
    /**
     * 对话历史消息列表。
     */
    private List<ChatMessage> history;
    
    /**
     * 是否启用流式响应。
     */
    private boolean stream;
    
    /**
     * 系统 Prompt（可选），优化场景下使用。
     */
    private String systemPrompt;
    
    public ChatRequest() {
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getContext() {
        return context;
    }
    
    public void setContext(String context) {
        this.context = context;
    }
    
    public List<ChatMessage> getHistory() {
        return history;
    }
    
    public void setHistory(List<ChatMessage> history) {
        this.history = history;
    }
    
    public boolean isStream() {
        return stream;
    }
    
    public void setStream(boolean stream) {
        this.stream = stream;
    }
    
    public String getSystemPrompt() {
        return systemPrompt;
    }
    
    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }
}
