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

package org.apache.rocketmq.proxy.common.channel;

import io.netty.channel.Channel;
import org.apache.rocketmq.proxy.grpc.v2.channel.GrpcClientChannel;
import org.apache.rocketmq.proxy.processor.channel.ChannelProtocolType;
import org.apache.rocketmq.proxy.processor.channel.RemoteChannel;
import org.apache.rocketmq.proxy.remoting.channel.RemotingChannel;

/**
 * 通道工具类：识别 Proxy 侧 Netty 通道类型及是否来自远端 Proxy 同步。
 */
public class ChannelHelper {

    /**
     * 判断通道是否由其他 Proxy 节点同步而来。
     *
     * @param channel Netty 通道
     * @return 若来自远端 Proxy 同步则返回 true
     */
    public static boolean isRemote(Channel channel) {
        return channel instanceof RemoteChannel;
    }

    /** 根据通道实现类推断协议类型（gRPC v2、Remoting 或未知）。 */
    public static ChannelProtocolType getChannelProtocolType(Channel channel) {
        // gRPC v2 客户端通道
        if (channel instanceof GrpcClientChannel) {
            return ChannelProtocolType.GRPC_V2;
        } else if (channel instanceof RemotingChannel) {
            // 经典 Remoting 协议通道
            return ChannelProtocolType.REMOTING;
        } else if (channel instanceof RemoteChannel) {
            RemoteChannel remoteChannel = (RemoteChannel) channel;
            return remoteChannel.getType();
        }
        return ChannelProtocolType.UNKNOWN;
    }
}
