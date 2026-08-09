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

package io.reactivex.rxjava4.core;

/**
 * 使用 onBackpressureBuffer 时处理缓冲区溢出的策略选项。
 */
public enum BackpressureOverflowStrategy {
    /**
     * 发出 {@link io.reactivex.rxjava4.exceptions.MissingBackpressureException MissingBackpressureException} 并终止序列。
     */
    ERROR,
    /** 丢弃缓冲区中最旧的值。 */
    DROP_OLDEST,
    /** 丢弃缓冲区中最新的值。 */
    DROP_LATEST
}
