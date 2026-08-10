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

import com.alibaba.nacos.api.ai.listener.AbstractNacosMcpServerListener;
import com.alibaba.nacos.api.ai.listener.NacosMcpServerEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MCP 服务监听器调用器。
 *
 * <p>继承 {@link AbstractAiListenerInvoker}，将 {@link NacosMcpServerEvent} 分派给 {@link AbstractNacosMcpServerListener} 并在调用前记录命名空间、MCP ID 等日志。</p>
 *
 * @author xiweng.yy
 */
public class McpServerListenerInvoker
    extends AbstractAiListenerInvoker<NacosMcpServerEvent, AbstractNacosMcpServerListener> {
    
    /** 日志记录器。 */
    private static final Logger LOGGER = LoggerFactory.getLogger(McpServerListenerInvoker.class);
    
    /**
     * 构造 MCP 服务监听器调用器。
     *
     * @param listener 目标 MCP 服务监听器
     */
        super(listener);
    }
    
    /** 记录 MCP 服务事件分派日志。 */
    protected void logInvoke(NacosMcpServerEvent event) {
        LOGGER.info("Invoke event namespaceId: {}, mcpId: {}, mcpName: {} to Listener: {}",
            event.getNamespaceId(),
            event.getMcpId(), event.getMcpName(), listener.toString());
    }
}
