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

import java.nio.ByteBuffer;

/**
 * 包装另一个 {@link ByteBuf} 的派生缓冲区抽象基类。
 * <p>
 * 引用计数、可访问性等生命周期操作均委托给 {@link #unwrap()} 返回的底层缓冲区；
 * 本类自身不持有独立引用计数。
 *
 * @deprecated 请勿使用。
 */
@Deprecated
public abstract class AbstractDerivedByteBuf extends AbstractByteBuf {

    /** @param maxCapacity 派生视图允许的最大容量 */
    protected AbstractDerivedByteBuf(int maxCapacity) {
        super(maxCapacity);
    }

    /** 可访问性委托给底层缓冲区 */
    @Override
    final boolean isAccessible() {
        return isAccessible0();
    }

    boolean isAccessible0() {
        return unwrap().isAccessible();
    }

    /** 引用计数委托给底层缓冲区 */
    @Override
    public final int refCnt() {
        return refCnt0();
    }

    int refCnt0() {
        return unwrap().refCnt();
    }

    @Override
    public final ByteBuf retain() {
        return retain0();
    }

    ByteBuf retain0() {
        unwrap().retain();
        return this;
    }

    @Override
    public final ByteBuf retain(int increment) {
        return retain0(increment);
    }

    ByteBuf retain0(int increment) {
        unwrap().retain(increment);
        return this;
    }

    @Override
    public final ByteBuf touch() {
        return touch0();
    }

    ByteBuf touch0() {
        unwrap().touch();
        return this;
    }

    @Override
    public final ByteBuf touch(Object hint) {
        return touch0(hint);
    }

    ByteBuf touch0(Object hint) {
        unwrap().touch(hint);
        return this;
    }

    @Override
    public final boolean release() {
        return release0();
    }

    boolean release0() {
        return unwrap().release();
    }

    @Override
    public final boolean release(int decrement) {
        return release0(decrement);
    }

    boolean release0(int decrement) {
        return unwrap().release(decrement);
    }

    @Override
    public boolean isReadOnly() {
        return unwrap().isReadOnly();
    }

    @Override
    public ByteBuffer internalNioBuffer(int index, int length) {
        return nioBuffer(index, length);
    }

    @Override
    public ByteBuffer nioBuffer(int index, int length) {
        return unwrap().nioBuffer(index, length);
    }

    @Override
    public boolean isContiguous() {
        return unwrap().isContiguous();
    }
}
