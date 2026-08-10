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
package io.netty.handler.codec.http3;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;

/**
 * See <a href="https://tools.ietf.org/html/draft-ietf-quic-http-32#section-7.2.1">DATA</a>.
 * <p>HTTP/3 消息体承载帧，payload 即原始字节；可同时出现在请求流与推送流上。
 * 继承 {@link ByteBufHolder} 以便与 Netty 引用计数模型一致。
 */
public interface Http3DataFrame extends ByteBufHolder, Http3RequestStreamFrame, Http3PushStreamFrame {

    @Override
    default long type() {
        return Http3CodecUtils.HTTP3_DATA_FRAME_TYPE;
    }

    @Override
    Http3DataFrame copy();

    @Override
    Http3DataFrame duplicate();

    @Override
    Http3DataFrame retainedDuplicate();

    @Override
    Http3DataFrame replace(ByteBuf content);

    @Override
    Http3DataFrame retain();

    @Override
    Http3DataFrame retain(int increment);

    @Override
    Http3DataFrame touch();

    @Override
    Http3DataFrame touch(Object hint);
}
