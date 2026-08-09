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

package io.reactivex.rxjava4.core.config;

import java.util.Objects;

import io.reactivex.rxjava4.annotations.NonNull;
import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.internal.functions.ObjectHelper;

/**
 * 标准配置块，可选延迟错误并调整最大并发数。
 * <p>
 * 本配置 record 结合了常规二元错误处理模式与三元错误处理模式。
 * 使用 {@link #StandardConcurrentConfig(boolean)} 构造器可创建上述二元情形。
 * TODO once value classes are available, make this a record class.
 * @param errorMode 内部或外部源出现错误时的处理方式
 * @param maxConcurrency 最大并发流数量
 * @since 4.0.0
 */
public record StandardConcurrentConfig(@NonNull ErrorMode errorMode, int maxConcurrency) {

    /**
     * 默认配置：无错误延迟，最大并发为 Flowable#bufferSize()。
     */
    public static final StandardConcurrentConfig DEFAULT = new StandardConcurrentConfig(false);

    /**
     * 默认配置：错误延迟，最大并发为 Flowable#bufferSize()。
     */
    public static final StandardConcurrentConfig DELAY_ERRORS = new StandardConcurrentConfig(true);

    /**
     * 默认配置：边界处错误延迟，最大并发为 Flowable#bufferSize()。
     */
    public static final StandardConcurrentConfig DELAY_ERRORS_BOUNDARY = new StandardConcurrentConfig(true);

    /**
     * 默认配置：无错误延迟，并发为 MAX_VALUE。
     */
    public static final StandardConcurrentConfig MAX_DEFAULT = new StandardConcurrentConfig(false, Integer.MAX_VALUE);

    /**
     * 默认配置：错误延迟，并发为 MAX_VALUE。
     */
    public static final StandardConcurrentConfig MAX_DELAY_ERRORS = new StandardConcurrentConfig(true, Integer.MAX_VALUE);

    /**
     * 默认配置：边界处错误延迟，并发为 MAX_VALUE。
     */
    public static final StandardConcurrentConfig MAX_DELAY_ERRORS_BOUNDARY = new StandardConcurrentConfig(true, Integer.MAX_VALUE);

    /**
     * 可选延迟错误，大小为 {@link Flowable#bufferSize()}。
     * @param delayErrors 是否延迟错误
     */
    public StandardConcurrentConfig(boolean delayErrors) {
        this(delayErrors, Flowable.bufferSize());
    }

    /**
     * Optionally delay error, {@link Flowable#bufferSize()} sizes
     * @param errorMode 内部或外部源出现错误时的处理方式
     */
    public StandardConcurrentConfig(ErrorMode errorMode) {
        this(errorMode, Flowable.bufferSize());
    }

    /**
     * 可选设置 buffer 大小，不延迟错误。
     * @param maxConcurrency the maximum number of concurrent flows
     */
    public StandardConcurrentConfig(int maxConcurrency) {
        this(false, maxConcurrency);
    }

    /**
     * 可选延迟错误并设置 buffer 大小。
     * @param delayErrors 是否延迟错误
     * @param maxConcurrency the maximum number of concurrent flows
     */
    public StandardConcurrentConfig(boolean delayErrors, int maxConcurrency) {
        this(delayErrors ? ErrorMode.END : ErrorMode.IMMEDIATE, maxConcurrency);
    }

    /**
     * 完全自定义配置。
     * @param errorMode 内部或外部源出现错误时的处理方式
     * @param maxConcurrency 最大并发流数量
     */
    public StandardConcurrentConfig {
        Objects.requireNonNull(errorMode, "errorMode is null");
        ObjectHelper.verifyPositive(maxConcurrency, "maxConcurrency");
    }

    /**
     * 若本配置为延迟错误处理模式（如 BOUNDARY 或 END）则返回 true。
     * @return 若为延迟错误处理模式则为 true
     */
    public boolean delayErrors() {
        return errorMode != ErrorMode.IMMEDIATE;
    }

    /**
     * 转换为本配置提供的默认 buffer 大小的缓冲版本。
     * @return 新的 {@code StandardConcurrentBufferConfig} 实例
     */
    public StandardConcurrentBufferedConfig toBuffered() {
        return new StandardConcurrentBufferedConfig(errorMode, maxConcurrency);
    }

    /**
     * 转换为本配置提供的给定 buffer 大小的缓冲版本。
     * @param bufferSize the expected number of items to buffer or prefetch from the various sources
     * @return the new {@code StandardConcurrentBufferConfig} instance
     */
    public StandardConcurrentBufferedConfig toBuffered(int bufferSize) {
        return new StandardConcurrentBufferedConfig(errorMode, maxConcurrency, bufferSize);
    }
}
