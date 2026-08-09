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

package io.reactivex.rxjava4.annotations;

/**
 * 背压支持种类的枚举。
 * @since 2.0
 */
public enum BackpressureKind {
    /**
     * 与背压相关的请求原样透传，不做变更。
     */
    PASS_THROUGH,
    /**
     * 算子完全支持背压，可通过批处理、仲裁或其他方式协调下游与上游的请求。
     */
    FULL,
    /**
     * 算子采用特殊的背压管理策略；详见对应 JavaDoc。
     */
    SPECIAL,
    /**
     * 算子向上游请求 {@link Long#MAX_VALUE}，同时仍尊重下游的背压。
     */
    UNBOUNDED_IN,
    /**
     * 若下游未及时或足额请求，算子将发出 {@link io.reactivex.rxjava4.exceptions.MissingBackpressureException MissingBackpressureException}。
     */
    ERROR,
    /**
     * 算子忽略一切背压，可能导致下游溢出。
     */
    NONE
}
