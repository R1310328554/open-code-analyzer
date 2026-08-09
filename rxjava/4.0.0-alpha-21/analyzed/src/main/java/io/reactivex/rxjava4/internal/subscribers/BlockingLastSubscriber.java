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
 * 阻塞直到上游发出最后一个值或完成。
 *
 * @param <T> 值类型
 */
public final class BlockingLastSubscriber<T> extends BlockingBaseSubscriber<T> {

    /** 持续覆盖 value 以保留最后一个元素。 */
    @Override
    public void onNext(T t) {
        value = t;
    }

    /** 清空 value、记录 error 并 countDown。 */
    @Override
    public void onError(Throwable t) {
        value = null;
        error = t;
        countDown();
    }
}
