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
package com.alibaba.csp.sentinel.annotation.cdi.interceptor;

import com.alibaba.csp.sentinel.EntryType;

import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;

import java.lang.annotation.*;

/**
 * CDI 拦截器绑定注解：声明 Sentinel 资源名、流控方向及 fallback/blockHandler 配置。
 *
 * @author Eric Zhao
 * @author seasidesky
 * @since 1.8.0
 */
@InterceptorBinding
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface SentinelResourceBinding {

    /**
     * @return Sentinel 资源名称
     */
    @Nonbinding
    String value() default "";

    /**
     * @return 入口类型（IN/OUT），默认 OUT
     */
    @Nonbinding
    EntryType entryType() default EntryType.OUT;

    /**
     * @return 资源分类类型编号
     */
    @Nonbinding
    int resourceType() default 0;

    /**
     * @return blockHandler 方法名，默认空
     */
    @Nonbinding
    String blockHandler() default "";

    /**
     * 默认 {@code blockHandler} 与原方法同处一类；多方法共享时可指定外部类，且 handler 必须为 static。
     *
     * @return blockHandler 所在类（最多一个）
     */
    @Nonbinding
    Class<?>[] blockHandlerClass() default {};

    /**
     * @return fallback 方法名，默认空
     */
    @Nonbinding
    String fallback() default "";

    /**
     * {@code defaultFallback} 为通用降级方法，应无参且返回类型与原方法兼容。
     *
     * @return defaultFallback 方法名，默认空
     */
    @Nonbinding
    String defaultFallback() default "";

    /**
     * 默认 {@code fallback} 与原方法同处一类；共享时可指定外部类，且 fallback 必须为 static。
     *
     * @return fallback 所在类（仅一个）
     */
    @Nonbinding
    Class<?>[] fallbackClass() default {};

    /**
     * @return 需追踪的异常类型列表，默认 {@link Throwable}
     */
    @Nonbinding
    Class<? extends Throwable>[] exceptionsToTrace() default {Throwable.class};

    /**
     * 指定忽略的异常类型；与 {@code exceptionsToTrace} 同时出现时以 {@code exceptionsToIgnore} 为准。
     *
     * @return 忽略的异常类型列表，默认空
     */
    @Nonbinding
    Class<? extends Throwable>[] exceptionsToIgnore() default {};
}
