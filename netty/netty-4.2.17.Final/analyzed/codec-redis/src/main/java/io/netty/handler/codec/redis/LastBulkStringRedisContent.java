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
import io.netty.buffer.Unpooled;
import io.netty.util.internal.UnstableApi;

/**
 * A last chunk of Bulk Strings.
 * <p>标记 Bulk String 分片序列的<strong>结束</strong>。{@link RedisBulkStringAggregator}
 * 仅在收到 {@link LastBulkStringRedisContent} 时拼接并 upstream 完整消息。
 * {@link #EMPTY_LAST_CONTENT} 表示零字节末块（正文已在先前分片收齐）。</p>
 */
@UnstableApi
public interface LastBulkStringRedisContent extends BulkStringRedisContent {

    /**
     * The 'end of content' marker in chunked encoding.
     * <p>分块传输中“内容结束”占位符：{@link #content()} 为空 buffer，引用计数恒为 1。</p>
     */
    LastBulkStringRedisContent EMPTY_LAST_CONTENT = new LastBulkStringRedisContent() {

        @Override
        public ByteBuf content() {
            return Unpooled.EMPTY_BUFFER;
        }

        @Override
        public LastBulkStringRedisContent copy() {
            return this;
        }

        @Override
        public LastBulkStringRedisContent duplicate() {
            return this;
        }

        @Override
        public LastBulkStringRedisContent retainedDuplicate() {
            return this;
        }

        @Override
        public LastBulkStringRedisContent replace(ByteBuf content) {
            return new DefaultLastBulkStringRedisContent(content);
        }

        @Override
        public LastBulkStringRedisContent retain(int increment) {
            return this;
        }

        @Override
        public LastBulkStringRedisContent retain() {
            return this;
        }

        @Override
        public int refCnt() {
            return 1;
        }

        @Override
        public LastBulkStringRedisContent touch() {
            return this;
        }

        @Override
        public LastBulkStringRedisContent touch(Object hint) {
            return this;
        }

        @Override
        public boolean release() {
            return false;
        }

        @Override
        public boolean release(int decrement) {
            return false;
        }
    };

    @Override
    LastBulkStringRedisContent copy();

    @Override
    LastBulkStringRedisContent duplicate();

    @Override
    LastBulkStringRedisContent retainedDuplicate();

    @Override
    LastBulkStringRedisContent replace(ByteBuf content);

    @Override
    LastBulkStringRedisContent retain();

    @Override
    LastBulkStringRedisContent retain(int increment);

    @Override
    LastBulkStringRedisContent touch();

    @Override
    LastBulkStringRedisContent touch(Object hint);
}
