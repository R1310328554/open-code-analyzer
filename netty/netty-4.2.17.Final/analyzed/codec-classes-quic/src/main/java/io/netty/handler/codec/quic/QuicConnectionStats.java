/*
 * Copyright 2020 The Netty Project
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
 * QUIC 连接级统计指标；实现未知时各方法可能返回 {@code -1}。
 */
public interface QuicConnectionStats {
    /** @return 连接收到的 QUIC 报文总数。 */
    long recv();

    /** @return 连接发送的 QUIC 报文总数。 */
    long sent();

    /** @return 连接丢失的 QUIC 报文数。 */
    long lost();

    /** @return 含重传数据的发送报文数。 */
    long retrans();

    /** @return 发送字节总数。 */
    long sentBytes();

    /** @return 接收字节总数。 */
    long recvBytes();

    /** @return 丢失字节总数。 */
    long lostBytes();

    /** @return 流数据重传字节总数。 */
    long streamRetransBytes();

    /** @return 连接已知路径（path）数量。 */
    long pathsCount();
}
