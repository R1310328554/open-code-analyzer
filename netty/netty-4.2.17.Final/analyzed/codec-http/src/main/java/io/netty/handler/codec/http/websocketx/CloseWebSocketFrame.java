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
package io.netty.handler.codec.http.websocketx;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.StringUtil;

/**
 * 关闭连接的 WebSocket 帧（opcode 0x8），载荷为 2 字节状态码加可选 UTF-8 原因。
 */
public class CloseWebSocketFrame extends WebSocketFrame {

    /**
     * 创建空关闭帧。
     */
    public CloseWebSocketFrame() {
        super(Unpooled.buffer(0));
    }

    /**
     * 以 {@link WebSocketCloseStatus} 创建关闭帧（含状态码与默认原因文本）。
     *
     * @param status
     *            Status code as per <a href="https://tools.ietf.org/html/rfc6455#section-7.4">RFC 6455</a>. For
     *            example, <tt>1000</tt> indicates normal closure.
     */
    public CloseWebSocketFrame(WebSocketCloseStatus status) {
        this(requireValidStatusCode(status.code()), status.reasonText());
    }

    /**
     * 使用指定 {@link ByteBufAllocator} 分配关闭帧载荷。
     *
     * @param status
     *            Status code as per <a href="https://tools.ietf.org/html/rfc6455#section-7.4">RFC 6455</a>. For
     *            example, <tt>1000</tt> indicates normal closure.
     */
    public CloseWebSocketFrame(WebSocketCloseStatus status, ByteBufAllocator allocator) {
        this(true, 0, allocator, requireValidStatusCode(status.code()), status.reasonText());
    }

    /**
     * 以状态枚举与自定义原因文本创建关闭帧。
     *
     * @param status
     *            Status code as per <a href="https://tools.ietf.org/html/rfc6455#section-7.4">RFC 6455</a>. For
     *            example, <tt>1000</tt> indicates normal closure.
     * @param reasonText
     *            Reason text. Set to null if no text.
     */
    public CloseWebSocketFrame(WebSocketCloseStatus status, String reasonText) {
        this(requireValidStatusCode(status.code()), reasonText);
    }

    /**
     * 以整数状态码与原因文本创建关闭帧。
     *
     * @param statusCode
     *            Integer status code as per <a href="https://tools.ietf.org/html/rfc6455#section-7.4">RFC 6455</a>. For
     *            example, <tt>1000</tt> indicates normal closure.
     * @param reasonText
     *            Reason text. Set to null if no text.
     */
    public CloseWebSocketFrame(int statusCode, String reasonText) {
        this(true, 0, requireValidStatusCode(statusCode), reasonText);
    }

    /**
     * 创建无状态码与原因文本的空关闭帧，可指定 FIN/RSV。
     *
     * @param finalFragment
     *            flag indicating if this frame is the final fragment
     * @param rsv
     *            reserved bits used for protocol extensions.
     */
    public CloseWebSocketFrame(boolean finalFragment, int rsv) {
        this(finalFragment, rsv, Unpooled.buffer(0));
    }

    /**
     * Creates a new close frame with closing status code and reason text
     *
     * @param finalFragment
     *            flag indicating if this frame is the final fragment
     * @param rsv
     *            reserved bits used for protocol extensions
     * @param statusCode
     *            Integer status code as per <a href="https://tools.ietf.org/html/rfc6455#section-7.4">RFC 6455</a>. For
     *            example, <tt>1000</tt> indicates normal closure.
     * @param reasonText
     *            Reason text. Set to null if no text.
     */
    public CloseWebSocketFrame(boolean finalFragment, int rsv, int statusCode, String reasonText) {
        this(finalFragment, rsv, UnpooledByteBufAllocator.DEFAULT, statusCode, reasonText);
    }

    /**
     * Creates a new close frame with closing status code and reason text
     *
     * @param finalFragment
     *            flag indicating if this frame is the final fragment
     * @param rsv
     *            reserved bits used for protocol extensions
     * @param allocator
     *            the ByteBuf allocator to use to build the binary data.
     * @param statusCode
     *            Integer status code as per <a href="https://tools.ietf.org/html/rfc6455#section-7.4">RFC 6455</a>. For
     *            example, <tt>1000</tt> indicates normal closure.
     * @param reasonText
     *            Reason text. Set to null if no text.
     */
    public CloseWebSocketFrame(boolean finalFragment, int rsv, ByteBufAllocator allocator,
                               int statusCode, String reasonText) {
        super(finalFragment, rsv, newBinaryData(allocator, requireValidStatusCode(statusCode), reasonText));
    }

    private static ByteBuf newBinaryData(ByteBufAllocator allocator, int statusCode, String reasonText) {
        if (reasonText == null) {
            reasonText = StringUtil.EMPTY_STRING;
        }

        ByteBuf binaryData = allocator.buffer(2 + reasonText.length());
        binaryData.writeShort(statusCode);
        if (!reasonText.isEmpty()) {
            binaryData.writeCharSequence(reasonText, CharsetUtil.UTF_8);
        }
        return binaryData;
    }

    /**
     * Creates a new close frame
     *
     * @param finalFragment
     *            flag indicating if this frame is the final fragment
     * @param rsv
     *            reserved bits used for protocol extensions
     * @param binaryData
     *            the content of the frame. Must be 2 byte integer followed by optional UTF-8 encoded string.
     */
    public CloseWebSocketFrame(boolean finalFragment, int rsv, ByteBuf binaryData) {
        super(finalFragment, rsv, binaryData);
    }

    /**
     * 解析载荷前 2 字节为 RFC 6455 关闭状态码；载荷不足时返回 {@code -1}。
     */
    public int statusCode() {
        ByteBuf binaryData = content();
        if (binaryData == null || binaryData.readableBytes() < 2) {
            return -1;
        }

        return binaryData.getUnsignedShort(binaryData.readerIndex());
    }

    /**
     * 返回状态码之后的 UTF-8 原因文本；无原因时返回空串。
     */
    public String reasonText() {
        ByteBuf binaryData = content();
        if (binaryData == null || binaryData.readableBytes() <= 2) {
            return "";
        }

        return binaryData.toString(binaryData.readerIndex() + 2, binaryData.readableBytes() - 2, CharsetUtil.UTF_8);
    }

    @Override
    public CloseWebSocketFrame copy() {
        return (CloseWebSocketFrame) super.copy();
    }

    @Override
    public CloseWebSocketFrame duplicate() {
        return (CloseWebSocketFrame) super.duplicate();
    }

    @Override
    public CloseWebSocketFrame retainedDuplicate() {
        return (CloseWebSocketFrame) super.retainedDuplicate();
    }

    @Override
    public CloseWebSocketFrame replace(ByteBuf content) {
        return new CloseWebSocketFrame(isFinalFragment(), rsv(), content);
    }

    @Override
    public CloseWebSocketFrame retain() {
        super.retain();
        return this;
    }

    @Override
    public CloseWebSocketFrame retain(int increment) {
        super.retain(increment);
        return this;
    }

    @Override
    public CloseWebSocketFrame touch() {
        super.touch();
        return this;
    }

    @Override
    public CloseWebSocketFrame touch(Object hint) {
        super.touch(hint);
        return this;
    }

    /** 校验关闭码符合 RFC 6455，非法则抛出 {@link IllegalArgumentException} */
    static int requireValidStatusCode(int statusCode) {
        if (WebSocketCloseStatus.isValidStatusCode(statusCode)) {
            return statusCode;
        } else {
            throw new IllegalArgumentException("WebSocket close status code does NOT comply with RFC-6455: " +
                    statusCode);
        }
    }
}
