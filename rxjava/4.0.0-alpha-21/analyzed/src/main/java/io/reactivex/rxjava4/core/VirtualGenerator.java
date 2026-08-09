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

package io.reactivex.rxjava4.core;

/**
 * 实现本接口以在
 * {@link Flowable#virtualCreate(VirtualGenerator, java.util.concurrent.ExecutorService)} 请求时生成元素。
 * <p>
 * 要从 {@link #generate(VirtualEmitter)} 正常返回以发出 {@code onComplete} 信号；
 * 要发出 {@code onError} 信号，则从 {@link #generate(VirtualEmitter)} 抛出任意异常。
 * @param <T> 生成的元素类型
 * @since 4.0.0
 */
@FunctionalInterface
public interface VirtualGenerator<T> {

    /**
     * 需实现的方法，用于开始发射元素。
     * @param emitter 使用 {@link VirtualEmitter#emit(Object)} 生成值
     * @throws Throwable 若生成器希望发出 {@code onError} 信号
     */
    void generate(VirtualEmitter<T> emitter) throws Throwable;
}