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

package org.apache.rocketmq.proxy.remoting;

import io.netty.channel.Channel;
import org.apache.rocketmq.proxy.remoting.activity.ClientManagerActivity;
import org.apache.rocketmq.remoting.ChannelEventListener;

/**
 * Remoting 客户端连接生命周期监听：在通道关闭/异常/空闲时清理客户端状态。
 */
public class ClientHousekeepingService implements ChannelEventListener {

    /** 客户端管理活动，负责注销已断开连接。 */
    private final ClientManagerActivity clientManagerActivity;

    /** 注入客户端管理活动处理器。 */
    public ClientHousekeepingService(ClientManagerActivity clientManagerActivity) {
        this.clientManagerActivity = clientManagerActivity;
    }

    @Override
    /** 通道建立连接时回调（当前无额外处理）。 */
    public void onChannelConnect(String remoteAddr, Channel channel) {

    }

    @Override
    /** 通道正常关闭时触发客户端注销。 */
    public void onChannelClose(String remoteAddr, Channel channel) {
        this.clientManagerActivity.doChannelCloseEvent(remoteAddr, channel);
    }

    @Override
    /** 通道异常时同样清理客户端注册信息。 */
    public void onChannelException(String remoteAddr, Channel channel) {
        this.clientManagerActivity.doChannelCloseEvent(remoteAddr, channel);
    }

    @Override
    /** 通道空闲超时时关闭并清理客户端。 */
    public void onChannelIdle(String remoteAddr, Channel channel) {
        this.clientManagerActivity.doChannelCloseEvent(remoteAddr, channel);
    }

    @Override
    /** 通道激活时回调（当前无额外处理）。 */
    public void onChannelActive(String remoteAddr, Channel channel) {

    }
}

