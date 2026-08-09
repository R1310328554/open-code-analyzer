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

import java.lang.annotation.*;

import io.reactivex.rxjava4.schedulers.Schedulers;

/**
 * 标明类或方法所使用的调度器类型。
 * <p>
 * 常量涵盖 {@link Schedulers} 中的实例，以及 {@linkplain #NONE 不使用调度器} 与
 * {@linkplain #CUSTOM 手动指定调度器} 两种取值。
 * 第三方库自定义取值时，应以基础包名加冒号（{@code :}）为命名空间，再跟可读名称
 * （例如 {@code com.example:ui-thread}）。
 * @since 2.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.TYPE})
public @interface SchedulerSupport {
    /**
     * 特殊值：算子/类不使用调度器。
     */
    String NONE = "none";
    /**
     * 特殊值：算子/类需要手动指定调度器。
     */
    String CUSTOM = "custom";

    // 内置调度器：
    /**
     * 算子/类在 RxJava 的 {@linkplain Schedulers#computation() 计算调度器} 上运行，或从中获取时序信息。
     */
    String COMPUTATION = "io.reactivex:computation";
    /**
     * 算子/类在 RxJava 的 {@linkplain Schedulers#cached() I/O 调度器} 上运行，或从中获取时序信息。
     * @deprecated 自 4.0.0 起弃用，请改用更具体的 {@link #CACHED} 或 {@link #VIRTUAL} 常量
     */
    @Deprecated(since = "4.0.0")
    String IO = "io.reactivex:io";
    /**
     * 算子/类在 RxJava 的 {@linkplain Schedulers#cached() 缓存调度器} 上运行，或从中获取时序信息。
     * @since 4.0.0
     */
    String CACHED = "io.reactivex:cached";
    /**
     * 算子/类在 RxJava 的 {@linkplain Schedulers#virtual() 虚拟调度器} 上运行，或从中获取时序信息。
     * @since 4.0.0
     */
    String VIRTUAL = "io.reactivex:virtual";
    /**
     * 算子/类在 RxJava 的 {@linkplain Schedulers#newThread() 新线程调度器} 上运行，或从中获取时序信息。
     */
    String NEW_THREAD = "io.reactivex:new-thread";
    /**
     * 算子/类在 RxJava 的 {@linkplain Schedulers#trampoline() 蹦床调度器} 上运行，或从中获取时序信息。
     */
    String TRAMPOLINE = "io.reactivex:trampoline";
    /**
     * 算子/类在 RxJava 的 {@linkplain Schedulers#single() 单线程调度器} 上运行，或从中获取时序信息。
     * <p>历史：2.0.8 — 实验性
     * @since 2.2
     */
    String SINGLE = "io.reactivex:single";

    /**
     * 类或方法所使用的调度器种类。
     * @return 类或方法所使用的调度器名称
     */
    String value();
}
