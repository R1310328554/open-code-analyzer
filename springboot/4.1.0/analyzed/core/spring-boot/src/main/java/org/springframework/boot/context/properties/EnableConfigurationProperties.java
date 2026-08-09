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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * 启用对 {@link ConfigurationProperties @ConfigurationProperties} 标注 Bean 的支持。
 * {@code @ConfigurationProperties} Bean 可按常规方式注册（例如通过 {@link Bean @Bean} 方法），
 * 也可为方便起见直接在此注解上指定。
 *
 * @author Dave Syer
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(EnableConfigurationPropertiesRegistrar.class)
public @interface EnableConfigurationProperties {

	/**
	 * 配置属性校验器的 Bean 名称。
	 *
	 * @since 2.2.0
	 */
	String VALIDATOR_BEAN_NAME = "configurationPropertiesValidator";

	/**
	 * 快速向 Spring 注册 {@link ConfigurationProperties @ConfigurationProperties}
	 * 标注 Bean 的便捷方式。无论此值如何，标准 Spring Bean 仍会被扫描。
	 *
	 * @return 要注册的 {@code @ConfigurationProperties} 标注 Bean
	 */
	Class<?>[] value() default {};

}
