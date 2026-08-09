/*
 * Copyright 2012 The Netty Project
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
package io.netty.handler.codec.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.ChannelPipeline;

/**
 * HTTP 分块传输中的内容块，继承 {@link HttpObject} 与 {@link ByteBufHolder}。
 * <p>
 * {@link HttpObjectDecoder} 在 {@link HttpMessage} 之后、内容较大或使用 chunked 编码时
 * 逐块产生 {@link HttpContent}；若希望 handler 只收到完整消息，
 * 可在 {@link HttpObjectDecoder} 后添加 {@link HttpObjectAggregator}。
 */
public interface HttpContent extends HttpObject, ByteBufHolder {
    @Override
    HttpContent copy();

    @Override
    HttpContent duplicate();

    @Override
    HttpContent retainedDuplicate();

    @Override
    HttpContent replace(ByteBuf content);

    @Override
    HttpContent retain();

    @Override
    HttpContent retain(int increment);

    @Override
    HttpContent touch();

    @Override
    HttpContent touch(Object hint);
}
