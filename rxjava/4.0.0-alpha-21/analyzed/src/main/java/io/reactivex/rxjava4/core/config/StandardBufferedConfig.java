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
 * 具有三种错误处理模式及 bufferSize 或 prefetch 类参数的算子配置 record。
 * @param errorMode 内部或外部源出现错误时的处理方式
 * @param bufferSize 预期从各源缓冲或预取的项目数量
 * @since 4.0.0
 */
public record StandardBufferedConfig(@NonNull ErrorMode errorMode, int bufferSize) {

    /**
     * 默认配置：无错误延迟，bufferSize 为 {@link Observable#bufferSize()}。
     */
    public static final StandardBufferedConfig DEFAULT = new StandardBufferedConfig(ErrorMode.IMMEDIATE);

    /**
     * 默认配置：错误延迟至结束，bufferSize 为 {@link Observable#bufferSize()}。
     */
    public static final StandardBufferedConfig DELAY_ERRORS = new StandardBufferedConfig(ErrorMode.END);

    /**
     * 默认配置：错误延迟至边界，bufferSize 为 {@link Observable#bufferSize()}。
     */
    public static final StandardBufferedConfig DELAY_ERRORS_BOUNDARY = new StandardBufferedConfig(ErrorMode.BOUNDARY);

    /**
     * 默认配置：无错误延迟，最大 bufferSize / prefetch 为 2。
     */
    public static final StandardBufferedConfig MIN_DEFAULT = new StandardBufferedConfig(ErrorMode.IMMEDIATE, 2);

    /**
     * 默认配置：错误延迟，最大 bufferSize / prefetch 为 2。
     */
    public static final StandardBufferedConfig MIN_DELAY_ERRORS = new StandardBufferedConfig(ErrorMode.END, 2);

    /**
     * 默认配置：错误延迟至边界，最大 bufferSize / prefetch 为 2。
     */
    public static final StandardBufferedConfig MIN_DELAY_ERRORS_BOUNDARY = new StandardBufferedConfig(ErrorMode.BOUNDARY, 2);

    /**
     * 将 errorMode 设为给定值，bufferSize 设为 {@link Flowable#bufferSize()}
     * @param errorMode 内部或外部源出现错误时的处理方式
     */
    public StandardBufferedConfig(@NonNull ErrorMode errorMode) {
        this(errorMode, Flowable.bufferSize());
    }

    /**
     * 将 errorMode 设为 IMMEDIATE（false）或 END（true），bufferSize 设为 {@link Flowable#bufferSize()}。
     * @param delayErrors 若为 true 使用 ErrorMode.END，否则使用 ErrorMode.IMMEDIATE
     */
    public StandardBufferedConfig(boolean delayErrors) {
        this(delayErrors ? ErrorMode.END : ErrorMode.IMMEDIATE);
    }

    /**
     * 将 errorMode 设为 IMMEDIATE，bufferSize 设为给定值。
     * @param bufferSize 处理内部源时期望缓冲的外部源数量
     */
    public StandardBufferedConfig(int bufferSize) {
        this(ErrorMode.IMMEDIATE, bufferSize);
    }

    /**
     * 将 errorMode 设为 IMMEDIATE（false）或 END（true），bufferSize 设为给定值。
     * @param delayErrors 若为 true 使用 ErrorMode.END，否则使用 ErrorMode.IMMEDIATE
     * @param bufferSize 处理内部源时期望缓冲的外部源数量
     */
    public StandardBufferedConfig(boolean delayErrors, int bufferSize) {
        this(delayErrors ? ErrorMode.END : ErrorMode.IMMEDIATE, bufferSize);
    }

    /**
     * 将 errorMode 与 bufferSize 设为给定值。
     * @param errorMode 内部或外部源出现错误时的处理方式
     * @param bufferSize 处理内部源时期望缓冲的外部源数量
     */
    public StandardBufferedConfig {
        Objects.requireNonNull(errorMode, "errorMode is null");
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
