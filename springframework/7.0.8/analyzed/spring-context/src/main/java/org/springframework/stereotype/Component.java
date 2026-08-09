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

package org.springframework.stereotype;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表示被注解的类是<em>组件</em>。
 *
 * <p>使用基于注解的配置和类路径扫描时，
 * 此类被视为自动检测的候选。
 *
 * <p>组件可通过本注解的 {@link #value value} 属性
 * 可选地指定逻辑组件名。
 *
 * <p>其他类级别注解也可视为标识组件，
 * 通常是特殊类型的组件——例如 {@link Repository @Repository} 注解
 * 或 AspectJ 的 {@link org.aspectj.lang.annotation.Aspect @Aspect} 注解。
 * 但注意 {@code @Aspect} 注解不会自动使类具备类路径扫描资格。
 *
 * <p>任何以 {@code @Component} 元注解的注解均视为<em>构造型</em>注解，
 * 使被注解类具备类路径扫描资格。例如 {@link Service @Service}、
 * {@link Controller @Controller} 和 {@link Repository @Repository} 均为构造型注解。
 * 构造型注解也可通过 {@link org.springframework.core.annotation.AliasFor @AliasFor}
 * 覆盖本注解 {@link #value} 属性来配置逻辑组件名。
 *
 * <p>自 Spring Framework 6.1 起，通过约定（即无 {@code @AliasFor} 的
 * {@code String value()} 属性）配置构造型组件名称的支持已弃用，
 * 并将在未来版本中移除。因此，自定义构造型注解必须使用 {@code @AliasFor}
 * 为本注解 {@link #value} 属性声明显式别名。
 * 具体示例见 {@link Repository#value()} 和
 * {@link org.springframework.web.bind.annotation.ControllerAdvice#name()
 * ControllerAdvice.name()} 的源码声明。
 *
 * @author Mark Fisher
 * @author Sam Brannen
 * @since 2.5
 * @see Repository
 * @see Service
 * @see Controller
 * @see org.springframework.context.annotation.ClassPathBeanDefinitionScanner
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Indexed
public @interface Component {

	/**
	 * value 可表示逻辑组件名的建议，
	 * 在自动检测组件时转为 Spring Bean 名称。
	 * @return 建议的组件名（若有），否则为空 String
	 */
	String value() default "";

}
