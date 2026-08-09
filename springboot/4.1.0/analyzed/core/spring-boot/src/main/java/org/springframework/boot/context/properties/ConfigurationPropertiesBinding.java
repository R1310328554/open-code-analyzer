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

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

/**
 * 用于配置 {@link ConfigurationProperties @ConfigurationProperties} 绑定所需 Bean
 * （如 Converter）的限定符。
 * <p>
 * 声明 {@code @ConfigurationPropertiesBinding} Bean 的 {@link Bean @Bean} 方法应为 {@code static}，
 * 以避免产生“bean is not eligible for getting processed by all BeanPostProcessors”警告。
 *
 * @author Dave Syer
 * @since 1.3.0
 */
@Qualifier(ConfigurationPropertiesBinding.VALUE)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigurationPropertiesBinding {

	/**
	 * {@link Qualifier @Qualifier} 的具体值。
	 */
	String VALUE = "org.springframework.boot.context.properties.ConfigurationPropertiesBinding";

}
