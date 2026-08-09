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

/**
 * 类似 {@link Runnable} 的函数式接口，但允许抛出受检异常。
 */
@FunctionalInterface
public interface Action {
    /**
     * 执行动作，并可选择抛出受检异常。
     * @throws Throwable 若实现需要可抛出任意类型的异常
     */
    void run() throws Throwable;
}
