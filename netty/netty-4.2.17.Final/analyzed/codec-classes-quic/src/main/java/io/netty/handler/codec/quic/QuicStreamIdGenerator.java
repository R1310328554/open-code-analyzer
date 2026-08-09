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
 * 按 RFC 9000 流 ID 编码规则，为 QUIC 流分配下一个可用流标识符。
 * 客户端与服务端起始 ID 不同，双向与单向流各自递增 4。
 */
final class QuicStreamIdGenerator {
    private long nextBidirectionalStreamId;
    private long nextUnidirectionalStreamId;

    QuicStreamIdGenerator(boolean server) {
        // 参见 RFC 9000 流类型与标识符编码：https://quicwg.org/base-drafts/rfc9000.html#name-stream-types-and-identifier
        nextBidirectionalStreamId = server ? 1 : 0;
        nextUnidirectionalStreamId = server ? 3 : 2;
    }

    /** 返回下一个双向或单向流 ID，并推进内部计数器。 */
    long nextStreamId(boolean bidirectional) {
        if (bidirectional) {
            long stream = nextBidirectionalStreamId;
            nextBidirectionalStreamId += 4;
            return stream;
        }
        long stream = nextUnidirectionalStreamId;
        nextUnidirectionalStreamId += 4;
        return stream;
    }
}
