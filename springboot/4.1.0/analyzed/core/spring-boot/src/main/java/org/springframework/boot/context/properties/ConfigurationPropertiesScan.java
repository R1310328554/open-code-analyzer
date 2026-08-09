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

import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

/**
 * 配置扫描 {@link ConfigurationProperties @ConfigurationProperties} 类时使用的基础包。
 * 可通过 {@link #basePackageClasses()}、{@link #basePackages()} 或其别名 {@link #value()}
 * 指定要扫描的包；未指定时从标注此注解的类所在包开始扫描。
 * <p>
 * 注意：标注或元标注 {@link Component @Component} 的类不会被此注解拾取。
 *
 * @author Madhura Bhave
 * @since 2.2.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ConfigurationPropertiesScanRegistrar.class)
@EnableConfigurationProperties
public @interface ConfigurationPropertiesScan {

	/**
	 * {@link #basePackages()} 属性的别名，使注解声明更简洁，例如：
	 * {@code @ConfigurationPropertiesScan("org.my.pkg")} 而非
	 * {@code @ConfigurationPropertiesScan(basePackages="org.my.pkg")}。
	 *
	 * @return 要扫描的基础包
	 */
	@AliasFor("basePackages")
	String[] value() default {};

	/**
	 * 扫描配置属性的基础包。{@link #value()} 是此属性的别名（且互斥）。
	 * <p>
	 * 可使用 {@link #basePackageClasses()} 作为基于字符串包名的类型安全替代。
	 *
	 * @return 要扫描的基础包
	 */
	@AliasFor("value")
	String[] basePackages() default {};

	/**
	 * 指定扫描配置属性包的类型安全替代 {@link #basePackages()}。
	 * 将扫描每个指定类所在的包。
	 * <p>
	 * 可在每个包中创建仅被此属性引用的无操作标记类或接口。
	 *
	 * @return 要扫描的基础包中的类
	 */
	Class<?>[] basePackageClasses() default {};

}
