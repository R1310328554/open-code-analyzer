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
package org.apache.rocketmq.namesrv.routeinfo;

import io.netty.channel.Channel;
import org.apache.rocketmq.namesrv.NamesrvController;
import org.apache.rocketmq.remoting.ChannelEventListener;

/**
 * Broker 连接生命周期监听器：在通道关闭、异常或空闲时触发路由表清理。
 */
public class BrokerHousekeepingService implements ChannelEventListener {

    /** NameServer 控制器，用于访问路由信息管理器。 */
    private final NamesrvController namesrvController;

    /** 构造监听器并绑定 NameServer 控制器。 */
    public BrokerHousekeepingService(NamesrvController namesrvController) {
        this.namesrvController = namesrvController;
    }

    @Override
    /** 新连接建立（当前无额外处理）。 */
    public void onChannelConnect(String remoteAddr, Channel channel) {
    }

    @Override
    /** 通道正常关闭时注销对应 Broker 路由信息。 */
    public void onChannelClose(String remoteAddr, Channel channel) {
        this.namesrvController.getRouteInfoManager().onChannelDestroy(channel);
    }

    @Override
    /** 通道异常时同样清理 Broker 路由，避免僵尸注册。 */
    public void onChannelException(String remoteAddr, Channel channel) {
        this.namesrvController.getRouteInfoManager().onChannelDestroy(channel);
    }

    @Override
    /** 通道空闲超时断开时清理路由。 */
    public void onChannelIdle(String remoteAddr, Channel channel) {
        this.namesrvController.getRouteInfoManager().onChannelDestroy(channel);
    }

    @Override
    /** 通道激活（当前无额外处理）。 */
    public void onChannelActive(String remoteAddr, Channel channel) {

    }
}
