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

import io.netty.util.IllegalReferenceCountException;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakTracker;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.ThrowableUtil;

import java.nio.ByteOrder;

/**
 * 带 {@link ResourceLeakDetector} 跟踪的简单泄漏感知 {@link ByteBuf} 包装器。
 * slice/duplicate 等派生视图共享或强制跟踪泄漏。
 */
class SimpleLeakAwareByteBuf extends WrappedByteBuf {

    /**
     * 与 {@link ResourceLeakTracker} 关联的对象：{@link ResourceLeakTracker#close(Object)} 时作为参数；
     * 创建 {@link #leak} 时 {@link ResourceLeakDetector#track(Object)} 也使用同一对象。
     */
    private final ByteBuf trackedByteBuf;
    /** 缓冲区泄漏跟踪器。 */
    /** 缓冲区泄漏跟踪器。 */
    final ResourceLeakTracker<ByteBuf> leak;

    SimpleLeakAwareByteBuf(ByteBuf wrapped, ByteBuf trackedByteBuf, ResourceLeakTracker<ByteBuf> leak) {
        super(wrapped);
        this.trackedByteBuf = ObjectUtil.checkNotNull(trackedByteBuf, "trackedByteBuf");
        this.leak = ObjectUtil.checkNotNull(leak, "leak");
    }

    SimpleLeakAwareByteBuf(ByteBuf wrapped, ResourceLeakTracker<ByteBuf> leak) {
        this(wrapped, wrapped, leak);
    }

    @Override
    public ByteBuf slice() {
        return newSharedLeakAwareByteBuf(super.slice());
    }

    @Override
    public ByteBuf retainedSlice() {
        try {
            return unwrappedDerived(super.retainedSlice());
        } catch (IllegalReferenceCountException irce) {
            ThrowableUtil.addSuppressed(irce, leak.getCloseStackTraceIfAny());
            throw irce;
        }
    }

    @Override
    public ByteBuf retainedSlice(int index, int length) {
        try {
            return unwrappedDerived(super.retainedSlice(index, length));
        } catch (IllegalReferenceCountException irce) {
            ThrowableUtil.addSuppressed(irce, leak.getCloseStackTraceIfAny());
            throw irce;
        }
    }

    @Override
    public ByteBuf retainedDuplicate() {
        try {
            return unwrappedDerived(super.retainedDuplicate());
        } catch (IllegalReferenceCountException irce) {
            ThrowableUtil.addSuppressed(irce, leak.getCloseStackTraceIfAny());
            throw irce;
        }
    }

    @Override
    public ByteBuf readRetainedSlice(int length) {
        try {
            return unwrappedDerived(super.readRetainedSlice(length));
        } catch (IllegalReferenceCountException irce) {
            ThrowableUtil.addSuppressed(irce, leak.getCloseStackTraceIfAny());
            throw irce;
        }
    }

    @Override
    public ByteBuf slice(int index, int length) {
        return newSharedLeakAwareByteBuf(super.slice(index, length));
    }

    @Override
    public ByteBuf duplicate() {
        return newSharedLeakAwareByteBuf(super.duplicate());
    }

    @Override
    public ByteBuf readSlice(int length) {
        return newSharedLeakAwareByteBuf(super.readSlice(length));
    }

    @Override
    public ByteBuf asReadOnly() {
        return newSharedLeakAwareByteBuf(super.asReadOnly());
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
    public ByteBuf retain() {
        try {
            return super.retain();
        } catch (IllegalReferenceCountException irce) {
            ThrowableUtil.addSuppressed(irce, leak.getCloseStackTraceIfAny());
            throw irce;
        }
    }

    @Override
    public ByteBuf retain(int increment) {
        try {
            return super.retain(increment);
        } catch (IllegalReferenceCountException irce) {
            ThrowableUtil.addSuppressed(irce, leak.getCloseStackTraceIfAny());
            throw irce;
        }
    }

    @Override
    public boolean release() {
        try {
            if (super.release()) {
                closeLeak();
                return true;
            }
            return false;
        } catch (IllegalReferenceCountException irce) {
            ThrowableUtil.addSuppressed(irce, leak.getCloseStackTraceIfAny());
            throw irce;
        }
    }

    @Override
    public boolean release(int decrement) {
        try {
            if (super.release(decrement)) {
                closeLeak();
                return true;
            }
            return false;
        } catch (IllegalReferenceCountException irce) {
            ThrowableUtil.addSuppressed(irce, leak.getCloseStackTraceIfAny());
            throw irce;
        }
    }

    private void closeLeak() {
        // 以 trackedByteBuf 关闭 ResourceLeakTracker，须与 track 时传入对象一致
        boolean closed = leak.close(trackedByteBuf);
        assert closed;
    }

    @Override
    public ByteBuf order(ByteOrder endianness) {
        if (order() == endianness) {
            return this;
        } else {
            return newSharedLeakAwareByteBuf(super.order(endianness));
        }
    }

    private ByteBuf unwrappedDerived(ByteBuf derived) {
        // 仅需解包 SwappedByteBuf；除 slice/duplicate 外，泄漏感知层主要遇到此类包装
        ByteBuf unwrappedDerived = unwrapSwapped(derived);

        if (unwrappedDerived instanceof AbstractPooledDerivedByteBuf) {
            // 更新 parent 指向本包装，以便正确关闭 ResourceLeakTracker
            ((AbstractPooledDerivedByteBuf) unwrappedDerived).parent(this);

            // 强制跟踪派生缓冲区（见 issue #13414）
            return newLeakAwareByteBuf(derived, AbstractByteBuf.leakDetector.trackForcibly(derived));
        }
        return newSharedLeakAwareByteBuf(derived);
    }

    @SuppressWarnings("deprecation")
    private static ByteBuf unwrapSwapped(ByteBuf buf) {
        if (buf instanceof SwappedByteBuf) {
            do {
                buf = buf.unwrap();
            } while (buf instanceof SwappedByteBuf);

            return buf;
        }
        return buf;
    }

    private SimpleLeakAwareByteBuf newSharedLeakAwareByteBuf(
            ByteBuf wrapped) {
        return newLeakAwareByteBuf(wrapped, trackedByteBuf, leak);
    }

    private SimpleLeakAwareByteBuf newLeakAwareByteBuf(
            ByteBuf wrapped, ResourceLeakTracker<ByteBuf> leakTracker) {
        return newLeakAwareByteBuf(wrapped, wrapped, leakTracker);
    }

    protected SimpleLeakAwareByteBuf newLeakAwareByteBuf(
            ByteBuf buf, ByteBuf trackedByteBuf, ResourceLeakTracker<ByteBuf> leakTracker) {
        return new SimpleLeakAwareByteBuf(buf, trackedByteBuf, leakTracker);
    }
}
