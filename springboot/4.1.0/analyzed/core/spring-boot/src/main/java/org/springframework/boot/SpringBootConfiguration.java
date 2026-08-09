/*
 * Copyright 2012-present the original author or authors.
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

package org.springframework.boot;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Indexed;

/**
 * 标识类提供 Spring Boot 应用的 {@link Configuration @Configuration}。
 * <p>
 * 可作为 Spring 标准 {@code @Configuration} 的替代，便于自动发现配置（例如测试中）。
 * 应用应仅包含<em>一个</em> {@code @SpringBootConfiguration}，
 * 惯用的 Spring Boot 应用通常通过 {@code @SpringBootApplication} 继承它。
 *
 * @author Phillip Webb
 * @author Andy Wilkinson
 * @since 1.4.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Configuration
@Indexed
public @interface SpringBootConfiguration {

	/**
	 * 是否代理 {@link Bean @Bean} 方法以强制 Bean 生命周期行为
	 * （例如用户代码直接调用 {@code @Bean} 方法时仍返回共享单例）。
	 * <p>
	 * 此特性通过运行时生成的 CGLIB 子类实现方法拦截，
	 * 配置类及其方法不能声明为 {@code final} 等限制适用。
	 * 默认为 {@code true}，允许配置类内部 Bean 互引及外部调用本配置的 {@code @Bean} 方法。
	 * 若各 {@code @Bean} 方法自洽且仅作容器工厂方法，可设为 {@code false} 以避免 CGLIB 处理。
	 * <p>
	 * 关闭代理等效于在非 {@code @Configuration} 类上声明 {@code @Bean} 的 Lite 模式
	 * （见 {@link Bean @Bean} 文档），行为上等效于移除 {@code @Configuration} 元注解。
	 *
	 * @return 是否代理 {@code @Bean} 方法
	 * @since 2.2
	 */
	@AliasFor(annotation = Configuration.class)
	boolean proxyBeanMethods() default true;

}
