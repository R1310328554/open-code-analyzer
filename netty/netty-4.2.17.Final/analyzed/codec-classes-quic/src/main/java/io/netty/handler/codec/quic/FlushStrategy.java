/*
 * Copyright 2021 The Netty Project
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

import io.netty.util.internal.ObjectUtil;

/**
 * 配置 QUIC 出站数据何时执行 {@code flush} 的策略接口。
 */
public interface FlushStrategy {

    /**
     * 默认 {@link FlushStrategy}：累计写入约 20 个最大 UDP 负载后刷新。
     */
    FlushStrategy DEFAULT = afterNumBytes(20 * Quic.MAX_DATAGRAM_SIZE);

    /**
     * 判断当前是否应立即 flush。
     *
     * @param numPackets    自上次 flush 以来已写入的包数。
     * @param numBytes      自上次 flush 以来已写入的字节数。
     * @return              若应立刻 flush 则 {@code true}，否则 {@code false}。
     */
    boolean shouldFlushNow(int numPackets, int numBytes);

    /**
     * 按累计字节数触发 flush 的策略。
     *
     * @param bytes 超过该字节数后执行 flush。
     * @return 对应的 {@link FlushStrategy}。
     */
    static FlushStrategy afterNumBytes(int bytes) {
        ObjectUtil.checkPositive(bytes, "bytes");
        return (numPackets, numBytes) -> numBytes > bytes;
    }

    /**
     * 按累计包数触发 flush 的策略。
     *
     * @param packets 超过该包数后执行 flush。
     * @return 对应的 {@link FlushStrategy}。
     */
    static FlushStrategy afterNumPackets(int packets) {
        ObjectUtil.checkPositive(packets, "packets");
        return (numPackets, numBytes) -> numPackets > packets;
    }
}
