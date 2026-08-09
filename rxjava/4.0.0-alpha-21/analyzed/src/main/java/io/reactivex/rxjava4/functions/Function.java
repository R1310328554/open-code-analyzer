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
 * 接收一个值并返回另一个值（可能类型不同）的函数式接口，允许抛出受检异常。
 *
 * @param <T> 输入值类型
 * @param <R> 输出值类型
 */
@FunctionalInterface
public interface Function<@NonNull T, @NonNull R> {
    /**
     * 对输入值进行计算并返回结果值。
     * @param t 输入值
     * @return 输出值
     * @throws Throwable 若实现需要可抛出任意类型的异常
     */
    R apply(T t) throws Throwable;
}
