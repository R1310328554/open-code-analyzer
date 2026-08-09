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

package org.springframework.boot.context.properties.bind;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于指定绑定不可变属性时的默认值的注解。也可用于嵌套属性，表示应始终绑定值（而非绑定 {@code null}）。
 * 仅当 {@link Binder} 使用的属性源中找不到该属性时，才会使用此注解中的值。例如，绑定
 * {@link org.springframework.boot.context.properties.ConfigurationProperties @ConfigurationProperties}
 * 时若属性存在于 {@link org.springframework.core.env.Environment} 中，即使属性值为空也不会使用默认值。
 * <p>
 * 注意：此注解不支持属性占位符解析，值必须为常量。
 *
 * @author Madhura Bhave
 * @author Pavel Anisimov
 * @since 2.2.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER })
@Documented
public @interface DefaultValue {

	/**
	 * 属性的默认值。集合或数组类属性可为值数组。
	 *
	 * @return 属性的默认值
	 */
	String[] value() default {};

}
