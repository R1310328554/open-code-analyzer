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

import io.reactivex.rxjava4.annotations.Nullable;
import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.operators.QueueDisposable;

/**
 * 表示无状态的空 Disposable，始终报告为空且已 dispose。
 * <p>它也是 async-fuseable，但始终为空。
 * <p>由于 EmptyDisposable 实现 QueueDisposable 且为空，
 * 不要在测试中用它再 signal onNext；请改用 Disposables.empty()。
 */
public enum EmptyDisposable implements QueueDisposable<Object> {
    /**
     * 由于 EmptyDisposable 实现 QueueDisposable 且为空，
     * 不要在测试中用它再 signal onNext；请改用 Disposables.empty()。
     */
    INSTANCE,
    /**
     * 对 isDisposed 返回 false 的空 disposable。
     */
    NEVER
    ;

    /** 无操作。 */
    @Override
    public void dispose() {
        // no-op
    }

    /** 仅 INSTANCE 常量视为已 dispose。 */
    @Override
    public boolean isDisposed() {
        return this == INSTANCE;
    }

    /** 订阅 INSTANCE 并向 Observer 发送 onComplete。 */
    public static void complete(Observer<?> observer) {
        observer.onSubscribe(INSTANCE);
        observer.onComplete();
    }

    /** 订阅 INSTANCE 并向 MaybeObserver 发送 onComplete。 */
    public static void complete(MaybeObserver<?> observer) {
        observer.onSubscribe(INSTANCE);
        observer.onComplete();
    }

    /** 订阅 INSTANCE 并向 Observer 发送 onError。 */
    public static void error(Throwable e, Observer<?> observer) {
        observer.onSubscribe(INSTANCE);
        observer.onError(e);
    }

    /** 订阅 INSTANCE 并向 CompletableObserver 发送 onComplete。 */
    public static void complete(CompletableObserver observer) {
        observer.onSubscribe(INSTANCE);
        observer.onComplete();
    }

    /** 订阅 INSTANCE 并向 CompletableObserver 发送 onError。 */
    public static void error(Throwable e, CompletableObserver observer) {
        observer.onSubscribe(INSTANCE);
        observer.onError(e);
    }

    /** 订阅 INSTANCE 并向 SingleObserver 发送 onError。 */
    public static void error(Throwable e, SingleObserver<?> observer) {
        observer.onSubscribe(INSTANCE);
        observer.onError(e);
    }

    /** 订阅 INSTANCE 并向 MaybeObserver 发送 onError。 */
    public static void error(Throwable e, MaybeObserver<?> observer) {
        observer.onSubscribe(INSTANCE);
        observer.onError(e);
    }

    /** 不应被调用。 */
    @Override
    public boolean offer(Object value) {
        throw new UnsupportedOperationException("Should not be called!");
    }

    /** 不应被调用。 */
    @Override
    public boolean offer(Object v1, Object v2) {
        throw new UnsupportedOperationException("Should not be called!");
    }

    /** 始终为空，返回 null。 */
    @Nullable
    @Override
    public Object poll() {
        return null; // always empty
    }

    /** 始终为空。 */
    @Override
    public boolean isEmpty() {
        return true; // always empty
    }

    /** 无内容可清。 */
    @Override
    public void clear() {
        // nothing to do
    }

    /** 返回 async 融合模式。 */
    @Override
    public int requestFusion(int mode) {
        return mode & ASYNC;
    }

}
