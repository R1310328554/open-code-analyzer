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

import java.util.concurrent.CompletableFuture;
import org.apache.rocketmq.proxy.common.ProxyContext;
import org.apache.rocketmq.proxy.service.ServiceManager;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;

/**
 * Broker 请求处理器：封装对指定 Broker 的同步/单向 Remoting 请求转发。
 */
public class RequestBrokerProcessor extends AbstractProcessor {

    /** 构造 Broker 请求处理器。 */
    public RequestBrokerProcessor(MessagingProcessor messagingProcessor,
        ServiceManager serviceManager) {
        super(messagingProcessor, serviceManager);
    }

    /** 向指定 Broker 发送同步 Remoting 请求并等待响应。 */
    CompletableFuture<RemotingCommand> request(ProxyContext ctx, String brokerName, RemotingCommand request, long timeoutMillis) {
        return serviceManager.getMessageService().request(ctx, brokerName, request, timeoutMillis);
    }

    /** 向指定 Broker 发送单向 Remoting 请求（不等待响应）。 */
    CompletableFuture<Void> requestOneway(ProxyContext ctx, String brokerName, RemotingCommand request, long timeoutMillis) {
        return serviceManager.getMessageService().requestOneway(ctx, brokerName, request, timeoutMillis);
    }
}
