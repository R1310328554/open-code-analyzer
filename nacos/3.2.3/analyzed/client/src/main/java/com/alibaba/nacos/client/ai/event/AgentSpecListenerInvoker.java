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

import com.alibaba.nacos.api.ai.listener.AbstractNacosAgentSpecListener;
import com.alibaba.nacos.api.ai.listener.NacosAgentSpecEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AgentSpec 监听器调用器。
 *
 * <p>将 {@link NacosAgentSpecEvent} 分派给 {@link AbstractNacosAgentSpecListener}。</p>
 *
 * @author nacos
 */
public class AgentSpecListenerInvoker
    extends AbstractAiListenerInvoker<NacosAgentSpecEvent, AbstractNacosAgentSpecListener> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentSpecListenerInvoker.class);
    
    public AgentSpecListenerInvoker(AbstractNacosAgentSpecListener listener) {
        super(listener);
    }
    
    @Override
    /** 记录 AgentSpec 事件回调日志。 */
    protected void logInvoke(NacosAgentSpecEvent event) {
        LOGGER.info("Invoke event agentSpecName: {} to Listener: {}", event.getAgentSpecName(),
            listener.toString());
    }
}
