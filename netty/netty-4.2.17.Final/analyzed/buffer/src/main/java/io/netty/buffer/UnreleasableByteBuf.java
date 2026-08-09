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
package io.netty.buffer;

import io.netty.util.internal.ObjectUtil;

import java.nio.ByteOrder;

/**
 * 包装 {@link ByteBuf} 并忽略 {@link #retain()} / {@link #release()}，防止调用方改变底层引用计数。
 */
final class UnreleasableByteBuf extends WrappedByteBuf {

    /** 缓存的字节序交换视图。 */
    private SwappedByteBuf swappedBuf;

    UnreleasableByteBuf(ByteBuf buf) {
        super(buf instanceof UnreleasableByteBuf ? buf.unwrap() : buf);
    }

    @Override
    public ByteBuf order(ByteOrder endianness) {
        if (ObjectUtil.checkNotNull(endianness, "endianness") == order()) {
            return this;
        }

        SwappedByteBuf swappedBuf = this.swappedBuf;
        if (swappedBuf == null) {
            this.swappedBuf = swappedBuf = new SwappedByteBuf(this);
        }
        return swappedBuf;
    }

    @Override
    public ByteBuf asReadOnly() {
        return buf.isReadOnly() ? this : new UnreleasableByteBuf(buf.asReadOnly());
    }

    @Override
    public ByteBuf readSlice(int length) {
        return new UnreleasableByteBuf(buf.readSlice(length));
    }

    @Override
    public ByteBuf readRetainedSlice(int length) {
        // 若 readSlice 后再 release 底层，单元测试中泄漏记录无法清理；逻辑上等价故仅用 readSlice
        return readSlice(length);
    }

    @Override
    public ByteBuf slice() {
        return new UnreleasableByteBuf(buf.slice());
    }

    @Override
    public ByteBuf retainedSlice() {
        // 同上：retainedSlice+release 会导致泄漏检测无法清理，改用 slice()
        return slice();
    }

    @Override
    public ByteBuf slice(int index, int length) {
        return new UnreleasableByteBuf(buf.slice(index, length));
    }

    @Override
    public ByteBuf retainedSlice(int index, int length) {
        // 同上：带索引的 retainedSlice 同理
        return slice(index, length);
    }

    @Override
    public ByteBuf duplicate() {
        return new UnreleasableByteBuf(buf.duplicate());
    }

    @Override
    public ByteBuf retainedDuplicate() {
        // 同上：retainedDuplicate 改用 duplicate()
        return duplicate();
    }

    @Override
    public ByteBuf retain(int increment) {
        return this;
    }

    @Override
    public ByteBuf retain() {
        return this;
    }

    @Override
    public ByteBuf touch() {
        return this;
    }

    @Override
    public ByteBuf touch(Object hint) {
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
}
