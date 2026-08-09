/*
 * Copyright 2014 The Netty Project
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
package io.netty.handler.codec.haproxy;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.ProtocolDetectionResult;
import io.netty.util.CharsetUtil;

import java.util.List;

import static io.netty.handler.codec.haproxy.HAProxyConstants.*;

/**
 * HAProxy PROXY 协议头部解码器，支持 v1 文本与 v2 二进制格式。
 * <p>
 * 解码完成后自动从 pipeline 移除自身（单次解码）。
 *
 * @see <a href="https://haproxy.1wt.eu/download/1.5/doc/proxy-protocol.txt">Proxy Protocol Specification</a>
 */
public class HAProxyMessageDecoder extends ByteToMessageDecoder {
    /** v1 头部最大长度（规范上限）。 */
    private static final int V1_MAX_LENGTH = 108;

    /** v2 头部最大长度（16 字节固定头 + 最大 unsigned short 地址/TLV 块）。 */
    private static final int V2_MAX_LENGTH = 16 + 65535;

    /** v2 完整头部最小长度（16 字节 + 最大地址信息 216 字节）。 */
    private static final int V2_MIN_LENGTH = 16 + 216;

    /** v2 附加 TLV 数据最大长度。 */
    private static final int V2_MAX_TLV = 65535 - 216;

    /** 二进制头部前缀长度。 */
    private static final int BINARY_PREFIX_LENGTH = BINARY_PREFIX.length;

    /** v1 协议检测结果常量。 */
    private static final ProtocolDetectionResult<HAProxyProtocolVersion> DETECTION_RESULT_V1 =
            ProtocolDetectionResult.detected(HAProxyProtocolVersion.V1);

    /** v2 协议检测结果常量。 */
    private static final ProtocolDetectionResult<HAProxyProtocolVersion> DETECTION_RESULT_V2 =
            ProtocolDetectionResult.detected(HAProxyProtocolVersion.V2);

    /** 从 {@link ByteBuf} 提取完整头部帧的提取器。 */
    private HeaderExtractor headerExtractor;

    /** 是否因超出 maxLength 而正在丢弃输入。 */
    private boolean discarding;

    /** 已丢弃的字节数。 */
    private int discardedBytes;

    /** 超出 maxLength 时是否立即抛异常（failFast）。 */
    private final boolean failFast;

    /** 是否已完成 PROXY 头部解码。 */
    private boolean finished;

    /** 检测到的协议版本（1 或 2），-1 表示尚未确定。 */
    private int version = -1;

    /** v2 可配置的最大头部尺寸（含地址块与 TLV）。 */
    private final int v2MaxHeaderSize;

    /** 创建无 TLV 限制、failFast 为 true 的解码器。 */
    public HAProxyMessageDecoder() {
        this(true);
    }

    /**
     * 创建无 TLV 限制的解码器。
     *
     * @param failFast Whether or not to throw an exception as soon as we exceed maxLength
     */
    public HAProxyMessageDecoder(boolean failFast) {
        v2MaxHeaderSize = V2_MAX_LENGTH;
        this.failFast = failFast;
    }

    /**
     * 创建限制 v2 TLV 大小的解码器（failFast 为 true）。
     * <p>
     * <b>注意：</b> TLV 限制仅影响 v2 二进制头；为最佳性能建议上游不发送 TLV。
     * </p>
     *
     * @param maxTlvSize maximum number of bytes allowed for additional data (Type-Length-Value vectors) in a v2 header
     */
    public HAProxyMessageDecoder(int maxTlvSize) {
        this(maxTlvSize, true);
    }

    /**
     * 创建限制 v2 TLV 大小并可配置 failFast 的解码器。
     *
     * @param maxTlvSize maximum number of bytes allowed for additional data (Type-Length-Value vectors) in a v2 header
     * @param failFast Whether or not to throw an exception as soon as we exceed maxLength
     */
    public HAProxyMessageDecoder(int maxTlvSize, boolean failFast) {
        if (maxTlvSize < 1) {
            v2MaxHeaderSize = V2_MIN_LENGTH;
        } else if (maxTlvSize > V2_MAX_TLV) {
            v2MaxHeaderSize = V2_MAX_LENGTH;
        } else {
            int calcMax = maxTlvSize + V2_MIN_LENGTH;
            if (calcMax > V2_MAX_LENGTH) {
                v2MaxHeaderSize = V2_MAX_LENGTH;
            } else {
                v2MaxHeaderSize = calcMax;
            }
        }
        this.failFast = failFast;
    }

    /** 检测缓冲中的协议版本；不足 13 字节或无法识别时返回 -1。 */
    private static int findVersion(final ByteBuf buffer) {
        final int n = buffer.readableBytes();
        // 规范：版本号位于第 13 字节
        if (n < 13) {
            return -1;
        }

        int idx = buffer.readerIndex();
        return match(BINARY_PREFIX, buffer, idx) ? buffer.getUnsignedByte(idx + BINARY_PREFIX_LENGTH) : 1;
    }

    /** 返回 v2 头部结束位置（16 + 地址长度）；数据不足时返回 -1。 */
    private static int findEndOfHeader(final ByteBuf buffer) {
        final int n = buffer.readableBytes();

        // 规范：第 15~16 字节为地址信息长度
        if (n < 16) {
            return -1;
        }

        int offset = buffer.readerIndex() + 14;

        // 总头部长度 = 16 字节固定头 + 动态地址块
        int totalHeaderBytes = 16 + buffer.getUnsignedShort(offset);

        // 确认缓冲中已有完整头部
        if (n >= totalHeaderBytes) {
            return totalHeaderBytes;
        } else {
            return -1;
        }
    }

    /** 查找 v1 文本头部的 CRLF 行尾位置；未找到返回 -1。 */
    private static int findEndOfLine(final ByteBuf buffer) {
        final int n = buffer.writerIndex();
        for (int i = buffer.readerIndex(); i < n; i++) {
            final byte b = buffer.getByte(i);
            if (b == '\r' && i < n - 1 && buffer.getByte(i + 1) == '\n') {
                return i;  // \r\n
            }
        }
        return -1;  // 未找到行尾
    }

    @Override
    public boolean isSingleDecode() {
        // 仅解码一次 PROXY 头，故始终返回 true 以提前退出解码循环
        return true;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
        if (finished) {
            ctx.pipeline().remove(this);
        }
    }

    @Override
    protected final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 确定协议版本（v1 或 v2）
        if (version == -1) {
            if ((version = findVersion(in)) == -1) {
                return;
            }
        }

        ByteBuf decoded;

        if (version == 1) {
            decoded = decodeLine(ctx, in);
        } else {
            decoded = decodeStruct(ctx, in);
        }

        if (decoded != null) {
            finished = true;
            try {
                if (version == 1) {
                    out.add(HAProxyMessage.decodeHeader(decoded.toString(CharsetUtil.US_ASCII)));
                } else {
                    out.add(HAProxyMessage.decodeHeader(decoded));
                }
            } catch (HAProxyProtocolException e) {
                fail(ctx, null, e);
            }
        }
    }

    /**
     * 从缓冲提取 v2 二进制头部帧。
     *
     * @param ctx     the {@link ChannelHandlerContext} which this {@link HAProxyMessageDecoder} belongs to
     * @param buffer  the {@link ByteBuf} from which to read data
     * @return frame  the {@link ByteBuf} which represent the frame or {@code null} if no frame could
     *                be created
     */
    private ByteBuf decodeStruct(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {
        if (headerExtractor == null) {
            headerExtractor = new StructHeaderExtractor(v2MaxHeaderSize);
        }
        return headerExtractor.extract(ctx, buffer);
    }

    /**
     * 从缓冲提取 v1 文本行头部帧。
     *
     * @param ctx     the {@link ChannelHandlerContext} which this {@link HAProxyMessageDecoder} belongs to
     * @param buffer  the {@link ByteBuf} from which to read data
     * @return frame  the {@link ByteBuf} which represent the frame or {@code null} if no frame could
     *                be created
     */
    private ByteBuf decodeLine(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {
        if (headerExtractor == null) {
            headerExtractor = new LineHeaderExtractor(V1_MAX_LENGTH);
        }
        return headerExtractor.extract(ctx, buffer);
    }

    private void failOverLimit(final ChannelHandlerContext ctx, int length) {
        failOverLimit(ctx, String.valueOf(length));
    }

    private void failOverLimit(final ChannelHandlerContext ctx, String length) {
        int maxLength = version == 1 ? V1_MAX_LENGTH : v2MaxHeaderSize;
        fail(ctx, "header length (" + length + ") exceeds the allowed maximum (" + maxLength + ')', null);
    }

    private void fail(final ChannelHandlerContext ctx, String errMsg, Exception e) {
        finished = true;
        ctx.close(); // 规范要求：协议错误时立即关闭连接
        HAProxyProtocolException ppex;
        if (errMsg != null && e != null) {
            ppex = new HAProxyProtocolException(errMsg, e);
        } else if (errMsg != null) {
            ppex = new HAProxyProtocolException(errMsg);
        } else if (e != null) {
            ppex = new HAProxyProtocolException(e);
        } else {
            ppex = new HAProxyProtocolException();
        }
        throw ppex;
    }

    /** 静态检测缓冲前缀，判断 v1/v2/无效/需更多数据。 */
    public static ProtocolDetectionResult<HAProxyProtocolVersion> detectProtocol(ByteBuf buffer) {
        if (buffer.readableBytes() < 12) {
            return ProtocolDetectionResult.needsMoreData();
        }

        int idx = buffer.readerIndex();

        if (match(BINARY_PREFIX, buffer, idx)) {
            return DETECTION_RESULT_V2;
        }
        if (match(TEXT_PREFIX, buffer, idx)) {
            return DETECTION_RESULT_V1;
        }
        return ProtocolDetectionResult.invalid();
    }

    private static boolean match(byte[] prefix, ByteBuf buffer, int idx) {
        for (int i = 0; i < prefix.length; i++) {
            final byte b = buffer.getByte(idx + i);
            if (b != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    /** 从 {@link ByteBuf} 提取完整头部帧的抽象基类。 */
    private abstract class HeaderExtractor {
        /** 允许的最大头部长度 */
        private final int maxHeaderSize;

        protected HeaderExtractor(int maxHeaderSize) {
            this.maxHeaderSize = maxHeaderSize;
        }

        /**
         * Create a frame out of the {@link ByteBuf} and return it.
         *
         * @param ctx     the {@link ChannelHandlerContext} which this {@link HAProxyMessageDecoder} belongs to
         * @param buffer  the {@link ByteBuf} from which to read data
         * @return frame  the {@link ByteBuf} which represent the frame or {@code null} if no frame could
         *                be created
         * @throws Exception if exceed maxLength
         */
        public ByteBuf extract(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {
            final int eoh = findEndOfHeader(buffer);
            if (!discarding) {
                if (eoh >= 0) {
                    final int length = eoh - buffer.readerIndex();
                    if (length > maxHeaderSize) {
                        buffer.readerIndex(eoh + delimiterLength(buffer, eoh));
                        failOverLimit(ctx, length);
                        return null;
                    }
                    ByteBuf frame = buffer.readSlice(length);
                    buffer.skipBytes(delimiterLength(buffer, eoh));
                    return frame;
                } else {
                    final int length = buffer.readableBytes();
                    if (length > maxHeaderSize) {
                        discardedBytes = length;
                        buffer.skipBytes(length);
                        discarding = true;
                        if (failFast) {
                            failOverLimit(ctx, "over " + discardedBytes);
                        }
                    }
                    return null;
                }
            } else {
                if (eoh >= 0) {
                    final int length = discardedBytes + eoh - buffer.readerIndex();
                    buffer.readerIndex(eoh + delimiterLength(buffer, eoh));
                    discardedBytes = 0;
                    discarding = false;
                    if (!failFast) {
                        failOverLimit(ctx, "over " + length);
                    }
                } else {
                    discardedBytes += buffer.readableBytes();
                    buffer.skipBytes(buffer.readableBytes());
                }
                return null;
            }
        }

        /**
         * 查找头部结束位置（CRLF 或由头部给出的长度）。
         *
         * @param buffer the buffer to be searched
         * @return {@code -1} if can not find the end, otherwise return the buffer index of end
         */
        protected abstract int findEndOfHeader(ByteBuf buffer);

        /**
         * 返回头部分隔符长度（v1 为 CRLF，v2 为 0）。
         *
         * @param buffer the buffer where delimiter is located
         * @param eoh index of delimiter
         * @return length of the delimiter
         */
        protected abstract int delimiterLength(ByteBuf buffer, int eoh);
    }

    private final class LineHeaderExtractor extends HeaderExtractor {

        LineHeaderExtractor(int maxHeaderSize) {
            super(maxHeaderSize);
        }

        @Override
        protected int findEndOfHeader(ByteBuf buffer) {
            return findEndOfLine(buffer);
        }

        @Override
        protected int delimiterLength(ByteBuf buffer, int eoh) {
            return buffer.getByte(eoh) == '\r' ? 2 : 1;
        }
    }

    private final class StructHeaderExtractor extends HeaderExtractor {

        StructHeaderExtractor(int maxHeaderSize) {
            super(maxHeaderSize);
        }

        @Override
        protected int findEndOfHeader(ByteBuf buffer) {
            return HAProxyMessageDecoder.findEndOfHeader(buffer);
        }

        @Override
        protected int delimiterLength(ByteBuf buffer, int eoh) {
            return 0;
        }
    }
}
