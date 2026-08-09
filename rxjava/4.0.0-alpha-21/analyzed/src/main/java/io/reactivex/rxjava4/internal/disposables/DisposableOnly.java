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

package io.reactivex.rxjava4.internal.disposables;

import io.reactivex.rxjava4.disposables.Disposable;

/**
 * {@link Disposable} 的扩展，允许不实现 {@link Disposable#isDisposed()}，
 * 因为在实践中几乎不需要或无法观测。
 * @since 4.0.0
 */
public interface DisposableOnly extends Disposable {

    /** 默认不支持 isDisposed，调用时抛出 {@link UnsupportedOperationException}。 */
    @Override
    default boolean isDisposed() {
        throw new UnsupportedOperationException("The class " + this.getClass() + " does not support isDisposed");
    }
}
