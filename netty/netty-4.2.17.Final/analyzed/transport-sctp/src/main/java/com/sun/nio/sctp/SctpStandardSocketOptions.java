/*
 * Copyright 2011 The Netty Project
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
package com.sun.nio.sctp;

import java.net.SocketAddress;

/**
 * JDK 标准 SCTP 套接字选项常量集合。
 * <p>涵盖分片、Nagle、主次地址、初始流数量及 SO_RCVBUF/SO_SNDBUF 等； 非 SCTP 平台字段为 {@code null} stub。</p>
 */
@SuppressWarnings("all")
public class SctpStandardSocketOptions {
    /** 平台不支持 SCTP 时类加载失败 */
    static {
        UnsupportedOperatingSystemException.raise();
    }

    /** 禁用 SCTP 层分片（整包发送） */
    public static final SctpSocketOption<Boolean> SCTP_DISABLE_FRAGMENTS = null;
    /** 显式完成分片消息（需应用层标记 end-of-record） */
    public static final SctpSocketOption<Boolean> SCTP_EXPLICIT_COMPLETE = null;
    /** 入站分片交错级别（0/1/2） */
    public static final SctpSocketOption<Integer> SCTP_FRAGMENT_INTERLEAVE = null;
    /** INIT 协商的最大入/出站流数 */
    public static final SctpSocketOption<InitMaxStreams> SCTP_INIT_MAXSTREAMS = null;
    /** SCTP 层 Nagle 类延迟（类似 TCP_NODELAY） */
    public static final SctpSocketOption<Boolean> SCTP_NODELAY = null;
    /** 读取或设置本端主传输地址 */
    public static final SctpSocketOption<SocketAddress> SCTP_PRIMARY_ADDR = null;
    /** 请求对端将某地址设为主路径 */
    public static final SctpSocketOption<SocketAddress> SCTP_SET_PEER_PRIMARY_ADDR = null;
    /** 关闭时 SO_LINGER 秒数 */
    public static final SctpSocketOption<Integer> SO_LINGER = null;
    /** 接收缓冲区大小（字节） */
    public static final SctpSocketOption<Integer> SO_RCVBUF = null;
    /** 发送缓冲区大小（字节） */
    public static final SctpSocketOption<Integer> SO_SNDBUF = null;

    /** INIT 阶段协商的入站/出站最大流数量 */
    public static class InitMaxStreams {

        /** 构造流上限描述（maxInStreams, maxOutStreams） */
        public static InitMaxStreams create(int i, int i1) {
            return null;
        }

        /** 最大入站流数 */
        public int maxInStreams() {
            return 0;
        }

        /** 最大出站流数 */
        public int maxOutStreams() {
            return 0;
        }

    }
}
