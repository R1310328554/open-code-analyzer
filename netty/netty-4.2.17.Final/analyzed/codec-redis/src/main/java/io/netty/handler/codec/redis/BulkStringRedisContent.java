/*
 * Copyright 2016 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License, version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package io.netty.handler.codec.redis;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.ChannelPipeline;
import io.netty.util.internal.UnstableApi;

/**
 * A chunk of bulk strings which is used for Redis chunked transfer-encoding.
 * {@link RedisDecoder} generates {@link BulkStringRedisContent} after
 * {@link BulkStringHeaderRedisMessage} when the content is large or the encoding of the content is chunked.
 * If you prefer not to receive {@link BulkStringRedisContent} in your handler,
 * place {@link RedisBulkStringAggregator} after {@link RedisDecoder} in the {@link ChannelPipeline}.
 * <p>Bulk String 正文的一个分片，继承 {@link ByteBufHolder} 以支持引用计数。
 * 大 value 或流式传输时解码器会多次 fire 本类型；末块为 {@link LastBulkStringRedisContent}。
 * 业务 handler 若只关心完整字符串，应在 {@link RedisDecoder} 后挂载 {@link RedisBulkStringAggregator}。</p>
 */
@UnstableApi
public interface BulkStringRedisContent extends RedisMessage, ByteBufHolder {

    @Override
    BulkStringRedisContent copy();

    @Override
    BulkStringRedisContent duplicate();

    @Override
    BulkStringRedisContent retainedDuplicate();

    @Override
    BulkStringRedisContent replace(ByteBuf content);

    @Override
    BulkStringRedisContent retain();

    @Override
    BulkStringRedisContent retain(int increment);

    @Override
    BulkStringRedisContent touch();

    @Override
    BulkStringRedisContent touch(Object hint);
}
