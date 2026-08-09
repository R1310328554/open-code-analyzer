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
package org.apache.rocketmq.broker.longpolling;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.rocketmq.remoting.protocol.RemotingCommand;

import io.netty.channel.Channel;

/**
 * POP 通知长轮询挂起请求：持有 RemotingCommand 与 Channel，支持超时与一次性完成标记。
 */
public class NotificationRequest {
    private RemotingCommand remotingCommand;
    private Channel channel;
    private long expired;
    private AtomicBoolean complete = new AtomicBoolean(false);

    /** 构造挂起请求，expired 为绝对过期时间戳（毫秒）。 */
    public NotificationRequest(RemotingCommand remotingCommand, Channel channel, long expired) {
        this.channel = channel;
        this.remotingCommand = remotingCommand;
        this.expired = expired;
    }

    /** 返回客户端 Netty 通道。 */
    public Channel getChannel() {
        return channel;
    }

    /** 返回原始 POP 通知 Remoting 请求。 */
    public RemotingCommand getRemotingCommand() {
        return remotingCommand;
    }

    /** 当前时间是否已超过过期时间（预留 500ms 缓冲）。 */
    public boolean isTimeout() {
        return System.currentTimeMillis() > (expired - 500);
    }

    /** CAS 标记请求已完成，防止重复响应。 */
    public boolean complete() {
        return complete.compareAndSet(false, true);
    }

    @Override
    public String toString() {
        return remotingCommand.toString();
    }
}
