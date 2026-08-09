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
 * 接受基本类型值并返回类型 T 的值的函数式接口（回调）。
 * @param <T> 返回值类型
 */
@FunctionalInterface
public interface IntFunction<@NonNull T> {
    /**
     * 根据基本类型 int 输入计算结果。
     * @param i 输入值
     * @return 结果对象
     * @throws Throwable 若实现需要可抛出任意类型的异常
     */
    T apply(int i) throws Throwable;
}
