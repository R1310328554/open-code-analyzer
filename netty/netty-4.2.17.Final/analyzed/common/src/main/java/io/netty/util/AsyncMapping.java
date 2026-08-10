/*
 * Copyright 2015 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.util;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;

/**
 * 异步映射：将输入 {@code IN} 转为 {@code OUT}，结果通过 {@link Future} 返回。
 *
 * <p>与同步 {@code Function} 不同，实现可在 I/O 或线程池完成后再完成传入的 {@link Promise}。</p>
 */
public interface AsyncMapping<IN, OUT> {

    /**
     * Returns the {@link Future} that will provide the result of the mapping. The given {@link Promise} will
     * be fulfilled when the result is available.
     *
     * <p>调用方通常传入外部 {@link Promise}；返回的 {@link Future} 与其共享同一结果。</p>
     */
    Future<OUT> map(IN input, Promise<OUT> promise);
}
