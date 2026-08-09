/*
 * Copyright 2024 The Netty Project
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
import io.netty.buffer.ByteBufHolder;

/**
 * 无法识别或未注册的 SPDY 控制帧，保留原始 type/flags 与 payload。
 * <p>解码器遇到未知帧类型时以此接口向上游透传，便于扩展或日志诊断；
 * 同时继承 {@link ByteBufHolder} 以参与 Netty 引用计数生命周期。
 */
public interface SpdyUnknownFrame extends SpdyFrame, ByteBufHolder {

    /** 帧类型字节（SPDY 控制帧 header 中的 type 字段）。 */
    int frameType();

    /** 帧标志位（如 FIN、UNIDIRECTIONAL 等，依帧类型而定）。 */
    byte flags();

    @Override
    SpdyUnknownFrame copy();

    @Override
    SpdyUnknownFrame duplicate();

    @Override
    SpdyUnknownFrame retainedDuplicate();

    @Override
    SpdyUnknownFrame replace(ByteBuf content);

    @Override
    SpdyUnknownFrame retain();

    @Override
    SpdyUnknownFrame retain(int increment);

    @Override
    SpdyUnknownFrame touch();

    @Override
    SpdyUnknownFrame touch(Object hint);
}
