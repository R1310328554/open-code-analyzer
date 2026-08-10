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
 * 会话历史：按时间顺序存储消息序列，可用于 Skill 生成或优化场景。
 * Conversation history containing a sequence of messages.
 * This represents a complete conversation that may be suitable for
 * skill generation or skill optimization.
 *
 * @author nacos
 */
public class ConversationHistory implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 按时间顺序排列的会话消息列表。
     */
    private List<ConversationMessage> messages;
    
    /**
     * 会话上下文或摘要（可选）。
     */
    private String context;
    
    /**
     * 会话标题或主题（可选）。
     */
    private String title;
    
    public ConversationHistory() {
    }
    
    public List<ConversationMessage> getMessages() {
        return messages;
    }
    
    public void setMessages(List<ConversationMessage> messages) {
        this.messages = messages;
    }
    
    public String getContext() {
        return context;
    }
    
    public void setContext(String context) {
        this.context = context;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
}
