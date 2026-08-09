/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.annotation;

import com.alibaba.csp.sentinel.EntryType;

import java.lang.annotation.*;

/**
 * 表示 Sentinel 资源定义的注解。
 *
 * @author Eric Zhao
 * @author zhaoyuguang
 * @since 0.1.1
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface SentinelResource {

    /**
     * @return Sentinel 资源名称
     */
    String value() default "";

    /**
     * @return Entry 类型（入站或出站），默认为出站
     */
    EntryType entryType() default EntryType.OUT;

    /**
     * @return 资源分类（类型）
     * @since 1.7.0
     */
    int resourceType() default 0;

    /**
     * @return 阻断异常处理函数名称，默认为空
     */
    String blockHandler() default "";

    /**
     * {@code blockHandler} 默认与原方法位于同一类。
     * 若多个方法签名相同且共用同一 blockHandler，
     * 可指定 blockHandler 所在类。注意 blockHandler 方法必须为 static。
     *
     * @return blockHandler 所在类，不应提供多个类
     */
    Class<?>[] blockHandlerClass() default {};

    /**
     * @return 降级函数名称，默认为空
     */
    String fallback() default "";

    /**
     * {@code defaultFallback} 用作默认通用降级方法。
     * 不应接受任何参数，返回类型应与原方法兼容。
     *
     * @return 默认降级方法名称，默认为空
     * @since 1.6.0
     */
    String defaultFallback() default "";

    /**
     * {@code fallback} 默认与原方法位于同一类。
     * 若多个方法签名相同且共用同一 fallback，
     * 可指定 fallback 所在类。注意共用 fallback 方法必须为 static。
     *
     * @return fallback 方法所在类（仅单个类）
     * @since 1.6.0
     */
    Class<?>[] fallbackClass() default {};

    /**
     * @return 要追踪的异常类列表，默认为 {@link Throwable}
     * @since 1.5.1
     */
    Class<? extends Throwable>[] exceptionsToTrace() default {Throwable.class};
    
    /**
     * 指定要忽略的异常。注意 {@code exceptionsToTrace} 不应与
     * {@code exceptionsToIgnore} 同时出现，否则 {@code exceptionsToIgnore} 优先级更高。
     *
     * @return 要忽略的异常类列表，默认为空
     * @since 1.6.0
     */
    Class<? extends Throwable>[] exceptionsToIgnore() default {};
}
