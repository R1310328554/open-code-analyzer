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

import org.springframework.beans.factory.BeanRegistrar;

/**
 * 指示要导入的一个或多个<em>组件类</em>——通常是 {@link Configuration @Configuration} 类。
 *
 * <p>提供与 Spring XML 中 {@code <import/>} 元素等价的功能。
 *
 * <p>允许导入 {@code @Configuration} 类、{@link ImportSelector}、
 * {@link ImportBeanDefinitionRegistrar} 和 {@link BeanRegistrar} 实现，
 * 以及普通组件类（类似于 {@link AnnotationConfigApplicationContext#register}）。
 *
 * <p>应通过 {@link org.springframework.beans.factory.annotation.Autowired @Autowired}
 * 注入来访问导入的 {@code @Configuration} 类中声明的 {@code @Bean} 定义。
 * 既可自动装配 Bean 本身，也可自动装配声明该 Bean 的配置类实例。
 * 后者便于在 IDE 中显式导航 {@code @Configuration} 类方法。
 *
 * <p>可直接在类级别声明，也可作为元注解使用。
 * 直接在类级别声明的 {@code @Import} 注解在作为元注解声明的 {@code @Import} 之后处理，
 * 从而允许直接声明的导入覆盖通过 {@code @Import} 元注解注册的 Bean。
 *
 * <p>自 Spring Framework 7.0 起，也支持在 {@code @Configuration} 类实现的接口上
 * 声明的 {@code @Import} 注解。本地声明的 {@code @Import} 在接口上的 {@code @Import}
 * 之后处理，从而允许本地导入覆盖通过接口继承的 {@code @Import} 注解注册的 Bean。
 *
 * <p>若需导入 XML 或其他非 {@code @Configuration} 的 Bean 定义资源，
 * 请改用 {@link ImportResource @ImportResource} 注解。
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.0
 * @see Configuration
 * @see ImportSelector
 * @see ImportBeanDefinitionRegistrar
 * @see ImportResource
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Import {

	/**
	 * 要导入的 {@link Configuration @Configuration}、{@link ImportSelector}、
	 * {@link ImportBeanDefinitionRegistrar}、{@link BeanRegistrar} 或普通组件类。
	 */
	Class<?>[] value();

}
