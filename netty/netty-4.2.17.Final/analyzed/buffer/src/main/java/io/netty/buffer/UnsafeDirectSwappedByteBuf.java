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
 * 针对带 {@code memoryAddress} 的直接 {@link ByteBuf} 的 {@link SwappedByteBuf} 实现。
 * 通过绝对地址 + {@link PlatformDependent} 完成字节序交换后的原生读写。
 */
final class UnsafeDirectSwappedByteBuf extends AbstractUnsafeSwappedByteBuf {

    /** 包装底层直接缓冲区。 */
    /** 包装底层直接缓冲区。 */
    UnsafeDirectSwappedByteBuf(AbstractByteBuf buf) {
        super(buf);
    }

    private static long addr(AbstractByteBuf wrapped, int index) {
        // 每次计算地址，不可缓存 memoryAddress：扩容后地址可能变化
        // See:
        // - https://github.com/netty/netty/issues/2587
        // - https://github.com/netty/netty/issues/2580
        return wrapped.memoryAddress() + index;
    }

    @Override
    /** 以交换后的字节序从直接内存读取 long。 */
    /** 以交换后的字节序从直接内存读取 long。 */
    protected long _getLong(AbstractByteBuf wrapped, int index) {
        return PlatformDependent.getLong(addr(wrapped, index));
    }

    @Override
    protected int _getInt(AbstractByteBuf wrapped, int index) {
        return PlatformDependent.getInt(addr(wrapped, index));
    }

    @Override
    protected short _getShort(AbstractByteBuf wrapped, int index) {
        return PlatformDependent.getShort(addr(wrapped, index));
    }

    @Override
    protected void _setShort(AbstractByteBuf wrapped, int index, short value) {
        PlatformDependent.putShort(addr(wrapped, index), value);
    }

    @Override
    protected void _setInt(AbstractByteBuf wrapped, int index, int value) {
        PlatformDependent.putInt(addr(wrapped, index), value);
    }

    @Override
    /** 以交换后的字节序向直接内存写入 long。 */
    /** 以交换后的字节序向直接内存写入 long。 */
    protected void _setLong(AbstractByteBuf wrapped, int index, long value) {
        PlatformDependent.putLong(addr(wrapped, index), value);
    }
}
