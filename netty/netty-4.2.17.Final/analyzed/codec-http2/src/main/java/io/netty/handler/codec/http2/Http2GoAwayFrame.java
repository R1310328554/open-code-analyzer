/*
 * Copyright 2016 The Netty Project
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
package io.netty.handler.codec.http2;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;

/**
 * HTTP/2 GOAWAY 帧抽象，表示连接级优雅关闭。
 *
 * <p>The last stream identifier <em>must not</em> be set by the application, but instead the
 * relative {@link #extraStreamIds()} should be used. The {@link #lastStreamId()} will only be
 * set for incoming GOAWAY frames by the HTTP/2 codec.
 *
 * <p>Graceful shutdown as described in the HTTP/2 spec can be accomplished by calling
 * {@code #setExtraStreamIds(Integer.MAX_VALUE)}.
 */
public interface Http2GoAwayFrame extends Http2Frame, ByteBufHolder {
    /**
     * 连接关闭原因，以 HTTP/2 错误码表示。
     */
    long errorCode();

    /**
     * GOAWAY 传输期间为对端预留的新流 ID 数量，实现双向优雅关闭。
     */
    int extraStreamIds();

    /**
     * 设置 GOAWAY 传输期间预留的新流 ID 数量。
     *
     * @see #extraStreamIds
     * @return {@code this}
     */
    Http2GoAwayFrame setExtraStreamIds(int extraStreamIds);

    /**
     * 返回最后处理的流 ID；未设置时返回 {@code -1}（出站帧由 codec 根据 extraStreamIds 计算）。
     */
    int lastStreamId();

    /**
     * 可选调试信息，永不为 {@code null}，但可为 empty。
     */
    @Override
    ByteBuf content();

    @Override
    Http2GoAwayFrame copy();

    @Override
    Http2GoAwayFrame duplicate();

    @Override
    Http2GoAwayFrame retainedDuplicate();

    @Override
    Http2GoAwayFrame replace(ByteBuf content);

    @Override
    Http2GoAwayFrame retain();

    @Override
    Http2GoAwayFrame retain(int increment);

    @Override
    Http2GoAwayFrame touch();

    @Override
    Http2GoAwayFrame touch(Object hint);
}
