/*
 * Copyright 2013 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.channel.udt;

import io.netty.channel.ServerChannel;
import io.netty.channel.udt.nio.NioUdtProvider;

/**
 * UDT {@link ServerChannel}.
 * <p>
 * Supported UDT {@link UdtServerChannel} are available via {@link NioUdtProvider}.
 * <p>UDT 服务端监听通道标记接口，同时继承 {@link ServerChannel} 与 {@link UdtChannel}。 字节流/消息 Acceptor 实现见 {@link NioUdtProvider}； 典型子类包括 {@code NioUdtByteAcceptorChannel} 与消息模式 Acceptor。</p>
 *
 * @deprecated The UDT transport is no longer maintained and will be removed.
 */
@Deprecated
/** 组合 {@link ServerChannel} 与 {@link UdtChannel} 能力的 UDT 监听端接口 */
public interface UdtServerChannel extends ServerChannel, UdtChannel {

}
