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

import io.reactivex.rxjava4.internal.disposables.NeverDisposableStreamerCancellation;

/**
 * 表示 {@code Streamer} 操作的完整、可 dispose 的取消接口。
 * <p>
 * 决定采用独立接口集，而非改造 {@link DisposableContainer}。
 * @since 4.0.0
 */
public interface DisposableStreamerCancellation extends StreamerCancellation, Disposable {

    /**
     * 返回常量实例：不执行任何操作、不可 dispose，且接受任何传入的 Disposable 但不注册或处理，
     * 因为该 {@code never} 实例本身就无法 dispose。
     * @return 共享的无操作常量实例
     */
    static DisposableStreamerCancellation never() {
        return NeverDisposableStreamerCancellation.INSTANCE;
    }
}
