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

package io.reactivex.rxjava4.internal.operators.observable;

import io.reactivex.rxjava4.annotations.Nullable;
import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.internal.observers.BasicIntQueueDisposable;

import java.io.Serial;

/**
 * 发射 [start, start+count) 范围内的 int 序列，支持 SYNC queue fusion。
 */
public final class ObservableRange extends Observable<Integer> {
    private final int start;
    private final long end;

    /**
     * @param start 首个发射值（含）
     * @param count 元素个数
     */
    public ObservableRange(int start, int count) {
        this.start = start;
        this.end = (long)start + count;
    }

    /** 创建 RangeDisposable 并同步 run 发射整数序列。 */
    @Override
    protected void subscribeActual(Observer<? super Integer> o) {
        RangeDisposable parent = new RangeDisposable(o, start, end);
        o.onSubscribe(parent);
        parent.run();
    }

    /** 同步迭代 [index, end) 或作为 fusion poll 源。 */
    static final class RangeDisposable
    extends BasicIntQueueDisposable<Integer> {

        @Serial
        private static final long serialVersionUID = 396518478098735504L;

        final Observer<? super Integer> downstream;

        final long end;

        long index;

        boolean fused;

        RangeDisposable(Observer<? super Integer> actual, long start, long end) {
            this.downstream = actual;
            this.index = start;
            this.end = end;
        }

        /** 非 fusion 路径：循环 onNext 直至 end 或 dispose，然后 onComplete。 */
        void run() {
            if (fused) {
                return;
            }
            Observer<? super Integer> actual = this.downstream;
            long e = end;
            for (long i = index; i != e && get() == 0; i++) {
                actual.onNext((int)i);
            }
            if (get() == 0) {
                lazySet(1);
                actual.onComplete();
            }
        }

        /** fusion 路径：poll 下一 int，耗尽返回 null 并置 done。 */
        @Nullable
        @Override
        public Integer poll() {
            long i = index;
            if (i != end) {
                index = i + 1;
                return (int)i;
            }
            lazySet(1);
            return null;
        }

        @Override
        public boolean isEmpty() {
            return index == end;
        }

        @Override
        public void clear() {
            index = end;
            lazySet(1);
        }

        @Override
        public void dispose() {
            set(1);
        }

        @Override
        public boolean isDisposed() {
            return get() != 0;
        }

        /** 请求 SYNC 时启用 fused 并返回 SYNC。 */
        @Override
        public int requestFusion(int mode) {
            if ((mode & SYNC) != 0) {
                fused = true;
                return SYNC;
            }
            return NONE;
        }
    }
}
