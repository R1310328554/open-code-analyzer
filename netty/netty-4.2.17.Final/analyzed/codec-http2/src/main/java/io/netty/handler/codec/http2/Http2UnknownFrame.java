/*
 * Copyright 2017 The Netty Project
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
 * 表示 HTTP/2 规范未定义或当前编解码器未显式支持的帧类型。
 * <p>载荷通过 {@link ByteBufHolder} 持有，便于扩展帧、实验性帧或前向兼容场景下的透传处理。
 */
public interface Http2UnknownFrame extends Http2StreamFrame, ByteBufHolder {

    @Override
    Http2FrameStream stream();

    @Override
    Http2UnknownFrame stream(Http2FrameStream stream);

    /** 原始帧类型字节（9 字节帧头中的 type 字段）。 */
    byte frameType();

    /** 该帧携带的标志位集合。 */
    Http2Flags flags();

    @Override
    Http2UnknownFrame copy();

    @Override
    Http2UnknownFrame duplicate();

    @Override
    Http2UnknownFrame retainedDuplicate();

    @Override
    Http2UnknownFrame replace(ByteBuf content);

    @Override
    Http2UnknownFrame retain();

    @Override
    Http2UnknownFrame retain(int increment);

    @Override
    Http2UnknownFrame touch();

    @Override
    Http2UnknownFrame touch(Object hint);
}
