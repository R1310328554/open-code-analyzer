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
import io.reactivex.rxjava4.core.Flowable;
import io.reactivex.rxjava4.functions.*;
import io.reactivex.rxjava4.internal.functions.*;

/**
 * onBackpressureBuffer() 算子的配置 record。
 * @param <T> 被比较序列的元素类型
 * @param capacity
 *                缓冲区可用槽位数。
 * @param delayError
 *                若为 {@code true}，当前 {@code Flowable} 的异常会延迟到所有缓冲元素被下游消费后再发出；
 *                若为 {@code false}，异常会立即通知下游，并跳过任何缓冲元素
 * @param unbounded
 *                若为 {@code true}，capacity 值被解释为无界缓冲区内部的「岛」大小
 * @param onDropped
 *                因容量限制无法缓冲时，对该元素调用的 {@link Consumer}。
 * @since 4.0.0
 */

public record OnBackpressureBufferConfig<T>(
        int capacity,
        boolean delayError,
        boolean unbounded,
        @NonNull Consumer<? super T> onDropped) {

    /**
     * 默认设置：无错误延迟、无界、无 onOverflow 或 onDropped 活动。
     */
    public static final OnBackpressureBufferConfig<Object> DEFAULT = new OnBackpressureBufferConfig<>(false, true);

    /**
     * 使用给定容量创建配置：无错误延迟、有界、无回调。
     * @param capacity
     *                缓冲区可用槽位数。
     */
    public OnBackpressureBufferConfig(int capacity) {
        this(capacity, false, false, Functions.emptyConsumer());
    }

    /**
     * 使用给定错误延迟模式创建配置：容量为 {@link Flowable#bufferSize()}、有界、无回调。
     * @param delayError
     *                if {@code true}, an exception from the current {@code Flowable} is delayed until all buffered elements have been
     *                consumed by the downstream; if {@code false}, an exception is immediately signaled to the downstream, skipping
     *                any buffered element
     */
    public OnBackpressureBufferConfig(boolean delayError) {
        this(Flowable.bufferSize(), delayError, false, Functions.emptyConsumer());
    }

    /**
     * 使用给定容量与错误延迟模式创建配置：有界、无回调。
     * @param capacity
     *                缓冲区可用槽位数。
     * @param delayError
     *                if {@code true}, an exception from the current {@code Flowable} is delayed until all buffered elements have been
     *                consumed by the downstream; if {@code false}, an exception is immediately signaled to the downstream, skipping
     *                any buffered element
     */
    public OnBackpressureBufferConfig(int capacity, boolean delayError) {
        this(capacity, delayError, false, Functions.emptyConsumer());
    }

    /**
     * 使用给定错误延迟模式与有界性创建配置：容量为 {@link Flowable#bufferSize()}、无回调。
     * @param delayError
     *                if {@code true}, an exception from the current {@code Flowable} is delayed until all buffered elements have been
     *                consumed by the downstream; if {@code false}, an exception is immediately signaled to the downstream, skipping
     *                any buffered element
     * @param unbounded
     *                若为 {@code true}，capacity 值被解释为无界缓冲区内部的「岛」大小
     */
    public OnBackpressureBufferConfig(boolean delayError, boolean unbounded) {
        this(Flowable.bufferSize(), delayError, unbounded, Functions.emptyConsumer());
    }

    /**
     * 使用给定容量、错误延迟模式与有界性创建配置：无回调。
     * @param capacity
     *                缓冲区可用槽位数。
     * @param delayError
     *                if {@code true}, an exception from the current {@code Flowable} is delayed until all buffered elements have been
     *                consumed by the downstream; if {@code false}, an exception is immediately signaled to the downstream, skipping
     *                any buffered element
     * @param unbounded
     *                若为 {@code true}，capacity 值被解释为无界缓冲区内部的「岛」大小
     */
    public OnBackpressureBufferConfig(int capacity, boolean delayError, boolean unbounded) {
        this(capacity, delayError, unbounded, Functions.emptyConsumer());
    }

    /**
     * 使用给定 onDropped 回调创建配置：容量为 {@link Flowable#bufferSize()}、无错误延迟、有界。
     * @param onDropped
     *                因容量限制无法缓冲时，对该元素调用的 {@link Consumer}。
     */
    public OnBackpressureBufferConfig(@NonNull Consumer<? super T> onDropped) {
        this(Flowable.bufferSize(), false, false, onDropped);
    }

    /**
     * 使用给定容量与 onDropped 回调创建配置：无错误延迟、有界。
     * @param capacity
     *                缓冲区可用槽位数。
     * @param onDropped
     *                因容量限制无法缓冲时，对该元素调用的 {@link Consumer}。
     */
    public OnBackpressureBufferConfig(int capacity, @NonNull Consumer<? super T> onDropped) {
        this(capacity, false, false, onDropped);
    }

    /**
     * 使用所有提供的值创建配置。
     * @param capacity
     *                缓冲区可用槽位数。
     * @param delayError
     *                if {@code true}, an exception from the current {@code Flowable} is delayed until all buffered elements have been
     *                consumed by the downstream; if {@code false}, an exception is immediately signaled to the downstream, skipping
     *                any buffered element
     * @param unbounded
     *                若为 {@code true}，capacity 值被解释为无界缓冲区内部的「岛」大小
     * @param onDropped
     *                因容量限制无法缓冲时，对该元素调用的 {@link Consumer}。
     */
    public OnBackpressureBufferConfig {
        ObjectHelper.verifyPositive(capacity, "capacity");
        Objects.requireNonNull(onDropped, "onDropped is null");
    }
}
