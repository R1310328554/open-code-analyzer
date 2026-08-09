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

import org.springframework.context.annotation.Import;

/**
 * 将包注册到 {@link AutoConfigurationPackages}。
 * 若未指定 {@link #basePackages basePackages} 或 {@link #basePackageClasses basePackageClasses}，
 * 则注册被注解类所在的包。
 *
 * @author Phillip Webb
 * @since 1.3.0
 * @see AutoConfigurationPackages
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(AutoConfigurationPackages.Registrar.class)
public @interface AutoConfigurationPackage {

	/**
	 * 应注册到 {@link AutoConfigurationPackages} 的基础包。
	 * <p>
	 * 可使用 {@link #basePackageClasses} 作为基于字符串包名的类型安全替代方案。
	 * @return 基础包名
	 * @since 2.3.0
	 */
	String[] basePackages() default {};

	/**
	 * {@link #basePackages} 的类型安全替代方案，用于指定要注册到
	 * {@link AutoConfigurationPackages} 的包。
	 * <p>
	 * 可在每个包中创建一个仅用于被本属性引用的空标记类或接口。
	 * @return 基础包对应的类
	 * @since 2.3.0
	 */
	Class<?>[] basePackageClasses() default {};

}
