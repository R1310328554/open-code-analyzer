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

import io.reactivex.rxjava4.disposables.*;

/**
 * 在 {@link Flowable#virtualCreate(VirtualGenerator, java.util.concurrent.ExecutorService)} 回调中交给用户代码的接口。
 * @param <T> 发射的元素类型
 * @since 4.0.0
 */
public interface VirtualEmitter<T> {

    /**
     * 发射下一个元素。
     * @param item 要发射的元素
     * @throws Throwable 若下游已取消则可抛出任意异常
     */
    void emit(T item) throws Throwable;

    /**
     * 返回 disposable 容器，用于在 await 运行期间转发取消通知。
     * @return 新的 Disposable 容器实例
     */
    DisposableStreamerCancellation canceller();
}