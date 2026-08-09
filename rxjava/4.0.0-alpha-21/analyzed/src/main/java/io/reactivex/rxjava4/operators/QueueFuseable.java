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

package io.reactivex.rxjava4.operators;

import io.reactivex.rxjava4.annotations.NonNull;

/**
 * 扩展 {@link SimpleQueue}，定义算子融合（fusion）模式常量与协商方法。
 * 上游/下游通过 {@link #requestFusion(int)} 决定是否用 poll 替代 onNext 链。
 *
 * @param <T> poll 返回的元素类型
 * @since 3.1.1
 */
public interface QueueFuseable<@NonNull T> extends SimpleQueue<T> {
    /** 上游不支持所请求融合模式时 {@link #requestFusion(int)} 的返回值。 */
    int NONE = 0;

    /**
     * 同步融合：值在 poll 时同步产生或已就绪；poll 返回 null 表示终止。
     * 此模式下上游不调用 onXXX，poll 须在串行 drain-loop 中调用并捕获异常。
     */
    int SYNC = 1;

    /**
     * 异步融合：上游值最终 经 poll 可用；onNext(null) 提示可 poll。
     * onError/onComplete 仍正常；poll 须串行调用并捕获异常。
     */
    int ASYNC = 2;

    /** 请求 SYNC 或 ASYNC 任一可接受模式（位或）。 */
    int ANY = SYNC | ASYNC;

    /**
     * 与 SYNC/ASYNC 组合使用：poll 将在异步边界之后调用，
     * 可能改变融合链上计算所在线程（如 map+observeOn 时 map 跑到 observeOn 线程）。
     */
    int BOUNDARY = 4;

    /**
     * 在 onSubscribe 返回前向 upstream 请求融合模式。
     * 不可重复调用或于 onSubscribe 之后调用。
     *
     * @param mode SYNC、ASYNC、ANY，可与 BOUNDARY 组合
     * @return 实际建立的 NONE、SYNC 或 ASYNC
     */
    int requestFusion(int mode);

}
