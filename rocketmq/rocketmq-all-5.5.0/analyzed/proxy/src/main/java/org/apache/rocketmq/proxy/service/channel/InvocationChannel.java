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

package org.apache.rocketmq.proxy.service.channel;

import io.netty.channel.ChannelFuture;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.rocketmq.proxy.config.ConfigurationManager;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;

/**
 * 调用通道：维护 opaque 与 {@link InvocationContextInterface} 的在途请求映射，
 * 响应到达时完成对应 CompletableFuture。
 */
public class InvocationChannel extends SimpleChannel {
    /** opaque 到在途调用上下文的并发映射表。 */
    protected final ConcurrentMap<Integer, InvocationContextInterface> inFlightRequestMap;

    /** 以远端/本地地址构造调用通道。 */
    public InvocationChannel(String remoteAddress, String localAddress) {
        super(remoteAddress, localAddress);
        this.inFlightRequestMap = new ConcurrentHashMap<>();
    }

    @Override
    /** 写出响应时按 opaque 查找并触发上下文回调。 */
    public ChannelFuture writeAndFlush(Object msg) {
        if (msg instanceof RemotingCommand) {
            RemotingCommand responseCommand = (RemotingCommand) msg;
            // 按 opaque 取出在途上下文并处理响应
            InvocationContextInterface context = inFlightRequestMap.remove(responseCommand.getOpaque());
            if (null != context) {
                context.handle(responseCommand);
            }
            inFlightRequestMap.remove(responseCommand.getOpaque());
        }
        return super.writeAndFlush(msg);
    }

    @Override
    /** 存在在途请求时视为可写。 */
    public boolean isWritable() {
        return inFlightRequestMap.size() > 0;
    }

    @Override
    /** 注册 opaque 对应的在途调用上下文。 */
    public void registerInvocationContext(int opaque, InvocationContextInterface context) {
        inFlightRequestMap.put(opaque, context);
    }

    @Override
    /** 移除指定 opaque 的在途上下文。 */
    public void eraseInvocationContext(int opaque) {
        inFlightRequestMap.remove(opaque);
    }

    @Override
    /** 扫描并清理超时的在途请求。 */
    public void clearExpireContext() {
        Iterator<Map.Entry<Integer, InvocationContextInterface>> iterator = inFlightRequestMap.entrySet().iterator();
        int count = 0;
        while (iterator.hasNext()) {
            Map.Entry<Integer, InvocationContextInterface> entry = iterator.next();
            // 按配置的超时秒数判定是否过期
            if (entry.getValue().expired(ConfigurationManager.getProxyConfig().getChannelExpiredInSeconds())) {
                iterator.remove();
                count++;
                log.debug("An expired request is found, request: {}", entry.getValue());
            }
        }
        if (count > 0) {
            log.warn("[BUG] {} expired in-flight requests is cleaned.", count);
        }
    }
}
