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
import io.netty.handler.codec.CorruptedFrameException;

/**
 * {@link DnsRecordDecoder} 的默认实现，负责从 {@link io.netty.buffer.ByteBuf} 解析问题段与资源记录。
 * <p>
 * 支持 PTR/CNAME/NS/MX 等类型的域名解压缩，其余类型保留原始 RDATA。
 *
 * @see DefaultDnsRecordEncoder
 */
public class DefaultDnsRecordDecoder implements DnsRecordDecoder {

    /** 根域名占位符。 */
    static final String ROOT = ".";

    /** 受保护构造，供子类扩展解码逻辑。 */
    protected DefaultDnsRecordDecoder() { }

    @Override
    public final DnsQuestion decodeQuestion(ByteBuf in) throws Exception {
        String name = decodeName(in);
        DnsRecordType type = DnsRecordType.valueOf(in.readUnsignedShort());
        int qClass = in.readUnsignedShort();
        return new DefaultDnsQuestion(name, type, qClass);
    }

    @Override
    public final <T extends DnsRecord> T decodeRecord(ByteBuf in) throws Exception {
        final int startOffset = in.readerIndex();
        final String name = decodeName(in);

        final int endOffset = in.writerIndex();
        if (endOffset - in.readerIndex() < 10) {
            // 剩余字节不足以读取类型/类/TTL/长度字段
            in.readerIndex(startOffset);
            return null;
        }

        final DnsRecordType type = DnsRecordType.valueOf(in.readUnsignedShort());
        final int aClass = in.readUnsignedShort();
        final long ttl = in.readUnsignedInt();
        final int length = in.readUnsignedShort();
        final int offset = in.readerIndex();

        if (endOffset - offset < length) {
            // RDATA 长度超出可读范围
            in.readerIndex(startOffset);
            return null;
        }

        @SuppressWarnings("unchecked")
        T record = (T) decodeRecord(name, type, aClass, ttl, in, offset, length);
        in.readerIndex(offset + length);
        return record;
    }

    /**
     * 根据 {@link #decodeRecord(ByteBuf)} 已解析的头部信息解码 RDATA。
     *
     * @param name the domain name of the record
     * @param type the type of the record
     * @param dnsClass the class of the record
     * @param timeToLive the TTL of the record
     * @param in the {@link ByteBuf} that contains the RDATA
     * @param offset the start offset of the RDATA in {@code in}
     * @param length the length of the RDATA
     *
     * @return {@link DnsRawRecord} 或具体类型记录；子类可覆写以返回自定义实现。
     */
    protected DnsRecord decodeRecord(
            String name, DnsRecordType type, int dnsClass, long timeToLive,
            ByteBuf in, int offset, int length) throws Exception {

        // DNS 报文压缩使域名可含指针，索引须保持有效，故不能用 slice 而需 duplicate
        // 参见 https://www.ietf.org/rfc/rfc1035 [4.1.4. Message compression]
        if (type == DnsRecordType.PTR) {
            return new DefaultDnsPtrRecord(
                    name, dnsClass, timeToLive, decodeName0(in.duplicate().setIndex(offset, offset + length)));
        }
        if (type == DnsRecordType.CNAME || type == DnsRecordType.NS) {
            ByteBuf decompressed = DnsCodecUtil.decompressDomainName(
                    in.duplicate().setIndex(offset, offset + length));
            try {
                DnsRecord record = new DefaultDnsRawRecord(name, type, dnsClass, timeToLive, decompressed);
                decompressed = null;
                return record;
            } finally {
                if (decompressed != null) {
                    decompressed.release();
                }
            }
        }
        if (type ==  DnsRecordType.MX) {
            // MX RDATA：16 位优先级 + 交换域名（可能压缩）
            if (length < 3) {
                throw new CorruptedFrameException("MX record RDATA is too short: " + length);
            }
            final int pref = in.getUnsignedShort(offset);
            ByteBuf exchange = null;
            ByteBuf out = null;
            try {
                exchange = DnsCodecUtil.decompressDomainName(
                        in.duplicate().setIndex(offset + 2, offset + length));

                // 组装解压后的 RDATA = [优先级][展开后的交换域名]
                out = in.alloc().buffer(2 + exchange.readableBytes());
                out.writeShort(pref);
                out.writeBytes(exchange);

                DnsRecord record = new DefaultDnsRawRecord(name, type, dnsClass, timeToLive, out);
                out = null;
                return record;
            } finally {
                if (exchange != null) {
                    exchange.release();
                }
                if (out != null) {
                    out.release();
                }
            }
        }

        ByteBuf content = in.retainedDuplicate();
        try {
            content.setIndex(offset, offset + length);
            DnsRecord record = new DefaultDnsRawRecord(name, type, dnsClass, timeToLive, content);
            content = null;
            return record;
        } finally {
            if (content != null) {
                content.release();
            }
        }
    }

    /**
     * 从 DNS 报文缓冲中读取域名；若含压缩指针，读完后 readerIndex 置于指针之后。
     *
     * @param in the byte buffer containing the DNS packet
     * @return the domain name for an entry
     */
    protected String decodeName0(ByteBuf in) {
        return decodeName(in);
    }

    /**
     * 静态入口：委托 {@link DnsCodecUtil#decodeDomainName} 解析域名。
     *
     * @param in the byte buffer containing the DNS packet
     * @return the domain name for an entry
     */
    public static String decodeName(ByteBuf in) {
        return DnsCodecUtil.decodeDomainName(in);
    }
}
