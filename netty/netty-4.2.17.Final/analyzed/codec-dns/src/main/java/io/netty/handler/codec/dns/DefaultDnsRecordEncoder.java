/*
 * Copyright 2015 The Netty Project
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
import io.netty.handler.codec.UnsupportedMessageTypeException;

/**
 * {@link DnsRecordEncoder} 的默认实现，将问题段与资源记录写入 {@link io.netty.buffer.ByteBuf}。
 * <p>
 * 支持 PTR、OPT/ECS 伪记录及原始 RDATA 的编码。
 *
 * @see DefaultDnsRecordDecoder
 */
public class DefaultDnsRecordEncoder implements DnsRecordEncoder {
    /** ECS 源前缀长度取模 8 的位掩码。 */
    private static final int PREFIX_MASK = Byte.SIZE - 1;

    /** 受保护构造，供子类扩展编码逻辑。 */
    protected DefaultDnsRecordEncoder() { }

    @Override
    public final void encodeQuestion(DnsQuestion question, ByteBuf out) throws Exception {
        encodeName(question.name(), out);
        out.writeShort(question.type().intValue());
        out.writeShort(question.dnsClass());
    }

    private static final Class<?>[] SUPPORTED_MESSAGES = new Class<?>[] {
            DnsQuestion.class, DnsPtrRecord.class,
            DnsOptEcsRecord.class, DnsOptPseudoRecord.class, DnsRawRecord.class };

    @Override
    public void encodeRecord(DnsRecord record, ByteBuf out) throws Exception {
        if (record instanceof DnsQuestion) {
            encodeQuestion((DnsQuestion) record, out);
        } else if (record instanceof DnsPtrRecord) {
            encodePtrRecord((DnsPtrRecord) record, out);
        } else if (record instanceof DnsOptEcsRecord) {
            encodeOptEcsRecord((DnsOptEcsRecord) record, out);
        } else if (record instanceof DnsOptPseudoRecord) {
            encodeOptPseudoRecord((DnsOptPseudoRecord) record, out);
        } else if (record instanceof DnsRawRecord) {
            encodeRawRecord((DnsRawRecord) record, out);
        } else {
            throw new UnsupportedMessageTypeException(record, SUPPORTED_MESSAGES);
        }
    }

    private void encodeRecord0(DnsRecord record, ByteBuf out) throws Exception {
        encodeName(record.name(), out);
        out.writeShort(record.type().intValue());
        out.writeShort(record.dnsClass());
        out.writeInt((int) record.timeToLive());
    }

    private void encodePtrRecord(DnsPtrRecord record, ByteBuf out) throws Exception {
        encodeRecord0(record, out);
        int writerIndex = out.writerIndex();
        // 先跳过 2 字节 RDATA 长度，写入域名后再回填
        // 参见 https://www.rfc-editor.org/rfc/rfc1035.html#section-3.2.1
        out.writerIndex(writerIndex + 2);
        encodeName(record.hostname(), out);
        int rdLength = out.writerIndex() - (writerIndex + 2);
        out.setShort(writerIndex, rdLength);
    }

    private void encodeOptPseudoRecord(DnsOptPseudoRecord record, ByteBuf out) throws Exception {
        encodeRecord0(record, out);
        out.writeShort(0);
    }

    private void encodeOptEcsRecord(DnsOptEcsRecord record, ByteBuf out) throws Exception {
        encodeRecord0(record, out);

        int sourcePrefixLength = record.sourcePrefixLength();
        int scopePrefixLength = record.scopePrefixLength();
        int lowOrderBitsToPreserve = sourcePrefixLength & PREFIX_MASK;

        byte[] bytes = record.address();
        int addressBits = bytes.length << 3;
        if (addressBits < sourcePrefixLength || sourcePrefixLength < 0) {
            throw new IllegalArgumentException(sourcePrefixLength + ": " +
                    sourcePrefixLength + " (expected: 0 >= " + addressBits + ')');
        }

        // 地址族编号：1=IPv4，2=IPv6（见 IANA 地址族编号表）
        final short addressNumber = (short) (bytes.length == 4 ? 1 : 2);
        int payloadLength = calculateEcsAddressLength(sourcePrefixLength, lowOrderBitsToPreserve);

        int fullPayloadLength = 2 + // OPTION-CODE
                2 + // OPTION-LENGTH
                2 + // FAMILY
                1 + // SOURCE PREFIX-LENGTH
                1 + // SCOPE PREFIX-LENGTH
                payloadLength; // ADDRESS 字节

        out.writeShort(fullPayloadLength);
        out.writeShort(8); // ECS 选项码固定为 8

        out.writeShort(fullPayloadLength - 4); // 不含 OPTION-CODE 与 OPTION-LENGTH
        out.writeShort(addressNumber);
        out.writeByte(sourcePrefixLength);
        out.writeByte(scopePrefixLength); // 查询中 scope 前缀长度必须为 0

        if (lowOrderBitsToPreserve > 0) {
            int bytesLength = payloadLength - 1;
            out.writeBytes(bytes, 0, bytesLength);

            // 末字节不足 8 位时用零填充
            out.writeByte(padWithZeros(bytes[bytesLength], lowOrderBitsToPreserve));
        } else {
            // 前缀长度按字节对齐，直接拷贝地址字节
            out.writeBytes(bytes, 0, payloadLength);
        }
    }

    /** 计算 ECS 地址载荷字节数（包内可见，供测试使用）。 */
    static int calculateEcsAddressLength(int sourcePrefixLength, int lowOrderBitsToPreserve) {
        return (sourcePrefixLength >>> 3) + (lowOrderBitsToPreserve != 0 ? 1 : 0);
    }

    private void encodeRawRecord(DnsRawRecord record, ByteBuf out) throws Exception {
        encodeRecord0(record, out);

        ByteBuf content = record.content();
        int contentLen = content.readableBytes();

        out.writeShort(contentLen);
        out.writeBytes(content, content.readerIndex(), contentLen);
    }

    /** 将域名编码为 DNS 标签序列，委托 {@link DnsCodecUtil#encodeDomainName}。 */
    protected void encodeName(String name, ByteBuf buf) throws Exception {
        DnsCodecUtil.encodeDomainName(name, buf);
    }

    private static byte padWithZeros(byte b, int lowOrderBitsToPreserve) {
        switch (lowOrderBitsToPreserve) {
        case 0:
            return 0;
        case 1:
            return (byte) (0x80 & b);
        case 2:
            return (byte) (0xC0 & b);
        case 3:
            return (byte) (0xE0 & b);
        case 4:
            return (byte) (0xF0 & b);
        case 5:
            return (byte) (0xF8 & b);
        case 6:
            return (byte) (0xFC & b);
        case 7:
            return (byte) (0xFE & b);
        case 8:
            return b;
        default:
            throw new IllegalArgumentException("lowOrderBitsToPreserve: " + lowOrderBitsToPreserve);
        }
    }
}
