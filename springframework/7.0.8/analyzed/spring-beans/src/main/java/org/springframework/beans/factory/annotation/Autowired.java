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

package org.springframework.beans.factory.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记构造器、字段、setter 或配置方法应由 Spring 的依赖注入设施进行自动装配。
 * 可作为 JSR-330 {@link jakarta.inject.Inject} 的替代，并额外提供 required/optional 语义。
 *
 * <h3>自动装配构造器</h3>
 * <p>同一 Bean 类中，最多只能有一个构造器将 {@link #required} 设为 {@code true}，
 * 表示创建 Spring Bean 时要自动装配的就是该构造器。若 {@code required} 为 {@code true}，
 * 则只能有一个构造器标注 {@code @Autowired}。若多个构造器标注了注解且均为
 * <i>非必需</i>，它们都会作为候选；容器会选择「能被匹配到的依赖最多」的那个。
 * 若所有候选都无法满足，则使用主构造器/默认构造器（若存在）。
 * 同样，若类声明了多个构造器但都未标注 {@code @Autowired}，也会使用主构造器/默认构造器（若存在）。
 * 若类一开始就只声明了一个构造器，即使未标注也会始终使用。带注解的构造器不必是 public。
 *
 * <h3>自动装配字段</h3>
 * <p>字段在 Bean 构造完成之后、任何配置方法调用之前注入。配置字段不必是 public。
 *
 * <h3>自动装配方法</h3>
 * <p>配置方法可以任意命名、参数个数不限；每个参数都会按匹配的 Bean 从容器自动装配。
 * Bean 属性的 setter 只是这类通用配置方法的特例。配置方法也不必是 public。
 *
 * <h3>自动装配参数</h3>
 * <p>虽然技术上可以在单个方法或构造器参数上声明 {@code @Autowired}，
 * 但框架大部分路径会忽略这类声明。核心 Spring Framework 中主动支持参数级自动装配的，
 * 主要是 {@code spring-test} 模块里的 JUnit Jupiter 支持（详见
 * <a href="https://docs.spring.io/spring/docs/current/spring-framework-reference/testing.html#testcontext-junit-jupiter-di">TestContext framework</a>
 * 参考文档）。
 *
 * <h3>多参数与 {@code required} 语义</h3>
 * <p>对于多参数构造器或方法，{@link #required} 作用于全部参数。
 * 个别参数仍可声明为 {@link java.util.Optional}、{@code @Nullable}，
 * 或 Kotlin 中的非空参数类型，从而覆盖基础的 {@code required} 语义。
 *
 * <h3>自动装配数组、集合与 Map</h3>
 * <p>依赖类型为数组、{@link java.util.Collection} 或 {@link java.util.Map} 时，
 * 容器会自动装配所有匹配声明元素/值类型的 Bean。此时 Map 的键类型须声明为
 * {@code String}，并解析为对应的 Bean 名称。容器提供的集合会排序：优先考虑目标组件的
 * {@link org.springframework.core.Ordered Ordered} 与
 * {@link org.springframework.core.annotation.Order @Order}，否则按其在容器中的注册顺序。
 * 另外，单个匹配的目标 Bean 本身也可以是泛型 {@code Collection} 或 {@code Map}，
 * 此时会按该集合/映射整体注入。
 *
 * <h3>不支持在 {@code BeanPostProcessor} 或 {@code BeanFactoryPostProcessor} 中使用</h3>
 * <p>实际注入由
 * {@link org.springframework.beans.factory.config.BeanPostProcessor BeanPostProcessor}
 * 执行，因此<strong>不能</strong>用 {@code @Autowired} 向
 * {@link org.springframework.beans.factory.config.BeanPostProcessor BeanPostProcessor}
 * 或
 * {@link org.springframework.beans.factory.config.BeanFactoryPostProcessor BeanFactoryPostProcessor}
 * 类型注入引用。请参阅 {@link AutowiredAnnotationBeanPostProcessor} 的 JavaDoc
 *（默认即检查本注解是否存在）。
 *
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Sam Brannen
 * @since 2.5
 * @see AutowiredAnnotationBeanPostProcessor
 * @see Qualifier
 * @see Value
 */
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Autowired {

	/**
	 * 声明被注解的依赖是否必须存在。
	 * <p>默认为 {@code true}。
	 */
	boolean required() default true;

}
