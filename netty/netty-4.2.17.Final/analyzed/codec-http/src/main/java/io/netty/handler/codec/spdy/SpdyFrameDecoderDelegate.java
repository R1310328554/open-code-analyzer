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

/**
 * {@link SpdyFrameDecoder} 的回调接口：每解析完一帧（或一帧的一部分）即通知实现方。
 * <p>带头部块的帧（SYN_STREAM/SYN_REPLY/HEADERS）分多步回调：先 {@code readXxxFrame}，
 * 再多次 {@link #readHeaderBlock(ByteBuf)}，最后 {@link #readHeaderBlockEnd()}。
 */
public interface SpdyFrameDecoderDelegate {

    /**
     * 收到 DATA 帧（可能为分包 chunk；{@code last} 表示该流最后一段数据）。
     */
    void readDataFrame(int streamId, boolean last, ByteBuf data);

    /**
     * 收到 SYN_STREAM 帧头部（不含 Name/Value 块，见 {@link #readHeaderBlock(ByteBuf)}）。
     */
    void readSynStreamFrame(
            int streamId, int associatedToStreamId, byte priority, boolean last, boolean unidirectional);

    /**
     * 收到 SYN_REPLY 帧头部（不含 Name/Value 块，见 {@link #readHeaderBlock(ByteBuf)}）。
     */
    void readSynReplyFrame(int streamId, boolean last);

    /**
     * 收到 RST_STREAM 帧。
     */
    void readRstStreamFrame(int streamId, int statusCode);

    /**
     * 收到 SETTINGS 帧起始（不含具体条目，见 {@link #readSetting(int, int, boolean, boolean)}）。
     */
    void readSettingsFrame(boolean clearPersisted);

    /**
     * 收到 SETTINGS 帧中的单条 ID/Value 条目。
     */
    void readSetting(int id, int value, boolean persistValue, boolean persisted);

    /**
     * SETTINGS 帧全部条目读取完毕。
     */
    void readSettingsEnd();

    /**
     * 收到 PING 帧。
     */
    void readPingFrame(int id);

    /**
     * 收到 GOAWAY 帧。
     */
    void readGoAwayFrame(int lastGoodStreamId, int statusCode);

    /**
     * 收到 HEADERS 帧头部（不含 Name/Value 块，见 {@link #readHeaderBlock(ByteBuf)}）。
     */
    void readHeadersFrame(int streamId, boolean last);

    /**
     * 收到 WINDOW_UPDATE 帧。
     */
    void readWindowUpdateFrame(int streamId, int deltaWindowSize);

    /**
     * 收到压缩头部块的一段数据（可能分多次回调）。
     */
    void readHeaderBlock(ByteBuf headerBlock);

    /**
     * 整个头部块接收完毕。
     */
    void readHeaderBlockEnd();

    /**
     * 发生不可恢复的会话级协议错误。
     */
    void readFrameError(String message);

    /**
     * 收到未知类型帧；默认实现直接释放 payload。
     *
     * @param frameType the frame type from the spdy header.
     * @param flags the flags in the frame header.
     * @param payload the payload of the frame.
     */
    default void readUnknownFrame(int frameType, byte flags, ByteBuf payload) {
        payload.release();
    }
}
