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
package io.netty.handler.codec.quic;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.Unpooled;

/**
 * QUIC STREAM 帧的 {@link ByteBufHolder} 表示，携带 payload 与可选 {@code FIN} 标志。
 */
public interface QuicStreamFrame extends ByteBufHolder {

    /**
     * 空 payload 且置 {@code FIN} 的常量帧，用于仅发送流结束信号。
     */
    QuicStreamFrame EMPTY_FIN = new QuicStreamFrame() {
        @Override
        public boolean hasFin() {
            return true;
        }

        @Override
        public QuicStreamFrame copy() {
            return this;
        }

        @Override
        public QuicStreamFrame duplicate() {
            return this;
        }

        @Override
        public QuicStreamFrame retainedDuplicate() {
            return this;
        }

        @Override
        public QuicStreamFrame replace(ByteBuf content) {
            return new DefaultQuicStreamFrame(content, hasFin());
        }

        @Override
        public QuicStreamFrame retain() {
            return this;
        }

        @Override
        public QuicStreamFrame retain(int increment) {
            return this;
        }

        @Override
        public QuicStreamFrame touch() {
            return this;
        }

        @Override
        public QuicStreamFrame touch(Object hint) {
            return this;
        }

        @Override
        public ByteBuf content() {
            return Unpooled.EMPTY_BUFFER;
        }

        @Override
        public int refCnt() {
            return 1;
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

    /**
     * 是否设置 {@code FIN}：为 {@code true} 时通知对端本流不再有数据写入。
     *
     * @return {@code true} if the FIN flag should be set, {@code false} otherwise.
     */
    boolean hasFin();

    @Override
    QuicStreamFrame copy();

    @Override
    QuicStreamFrame duplicate();

    @Override
    QuicStreamFrame retainedDuplicate();

    @Override
    QuicStreamFrame replace(ByteBuf content);

    @Override
    QuicStreamFrame retain();

    @Override
    QuicStreamFrame retain(int increment);

    @Override
    QuicStreamFrame touch();

    @Override
    QuicStreamFrame touch(Object hint);
}
