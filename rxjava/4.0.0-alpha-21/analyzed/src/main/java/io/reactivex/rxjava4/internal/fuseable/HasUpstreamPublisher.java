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

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.annotations.NonNull;

/**
 * 表示实现者可通过 {@link #source()} 方法提供上游 Publisher 类源。
 *
 * @param <T> 值类型
 */
public interface HasUpstreamPublisher<@NonNull T> {
    /**
     * 返回源 Publisher。
     * <p>
     * 本方法用于发现序列的组装图。
     * @return 源 Publisher
     */
    @NonNull
    Publisher<T> source();
}
