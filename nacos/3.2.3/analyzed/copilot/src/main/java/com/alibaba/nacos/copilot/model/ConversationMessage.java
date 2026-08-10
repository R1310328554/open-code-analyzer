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
import java.util.Map;

/**
 * 会话消息：表示历史中的一条记录，可为用户输入、工具调用或模型回复。
 * Conversation message in the conversation history.
 * Represents a single message in a conversation, which can be:
 * - User input
 * - Tool call
 * - Model response
 *
 * @author nacos
 */
public class ConversationMessage implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 消息类型，如 user、tool_call、model 等。
     */
    private String type;
    
    /**
     * 消息内容。
     */
    private String content;
    
    /**
     * 工具名称（type 为 tool_call 时有效）。
     */
    private String toolName;
    
    /**
     * 工具输入参数（type 为 tool_call 时有效）。
     */
    private Map<String, Object> toolInput;
    
    /**
     * 工具输出结果（type 为 tool_call 时有效）。
     */
    private Object toolOutput;
    
    /**
     * 消息时间戳（可选）。
     */
    private Long timestamp;
    
    /**
     * 附加元数据（可选）。
     */
    private Map<String, Object> metadata;
    
    public ConversationMessage() {
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getToolName() {
        return toolName;
    }
    
    public void setToolName(String toolName) {
        this.toolName = toolName;
    }
    
    public Map<String, Object> getToolInput() {
        return toolInput;
    }
    
    public void setToolInput(Map<String, Object> toolInput) {
        this.toolInput = toolInput;
    }
    
    public Object getToolOutput() {
        return toolOutput;
    }
    
    public void setToolOutput(Object toolOutput) {
        this.toolOutput = toolOutput;
    }
    
    public Long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
