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
import io.reactivex.rxjava4.core.Streamable;

/**
 * {@link DisposableStreamerCancellation} 的不可 dispose 视图，
 * 允许同步检测 disposed 状态，并允许添加/移除在完整容器 dispose 时需清理的 {@link Disposable} 资源。
 * <p>
 * 提供此视图是为了防止在 {@link Streamable#stream(StreamerCancellation)} 实现中调用
 * {@link DisposableStreamerCancellation#dispose()}，因为 dispose 流是调用方/下游的特权。
 * <p>
 * 使用 {@link #derive()} 创建具有完整 dispose 能力的子容器。
 * <p>
 * 本接口不支持 {@link DisposableContainer#reset()} 与 {@link DisposableContainer#clear()}，
 * 否则会误删其它算子添加/注册的 {@code Disposable}。
 * @since 4.0.0
 */
public interface StreamerCancellation {
    /**
     * 若本资源已被 dispose 则返回 true。
     * @return 若本资源已被 dispose 则为 true
     */
    boolean isDisposed();

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
     * 创建派生子取消管理接口：可由本取消管理对象 dispose，
     * 但 dispose 子取消管理器不会 dispose 当前 {@code StreamerCancellation} 实例。
     * @return 派生的子取消管理对象
     */
    @NonNull
    DisposableStreamerCancellation derive();

}
