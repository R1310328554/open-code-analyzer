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
 * 标准配置块，可选延迟错误、调整最大并发数以及 buffer/prefetch 大小。
 * <p>
 * 本配置 record 结合了常规二元错误处理模式与三元错误处理模式。
 * 使用 {@link #StandardConcurrentBufferedConfig(boolean)} 构造器可创建上述二元情形。
 * TODO once value classes are available, make this a record class.
 * @param errorMode 内部或外部源出现错误时的处理方式
 * @param maxConcurrency 最大并发流数量
 * @param bufferSize 预期从各源缓冲或预取的项目数量
 * @since 4.0.0
 */
public record StandardConcurrentBufferedConfig(@NonNull ErrorMode errorMode, int maxConcurrency, int bufferSize) {

    /**
     * 默认配置：无错误延迟，最大并发与 buffer 大小均为 Flowable#bufferSize()。
     */
    public static final StandardConcurrentBufferedConfig DEFAULT = new StandardConcurrentBufferedConfig(false);

    /**
     * 默认配置：错误延迟，最大并发与 buffer 大小均为 Flowable#bufferSize()。
     */
    public static final StandardConcurrentBufferedConfig DELAY_ERRORS = new StandardConcurrentBufferedConfig(true);

    /**
     * 默认配置：边界处错误延迟，最大并发与 buffer 大小均为 Flowable#bufferSize()。
     */
    public static final StandardConcurrentBufferedConfig DELAY_ERRORS_BOUNDARY = new StandardConcurrentBufferedConfig(ErrorMode.BOUNDARY);

    /**
     * 默认配置：无错误延迟，并发为 MAX_VALUE，buffer 大小为 Flowable#bufferSize()。
     */
    public static final StandardConcurrentBufferedConfig MAX_DEFAULT = new StandardConcurrentBufferedConfig(false, Integer.MAX_VALUE, Flowable.bufferSize());

    /**
     * 默认配置：错误延迟，并发为 MAX_VALUE，buffer 大小为 Flowable#bufferSize()。
     */
    public static final StandardConcurrentBufferedConfig MAX_DELAY_ERRORS = new StandardConcurrentBufferedConfig(true, Integer.MAX_VALUE, Flowable.bufferSize());

    /**
     * 默认配置：边界处错误延迟，并发为 MAX_VALUE，buffer 大小为 Flowable#bufferSize()。
     */
    public static final StandardConcurrentBufferedConfig MAX_DELAY_ERRORS_BOUNDARY =
            new StandardConcurrentBufferedConfig(ErrorMode.BOUNDARY, Integer.MAX_VALUE, Flowable.bufferSize());

    /**
     * 将 errorMode 设为 IMMEDIATE（false）或 END（true），maxConcurrency 与 bufferSize 均为 {@link Flowable#bufferSize()}。
     * @param delayErrors 是否延迟错误
     */
    public StandardConcurrentBufferedConfig(boolean delayErrors) {
        this(delayErrors, Flowable.bufferSize(), Flowable.bufferSize());
    }

    /**
     * 将 errorMode 设为给定值，maxConcurrency 与 bufferSize 均为 {@link Flowable#bufferSize()}。
     * @param errorMode 内部或外部源出现错误时的处理方式
     */
    public StandardConcurrentBufferedConfig(ErrorMode errorMode) {
        this(errorMode, Flowable.bufferSize(), Flowable.bufferSize());
    }

    /**
     * 将 errorMode 设为 IMMEDIATE，maxConcurrency 为给定值，bufferSize 为 {@link Flowable#bufferSize()}。
     * @param maxConcurrency the maximum number of concurrent flows
     */
    public StandardConcurrentBufferedConfig(int maxConcurrency) {
        this(false, maxConcurrency, Flowable.bufferSize());
    }

    /**
     * 将 errorMode 设为 IMMEDIATE（false）或 END（true），maxConcurrency 为给定值，bufferSize 为 {@link Flowable#bufferSize()}。
     * @param delayErrors 是否延迟错误
     * @param maxConcurrency the maximum number of concurrent flows
     */
    public StandardConcurrentBufferedConfig(boolean delayErrors, int maxConcurrency) {
        this(delayErrors, maxConcurrency, Flowable.bufferSize());
    }

    /**
     * 将 errorMode 设为给定值，maxConcurrency 为给定值，bufferSize 为 {@link Flowable#bufferSize()}。
     * @param errorMode 内部或外部源出现错误时的处理方式
     * @param maxConcurrency the maximum number of concurrent flows
     */
    public StandardConcurrentBufferedConfig(ErrorMode errorMode, int maxConcurrency) {
        this(errorMode, maxConcurrency, Flowable.bufferSize());
    }

    /**
     * 将 errorMode 设为 IMMEDIATE（false）或 END（true），maxConcurrency 与 bufferSize 均为给定值。
     * @param delayErrors 是否延迟错误
     * @param maxConcurrency the maximum number of concurrent flows
     * @param bufferSize 预期从各源缓冲或预取的项目数量
     */
    public StandardConcurrentBufferedConfig(boolean delayErrors, int maxConcurrency, int bufferSize) {
        this(delayErrors ? ErrorMode.END : ErrorMode.IMMEDIATE, maxConcurrency, bufferSize);
    }

    /**
     * 完全自定义配置。
     * @param errorMode 内部或外部源出现错误时的处理方式
     * @param maxConcurrency 最大并发流数量
     * @param bufferSize 预期从各源缓冲或预取的项目数量
     */
    public StandardConcurrentBufferedConfig {
        Objects.requireNonNull(errorMode, "errorMode is null");
        ObjectHelper.verifyPositive(maxConcurrency, "maxConcurrency");
        ObjectHelper.verifyPositive(bufferSize, "bufferSize");
    }

    /**
     * 若本配置为延迟错误处理模式（如 BOUNDARY 或 END）则返回 true。
     * @return 若为延迟错误处理模式则为 true
     */
    public boolean delayErrors() {
        return errorMode != ErrorMode.IMMEDIATE;
    }
}
