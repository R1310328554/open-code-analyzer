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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.net.InetSocketAddress;

import static io.netty.util.internal.ObjectUtil.checkPositiveOrZero;

/**
 * 解析 QUIC 报文首部，成功后通过回调通知调用方。
 * <p>
 * 不再使用时须调用 {@link #close()} 释放资源，否则可能导致泄漏。
 * <p>
 * 面向高级场景；一般应用直接使用 {@link QuicClientCodecBuilder} 或 {@link QuicServerCodecBuilder} 即可。
 */
public final class QuicHeaderParser implements AutoCloseable {
    // See https://datatracker.ietf.org/doc/rfc7714/
    private static final int AES_128_GCM_TAG_LENGTH = 16;

    private final int localConnectionIdLength;
    private boolean closed;

    /** 指定本地连接 ID 长度（短头报文解析时使用）。 */
    public QuicHeaderParser(int localConnectionIdLength) {
        this.localConnectionIdLength = checkPositiveOrZero(localConnectionIdLength, "localConnectionIdLength");
    }

    /** 标记解析器已关闭，后续 {@link #parse} 将抛出 {@link IllegalStateException}。 */
    @Override
    public void close() {
        if (!closed) {
            closed = true;
        }
    }

    /**
     * 解析 QUIC 报文并提取首部字段。本方法不接管 {@code packet} 所有权，调用方须在不再需要时自行 {@link ByteBuf#release()}。
     *
     * @param sender        the sender of the packet. This is directly passed to the {@link QuicHeaderProcessor} once
     *                      parsing was successful.
     * @param recipient     the recipient of the packet.This is directly passed to the {@link QuicHeaderProcessor} once
     *                      parsing was successful.
     * @param packet        raw QUIC packet itself. The ownership of the packet is not transferred. This is directly
     *                      passed to the {@link QuicHeaderProcessor} once parsing was successful.
     * @param callback      the {@link QuicHeaderProcessor} that is called once a QUIC packet could be parsed and all
     *                      the header values be extracted.
     * @throws Exception    thrown if we couldn't parse the header or if the {@link QuicHeaderProcessor} throws an
     *                      exception.
     */
    public void parse(InetSocketAddress sender, InetSocketAddress recipient, ByteBuf packet,
                      QuicHeaderProcessor callback) throws Exception {
        if (closed) {
            throw new IllegalStateException(QuicHeaderParser.class.getSimpleName() + " is already closed");
        }

        // See https://datatracker.ietf.org/doc/html/rfc9000#section-17
        int offset = 0;
        int readable = packet.readableBytes();

        checkReadable(offset, readable, Byte.BYTES);
        byte first = packet.getByte(offset);
        offset += Byte.BYTES;

        final QuicPacketType type;
        final long version;
        final ByteBuf dcid;
        final ByteBuf scid;
        final ByteBuf token;

        if (hasShortHeader(first)) {
            // See https://www.rfc-editor.org/rfc/rfc9000.html#section-17.3
            // 1-RTT Packet {
            //  Header Form (1) = 0,
            //  Fixed Bit (1) = 1,
            //  Spin Bit (1),
            //  Reserved Bits (2),
            //  Key Phase (1),
            //  Packet Number Length (2),
            //  Destination Connection ID (0..160),
            //  Packet Number (8..32),
            //  Packet Payload (8..),
            //}
            version = 0;
            type = QuicPacketType.SHORT;
            // Short packets have no source connection id and no token.
            scid = Unpooled.EMPTY_BUFFER;
            token = Unpooled.EMPTY_BUFFER;
            dcid = sliceCid(packet, offset, localConnectionIdLength);
        } else {
            // See https://www.rfc-editor.org/rfc/rfc9000.html#section-17.2
            // Long Header Packet {
            //  Header Form (1) = 1,
            //  Fixed Bit (1) = 1,
            //  Long Packet Type (2),
            //  Type-Specific Bits (4),
            //  Version (32),
            //  Destination Connection ID Length (8),
            //  Destination Connection ID (0..160),
            //  Source Connection ID Length (8),
            //  Source Connection ID (0..160),
            //  Type-Specific Payload (..),
            //}
            checkReadable(offset, readable, Integer.BYTES);

            // Version uses an unsigned int:
            // https://www.rfc-editor.org/rfc/rfc9000.html#section-15
            version = packet.getUnsignedInt(offset);
            offset += Integer.BYTES;
            type = typeOfLongHeader(first, version);

            int dcidLen = packet.getUnsignedByte(offset);
            checkCidLength(dcidLen);
            offset += Byte.BYTES;
            dcid = sliceCid(packet, offset, dcidLen);
            offset += dcidLen;

            int scidLen = packet.getUnsignedByte(offset);
            checkCidLength(scidLen);
            offset += Byte.BYTES;
            scid = sliceCid(packet, offset, scidLen);
            offset += scidLen;
            token = sliceToken(type, packet, offset, readable);
        }
        callback.process(sender, recipient, packet, type, version, scid, dcid, token);
    }

    // QUIC v1 连接 ID 最大 20 字节（RFC 9000 §17.2）
    private static void checkCidLength(int length) throws QuicException {
        if (length > Quic.MAX_CONN_ID_LEN) {
            throw new QuicException("connection id to large: "  + length + " > " + Quic.MAX_CONN_ID_LEN,
                    QuicTransportError.PROTOCOL_VIOLATION);
        }
    }

    private static ByteBuf sliceToken(QuicPacketType type, ByteBuf packet, int offset, int readable)
            throws QuicException {
        switch (type) {
            case INITIAL:
                // See
                checkReadable(offset, readable, Byte.BYTES);
                int numBytes = numBytesForVariableLengthInteger(packet.getByte(offset));
                int len = (int) getVariableLengthInteger(packet, offset, numBytes);
                offset += numBytes;

                checkReadable(offset, readable, len);
                return packet.slice(offset, len);
            case RETRY:
                // RETRY 报文 token 不含完整性标签（RFC 9000 §17.2.5）
                checkReadable(offset, readable, AES_128_GCM_TAG_LENGTH);
                int tokenLen = readable - offset - AES_128_GCM_TAG_LENGTH;
                return packet.slice(offset, tokenLen);
            default:
                // No token included.
                return Unpooled.EMPTY_BUFFER;
        }
    }
    private static QuicException newProtocolViolationException(String message) {
        return new QuicException(message, QuicTransportError.PROTOCOL_VIOLATION);
    }

    static ByteBuf sliceCid(ByteBuf buffer, int offset, int len) throws QuicException {
        checkReadable(offset, buffer.readableBytes(), len);
        return buffer.slice(offset, len);
    }

    private static void checkReadable(int offset, int readable, int needed) throws QuicException {
        int r = readable - offset;
        if (r < needed) {
            throw newProtocolViolationException("Not enough bytes to read, " + r + " < " + needed);
        }
    }

    /**
     * 从 {@link ByteBuf} 读取 QUIC 变长整数。
     * 参见 <a href="https://tools.ietf.org/html/draft-ietf-quic-transport-32#section-16">
     *     Variable-Length Integer Encoding </a>
     */
    private static long getVariableLengthInteger(ByteBuf in, int offset, int len) throws QuicException {
        checkReadable(offset, in.readableBytes(), len);
        switch (len) {
            case 1:
                return in.getUnsignedByte(offset);
            case 2:
                return in.getUnsignedShort(offset) & 0x3fff;
            case 4:
                return in.getUnsignedInt(offset) & 0x3fffffff;
            case 8:
                return in.getLong(offset) & 0x3fffffffffffffffL;
            default:
                throw newProtocolViolationException("Unsupported length:" + len);
        }
    }

    /**
     * 根据首字节判断变长整数编码所占字节数。
     * 参见 <a href="https://tools.ietf.org/html/draft-ietf-quic-transport-32#section-16">
     *     Variable-Length Integer Encoding </a>
     */
    private static int numBytesForVariableLengthInteger(byte b) {
        byte val = (byte) (b >> 6);
        if ((val & 1) != 0) {
            if ((val & 2) != 0) {
                return 8;
            }
            return 2;
        }
        if ((val & 2) != 0) {
            return 4;
        }
        return 1;
    }

    /** 判断首字节是否为 QUIC 短头格式（Header Form 位为 0）。 */
    static boolean hasShortHeader(byte b) {
        return (b & 0x80) == 0;
    }

    private static QuicPacketType typeOfLongHeader(byte first, long version) throws QuicException {
        if (version == 0) {
            // version 为 0 表示版本协商报文（RFC 9000 §17.2.1），忽略 first 中其余类型位
            return QuicPacketType.VERSION_NEGOTIATION;
        }
        int packetType = (first & 0x30) >> 4;
        switch (packetType) {
            case 0x00:
                return QuicPacketType.INITIAL;
            case 0x01:
                return QuicPacketType.ZERO_RTT;
            case 0x02:
                return QuicPacketType.HANDSHAKE;
            case 0x03:
                return QuicPacketType.RETRY;
            default:
                throw newProtocolViolationException("Unknown packet type: " + packetType);
        }
    }

    /**
     * QUIC 报文首部解析成功后的回调接口。
     */
    public interface QuicHeaderProcessor {

        /**
         * 报文首部解析完成时调用；{@code packet} 等缓冲区所有权不转移，需 {@link ByteBuf#retain()} 才能跨方法持有。
         *
         * @param sender        the sender of the QUIC packet.
         * @param recipient     the recipient of the QUIC packet.
         * @param packet        the raw QUIC packet. The ownership is not transferred, which means you will need to call
         *                      {@link ByteBuf#retain()} on it if you want to keep a reference after this method
         *                      returns.
         * @param type          the type of the packet.
         * @param version       the version of the packet.
         * @param scid          the source connection id. The ownership is not transferred and its generally not allowed
         *                      to hold any references to this buffer outside of the method as it will be re-used.
         * @param dcid          the destination connection id. The ownership is not transferred and its generally not
         *                      allowed to hold any references to this buffer outside of the method as it will be
         *                      re-used.
         * @param token         the token.The ownership is not transferred and its generally not allowed
         *                      to hold any references to this buffer outside of the method as it will be re-used.
         * @throws Exception    throws if an error happens during processing.
         */
        void process(InetSocketAddress sender, InetSocketAddress recipient, ByteBuf packet,
                     QuicPacketType type, long version, ByteBuf scid, ByteBuf dcid, ByteBuf token) throws Exception;
    }
}
