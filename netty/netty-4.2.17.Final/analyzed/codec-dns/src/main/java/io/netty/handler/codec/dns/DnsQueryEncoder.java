/*
 * Copyright 2019 The Netty Project
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
package io.netty.handler.codec.dns;

import io.netty.buffer.ByteBuf;

import static io.netty.util.internal.ObjectUtil.checkNotNull;

/** DNS 查询报文编码器（包内可见），将 {@link DnsQuery} 写入 {@link ByteBuf}。 */
final class DnsQueryEncoder {

    private final DnsRecordEncoder recordEncoder;

    /** 使用指定 {@code recordEncoder} 创建编码器。 */
    DnsQueryEncoder(DnsRecordEncoder recordEncoder) {
        this.recordEncoder = checkNotNull(recordEncoder, "recordEncoder");
    }

    /** 将 {@link DnsQuery} 编码为字节序列（头部 + 问题段 + 附加段）。 */
    void encode(DnsQuery query, ByteBuf out) throws Exception {
        encodeHeader(query, out);
        encodeQuestions(query, out);
        encodeRecords(query, DnsSection.ADDITIONAL, out);
    }

    /**
     * 编码固定 12 字节的 DNS 查询头部（QR=0，含 opCode 与 RD 标志）。
     *
     * @param query the query header being encoded
     * @param buf   the buffer the encoded data should be written to
     */
    private static void encodeHeader(DnsQuery query, ByteBuf buf) {
        buf.writeShort(query.id());
        int flags = 0;
        flags |= (query.opCode().byteValue() & 0xFF) << 14;
        if (query.isRecursionDesired()) {
            flags |= 1 << 8;
        }
        buf.writeShort(flags);
        buf.writeShort(query.count(DnsSection.QUESTION));
        buf.writeShort(0); // 应答段计数固定为 0
        buf.writeShort(0); // 权威段计数固定为 0
        buf.writeShort(query.count(DnsSection.ADDITIONAL));
    }

    private void encodeQuestions(DnsQuery query, ByteBuf buf) throws Exception {
        final int count = query.count(DnsSection.QUESTION);
        for (int i = 0; i < count; i++) {
            recordEncoder.encodeQuestion((DnsQuestion) query.recordAt(DnsSection.QUESTION, i), buf);
        }
    }

    private void encodeRecords(DnsQuery query, DnsSection section, ByteBuf buf) throws Exception {
        final int count = query.count(section);
        for (int i = 0; i < count; i++) {
            recordEncoder.encodeRecord(query.recordAt(section, i), buf);
        }
    }
}
