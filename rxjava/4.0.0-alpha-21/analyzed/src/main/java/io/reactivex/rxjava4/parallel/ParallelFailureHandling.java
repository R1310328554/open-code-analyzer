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

package io.reactivex.rxjava4.parallel;

import io.reactivex.rxjava4.functions.BiFunction;

/**
 * 并行算子内错误处理策略枚举，同时作为 BiFunction 恒返回自身。
 * <p>History: 2.0.8 - experimental
 * @since 2.2
 */
public enum ParallelFailureHandling implements BiFunction<Long, Throwable, ParallelFailureHandling> {
    /** 停止当前 rail 并丢弃错误。 */
    STOP,
    /** 停止当前 rail 并向下游传播错误。 */
    ERROR,
    /** 跳过当前项与错误，继续处理下一项。 */
    SKIP,
    /** 重试当前元素。 */
    RETRY;

    @Override
    public ParallelFailureHandling apply(Long t1, Throwable t2) {
        return this;
    }
}
