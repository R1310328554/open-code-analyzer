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

package io.netty.buffer;

import io.netty.util.internal.PlatformDependent;

/**
 * 针对堆上 {@code byte[]} 且通过 Unsafe 访问的 {@link ByteBuf} 的 {@link SwappedByteBuf} 实现。
 */
final class UnsafeHeapSwappedByteBuf extends AbstractUnsafeSwappedByteBuf {

    /** 包装底层堆缓冲区。 */
    /** 包装底层堆缓冲区。 */
    UnsafeHeapSwappedByteBuf(AbstractByteBuf buf) {
        super(buf);
    }

    /** 将逻辑索引转为数组绝对下标（含 {@link ByteBuf#arrayOffset()}）。 */
    /** 将逻辑索引转为数组绝对下标（含 {@link ByteBuf#arrayOffset()}）。 */
    private static int idx(ByteBuf wrapped, int index) {
        return wrapped.arrayOffset() + index;
    }

    @Override
    /** 以交换后的字节序从堆数组读取 long。 */
    /** 以交换后的字节序从堆数组读取 long。 */
    protected long _getLong(AbstractByteBuf wrapped, int index) {
        return PlatformDependent.getLong(wrapped.array(), idx(wrapped, index));
    }

    @Override
    protected int _getInt(AbstractByteBuf wrapped, int index) {
        return PlatformDependent.getInt(wrapped.array(), idx(wrapped, index));
    }

    @Override
    protected short _getShort(AbstractByteBuf wrapped, int index) {
        return PlatformDependent.getShort(wrapped.array(), idx(wrapped, index));
    }

    @Override
    protected void _setShort(AbstractByteBuf wrapped, int index, short value) {
        PlatformDependent.putShort(wrapped.array(), idx(wrapped, index), value);
    }

    @Override
    protected void _setInt(AbstractByteBuf wrapped, int index, int value) {
        PlatformDependent.putInt(wrapped.array(), idx(wrapped, index), value);
    }

    @Override
    /** 以交换后的字节序向堆数组写入 long。 */
    /** 以交换后的字节序向堆数组写入 long。 */
    protected void _setLong(AbstractByteBuf wrapped, int index, long value) {
        PlatformDependent.putLong(wrapped.array(), idx(wrapped, index), value);
    }
}
