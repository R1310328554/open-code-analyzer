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
package io.netty.handler.codec.spdy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.internal.ObjectUtil;

import static io.netty.handler.codec.spdy.SpdyCodecUtil.SPDY_DATA_FLAG_FIN;
import static io.netty.handler.codec.spdy.SpdyCodecUtil.SPDY_DATA_FRAME;
import static io.netty.handler.codec.spdy.SpdyCodecUtil.SPDY_FLAG_FIN;
import static io.netty.handler.codec.spdy.SpdyCodecUtil.SPDY_FLAG_UNIDIRECTIONAL;
import static io.netty.handler.codec.spdy.SpdyCodecUtil.SPDY_GOAWAY_FRAME;
import static io.netty.handler.codec.spdy.SpdyCodecUtil.SPDY_HEADERS_FRAME;
import static io.netty.handler.codec.spdy.SpdyCodecUtil.SPDY_HEADER_FLAGS_OFFSET;
import static io.netty.handler.codec.spdy.SpdyCodecUtil.SPDY_HEADER_LENGTH_OFFSET;
import static io.netty.handler.codec.spdy.SpdyCodecUtil.SPDY_HEADER_SIZE;
import static io.netty.handler.codec.spdy.SpdyCodecUtil.SPDY_HEADER_TYPE_OFFSET;
import static io.netty.handler.codec.spdy.SpdyCodecUtil.SPDY_PING_FRAME;
import static io.netty.handler.codec.spdy.SpdyCodecUtil.SPDY_RST_STREAM_FRAME;
import static io.netty.handler.codec.spdy.SpdyCodecUtil.SPDY_SETTINGS_CLEAR;
import static io.netty.handler.codec.spdy.SpdyCodecUtil.SPDY_SETTINGS_FRAME;
import static io.netty.handler.codec.spdy.SpdyCodecUtil.SPDY_SETTINGS_PERSISTED;
import static io.netty.handler.codec.spdy.SpdyCodecUtil.SPDY_SETTINGS_PERSIST_VALUE;
import static io.netty.handler.codec.spdy.SpdyCodecUtil.SPDY_SYN_REPLY_FRAME;
import static io.netty.handler.codec.spdy.SpdyCodecUtil.SPDY_SYN_STREAM_FRAME;
import static io.netty.handler.codec.spdy.SpdyCodecUtil.SPDY_WINDOW_UPDATE_FRAME;
import static io.netty.handler.codec.spdy.SpdyCodecUtil.getSignedInt;
import static io.netty.handler.codec.spdy.SpdyCodecUtil.getUnsignedInt;
import static io.netty.handler.codec.spdy.SpdyCodecUtil.getUnsignedMedium;
import static io.netty.handler.codec.spdy.SpdyCodecUtil.getUnsignedShort;

/**
 * 将入站 {@link ByteBuf} 按 SPDY/3 帧格式解码，通过 {@link SpdyFrameDecoderDelegate} 回调逐帧通知。
 * <p>内部采用有限状态机：先读 8 字节公共头，再按帧类型分支；DATA 帧可拆分为多个 chunk（受 {@code maxChunkSize} 限制）。
 */
public class SpdyFrameDecoder {

    protected final SpdyFrameDecoderDelegate delegate;
    protected final int spdyVersion;
    static final int DEFAULT_MAX_NUM_SETTINGS = 64;
    private final int maxChunkSize;
    private final int maxNumSettings;

    private int frameType;
    private State state;

    // SPDY common header fields
    private byte flags;
    private int length;
    private int streamId;

    /** SETTINGS 帧中待读取的条目数 */
    private int numSettings;

    /** 解码状态机各阶段 */
    private enum State {
        /** 读取 8 字节公共帧头 */
        READ_COMMON_HEADER,
        /** 读取 DATA 帧 payload（可分包） */
        READ_DATA_FRAME,
        READ_SYN_STREAM_FRAME,
        READ_SYN_REPLY_FRAME,
        READ_RST_STREAM_FRAME,
        READ_SETTINGS_FRAME,
        READ_SETTING,
        READ_PING_FRAME,
        READ_GOAWAY_FRAME,
        READ_HEADERS_FRAME,
        READ_WINDOW_UPDATE_FRAME,
        /** 扩展/未知控制帧 */
        READ_UNKNOWN_FRAME,
        /** 读取 Zlib 压缩的 Name/Value 头部块 */
        READ_HEADER_BLOCK,
        /** 丢弃不支持的帧体 */
        DISCARD_FRAME,
        /** 不可恢复协议错误，丢弃后续输入 */
        FRAME_ERROR
    }

    /**
     * 使用指定 {@code version} 及默认 {@code maxChunkSize (8192)} 创建解码器。
     */
    public SpdyFrameDecoder(SpdyVersion spdyVersion, SpdyFrameDecoderDelegate delegate) {
        this(spdyVersion, delegate, 8192);
    }

    /**
     * 使用指定参数创建解码器。
     */
    public SpdyFrameDecoder(SpdyVersion spdyVersion, SpdyFrameDecoderDelegate delegate, int maxChunkSize) {
        this(spdyVersion, delegate, maxChunkSize, DEFAULT_MAX_NUM_SETTINGS);
    }

    /**
     * 使用指定参数创建解码器，含 SETTINGS 条目数上限。
     */
    public SpdyFrameDecoder(SpdyVersion spdyVersion, SpdyFrameDecoderDelegate delegate,
                            int maxChunkSize, int maxNumSettings) {
        this.spdyVersion = ObjectUtil.checkNotNull(spdyVersion, "spdyVersion").version();
        this.delegate = ObjectUtil.checkNotNull(delegate, "delegate");
        this.maxChunkSize = ObjectUtil.checkPositive(maxChunkSize, "maxChunkSize");
        this.maxNumSettings = ObjectUtil.checkPositive(maxNumSettings, "maxNumSettings");
        state = State.READ_COMMON_HEADER;
    }

    /**
     * 从 buffer 中尽可能多地解码帧；数据不足时保留当前状态等待更多字节。
     */
    public void decode(ByteBuf buffer) {
        boolean last;
        int statusCode;

        while (true) {
            switch(state) {
                case READ_COMMON_HEADER:
                    if (buffer.readableBytes() < SPDY_HEADER_SIZE) {
                        return;
                    }

                    int frameOffset  = buffer.readerIndex();
                    int flagsOffset  = frameOffset + SPDY_HEADER_FLAGS_OFFSET;
                    int lengthOffset = frameOffset + SPDY_HEADER_LENGTH_OFFSET;
                    buffer.skipBytes(SPDY_HEADER_SIZE);

                    boolean control = (buffer.getByte(frameOffset) & 0x80) != 0;

                    int version;
                    if (control) {
                        // Decode control frame common header
                        version = getUnsignedShort(buffer, frameOffset) & 0x7FFF;
                        frameType = getUnsignedShort(buffer, frameOffset + SPDY_HEADER_TYPE_OFFSET);
                        streamId = 0; // Default to session Stream-ID
                    } else {
                        // Decode data frame common header
                        version = spdyVersion; // Default to expected version
                        frameType = SPDY_DATA_FRAME;
                        streamId = getUnsignedInt(buffer, frameOffset);
                    }

                    flags  = buffer.getByte(flagsOffset);
                    length = getUnsignedMedium(buffer, lengthOffset);

                    // Check version first then validity
                    if (version != spdyVersion) {
                        state = State.FRAME_ERROR;
                        delegate.readFrameError("Invalid SPDY Version");
                    } else if (!isValidFrameHeader(streamId, frameType, flags, length)) {
                        state = State.FRAME_ERROR;
                        delegate.readFrameError("Invalid Frame Error");
                    } else if (isValidUnknownFrameHeader(streamId, frameType, flags, length)) {
                        state = State.READ_UNKNOWN_FRAME;
                    } else {
                        state = getNextState(frameType, length);
                    }
                    break;

                case READ_DATA_FRAME:
                    if (length == 0) {
                        state = State.READ_COMMON_HEADER;
                        delegate.readDataFrame(streamId, hasFlag(flags, SPDY_DATA_FLAG_FIN), Unpooled.buffer(0));
                        break;
                    }

                    // Generate data frames that do not exceed maxChunkSize
                    int dataLength = Math.min(maxChunkSize, length);

                    // Wait until entire frame is readable
                    if (buffer.readableBytes() < dataLength) {
                        return;
                    }

                    ByteBuf data = buffer.readRetainedSlice(dataLength);
                    length -= dataLength;

                    if (length == 0) {
                        state = State.READ_COMMON_HEADER;
                    }

                    last = length == 0 && hasFlag(flags, SPDY_DATA_FLAG_FIN);

                    delegate.readDataFrame(streamId, last, data);
                    break;

                case READ_SYN_STREAM_FRAME:
                    if (buffer.readableBytes() < 10) {
                        return;
                    }

                    int offset = buffer.readerIndex();
                    streamId = getUnsignedInt(buffer, offset);
                    int associatedToStreamId = getUnsignedInt(buffer, offset + 4);
                    byte priority = (byte) (buffer.getByte(offset + 8) >> 5 & 0x07);
                    last = hasFlag(flags, SPDY_FLAG_FIN);
                    boolean unidirectional = hasFlag(flags, SPDY_FLAG_UNIDIRECTIONAL);
                    buffer.skipBytes(10);
                    length -= 10;

                    if (streamId == 0) {
                        state = State.FRAME_ERROR;
                        delegate.readFrameError("Invalid SYN_STREAM Frame");
                    } else {
                        state = State.READ_HEADER_BLOCK;
                        delegate.readSynStreamFrame(streamId, associatedToStreamId, priority, last, unidirectional);
                    }
                    break;

                case READ_SYN_REPLY_FRAME:
                    if (buffer.readableBytes() < 4) {
                        return;
                    }

                    streamId = getUnsignedInt(buffer, buffer.readerIndex());
                    last = hasFlag(flags, SPDY_FLAG_FIN);

                    buffer.skipBytes(4);
                    length -= 4;

                    if (streamId == 0) {
                        state = State.FRAME_ERROR;
                        delegate.readFrameError("Invalid SYN_REPLY Frame");
                    } else {
                        state = State.READ_HEADER_BLOCK;
                        delegate.readSynReplyFrame(streamId, last);
                    }
                    break;

                case READ_RST_STREAM_FRAME:
                    if (buffer.readableBytes() < 8) {
                        return;
                    }

                    streamId = getUnsignedInt(buffer, buffer.readerIndex());
                    statusCode = getSignedInt(buffer, buffer.readerIndex() + 4);
                    buffer.skipBytes(8);

                    if (streamId == 0 || statusCode == 0) {
                        state = State.FRAME_ERROR;
                        delegate.readFrameError("Invalid RST_STREAM Frame");
                    } else {
                        state = State.READ_COMMON_HEADER;
                        delegate.readRstStreamFrame(streamId, statusCode);
                    }
                    break;

                case READ_SETTINGS_FRAME:
                    if (buffer.readableBytes() < 4) {
                        return;
                    }

                    boolean clear = hasFlag(flags, SPDY_SETTINGS_CLEAR);

                    numSettings = getUnsignedInt(buffer, buffer.readerIndex());
                    buffer.skipBytes(4);
                    length -= 4;

                    // Validate frame length against number of entries. Each ID/Value entry is 8 bytes.
                    if ((length & 0x07) != 0 || length >> 3 != numSettings) {
                        state = State.FRAME_ERROR;
                        delegate.readFrameError("Invalid SETTINGS Frame");
                    } else if (numSettings > maxNumSettings) {
                        state = State.FRAME_ERROR;
                        delegate.readFrameError("Invalid SETTINGS Frame (allowed number of settings exceeded: "
                                + numSettings + " > " + maxNumSettings + ')');
                    } else {
                        state = State.READ_SETTING;
                        delegate.readSettingsFrame(clear);
                    }
                    break;

                case READ_SETTING:
                    if (numSettings == 0) {
                        state = State.READ_COMMON_HEADER;
                        delegate.readSettingsEnd();
                        break;
                    }

                    if (buffer.readableBytes() < 8) {
                        return;
                    }

                    byte settingsFlags = buffer.getByte(buffer.readerIndex());
                    int id = getUnsignedMedium(buffer, buffer.readerIndex() + 1);
                    int value = getSignedInt(buffer, buffer.readerIndex() + 4);
                    boolean persistValue = hasFlag(settingsFlags, SPDY_SETTINGS_PERSIST_VALUE);
                    boolean persisted = hasFlag(settingsFlags, SPDY_SETTINGS_PERSISTED);
                    buffer.skipBytes(8);

                    --numSettings;

                    delegate.readSetting(id, value, persistValue, persisted);
                    break;

                case READ_PING_FRAME:
                    if (buffer.readableBytes() < 4) {
                        return;
                    }

                    int pingId = getSignedInt(buffer, buffer.readerIndex());
                    buffer.skipBytes(4);

                    state = State.READ_COMMON_HEADER;
                    delegate.readPingFrame(pingId);
                    break;

                case READ_GOAWAY_FRAME:
                    if (buffer.readableBytes() < 8) {
                        return;
                    }

                    int lastGoodStreamId = getUnsignedInt(buffer, buffer.readerIndex());
                    statusCode = getSignedInt(buffer, buffer.readerIndex() + 4);
                    buffer.skipBytes(8);

                    state = State.READ_COMMON_HEADER;
                    delegate.readGoAwayFrame(lastGoodStreamId, statusCode);
                    break;

                case READ_HEADERS_FRAME:
                    if (buffer.readableBytes() < 4) {
                        return;
                    }

                    streamId = getUnsignedInt(buffer, buffer.readerIndex());
                    last = hasFlag(flags, SPDY_FLAG_FIN);

                    buffer.skipBytes(4);
                    length -= 4;

                    if (streamId == 0) {
                        state = State.FRAME_ERROR;
                        delegate.readFrameError("Invalid HEADERS Frame");
                    } else {
                        state = State.READ_HEADER_BLOCK;
                        delegate.readHeadersFrame(streamId, last);
                    }
                    break;

                case READ_WINDOW_UPDATE_FRAME:
                    if (buffer.readableBytes() < 8) {
                        return;
                    }

                    streamId = getUnsignedInt(buffer, buffer.readerIndex());
                    int deltaWindowSize = getUnsignedInt(buffer, buffer.readerIndex() + 4);
                    buffer.skipBytes(8);

                    if (deltaWindowSize == 0) {
                        state = State.FRAME_ERROR;
                        delegate.readFrameError("Invalid WINDOW_UPDATE Frame");
                    } else {
                        state = State.READ_COMMON_HEADER;
                        delegate.readWindowUpdateFrame(streamId, deltaWindowSize);
                    }
                    break;

                case READ_UNKNOWN_FRAME:
                    if (decodeUnknownFrame(frameType, flags, length, buffer)) {
                        state = State.READ_COMMON_HEADER;
                        break;
                    }
                    return;

                case READ_HEADER_BLOCK:
                    if (length == 0) {
                        state = State.READ_COMMON_HEADER;
                        delegate.readHeaderBlockEnd();
                        break;
                    }

                    if (!buffer.isReadable()) {
                        return;
                    }

                    int compressedBytes = Math.min(buffer.readableBytes(), length);
                    ByteBuf headerBlock = buffer.readRetainedSlice(compressedBytes);
                    length -= compressedBytes;

                    delegate.readHeaderBlock(headerBlock);
                    break;

                case DISCARD_FRAME:
                    int numBytes = Math.min(buffer.readableBytes(), length);
                    buffer.skipBytes(numBytes);
                    length -= numBytes;
                    if (length == 0) {
                        state = State.READ_COMMON_HEADER;
                        break;
                    }
                    return;

                case FRAME_ERROR:
                    buffer.skipBytes(buffer.readableBytes());
                    return;

                default:
                    throw new Error("Unexpected state: " + state);
            }
        }
    }

    private static boolean hasFlag(byte flags, byte flag) {
        return (flags & flag) != 0;
    }

    private static State getNextState(int type, int length) {
        switch (type) {
            case SPDY_DATA_FRAME:
                return State.READ_DATA_FRAME;

            case SPDY_SYN_STREAM_FRAME:
                return State.READ_SYN_STREAM_FRAME;

            case SPDY_SYN_REPLY_FRAME:
                return State.READ_SYN_REPLY_FRAME;

            case SPDY_RST_STREAM_FRAME:
                return State.READ_RST_STREAM_FRAME;

            case SPDY_SETTINGS_FRAME:
                return State.READ_SETTINGS_FRAME;

            case SPDY_PING_FRAME:
                return State.READ_PING_FRAME;

            case SPDY_GOAWAY_FRAME:
                return State.READ_GOAWAY_FRAME;

            case SPDY_HEADERS_FRAME:
                return State.READ_HEADERS_FRAME;

            case SPDY_WINDOW_UPDATE_FRAME:
                return State.READ_WINDOW_UPDATE_FRAME;

            default:

                if (length != 0) {
                    return State.DISCARD_FRAME;
                } else {
                    return State.READ_COMMON_HEADER;
                }
        }
    }

    /**
     * 解码未知帧；成功解析返回 {@code true}，数据不足返回 {@code false}。
     */
    protected boolean decodeUnknownFrame(int frameType, byte flags, int length, ByteBuf buffer) {
        if (length == 0) {
            delegate.readUnknownFrame(frameType, flags, Unpooled.EMPTY_BUFFER);
            return true;
        }
        if (buffer.readableBytes() < length) {
            return false;
        }
        ByteBuf data = buffer.readRetainedSlice(length);
        delegate.readUnknownFrame(frameType, flags, data);
        return true;
    }

    /**
     * 判断未知帧头是否合法；合法则走 {@link #decodeUnknownFrame(int, byte, int, ByteBuf)}，否则丢弃。
     * */
    protected boolean isValidUnknownFrameHeader(int streamId, int type, byte flags, int length) {
        return false;
    }

    private static boolean isValidFrameHeader(int streamId, int type, byte flags, int length) {
        switch (type) {
            case SPDY_DATA_FRAME:
                return streamId != 0;

            case SPDY_SYN_STREAM_FRAME:
                return length >= 10;

            case SPDY_SYN_REPLY_FRAME:
                return length >= 4;

            case SPDY_RST_STREAM_FRAME:
                return flags == 0 && length == 8;

            case SPDY_SETTINGS_FRAME:
                return length >= 4;

            case SPDY_PING_FRAME:
                return length == 4;

            case SPDY_GOAWAY_FRAME:
                return length == 8;

            case SPDY_HEADERS_FRAME:
                return length >= 4;

            case SPDY_WINDOW_UPDATE_FRAME:
                return length == 8;

            default:
                return true;
        }
    }
}
