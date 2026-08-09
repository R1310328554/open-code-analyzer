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

package org.springframework.boot.autoconfigure;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.annotation.AliasFor;

/**
 * 标识一个声明了一个或多个 {@link Bean @Bean} 方法、并同时触发 {@link EnableAutoConfiguration
 * 自动配置} 与 {@link ComponentScan 组件扫描} 的 {@link Configuration 配置} 类。
 * 该注解是便捷组合注解，等效于同时声明 {@code @SpringBootConfiguration}、
 * {@code @EnableAutoConfiguration} 和 {@code @ComponentScan}。
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @author Andy Wilkinson
 * @since 1.2.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(excludeFilters = { @Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
		@Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class) })
public @interface SpringBootApplication {

	/**
	 * 排除指定的自动配置类，使其永远不会被应用。
	 * <p>
	 * 由于该注解通过加载类字节码解析，在此指定最终可能不在类路径上的类是安全的，
	 * 但前提是注解直接标注在受影响的组件上，<b>而非</b>作为组合元注解使用。
	 * 若要将该注解作为元注解使用，请仅使用 {@link #excludeName} 属性。
	 * @return 要排除的类
	 */
	@AliasFor(annotation = EnableAutoConfiguration.class)
	Class<?>[] exclude() default {};

	/**
	 * 排除指定的自动配置类名，使其永远不会被应用。
	 * @return 要排除的类名
	 * @since 1.3.0
	 */
	@AliasFor(annotation = EnableAutoConfiguration.class)
	String[] excludeName() default {};

	/**
	 * 要扫描带注解组件的基础包。可使用 {@link #scanBasePackageClasses} 作为基于字符串包名的类型安全替代方案。
	 * <p>
	 * <strong>注意：</strong>该设置仅是 {@link ComponentScan @ComponentScan} 的别名。
	 * 它对 {@code @Entity} 扫描或 Spring Data 仓库扫描没有影响；后者应添加
	 * {@code @EntityScan} 和 {@code @Enable...Repositories} 注解。
	 * @return 要扫描的基础包
	 * @since 1.3.0
	 */
	@AliasFor(annotation = ComponentScan.class, attribute = "basePackages")
	String[] scanBasePackages() default {};

	/**
	 * 指定要扫描带注解组件包的类型安全替代方案，等效于 {@link #scanBasePackages}。
	 * 每个指定类所在的包都会被扫描。
	 * <p>
	 * 可考虑在每个包中创建一个仅用于被该属性引用的无操作标记类或接口。
	 * <p>
	 * <strong>注意：</strong>该设置仅是 {@link ComponentScan @ComponentScan} 的别名。
	 * 它对 {@code @Entity} 扫描或 Spring Data 仓库扫描没有影响；后者应添加
	 * {@code @EntityScan} 和 {@code @Enable...Repositories} 注解。
	 * @return 要扫描的基础包
	 * @since 1.3.0
	 */
	@AliasFor(annotation = ComponentScan.class, attribute = "basePackageClasses")
	Class<?>[] scanBasePackageClasses() default {};

	/**
	 * 用于在 Spring 容器内为检测到的组件命名的 {@link BeanNameGenerator} 类。
	 * <p>
	 * 默认值 {@link BeanNameGenerator} 接口本身表示：处理该 {@code @SpringBootApplication}
	 * 注解的扫描器应使用其继承的 Bean 名称生成器，例如默认的
	 * {@link AnnotationBeanNameGenerator}，或在引导时提供给应用上下文的自定义实例。
	 * @return 要使用的 {@link BeanNameGenerator}
	 * @see SpringApplication#setBeanNameGenerator(BeanNameGenerator)
	 * @since 2.3.0
	 */
	@AliasFor(annotation = ComponentScan.class, attribute = "nameGenerator")
	Class<? extends BeanNameGenerator> nameGenerator() default BeanNameGenerator.class;

	/**
	 * 指定是否应对 {@link Bean @Bean} 方法进行代理以强制执行 Bean 生命周期行为，
	 * 例如即使用户代码直接调用 {@code @Bean} 方法也返回共享的单例 Bean 实例。
	 * 该特性需要方法拦截，通过运行时生成的 CGLIB 子类实现，并带来限制，
	 * 例如配置类及其方法不能声明为 {@code final}。
	 * <p>
	 * 默认值为 {@code true}，允许配置类内的“Bean 间引用”，以及外部对该配置类
	 * {@code @Bean} 方法的调用（例如来自其他配置类）。若该配置中每个 {@code @Bean}
	 * 方法都是自包含的、仅作为容器使用的普通工厂方法，可将此标志设为 {@code false}
	 * 以避免 CGLIB 子类处理。
	 * <p>
	 * 关闭 Bean 方法拦截后，{@code @Bean} 方法将像声明在非 {@code @Configuration}
	 * 类上一样单独处理，即“@Bean Lite 模式”（参见 {@link Bean @Bean 的 javadoc}），
	 * 因此在行为上等效于移除 {@code @Configuration} 构造型。
	 * @since 2.2
	 * @return 是否代理 {@code @Bean} 方法
	 */
	@AliasFor(annotation = Configuration.class)
	boolean proxyBeanMethods() default true;

}
