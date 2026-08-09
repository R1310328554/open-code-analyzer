/*
 * Copyright 2002-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.context.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 为 {@link Bean @Bean} 方法或 {@link org.springframework.stereotype.Component @Component} 类
 * 建议特定代理类型，覆盖全局默认配置。
 *
 * <p>仅在 Bean 实际会被自动代理时生效；是否自动代理取决于外部配置。
 *
 * @author Juergen Hoeller
 * @since 7.0
 * @see org.springframework.aop.framework.autoproxy.AutoProxyUtils#PRESERVE_TARGET_CLASS_ATTRIBUTE
 * @see org.springframework.aop.framework.autoproxy.AutoProxyUtils#EXPOSED_INTERFACES_ATTRIBUTE
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Proxyable {

	/**
	 * 建议的代理类型：{@link ProxyType#INTERFACES} 表示 JDK 动态代理，
	 * {@link ProxyType#TARGET_CLASS} 表示 CGLIB 代理，覆盖全局默认配置。
	 */
	ProxyType value() default ProxyType.DEFAULT;

	/**
	 * 建议 JDK 动态代理暴露的特定接口，覆盖全局默认配置。
	 * <p>仅当 {@link #value()} 不是 {@link ProxyType#TARGET_CLASS} 时生效。
	 */
	Class<?>[] interfaces() default {};

}
