/*
 * Copyright 2023 The Netty Project
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

/**
 * QUIC 连接协商后的传输参数只读视图，反映对端或本地生效的限值与选项。
 */
public interface QuicTransportParameters {

    /** 最大空闲超时时间（毫秒）。
     * @return timeout.
     */
    long maxIdleTimeout();

    /**
     * 最大 UDP 载荷大小（字节）。
     *
     * @return maximum payload size.
     */
    long maxUdpPayloadSize();

    /**
     * 连接级初始最大数据量（流量控制）。
     *
     * @return flowcontrol.
     */
    long initialMaxData();

    /**
     * 本地发起的双向流初始最大数据量。
     *
     * @return flowcontrol.
     */
    long initialMaxStreamDataBidiLocal();

    /**
     * 对端发起的双向流初始最大数据量。
     *
     * @return flowcontrol.
     */
    long initialMaxStreamDataBidiRemote();

    /**
     * 单向流初始最大数据量。
     *
     * @return flowcontrol.
     */
    long initialMaxStreamDataUni();

    /**
     * 初始最大双向流数量。
     *
     * @return streams.
     */
    long initialMaxStreamsBidi();

    /**
     * 初始最大单向流数量。
     *
     * @return streams.
     */
    long initialMaxStreamsUni();

    /**
     * ACK 延迟指数（用于解码 ACK 延迟字段）。
     *
     * @return exponent.
     */
    long ackDelayExponent();

    /**
     * 最大 ACK 延迟（毫秒）。
     *
     * @return delay.
     */
    long maxAckDelay();

    /**
     * 是否禁用主动连接迁移。
     *
     * @return disabled.
     */
    boolean disableActiveMigration();

    /**
     * 活跃连接 ID 数量上限。
     *
     * @return limit.
     */
    long activeConnIdLimit();

    /**
     * DATAGRAM 扩展参数：最大 datagram 帧载荷；未协商时为 0。
     *
     * @return param.
     */
    long maxDatagramFrameSize();
}
