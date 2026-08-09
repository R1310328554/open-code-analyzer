/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.proxy.processor;

import org.apache.rocketmq.common.consumer.ReceiptHandle;
import org.apache.rocketmq.common.utils.AbstractStartAndShutdown;
import org.apache.rocketmq.proxy.common.ProxyException;
import org.apache.rocketmq.proxy.common.ProxyExceptionCode;
import org.apache.rocketmq.proxy.service.ServiceManager;

/**
 * Proxy 处理器抽象基类：持有 {@link MessagingProcessor} 与 {@link ServiceManager}，提供回执句柄校验。
 */
public abstract class AbstractProcessor extends AbstractStartAndShutdown {

    /** 消息处理门面，协调各子 Processor。 */
    protected MessagingProcessor messagingProcessor;
    /** 底层服务管理器，提供路由/消息/事务等服务。 */
    protected ServiceManager serviceManager;

    /** 回执句柄已过期时的统一异常实例。 */
    protected static final ProxyException EXPIRED_HANDLE_PROXY_EXCEPTION = new ProxyException(ProxyExceptionCode.INVALID_RECEIPT_HANDLE, "receipt handle is expired");

    /** 构造处理器并注入消息处理器与服务管理器。 */
    public AbstractProcessor(MessagingProcessor messagingProcessor,
        ServiceManager serviceManager) {
        this.messagingProcessor = messagingProcessor;
        this.serviceManager = serviceManager;
    }

    /** 校验回执句柄未过期，否则抛出 {@link #EXPIRED_HANDLE_PROXY_EXCEPTION}。 */
    protected void validateReceiptHandle(ReceiptHandle handle) {
        if (handle.isExpired()) {
            throw EXPIRED_HANDLE_PROXY_EXCEPTION;
        }
    }
}
