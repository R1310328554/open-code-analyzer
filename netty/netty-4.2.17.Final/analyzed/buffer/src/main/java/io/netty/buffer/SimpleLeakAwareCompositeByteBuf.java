/*
 * Copyright 2016 The Netty Project
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


import io.netty.util.ResourceLeakTracker;
import io.netty.util.internal.ObjectUtil;

import java.nio.ByteOrder;

/**
 * 组合 {@link ByteBuf} 的简单泄漏感知包装器。
 * <p>
 * 释放时关闭 {@link ResourceLeakTracker}；slice/duplicate 等派生视图仍保持泄漏跟踪。
 */
class SimpleLeakAwareCompositeByteBuf extends WrappedCompositeByteBuf {

    /** 组合缓冲区的泄漏跟踪器。 */
    final ResourceLeakTracker<ByteBuf> leak;

    /** 包装组合缓冲区并绑定泄漏跟踪器。 */
    SimpleLeakAwareCompositeByteBuf(CompositeByteBuf wrapped, ResourceLeakTracker<ByteBuf> leak) {
        super(wrapped);
        this.leak = ObjectUtil.checkNotNull(leak, "leak");
    }

    @Override
    public boolean release() {
        // 先 unwrap：super.release() 可能改变 unwrap() 返回的 ByteBuf 实例
        ByteBuf unwrapped = unwrap();
        if (super.release()) {
            closeLeak(unwrapped);
            return true;
        }
        return false;
    }

    @Override
    public boolean release(int decrement) {
        // Call unwrap() before just in case that super.release() will change the ByteBuf instance that is returned
        // by unwrap().
        ByteBuf unwrapped = unwrap();
        if (super.release(decrement)) {
            closeLeak(unwrapped);
            return true;
        }
        return false;
    }

    private void closeLeak(ByteBuf trackedByteBuf) {
        // 以 trackedByteBuf 关闭跟踪器，须与 DefaultResourceLeak.track(...) 时使用的对象一致
        boolean closed = leak.close(trackedByteBuf);
        assert closed;
    }

    @Override
    public ByteBuf order(ByteOrder endianness) {
        if (order() == endianness) {
            return this;
        } else {
            return newLeakAwareByteBuf(super.order(endianness));
        }
    }

    @Override
    public ByteBuf slice() {
        return newLeakAwareByteBuf(super.slice());
    }

    @Override
    public ByteBuf retainedSlice() {
        return newLeakAwareByteBuf(super.retainedSlice());
    }

    @Override
    public ByteBuf slice(int index, int length) {
        return newLeakAwareByteBuf(super.slice(index, length));
    }

    @Override
    public ByteBuf retainedSlice(int index, int length) {
        return newLeakAwareByteBuf(super.retainedSlice(index, length));
    }

    @Override
    public ByteBuf duplicate() {
        return newLeakAwareByteBuf(super.duplicate());
    }

    @Override
    public ByteBuf retainedDuplicate() {
        return newLeakAwareByteBuf(super.retainedDuplicate());
    }

    @Override
    public ByteBuf readSlice(int length) {
        return newLeakAwareByteBuf(super.readSlice(length));
    }

    @Override
    public ByteBuf readRetainedSlice(int length) {
        return newLeakAwareByteBuf(super.readRetainedSlice(length));
    }

    @Override
    public ByteBuf asReadOnly() {
        return newLeakAwareByteBuf(super.asReadOnly());
    }

    /** 为单组件视图创建泄漏感知包装。 */
    private SimpleLeakAwareByteBuf newLeakAwareByteBuf(ByteBuf wrapped) {
        return newLeakAwareByteBuf(wrapped, unwrap(), leak);
    }

    /** 供子类定制泄漏感知 ByteBuf 的工厂方法。 */
    protected SimpleLeakAwareByteBuf newLeakAwareByteBuf(
            ByteBuf wrapped, ByteBuf trackedByteBuf, ResourceLeakTracker<ByteBuf> leakTracker) {
        return new SimpleLeakAwareByteBuf(wrapped, trackedByteBuf, leakTracker);
    }
}
