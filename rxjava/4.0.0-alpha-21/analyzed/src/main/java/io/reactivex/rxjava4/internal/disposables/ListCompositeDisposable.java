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

package io.reactivex.rxjava4.internal.disposables;

import java.util.*;

import io.reactivex.rxjava4.disposables.*;
import io.reactivex.rxjava4.exceptions.*;
import io.reactivex.rxjava4.internal.util.ExceptionHelper;

/**
 * 可持有多个其它 disposable 的 disposable 容器。
 */
public final class ListCompositeDisposable
implements Disposable, DisposableContainer, DisposableStreamerCancellation {

    List<Disposable> resources;

    volatile boolean disposed;

    /** 构造空的 ListCompositeDisposable。 */
    public ListCompositeDisposable() {
    }

    /** @param resources 初始 disposable 数组 */
    public ListCompositeDisposable(Disposable... resources) {
        Objects.requireNonNull(resources, "resources is null");
        this.resources = new LinkedList<>();
        for (Disposable d : resources) {
            Objects.requireNonNull(d, "Disposable item is null");
            this.resources.add(d);
        }
    }

    /** @param resources 初始 disposable 集合 */
    public ListCompositeDisposable(Iterable<? extends Disposable> resources) {
        Objects.requireNonNull(resources, "resources is null");
        this.resources = new LinkedList<>();
        for (Disposable d : resources) {
            Objects.requireNonNull(d, "Disposable item is null");
            this.resources.add(d);
        }
    }

    /** dispose 所有持有的 disposable 并标记容器为已 dispose。 */
    @Override
    public void dispose() {
        if (disposed) {
            return;
        }
        List<Disposable> set;
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

    /** 若容器已被 dispose 则返回 true。 */
    @Override
    public boolean isDisposed() {
        return disposed;
    }

    /** 添加 disposable；若容器已 dispose 则直接 dispose 该 disposable。 */
    @Override
    public boolean add(Disposable d) {
        Objects.requireNonNull(d, "d is null");
        if (!disposed) {
            synchronized (this) {
                if (!disposed) {
                    List<Disposable> set = resources;
                    if (set == null) {
                        set = new LinkedList<>();
                        resources = set;
                    }
                    set.add(d);
                    return true;
                }
            }
        }
        d.dispose();
        return false;
    }

    /** 批量添加 disposable；若容器已 dispose 则 dispose 所有参数。 */
    public boolean addAll(Disposable... ds) {
        Objects.requireNonNull(ds, "ds is null");
        if (!disposed) {
            synchronized (this) {
                if (!disposed) {
                    List<Disposable> set = resources;
                    if (set == null) {
                        set = new LinkedList<>();
                        resources = set;
                    }
                    for (Disposable d : ds) {
                        Objects.requireNonNull(d, "d is null");
                        set.add(d);
                    }
                    return true;
                }
            }
        }
        for (Disposable d : ds) {
            d.dispose();
        }
        return false;
    }

    /** 移除并 dispose 给定 disposable。 */
    @Override
    public boolean remove(Disposable d) {
        if (delete(d)) {
            d.dispose();
            return true;
        }
        return false;
    }

    /** 移除但不 dispose 给定 disposable。 */
    @Override
    public boolean delete(Disposable d) {
        Objects.requireNonNull(d, "Disposable item is null");
        if (disposed) {
            return false;
        }
        synchronized (this) {
            if (disposed) {
                return false;
            }

            List<Disposable> set = resources;
            if (set == null || !set.remove(d)) {
                return false;
            }
        }
        return true;
    }

    /** 移除并 dispose 所有持有的 disposable。 */
    public void clear() {
        if (disposed) {
            return;
        }
        List<Disposable> set;
        synchronized (this) {
            if (disposed) {
                return;
            }

            set = resources;
            resources = null;
        }

        dispose(set);
    }

    /** 依次 dispose 列表中的 disposable，聚合异常为 CompositeException。 */
    void dispose(List<Disposable> set) {
        if (set == null) {
            return;
        }
        List<Throwable> errors = null;
        for (Disposable o : set) {
            try {
                o.dispose();
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                if (errors == null) {
                    errors = new ArrayList<>();
                }
                errors.add(ex);
            }
        }
        if (errors != null) {
            if (errors.size() == 1) {
                throw ExceptionHelper.wrapOrThrow(errors.getFirst());
            }
            throw new CompositeException(errors);
        }
    }

    /** 清空持有的 disposable 但不 dispose 它们。 */
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

    /** 派生新的 ListCompositeDisposable 并注册自清理逻辑。 */
    @Override
    public DisposableStreamerCancellation derive() {
        var result = new ListCompositeDisposable();

        add(result);
        result.add(Disposable.fromRunnable(() -> delete(result)));

        return result;
    }

}
