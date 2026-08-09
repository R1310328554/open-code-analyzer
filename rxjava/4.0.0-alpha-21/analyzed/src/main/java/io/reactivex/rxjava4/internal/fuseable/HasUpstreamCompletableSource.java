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

package io.reactivex.rxjava4.internal.fuseable;

import io.reactivex.rxjava4.annotations.NonNull;
import io.reactivex.rxjava4.core.CompletableSource;

/**
 * 表示实现者可通过 {@link #source()} 方法提供上游 CompletableSource 类源。
 */
public interface HasUpstreamCompletableSource {
    /**
     * 返回本 Completable 的上游源。
     * <p>用于发现 observable 链。
     * @return 源 CompletableSource
     */
    @NonNull
    CompletableSource source();
}
