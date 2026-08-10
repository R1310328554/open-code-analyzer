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

import com.alibaba.nacos.api.ai.listener.AbstractNacosAgentCardListener;
import com.alibaba.nacos.api.ai.listener.NacosAgentCardEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Agent Card 监听器调用器。
 *
 * <p>将 {@link NacosAgentCardEvent} 分派给 {@link AbstractNacosAgentCardListener}，
 * 并在调用时记录 agentName 与 listener 信息。</p>
 *
 * @author xiweng.yy
 */
public class AgentCardListenerInvoker
    extends AbstractAiListenerInvoker<NacosAgentCardEvent, AbstractNacosAgentCardListener> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentCardListenerInvoker.class);
    
    /** @param listener Agent Card 监听器实例 */
    public AgentCardListenerInvoker(AbstractNacosAgentCardListener listener) {
        super(listener);
    }
    
    /** 记录 Agent Card 事件回调日志。 */
    protected void logInvoke(NacosAgentCardEvent event) {
        LOGGER.info("Invoke event agentName: {} to Listener: {}", event.getAgentName(),
            listener.toString());
    }
}
