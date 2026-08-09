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

import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.core.annotation.AliasFor;

/**
 * 指示要导入的、包含 Bean 定义的一个或多个资源。
 *
 * <p>与 {@link Import @Import} 类似，本注解提供与 Spring XML 配置中
 * {@code <import/>} 元素类似的功能。通常用于设计由
 * {@link AnnotationConfigApplicationContext} 引导的 {@link Configuration @Configuration} 类，
 * 但仍需 XML 命名空间等功能。
 *
 * <p>默认情况下，传给 {@link #locations() locations} 或 {@link #value() value}
 * 属性的参数将按以下规则处理：以 {@code ".groovy"} 结尾的资源位置使用
 * {@link org.springframework.beans.factory.groovy.GroovyBeanDefinitionReader GroovyBeanDefinitionReader}；
 * 否则使用 {@link org.springframework.beans.factory.xml.XmlBeanDefinitionReader XmlBeanDefinitionReader}
 * 解析 Spring {@code <beans/>} XML 文件。也可声明 {@link #reader} 属性以选择自定义
 * {@link BeanDefinitionReader} 实现。
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 3.0
 * @see Configuration
 * @see Import
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface ImportResource {

	/**
	 * {@link #locations} 的别名。
	 * @see #locations
	 * @see #reader
	 */
	@AliasFor("locations")
	String[] value() default {};

	/**
	 * 要导入的资源位置。
	 * <p>支持 {@code classpath:}、{@code file:} 等资源加载前缀。
	 * <p>有关资源处理方式，请参阅 {@link #reader} 的 Javadoc。
	 * @since 4.2
	 * @see #value
	 * @see #reader
	 */
	@AliasFor("value")
	String[] locations() default {};

	/**
	 * 处理通过 {@link #locations() locations} 或 {@link #value() value}
	 * 属性指定的资源时使用的 {@link BeanDefinitionReader} 实现。
	 * <p>配置的 {@code BeanDefinitionReader} 类型必须声明接受单个
	 * {@link org.springframework.beans.factory.support.BeanDefinitionRegistry
	 * BeanDefinitionRegistry} 参数的构造函数。
	 * <p>默认情况下，读取器将按资源路径适配：{@code ".groovy"} 文件使用
	 * {@link org.springframework.beans.factory.groovy.GroovyBeanDefinitionReader
	 * GroovyBeanDefinitionReader} 处理；其余资源使用
	 * {@link org.springframework.beans.factory.xml.XmlBeanDefinitionReader
	 * XmlBeanDefinitionReader} 处理。
	 * @see #locations
	 * @see #value
	 */
	Class<? extends BeanDefinitionReader> reader() default BeanDefinitionReader.class;

}
