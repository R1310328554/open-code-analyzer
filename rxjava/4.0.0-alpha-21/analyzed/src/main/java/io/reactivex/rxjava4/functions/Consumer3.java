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

package io.reactivex.rxjava4.functions;

import io.reactivex.rxjava4.annotations.NonNull;

/**
 * 接受三个值（可能类型不同）的函数式接口（回调）。
 * @param <T1> 第一个值的类型
 * @param <T2> 第二个值的类型
 * @param <T3> 第三个值的类型
 * @since 4.0.0
 */
@FunctionalInterface
public interface Consumer3<@NonNull T1, @NonNull T2, @NonNull T3> {

    /**
     * 对给定值执行操作。
     * @param t1 第一个值
     * @param t2 第二个值
     * @param t3 第三个值
     * @throws Throwable 若实现需要可抛出任意类型的异常
     */
    void accept(T1 t1, T2 t2, T3 t3) throws Throwable;
}
