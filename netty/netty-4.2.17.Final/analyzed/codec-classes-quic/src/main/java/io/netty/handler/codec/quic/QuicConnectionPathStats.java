/*
 * Copyright 2024 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.codec.quic;

import java.net.InetSocketAddress;

/**
 * QUIC 连接某条路径（path）的统计指标。
 * 实现未知时，各方法可能返回 {@code -1}。
 */
public interface QuicConnectionPathStats {
    /** @return 此路径使用的本地地址。 */
    InetSocketAddress localAddress();

    /** @return 此路径观测到的对端地址。 */
    InetSocketAddress peerAddress();

    /** @return 路径验证状态。 */
    long validationState();

    /** @return 此路径当前是否活跃。 */
    boolean active();

    /** @return 此路径收到的 QUIC 报文数。 */
    long recv();

    /** @return 此路径发送的 QUIC 报文数。 */
    long sent();

    /** @return 此路径丢失的 QUIC 报文数。 */
    long lost();

    /** @return 此路径上含重传数据的发送报文数。 */
    long retrans();

    /** @return 路径估计往返时延 RTT（纳秒）。 */
    long rtt();

    /** @return 路径拥塞窗口 cwnd 大小（字节）。 */
    long cwnd();

    /** @return 此路径发送的字节数。 */
    long sentBytes();

    /** @return 此路径接收的字节数。 */
    long recvBytes();

    /** @return 此路径丢失的字节数。 */
    long lostBytes();

    /** @return 此路径上流数据重传字节数。 */
    long streamRetransBytes();

    /** @return 路径当前 PMTU（路径 MTU）。 */
    long pmtu();

    /** @return 最近估计的数据交付速率（字节/秒）。 */
    long deliveryRate();
}
