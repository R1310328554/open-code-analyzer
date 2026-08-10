/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.testsuite.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.keycloak.provider.Provider;

/**
 * 标识测试依赖的 Provider 必须在 SessionFactory 中可用；否则跳过测试。
 *
 * @author hmlnarik
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Repeatable(RequireProviders.class)
public @interface RequireProvider {
    Class<? extends Provider> value() default Provider.class;

    /**
     * 指定必须存在的 provider ID 白名单；{@code only} 中至少有一个可用才满足要求。
     * 若与 {@link #exclude()} 同时使用，则两条规则均生效。
     * <p />
     * 例如，可用 provider 为 {@code provider1}、{@code provider2}、{@code provider3}，
     * 规则为 {@code @RequireProvider{value = MyFactory.class, only = [provider1, provider2], exclude = [provider2]}} 时，
     * 仅当 SessionFactory 中存在 {@code provider1} 时测试才会运行。
     */
    String[] only() default {};

    /**
     * 指定不满足要求的 provider ID 黑名单；必须存在 {@code value()} 类型的其他 provider。
     * 若与 {@link #only()} 同时使用，则两条规则均生效。
     * <p />
     * 示例同上：最终仅 {@code provider1} 可用时测试才会运行。
     */
    String[] exclude() default {};

}
