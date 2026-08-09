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

package org.apache.rocketmq.broker.processor;

import org.apache.rocketmq.broker.mqtrace.SendMessageContext;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;

/**
 * 发送消息异步回调：在 Broker 完成发送处理后通知调用方。
 */
public interface SendMessageCallback {
    /**
     * 发送完成时回调。
     *
     * @param ctx 发送追踪上下文
     * @param response 发送响应命令
     */
    /** 发送处理结束（成功或失败）时触发。 */
    void onComplete(SendMessageContext ctx, RemotingCommand response);
}
