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
package org.apache.rocketmq.client.impl.mqclient;

import io.netty.channel.ChannelHandlerContext;
import org.apache.rocketmq.client.impl.ClientRemotingProcessor;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;

/**
 * 空实现的客户端 Remoting 处理器：收到服务端请求时直接返回 null，不做任何业务处理。
 * 用于仅需出站通信、无需处理入站指令的轻量客户端场景。
 */
public class DoNothingClientRemotingProcessor extends ClientRemotingProcessor {

    /** 以 MQClient 实例构造，委托父类注册 Remoting 回调。 */
    public DoNothingClientRemotingProcessor(MQClientInstance mqClientFactory) {
        super(mqClientFactory);
    }

    @Override
    /** 忽略入站请求，始终返回 null 表示不响应。 */
    public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) {
        return null;
    }
}
