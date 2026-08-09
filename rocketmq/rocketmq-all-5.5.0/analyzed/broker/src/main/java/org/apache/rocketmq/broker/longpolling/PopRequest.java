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

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;
import org.apache.rocketmq.remoting.protocol.heartbeat.SubscriptionData;
import org.apache.rocketmq.store.MessageFilter;

/**
 * POP 长轮询挂起请求：封装 Remoting 命令、Netty 通道、过期时间及可选订阅过滤条件。
 */
public class PopRequest {
    private static final AtomicLong COUNTER = new AtomicLong(Long.MIN_VALUE);

    private final RemotingCommand remotingCommand;
    private final ChannelHandlerContext ctx;
    private final AtomicBoolean complete = new AtomicBoolean(false);
    private final long op = COUNTER.getAndIncrement();

    private final long expired;
    private final SubscriptionData subscriptionData;
    private final MessageFilter messageFilter;

    /** 构造挂起请求，{@code expired} 为绝对超时时间戳（毫秒）。 */
    public PopRequest(RemotingCommand remotingCommand, ChannelHandlerContext ctx,
        long expired, SubscriptionData subscriptionData, MessageFilter messageFilter) {

        this.ctx = ctx;
        this.remotingCommand = remotingCommand;
        this.expired = expired;
        this.subscriptionData = subscriptionData;
        this.messageFilter = messageFilter;
    }

    /** 返回客户端 Netty 通道。 */
    public Channel getChannel() {
        return ctx.channel();
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public RemotingCommand getRemotingCommand() {
        return remotingCommand;
    }

    /** 距过期时间不足 50ms 即视为超时。 */
    public boolean isTimeout() {
        return System.currentTimeMillis() > (expired - 50);
    }

    /** CAS 标记请求已完成，防止重复唤醒。 */
    public boolean complete() {
        return complete.compareAndSet(false, true);
    }

    public long getExpired() {
        return expired;
    }

    public SubscriptionData getSubscriptionData() {
        return subscriptionData;
    }

    public MessageFilter getMessageFilter() {
        return messageFilter;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PopRequest{");
        sb.append("cmd=").append(remotingCommand);
        sb.append(", ctx=").append(ctx);
        sb.append(", expired=").append(expired);
        sb.append(", complete=").append(complete);
        sb.append(", op=").append(op);
        sb.append('}');
        return sb.toString();
    }

    /** 按过期时间升序、操作序号升序排序，用于 {@link ConcurrentSkipListSet}。 */
    public static final Comparator<PopRequest> COMPARATOR = (o1, o2) -> {
        int ret = (int) (o1.getExpired() - o2.getExpired());

        if (ret != 0) {
            return ret;
        }
        ret = (int) (o1.op - o2.op);
        if (ret != 0) {
            return ret;
        }
        return -1;
    };
}
