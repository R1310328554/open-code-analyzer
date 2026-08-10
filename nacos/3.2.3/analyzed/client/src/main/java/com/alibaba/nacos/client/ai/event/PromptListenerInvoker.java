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

import com.alibaba.nacos.api.ai.listener.AbstractNacosPromptListener;
import com.alibaba.nacos.api.ai.listener.NacosPromptEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Prompt 监听器调用器。
 *
 * <p>将 {@link NacosPromptEvent} 分派给 {@link AbstractNacosPromptListener}，并在调用前记录 promptKey 日志。</p>
 *
 * @author nacos
 */
public class PromptListenerInvoker
    extends AbstractAiListenerInvoker<NacosPromptEvent, AbstractNacosPromptListener> {
    
    /** 日志记录器。 */
    private static final Logger LOGGER = LoggerFactory.getLogger(PromptListenerInvoker.class);
    
    /**
     * 构造 Prompt 监听器调用器。
     *
     * @param listener 目标 Prompt 监听器
     */
        super(listener);
    }
    
    @Override
    /** 记录 Prompt 事件分派日志。 */
    protected void logInvoke(NacosPromptEvent event) {
        LOGGER.info("Invoke event promptKey: {} to Listener: {}", event.getPromptKey(),
            listener.toString());
    }
}
