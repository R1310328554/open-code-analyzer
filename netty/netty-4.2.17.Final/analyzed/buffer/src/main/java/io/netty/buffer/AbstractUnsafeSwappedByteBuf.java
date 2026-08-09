/*
 * Copyright 2015 The Netty Project
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

import java.nio.ByteOrder;

import static io.netty.util.internal.PlatformDependent.BIG_ENDIAN_NATIVE_ORDER;

/**
 * 基于 Unsafe 读写原语的字节序交换 {@link SwappedByteBuf} 实现。
 * <p>
 * 在底层 {@link AbstractByteBuf} 上按目标字节序读写多字节整数；
 * 若目标序与本地序不同则在读写时调用 {@code reverseBytes}。
 */
abstract class AbstractUnsafeSwappedByteBuf extends SwappedByteBuf {
    private final boolean nativeByteOrder;
    private final AbstractByteBuf wrapped;

    AbstractUnsafeSwappedByteBuf(AbstractByteBuf buf) {
        super(buf);
        assert PlatformDependent.isUnaligned(); // 要求 CPU 支持非对齐访问

        wrapped = buf;
        nativeByteOrder = BIG_ENDIAN_NATIVE_ORDER == (order() == ByteOrder.BIG_ENDIAN);
    }

    @Override
    public final long getLong(int index) {
        wrapped.checkIndex(index, 8);
        long v = _getLong(wrapped, index);
        return nativeByteOrder ? v : Long.reverseBytes(v);
    }

    @Override
    public final float getFloat(int index) {
        return Float.intBitsToFloat(getInt(index));
    }

    @Override
    public final double getDouble(int index) {
        return Double.longBitsToDouble(getLong(index));
    }

    @Override
    public final char getChar(int index) {
        return (char) getShort(index);
    }

    @Override
    public final long getUnsignedInt(int index) {
        return getInt(index) & 0xFFFFFFFFL;
    }

    @Override
    public final int getInt(int index) {
        wrapped.checkIndex(index, 4);
        int v = _getInt(wrapped, index);
        return nativeByteOrder ? v : Integer.reverseBytes(v);
    }

    @Override
    public final int getUnsignedShort(int index) {
        return getShort(index) & 0xFFFF;
    }

    @Override
    public final short getShort(int index) {
        wrapped.checkIndex(index, 2);
        short v = _getShort(wrapped, index);
        return nativeByteOrder ? v : Short.reverseBytes(v);
    }

    @Override
    public final ByteBuf setShort(int index, int value) {
        wrapped.checkIndex(index, 2);
        _setShort(wrapped, index, nativeByteOrder ? (short) value : Short.reverseBytes((short) value));
        return this;
    }

    @Override
    public final ByteBuf setInt(int index, int value) {
        wrapped.checkIndex(index, 4);
        _setInt(wrapped, index, nativeByteOrder ? value : Integer.reverseBytes(value));
        return this;
    }

    @Override
    public final ByteBuf setLong(int index, long value) {
        wrapped.checkIndex(index, 8);
        _setLong(wrapped, index, nativeByteOrder ? value : Long.reverseBytes(value));
        return this;
    }

    @Override
    public final ByteBuf setChar(int index, int value) {
        setShort(index, value);
        return this;
    }

    @Override
    public final ByteBuf setFloat(int index, float value) {
        setInt(index, Float.floatToRawIntBits(value));
        return this;
    }

    @Override
    public final ByteBuf setDouble(int index, double value) {
        setLong(index, Double.doubleToRawLongBits(value));
        return this;
    }

    @Override
    public final ByteBuf writeShort(int value) {
        wrapped.ensureWritable0(2);
        _setShort(wrapped, wrapped.writerIndex, nativeByteOrder ? (short) value : Short.reverseBytes((short) value));
        wrapped.writerIndex += 2;
        return this;
    }

    @Override
    public final ByteBuf writeInt(int value) {
        wrapped.ensureWritable0(4);
        _setInt(wrapped, wrapped.writerIndex, nativeByteOrder ? value : Integer.reverseBytes(value));
        wrapped.writerIndex += 4;
        return this;
    }

    @Override
    public final ByteBuf writeLong(long value) {
        wrapped.ensureWritable0(8);
        _setLong(wrapped, wrapped.writerIndex, nativeByteOrder ? value : Long.reverseBytes(value));
        wrapped.writerIndex += 8;
        return this;
    }

    @Override
    public final ByteBuf writeChar(int value) {
        writeShort(value);
        return this;
    }

    @Override
    public final ByteBuf writeFloat(float value) {
        writeInt(Float.floatToRawIntBits(value));
        return this;
    }

    @Override
    public final ByteBuf writeDouble(double value) {
        writeLong(Double.doubleToRawLongBits(value));
        return this;
    }

    /** 子类实现的底层 short 读取（无字节序转换） */
    protected abstract short _getShort(AbstractByteBuf wrapped, int index);
    /** 子类实现的底层 int 读取（无字节序转换） */
    protected abstract int _getInt(AbstractByteBuf wrapped, int index);
    /** 子类实现的底层 long 读取（无字节序转换） */
    protected abstract long _getLong(AbstractByteBuf wrapped, int index);
    protected abstract void _setShort(AbstractByteBuf wrapped, int index, short value);
    protected abstract void _setInt(AbstractByteBuf wrapped, int index, int value);
    protected abstract void _setLong(AbstractByteBuf wrapped, int index, long value);
}
