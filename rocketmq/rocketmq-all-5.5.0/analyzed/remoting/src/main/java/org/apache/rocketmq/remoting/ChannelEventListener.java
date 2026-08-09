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
package org.apache.rocketmq.remoting;

import io.netty.channel.Channel;

/**
 * Netty 通道生命周期事件监听器：连接建立、关闭、异常与空闲回调。
 */
public interface ChannelEventListener {
    /** 通道物理连接建立时触发。 */
    void onChannelConnect(final String remoteAddr, final Channel channel);

    /** 通道关闭时触发。 */
    void onChannelClose(final String remoteAddr, final Channel channel);

    /** 通道发生 I/O 异常时触发。 */
    void onChannelException(final String remoteAddr, final Channel channel);

    /** 通道读写空闲超时时触发。 */
    void onChannelIdle(final String remoteAddr, final Channel channel);

    /** 通道激活（可收发数据）时触发。 */
    void onChannelActive(final String remoteAddr, final Channel channel);
}
