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

package org.springframework.boot.context.properties;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Indexed;

/**
 * 外部化配置的注解。若要将外部属性（如 .properties 文件）绑定并校验到对象上，
 * 可将其添加到类定义或 {@code @Configuration} 类中的 {@code @Bean} 方法。
 * <p>
 * 绑定方式：调用被注解类的 setter；若使用 {@link ConstructorBinding @ConstructorBinding}，
 * 则绑定到构造器参数。
 * <p>
 * 与 {@code @Value} 不同，属性值已外部化，因此不会求值 SpEL 表达式。
 *
 * @author Dave Syer
 * @since 1.0.0
 * @see ConfigurationPropertiesScan
 * @see ConstructorBinding
 * @see ConfigurationPropertiesBindingPostProcessor
 * @see EnableConfigurationProperties
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Indexed
public @interface ConfigurationProperties {

	/**
	 * 可绑定到此对象的有效属性前缀，{@link #prefix()} 的同义词。
	 * 有效前缀由一个或多个以点分隔的单词组成（如 {@code "acme.system.feature"}）。
	 *
	 * @return 要绑定的属性前缀
	 */
	@AliasFor("prefix")
	String value() default "";

	/**
	 * 可绑定到此对象的有效属性前缀，{@link #value()} 的同义词。
	 * 有效前缀由一个或多个以点分隔的单词组成（如 {@code "acme.system.feature"}）。
	 *
	 * @return 要绑定的属性前缀
	 */
	@AliasFor("value")
	String prefix() default "";

	/**
	 * 绑定到此对象时是否忽略无效字段。
	 * “无效”指所用绑定器认为无效，通常指类型错误或无法强制转换为正确类型的字段。
	 *
	 * @return 标志值（默认 false）
	 */
	boolean ignoreInvalidFields() default false;

	/**
	 * 绑定到此对象时是否忽略未知字段。
	 * 未知字段可能是 Properties 配置错误的信号。
	 *
	 * @return 标志值（默认 true）
	 */
	boolean ignoreUnknownFields() default true;

}
