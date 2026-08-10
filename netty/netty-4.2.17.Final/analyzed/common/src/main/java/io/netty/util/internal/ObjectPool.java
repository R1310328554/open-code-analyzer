/*
 * Copyright 2019 The Netty Project
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
package io.netty.util.internal;

import io.netty.util.Recycler;

/**
 * Light-weight object pool.
 *
 * @param <T> the type of the pooled object
 *
 * <p>轻量对象池抽象；已弃用，请改用 {@link Recycler}。</p>
 */
public abstract class ObjectPool<T> {

    ObjectPool() { }

    /**
     * Get a {@link Object} from the {@link ObjectPool}. The returned {@link Object} may be created via
     * {@link ObjectCreator#newObject(Handle)} if no pooled {@link Object} is ready to be reused.
     *
     * @deprecated For removal. Please use {@link Recycler#get()} instead.
     *
     * <p>从池中取对象；无可用实例时通过 {@link ObjectCreator} 新建。</p>
     */
    @Deprecated
    public abstract T get();

    /**
     * Handle for an pooled {@link Object} that will be used to notify the {@link ObjectPool} once it can
     * reuse the pooled {@link Object} again.
     * @param <T>
     *
     * <p>池化对象持有的句柄，用于归还（recycle）到池。</p>
     */
    public interface Handle<T> {
        /**
         * Recycle the {@link Object} if possible and so make it ready to be reused.
         *
         * <p>将对象归还池中以便复用。</p>
         */
        void recycle(T self);
    }

    /**
     * Creates a new Object which references the given {@link Handle} and calls {@link Handle#recycle(Object)} once
     * it can be re-used.
     *
     * @param <T> the type of the pooled object
     *
     * @deprecated For removal. Please use {@link Recycler()} instead.
     *
     * <p>工厂：创建持有 {@link Handle} 的新对象，用完后通过 handle 回收。</p>
     */
    @Deprecated
    public interface ObjectCreator<T> {

        /**
         * Creates an returns a new {@link Object} that can be used and later recycled via
         * {@link Handle#recycle(Object)}.
         *
         * @param handle can NOT be null.
         *
         * <p>创建新对象；handle 不可为 null。</p>
         */
        T newObject(Handle<T> handle);
    }

    /**
     * Creates a new {@link ObjectPool} which will use the given {@link ObjectCreator} to create the {@link Object}
     * that should be pooled.
     *
     * @deprecated For removal. Please use {@link Recycler()} instead.
     *
     * <p>基于 {@link ObjectCreator} 构造 {@link Recycler} 包装的 {@link ObjectPool}。</p>
     */
    @Deprecated
    public static <T> ObjectPool<T> newPool(final ObjectCreator<T> creator) {
        return new RecyclerObjectPool<T>(ObjectUtil.checkNotNull(creator, "creator"));
    }

    /** 内部实现：委托 {@link Recycler} 完成分配与回收。 */
    @Deprecated
    private static final class RecyclerObjectPool<T> extends ObjectPool<T> {
        private final Recycler<T> recycler;

        RecyclerObjectPool(final ObjectCreator<T> creator) {
             recycler = new Recycler<T>() {
                @Override
                protected T newObject(Handle<T> handle) {
                    return creator.newObject(handle);
                }
            };
        }

        @Override
        public T get() {
            return recycler.get();
        }
    }
}
