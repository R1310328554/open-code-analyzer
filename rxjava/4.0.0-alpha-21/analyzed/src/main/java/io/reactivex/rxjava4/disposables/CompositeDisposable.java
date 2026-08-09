/*
 * Copyright (c) 2016-present, RxJava Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package io.reactivex.rxjava4.disposables;

import java.util.*;

import io.reactivex.rxjava4.annotations.*;
import io.reactivex.rxjava4.exceptions.*;
import io.reactivex.rxjava4.internal.util.*;

/**
 * 可持有多个其他 {@link Disposable} 的 disposable 容器，
 * 对 {@link #add(Disposable)}、{@link #remove(Disposable)} 与 {@link #delete(Disposable)}
 * 操作提供<em>O(1)</em>时间复杂度。
 */
public final class CompositeDisposable
implements Disposable, DisposableContainer, DisposableStreamerCancellation {

    OpenHashSet<Disposable> resources;

    volatile boolean disposed;

    /**
     * 创建空的 {@code CompositeDisposable}。
     */
    public CompositeDisposable() {
    }

    /**
     * 使用给定初始 {@link Disposable} 数组创建 {@code CompositeDisposable}。
     * @param disposables 初始 {@code Disposable} 数组
     * @throws NullPointerException 若 {@code disposables} 或其任一数组元素为 {@code null}
     */
    public CompositeDisposable(@NonNull Disposable... disposables) {
        Objects.requireNonNull(disposables, "disposables is null");
        this.resources = new OpenHashSet<>(disposables.length + 1);
        for (Disposable d : disposables) {
            Objects.requireNonNull(d, "A Disposable in the disposables array is null");
            this.resources.add(d);
        }
    }

    /**
     * 使用给定初始 {@link Disposable} {@link Iterable} 序列创建 {@code CompositeDisposable}。
     * @param disposables 初始 {@code Disposable} 的 {@code Iterable} 序列
     * @throws NullPointerException 若 {@code disposables} 或其任一元素为 {@code null}
     */
    public CompositeDisposable(@NonNull Iterable<? extends Disposable> disposables) {
        Objects.requireNonNull(disposables, "disposables is null");
        this.resources = new OpenHashSet<>();
        for (Disposable d : disposables) {
            Objects.requireNonNull(d, "A Disposable item in the disposables sequence is null");
            this.resources.add(d);
        }
    }

    @Override
    public void dispose() {
        if (disposed) {
            return;
        }
        OpenHashSet<Disposable> set;
        synchronized (this) {
            if (disposed) {
                return;
            }
            disposed = true;
            set = resources;
            resources = null;
        }

        dispose(set);
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    /**
     * 向本容器添加 {@link Disposable}；若容器已 dispose 则 dispose 该 disposable。
     * @param disposable 要添加的 {@code Disposable}，不可为 {@code null}
     * @return 成功则为 {@code true}；若本容器已 dispose 则为 {@code false}
     * @throws NullPointerException 若 {@code disposable} 为 {@code null}
     */
    @Override
    public boolean add(@NonNull Disposable disposable) {
        Objects.requireNonNull(disposable, "disposable is null");
        if (!disposed) {
            synchronized (this) {
                if (!disposed) {
                    OpenHashSet<Disposable> set = resources;
                    if (set == null) {
                        set = new OpenHashSet<>();
                        resources = set;
                    }
                    set.add(disposable);
                    return true;
                }
            }
        }
        disposable.dispose();
        return false;
    }

    /**
     * 原子地将给定 {@link Disposable} 数组添加到容器；若容器已 dispose 则 dispose 全部。
     * @param disposables {@code Disposable} 数组
     * @return 操作成功则为 {@code true}；若容器已 dispose 则为 {@code false}
     * @throws NullPointerException 若 {@code disposables} 或其任一数组元素为 {@code null}
     */
    public boolean addAll(@NonNull Disposable... disposables) {
        Objects.requireNonNull(disposables, "disposables is null");
        if (!disposed) {
            synchronized (this) {
                if (!disposed) {
                    OpenHashSet<Disposable> set = resources;
                    if (set == null) {
                        set = new OpenHashSet<>(disposables.length + 1);
                        resources = set;
                    }
                    for (Disposable d : disposables) {
                        Objects.requireNonNull(d, "A Disposable in the disposables array is null");
                        set.add(d);
                    }
                    return true;
                }
            }
        }
        for (Disposable d : disposables) {
            d.dispose();
        }
        return false;
    }

    /**
     * 若给定 {@link Disposable} 属于本容器，则移除并 dispose 它。
     * @param disposable 要移除并 dispose 的 disposable，不可为 {@code null}
     * @return 操作成功则为 {@code true}
     * @throws NullPointerException 若 {@code disposable} 为 {@code null}
     */
    @Override
    public boolean remove(@NonNull Disposable disposable) {
        if (delete(disposable)) {
            disposable.dispose();
            return true;
        }
        return false;
    }

    /**
     * 若给定 {@link Disposable} 属于本容器，则移除（但不 dispose）它。
     * @param disposable 要移除的 disposable，不可为 {@code null}
     * @return 操作成功则为 {@code true}
     * @throws NullPointerException 若 {@code disposable} 为 {@code null}
     */
    @Override
    public boolean delete(@NonNull Disposable disposable) {
        Objects.requireNonNull(disposable, "disposable is null");
        if (disposed) {
            return false;
        }
        synchronized (this) {
            if (disposed) {
                return false;
            }

            OpenHashSet<Disposable> set = resources;
            if (set == null || !set.remove(disposable)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 原子地清空容器，然后 dispose 先前持有的全部 {@link Disposable}。
     */
    public void clear() {
        if (disposed) {
            return;
        }
        OpenHashSet<Disposable> set;
        synchronized (this) {
            if (disposed) {
                return;
            }

            set = resources;
            resources = null;
        }

        dispose(set);
    }

    /**
     * 返回当前持有的 {@link Disposable} 数量。
     * @return 当前持有的 {@code Disposable} 数量
     */
    public int size() {
        if (disposed) {
            return 0;
        }
        synchronized (this) {
            if (disposed) {
                return 0;
            }
            OpenHashSet<Disposable> set = resources;
            return set != null ? set.size() : 0;
        }
    }

    /**
     * 通过抑制非致命 {@link Throwable} 直至结束来 dispose {@link OpenHashSet} 的内容。
     * @param set 要 dispose 元素的 {@code OpenHashSet}
     */
    void dispose(@Nullable OpenHashSet<Disposable> set) {
        if (set == null) {
            return;
        }
        List<Throwable> errors = null;
        Object[] array = set.keys();
        for (Object o : array) {
            if (o instanceof Disposable) {
                try {
                    ((Disposable) o).dispose();
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    if (errors == null) {
                        errors = new ArrayList<>();
                    }
                    errors.add(ex);
                }
            }
        }
        if (errors != null) {
            if (errors.size() == 1) {
                throw ExceptionHelper.wrapOrThrow(errors.getFirst());
            }
            throw new CompositeException(errors);
        }
    }

    @Override
    public void reset() {
        if (disposed) {
            return;
        }
        synchronized (this) {
            if (disposed) {
                return;
            }
            resources = null;
        }
    }

    @Override
    public DisposableStreamerCancellation derive() {
        var result = new CompositeDisposable();

        add(result);
        result.add(new DerivedCleaner(this, result));

        return result;
    }

    record DerivedCleaner(DisposableContainer parent, Disposable child) implements Disposable {

        @Override
        public void dispose() {
            parent.delete(child);
        }

        @Override
        public boolean isDisposed() {
            return child.isDisposed();
        }
    }
}
