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

package io.reactivex.rxjava4.internal.operators.single;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.functions.Predicate;
import io.reactivex.rxjava4.internal.operators.maybe.MaybeOnErrorComplete;

/**
 * 上游 onError 且 predicate 对 Throwable 返回 true 时转为 Maybe 的 onComplete。
 * predicate 为 false 时正常转发 onError；成功仍 onSuccess。
 * @param <T> 元素类型
 * @since 3.0.0
 */
public final class SingleOnErrorComplete<T> extends Maybe<T> {

    final Single<T> source;

    final Predicate<? super Throwable> predicate;

    /**
     * @param source 上游 Single
     * @param predicate 判定是否吞掉错误并 onComplete 的谓词
     */
    public SingleOnErrorComplete(Single<T> source,
            Predicate<? super Throwable> predicate) {
        this.source = source;
        this.predicate = predicate;
    }

    /** 复用 MaybeOnErrorComplete.OnErrorCompleteMultiObserver 处理错误完成逻辑。 */
    @Override
    protected void subscribeActual(MaybeObserver<? super T> observer) {
        source.subscribe(new MaybeOnErrorComplete.OnErrorCompleteMultiObserver<>(observer, predicate));
    }
}
