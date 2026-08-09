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

/**
 * 标记 {@link ConfigurationProperties @ConfigurationProperties} 对象中某个 getter 已弃用。
 * 此注解不影响实际绑定过程，但供 {@code spring-boot-configuration-processor}
 * 添加弃用元数据。
 * <p>
 * 此注解<strong>必须</strong>标注在已弃用元素的 getter 上。
 *
 * @author Phillip Webb
 * @author Scott Frederick
 * @since 1.3.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DeprecatedConfigurationProperty {

	/**
	 * 弃用原因。
	 *
	 * @return 弃用原因
	 */
	String reason() default "";

	/**
	 * 应改用的字段（若有）。
	 *
	 * @return 替代字段
	 */
	String replacement() default "";

	/**
	 * 属性开始弃用的版本。
	 *
	 * @return 版本号
	 */
	String since() default "";

}
