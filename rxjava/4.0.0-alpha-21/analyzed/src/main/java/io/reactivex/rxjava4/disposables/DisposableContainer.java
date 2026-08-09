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

package io.reactivex.rxjava4.disposables;

import io.reactivex.rxjava4.annotations.NonNull;

/**
 * 向容器添加与移除 {@link Disposable} 的通用接口。
 * @since 2.0
 */
public interface DisposableContainer extends Disposable {

    /**
     * 向本容器添加 disposable；若容器已被 dispose 则直接 dispose 该 disposable。
     * @param d 要添加的 disposable，不可为 null
     * @return 成功则为 true；若本容器已被 dispose 则为 false
     */
    boolean add(@NonNull Disposable d);

    /**
     * 若给定 disposable 属于本容器，则移除并 dispose 它。
     * @param d 要移除并 dispose 的 disposable，不可为 null
     * @return 若操作成功则为 true
     */
    boolean remove(@NonNull Disposable d);

    /**
     * 若给定 disposable 属于本容器，则移除但不 dispose 它。
     * @param d 要移除的 disposable，不可为 null
     * @return 若操作成功则为 true
     */
    boolean delete(@NonNull Disposable d);

    /**
     * 移除所有包含的 {@link Disposable} 但不 dispose 它们，使本容器恢复为空。
     * @since 4.0.0
     */
    void reset();

    /**
     * 移除并 dispose 所有包含的 {@link Disposable}，使本容器恢复为空，但不 dispose 整个容器。
     */
    void clear();

}
