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

package com.alibaba.nacos.client.ai.event;

import com.alibaba.nacos.api.ai.model.prompt.Prompt;
import com.alibaba.nacos.common.notify.Event;

/**
 * Prompt 变更内部通知事件。
 *
 * <p>当 {@code NacosPromptCacheHolder} 检测到 Prompt 内容变更时发布，携带 promptKey、缓存键及最新 {@link Prompt} 对象，供 {@code AiChangeNotifier} 分发给监听器。</p>
 *
 * @author nacos
 */
public class PromptChangedEvent extends Event {
    
    /** 序列化版本号。 */
    private static final long serialVersionUID = 1L;
    
    /** Prompt 业务键。 */
    private final String promptKey;
    
    /** 本地缓存键（含版本/标签维度）。 */
    private final String cacheKey;
    
    /** 变更后的 Prompt 详情。 */
    private final Prompt prompt;
    
    /**
     * 构造 Prompt 变更事件。
     *
     * @param promptKey Prompt 业务键
     * @param cacheKey  本地缓存键
     * @param prompt    最新 Prompt 对象
     */
        this.promptKey = promptKey;
        this.cacheKey = cacheKey;
        this.prompt = prompt;
    }
    
    /** 返回 Prompt 业务键。 */
    public String getPromptKey() {
        return promptKey;
    }
    
    /** 返回变更后的 Prompt 详情。 */
    public Prompt getPrompt() {
        return prompt;
    }
    
    /** 返回本地缓存键。 */
    public String getCacheKey() {
        return cacheKey;
    }
}
