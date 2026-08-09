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

package io.reactivex.rxjava4.operators;

import io.reactivex.rxjava4.annotations.NonNull;
import io.reactivex.rxjava4.core.FlowableSubscriber;

/**
 * 在 {@link FlowableSubscriber} 基础上增加 {@link #tryOnNext(Object)}，
 * 告知调用方指定值是否已被接受。
 *
 * <p>某些 queue-drain 或 source-drain 算子可据此避免为丢弃的值额外 request(1)。
 *
 * @param <T> 值类型
 * @since 3.1.1
 */
public interface ConditionalSubscriber<@NonNull T> extends FlowableSubscriber<T> {
    /**
     * 有条件地接收值。
     * @param t 要传递的值
     * @return 若值已被接受则为 true；若被拒绝且可立即发送下一值则为 false
     */
    boolean tryOnNext(@NonNull T t);
}
