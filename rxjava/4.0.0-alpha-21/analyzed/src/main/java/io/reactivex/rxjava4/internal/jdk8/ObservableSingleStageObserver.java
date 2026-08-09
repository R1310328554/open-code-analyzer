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

package io.reactivex.rxjava4.internal.jdk8;

import java.util.NoSuchElementException;

/**
 * 通过底层 CompletableFuture 传递源序列的唯一元素；
 * 若上游为空则传递默认项；若上游有多于一个元素则传递 {@link IllegalArgumentException}。
 *
 * @param <T> 元素类型
 * @since 3.0.0
 */
public final class ObservableSingleStageObserver<T> extends ObservableStageObserver<T> {

    final boolean hasDefault;

    final T defaultItem;

    /** @param hasDefault 上游为空时是否使用默认值；@param defaultItem 默认元素 */
    public ObservableSingleStageObserver(boolean hasDefault, T defaultItem) {
        this.hasDefault = hasDefault;
        this.defaultItem = defaultItem;
    }

    /** 仅允许一个元素；出现第二个元素时以 IllegalArgumentException 完成。 */
    @Override
    public void onNext(T t) {
        if (value != null) {
            value = null;
            completeExceptionally(new IllegalArgumentException("Sequence contains more than one element!"));
        } else {
            value = t;
        }
    }

    /** 上游完成时发射唯一元素、默认值或 NoSuchElementException。 */
    @Override
    public void onComplete() {
        if (!isDone()) {
            T v = value;
            clear();
            if (v != null) {
                complete(v);
            } else if (hasDefault) {
                complete(defaultItem);
            } else {
                completeExceptionally(new NoSuchElementException());
            }
        }
    }
}
