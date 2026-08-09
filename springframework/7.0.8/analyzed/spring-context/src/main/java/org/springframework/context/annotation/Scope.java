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

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.annotation.AliasFor;

/**
 * 与 {@link org.springframework.stereotype.Component @Component} 配合作为类型级注解时，
 * {@code @Scope} 指定注解类型实例应使用的作用域名称。
 *
 * <p>与 {@link Bean @Bean} 配合作为方法级注解时，{@code @Scope} 指定方法返回实例应使用的作用域名称。
 *
 * <p><b>注意：</b>{@code @Scope} 注解仅在具体 Bean 类（带注解的组件）或工厂方法
 * （{@code @Bean} 方法）上被内省。与 XML Bean 定义不同，不存在 Bean 定义继承的概念，
 * 类级继承层次对元数据无意义。
 *
 * <p>此处的<em>作用域</em>指实例的生命周期，例如 {@code singleton}、{@code prototype} 等。
 * Spring 内置作用域可通过 {@link ConfigurableBeanFactory} 与 {@code WebApplicationContext}
 * 接口中的 {@code SCOPE_*} 常量引用。
 *
 * <p>要注册额外的自定义作用域，请参阅
 * {@link org.springframework.beans.factory.config.CustomScopeConfigurer CustomScopeConfigurer}。
 *
 * @author Mark Fisher
 * @author Chris Beams
 * @author Sam Brannen
 * @since 2.5
 * @see org.springframework.stereotype.Component
 * @see org.springframework.context.annotation.Bean
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Scope {

	/**
	 * {@link #scopeName} 的别名。
	 * @see #scopeName
	 */
	@AliasFor("scopeName")
	String value() default "";

	/**
	 * 指定带注解组件/Bean 应使用的作用域名称。
	 * <p>默认为空字符串（{@code ""}），表示
	 * {@link ConfigurableBeanFactory#SCOPE_SINGLETON SCOPE_SINGLETON}。
	 * @since 4.2
	 * @see ConfigurableBeanFactory#SCOPE_PROTOTYPE
	 * @see ConfigurableBeanFactory#SCOPE_SINGLETON
	 * @see org.springframework.web.context.WebApplicationContext#SCOPE_REQUEST
	 * @see org.springframework.web.context.WebApplicationContext#SCOPE_SESSION
	 * @see #value
	 */
	@AliasFor("value")
	String scopeName() default "";

	/**
	 * 指定组件是否应配置为作用域代理，以及代理应基于接口还是子类。
	 * <p>默认为 {@link ScopedProxyMode#DEFAULT}，通常表示除非在组件扫描指令级别
	 * 配置了不同默认值，否则不创建作用域代理。
	 * <p>类似于 Spring XML 中的 {@code <aop:scoped-proxy/>} 支持。
	 * @see ScopedProxyMode
	 */
	ScopedProxyMode proxyMode() default ScopedProxyMode.DEFAULT;

}
