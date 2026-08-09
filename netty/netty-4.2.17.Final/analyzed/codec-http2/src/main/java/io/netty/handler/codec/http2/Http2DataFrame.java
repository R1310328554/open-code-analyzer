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
 * HTTP/2 DATA 帧抽象：承载应用载荷，同时是 {@link Http2StreamFrame} 与 {@link ByteBufHolder}。
 */
public interface Http2DataFrame extends Http2StreamFrame, ByteBufHolder {

    /**
     * 帧 padding 字节数，非负且小于 256。
     */
    int padding();

    /**
     * DATA 帧载荷，永不为 {@code null}（可为 empty buffer）。
     */
    @Override
    ByteBuf content();

    /**
     * 初始计入流控的字节数；即使后续消费 {@link #content()} 此值不变，用于 WINDOW_UPDATE 计算。
     */
    int initialFlowControlledBytes();

    /**
     * 是否设置 END_STREAM 标志（本帧结束该流）。
     */
    boolean isEndStream();

    @Override
    Http2DataFrame copy();

    @Override
    Http2DataFrame duplicate();

    @Override
    Http2DataFrame retainedDuplicate();

    @Override
    Http2DataFrame replace(ByteBuf content);

    @Override
    Http2DataFrame retain();

    @Override
    Http2DataFrame retain(int increment);

    @Override
    Http2DataFrame touch();

    @Override
    Http2DataFrame touch(Object hint);
}
