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

package io.reactivex.rxjava4.internal.operators.completable;

import io.reactivex.rxjava4.core.*;

/**
 * 直接订阅上游 {@link CompletableSource}，不做 null 校验或插件包装。
 */
public final class CompletableFromUnsafeSource extends Completable {

    final CompletableSource source;

    /** @param source 上游 CompletableSource */
    public CompletableFromUnsafeSource(CompletableSource source) {
        this.source = source;
    }

    /** 直接将 observer 传给上游 source。 */
    @Override
    protected void subscribeActual(CompletableObserver observer) {
        source.subscribe(observer);
    }
}
