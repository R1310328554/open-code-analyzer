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
package io.netty.channel.sctp;

import com.sun.nio.sctp.SctpStandardSocketOptions.InitMaxStreams;
import io.netty.channel.ChannelOption;

import java.net.SocketAddress;

/**
 * Option for configuring the SCTP transport
 * <p>SCTP {@link ChannelOption} 键：分片、流数量、NODELAY、主地址等， 与 {@link SctpChannelConfig} / JDK {@code SctpStandardSocketOptions} 对应。</p>
 */
public final class SctpChannelOption<T> extends ChannelOption<T> {

    /** 禁用 SCTP 层分片 */
    public static final ChannelOption<Boolean> SCTP_DISABLE_FRAGMENTS =
            valueOf(SctpChannelOption.class, "SCTP_DISABLE_FRAGMENTS");
    /** 显式标记分片消息完成 */
    public static final ChannelOption<Boolean> SCTP_EXPLICIT_COMPLETE =
            valueOf(SctpChannelOption.class, "SCTP_EXPLICIT_COMPLETE");
    /** 入站分片交错级别（0/1/2） */
    public static final ChannelOption<Integer> SCTP_FRAGMENT_INTERLEAVE =
            valueOf(SctpChannelOption.class, "SCTP_FRAGMENT_INTERLEAVE");
    /** INIT 阶段最大入/出站流数 */
    public static final ChannelOption<InitMaxStreams> SCTP_INIT_MAXSTREAMS =
            valueOf(SctpChannelOption.class, "SCTP_INIT_MAXSTREAMS");

    /** SCTP Nagle 类延迟开关 */
    public static final ChannelOption<Boolean> SCTP_NODELAY =
            valueOf(SctpChannelOption.class, "SCTP_NODELAY");
    /** 本端主传输地址 */
    public static final ChannelOption<SocketAddress> SCTP_PRIMARY_ADDR =
            valueOf(SctpChannelOption.class, "SCTP_PRIMARY_ADDR");
    /** 请求对端将某地址设为主路径 */
    public static final ChannelOption<SocketAddress> SCTP_SET_PEER_PRIMARY_ADDR =
            valueOf(SctpChannelOption.class, "SCTP_SET_PEER_PRIMARY_ADDR");

    @SuppressWarnings({ "unused", "deprecation" })
    /** 禁止实例化，仅通过静态常量引用 */
    private SctpChannelOption() {
        super(null);
    }
}
