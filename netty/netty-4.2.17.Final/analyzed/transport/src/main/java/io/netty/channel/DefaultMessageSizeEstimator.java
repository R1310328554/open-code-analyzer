/*
 * Copyright 2013 The Netty Project
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
package io.netty.channel;

import static io.netty.util.internal.ObjectUtil.checkPositiveOrZero;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;

/**
 * Default {@link MessageSizeEstimator} implementation which supports the estimation of the size of
 * {@link ByteBuf}, {@link ByteBufHolder} and {@link FileRegion}.
 * <p>默认 {@link MessageSizeEstimator}：估算出站消息大小以辅助写缓冲区水位计算。
 * {@link ByteBuf}/{@link ByteBufHolder} 用可读字节数，{@link FileRegion} 返回 0，未知类型用配置值。</p>
 */
public final class DefaultMessageSizeEstimator implements MessageSizeEstimator {

    /** 按消息类型估算大小的句柄 */
    private static final class HandleImpl implements Handle {
        /** 未知消息类型的默认估算字节数 */
        private final int unknownSize;

        private HandleImpl(int unknownSize) {
            this.unknownSize = unknownSize;
        }

        @Override
        public int size(Object msg) {
            if (msg instanceof ByteBuf) {
                return ((ByteBuf) msg).readableBytes();
            }
            if (msg instanceof ByteBufHolder) {
                return ((ByteBufHolder) msg).content().readableBytes();
            }
            if (msg instanceof FileRegion) {
                return 0;
            }
            return unknownSize;
        }
    }

    /**
     * Return the default implementation which returns {@code 8} for unknown messages.
     * <p>全局默认实例：未知消息按 8 字节估算。</p>
     */
    public static final MessageSizeEstimator DEFAULT = new DefaultMessageSizeEstimator(8);

    private final Handle handle;

    /**
     * Create a new instance
     * <p>指定未知消息类型的估算字节数。</p>
     *
     * @param unknownSize       The size which is returned for unknown messages.
     */
    public DefaultMessageSizeEstimator(int unknownSize) {
        checkPositiveOrZero(unknownSize, "unknownSize");
        handle = new HandleImpl(unknownSize);
    }

    @Override
    public Handle newHandle() {
        return handle;
    }
}
