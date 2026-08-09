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

import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Profiles;

/**
 * 指示当 {@linkplain #value 指定的一个或多个 profile} 处于激活状态时，组件才符合注册条件。
 *
 * <p><em>Profile</em> 是命名的逻辑分组，可通过 {@link ConfigurableEnvironment#setActiveProfiles}
 * 以编程方式激活，或通过将 {@link AbstractEnvironment#ACTIVE_PROFILES_PROPERTY_NAME
 * spring.profiles.active} 属性设为 JVM 系统属性、环境变量，或 Web 应用 {@code web.xml}
 * 中的 Servlet 上下文参数来声明式激活。集成测试中也可通过 {@code @ActiveProfiles} 声明式激活。
 *
 * <p>若上述方式均未激活任何 profile，则启用默认 profile 作为回退。默认 profile 名称为
 * {@value AbstractEnvironment#RESERVED_DEFAULT_PROFILE_NAME}。可通过
 * {@link ConfigurableEnvironment#setDefaultProfiles} 或
 * {@link AbstractEnvironment#DEFAULT_PROFILES_PROPERTY_NAME spring.profiles.default}
 * 属性（JVM 系统属性、环境变量或 {@code web.xml} Servlet 上下文参数）声明式修改。
 *
 * <p>{@code @Profile} 可用于以下场景：
 * <ul>
 * <li>直接或间接标注了 {@code @Component} 的类（含 {@link Configuration @Configuration}）</li>
 * <li>作为元注解，用于组合自定义构造型注解</li>
 * <li>任意 {@link Bean @Bean} 方法</li>
 * </ul>
 *
 * <p>若 {@code @Configuration} 类标注了 {@code @Profile}，除非至少一个指定 profile 已激活，
 * 否则该类关联的全部 {@code @Bean} 方法与 {@link Import @Import} 注解均会被跳过。
 * profile 字符串可以是简单名称（如 {@code "p1"}）或 profile 表达式（如 {@code "p1 & p2"}）。
 * 支持格式详见 {@link Profiles#of(String...)}。
 *
 * <p>这与 Spring XML 行为类似：若 {@code <beans>} 元素提供了 {@code profile} 属性
 * （如 {@code <beans profile="p1,p2">}），除非 profile 'p1' 或 'p2' 至少其一已激活，
 * 否则不会解析该元素。同理，标注 {@code @Profile({"p1", "p2"})} 的 {@code @Component}
 * 或 {@code @Configuration} 类，除非至少一个 profile 已激活，否则不会注册或处理。
 *
 * <p>若 profile 以 NOT 运算符（{@code !}）为前缀，则在该 profile <em>未</em>激活时注册组件——
 * 例如 {@code @Profile({"p1", "!p2"})} 在 profile 'p1' 激活或 profile 'p2' <em>未</em>激活时注册。
 *
 * <p>若省略 {@code @Profile}，无论哪些 profile（若有）处于激活状态，均会注册。
 *
 * <p><b>注意：</b>在 {@code @Bean} 方法上使用 {@code @Profile} 时，有一种特殊情况：
 * 同名 Java 方法重载（类似构造器重载）时，{@code @Profile} 条件必须在所有重载方法上一致声明。
 * 若不一致，仅第一个声明上的条件生效。因此 {@code @Profile} 不能用于在重载方法间按参数签名选择；
 * 同一 Bean 的多个工厂方法在创建时遵循 Spring 构造器解析算法。
 * <b>若需为不同 profile 定义替代 Bean，请使用指向相同 {@link Bean#name bean 名称}
 * 的不同 Java 方法名</b>；参见 {@link Configuration @Configuration} JavaDoc 中的
 * {@code ProfileDatabaseConfig} 示例。
 *
 * <p>通过 XML 定义 Spring Bean 时，可使用 {@code <beans>} 元素的 {@code "profile"} 属性。
 * 详见 {@code spring-beans} XSD（3.1 及以上版本）文档。
 *
 * @author Chris Beams
 * @author Phillip Webb
 * @author Sam Brannen
 * @since 3.1
 * @see ConfigurableEnvironment#setActiveProfiles
 * @see ConfigurableEnvironment#setDefaultProfiles
 * @see AbstractEnvironment#ACTIVE_PROFILES_PROPERTY_NAME
 * @see AbstractEnvironment#DEFAULT_PROFILES_PROPERTY_NAME
 * @see Conditional
 * @see org.springframework.test.context.ActiveProfiles
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(ProfileCondition.class)
public @interface Profile {

	/**
	 * 标注组件符合注册条件时所依赖的 profile 集合。
	 */
	String[] value();

}
