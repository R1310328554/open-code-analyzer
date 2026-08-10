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

package com.alibaba.nacos.api.ai.listener;

import com.alibaba.nacos.api.ai.model.prompt.Prompt;

/**
 * Nacos AI 模块 Prompt 配置变更事件。
 *
 * <p>当订阅的 Prompt 在服务端发生增删改时触发；
 * {@link #getPrompt()} 在删除场景下可能为 null。</p>
 *
 * @author nacos
 */
public class NacosPromptEvent implements NacosAiEvent {
    
    private final String promptKey;
    
    private final Prompt prompt;
    
    /**
     * 构造 Prompt 变更事件。
     *
     * @param promptKey Prompt 配置键
     * @param prompt Prompt 对象，删除时为 null
     */
        this.promptKey = promptKey;
        this.prompt = prompt;
    }
    
    /**
     * 获取 Prompt 配置键。
     *
     * @return prompt key
     */
    public String getPromptKey() {
        return promptKey;
    }
    
    /**
     * 获取 Prompt 对象；若 Prompt 已被删除则返回 null。
     *
     * @return prompt object, may be null if prompt is deleted
     */
    public Prompt getPrompt() {
        return prompt;
    }
}
