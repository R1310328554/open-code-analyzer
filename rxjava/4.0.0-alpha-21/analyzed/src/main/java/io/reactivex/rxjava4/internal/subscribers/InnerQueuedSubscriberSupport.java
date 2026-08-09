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

package io.reactivex.rxjava4.internal.subscribers;

/**
 * 允许 InnerQueuedSubscriber 向父级回传信号的接口。
 *
 * @param <T> 值类型
 */
public interface InnerQueuedSubscriberSupport<T> {

    /** 内部 subscriber 收到下一项时回调。 */
    void innerNext(InnerQueuedSubscriber<T> inner, T value);

    /** 内部 subscriber 收到错误时回调。 */
    void innerError(InnerQueuedSubscriber<T> inner, Throwable e);

    /** 内部 subscriber 完成时回调。 */
    void innerComplete(InnerQueuedSubscriber<T> inner);

    /** 触发队列排空（drain）逻辑。 */
    void drain();
}
