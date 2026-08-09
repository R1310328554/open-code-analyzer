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
 * 根据多个输入值计算结果的函数式接口（回调）。
 * @param <T1> 第第一个值的类型
 * @param <T2> 第第二个值的类型
 * @param <T3> 第第三个值的类型
 * @param <T4> 第第四个值的类型
 * @param <T5> 第第五个值的类型
 * @param <T6> 第第六个值的类型
 * @param <R> 结果类型
 */
@FunctionalInterface
public interface Function6<@NonNull T1, @NonNull T2, @NonNull T3, @NonNull T4, @NonNull T5, @NonNull T6, @NonNull R> {
    /**
     * 根据输入值计算结果。
     * @param t1 第第一个值
     * @param t2 第第二个值
     * @param t3 第第三个值
     * @param t4 第第四个值
     * @param t5 第第五个值
     * @param t6 第第六个值
     * @return 计算结果
     * @throws Throwable 若实现需要可抛出任意类型的异常
     */
    R apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6) throws Throwable;
}
