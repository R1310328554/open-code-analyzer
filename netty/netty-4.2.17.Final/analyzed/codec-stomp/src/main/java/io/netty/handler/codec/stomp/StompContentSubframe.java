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
package io.netty.handler.codec.stomp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.ChannelPipeline;

/**
 * An STOMP chunk which is used for STOMP chunked transfer-encoding. {@link StompSubframeDecoder} generates
 * {@link StompContentSubframe} after {@link StompHeadersSubframe} when the content is large or the encoding of
 * the content is 'chunked.  If you prefer not to receive multiple {@link StompSubframe}s for a single
 * {@link StompFrame}, place {@link StompSubframeAggregator} after {@link StompSubframeDecoder} in the
 * {@link ChannelPipeline}.
 * <p>STOMP 帧正文的单个分块。当正文较大或按分块方式传输时，{@link StompSubframeDecoder} 在
 * {@link StompHeadersSubframe} 之后依次输出多个本类型子帧；最后一帧为 {@link LastStompContentSubframe}。
 * 若希望上游只处理完整的 {@link StompFrame}，可在管道中于解码器之后插入 {@link StompSubframeAggregator}。</p>
 */
public interface StompContentSubframe extends ByteBufHolder, StompSubframe {
    @Override
    StompContentSubframe copy();

    @Override
    StompContentSubframe duplicate();

    @Override
    StompContentSubframe retainedDuplicate();

    @Override
    StompContentSubframe replace(ByteBuf content);

    @Override
    StompContentSubframe retain();

    @Override
    StompContentSubframe retain(int increment);

    @Override
    StompContentSubframe touch();

    @Override
    StompContentSubframe touch(Object hint);
}
