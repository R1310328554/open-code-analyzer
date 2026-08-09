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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.annotation.ImportCandidates;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AliasFor;

/**
 * 标识某个类提供可由 Spring Boot 自动应用的配置。自动配置类本质上是普通的
 * {@link Configuration @Configuration}，区别在于
 * {@link Configuration#proxyBeanMethods() proxyBeanMethods} 始终为 {@code false}。
 * 它们通过 {@link ImportCandidates} 被发现。
 * <p>
 * 通常，自动配置类是顶层类，并标注为 {@link Conditional @Conditional}
 *（最常见的是使用 {@link ConditionalOnClass @ConditionalOnClass} 和
 * {@link ConditionalOnMissingBean @ConditionalOnMissingBean}）。
 *
 * @author Moritz Halbritter
 * @see EnableAutoConfiguration
 * @see AutoConfigureBefore
 * @see AutoConfigureAfter
 * @see Conditional
 * @see ConditionalOnClass
 * @see ConditionalOnMissingBean
 * @since 2.7.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore
@AutoConfigureAfter
public @interface AutoConfiguration {

	/**
	 * 显式指定与 {@code @AutoConfiguration} 类关联的 Spring bean 定义名称。
	 * 若未指定（常见情况），将自动生成 bean 名称。
	 * <p>
	 * 自定义名称仅在该 {@code @AutoConfiguration} 类通过组件扫描被发现，
	 * 或直接提供给 {@link AnnotationConfigApplicationContext} 时生效。
	 * 若以传统 XML bean 定义方式注册，则以 bean 元素的 name/id 为准。
	 * @return 显式组件名称（未指定时为空字符串）
	 * @see AnnotationBeanNameGenerator
	 */
	@AliasFor(annotation = Configuration.class)
	String value() default "";

	/**
	 * 尚未应用的自动配置类。
	 * <p>
	 * 由于本注解通过加载类字节码解析，在此指定最终可能不在类路径上的类是安全的，
	 * 但前提是注解直接标注在受影响的组件上，<b>而非</b>作为组合/元注解使用。
	 * 若要将本注解用作元注解，请仅使用 {@link #beforeName} 属性。
	 * @return 自动配置类
	 */
	@AliasFor(annotation = AutoConfigureBefore.class, attribute = "value")
	Class<?>[] before() default {};

	/**
	 * 尚未应用的自动配置类名称。
	 * 若自动配置类不是顶层类，名称应使用 {@code $} 分隔外部类，
	 * 例如 {@code com.example.Outer$NestedAutoConfiguration}。
	 * @return 类名
	 */
	@AliasFor(annotation = AutoConfigureBefore.class, attribute = "name")
	String[] beforeName() default {};

	/**
	 * 已经应用的自动配置类。
	 * <p>
	 * 由于本注解通过加载类字节码解析，在此指定最终可能不在类路径上的类是安全的，
	 * 但前提是注解直接标注在受影响的组件上，<b>而非</b>作为组合/元注解使用。
	 * 若要将本注解用作元注解，请仅使用 {@link #afterName} 属性。
	 * @return 自动配置类
	 */
	@AliasFor(annotation = AutoConfigureAfter.class, attribute = "value")
	Class<?>[] after() default {};

	/**
	 * 已经应用的自动配置类名称。
	 * 若自动配置类不是顶层类，类名应使用 {@code $} 分隔外部类，
	 * 例如 {@code com.example.Outer$NestedAutoConfiguration}。
	 * @return 类名
	 */
	@AliasFor(annotation = AutoConfigureAfter.class, attribute = "name")
	String[] afterName() default {};

}
