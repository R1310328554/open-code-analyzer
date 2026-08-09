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

package org.springframework.format.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明字段或方法参数应按数字格式化。
 *
 * <p>支持按样式或自定义模式字符串格式化。
 * 可应用于任何 JDK {@code Number} 类型，例如 {@code Double} 和 {@code Long}。
 *
 * <p>对于基于样式的格式化，将 {@link #style} 属性设为所需的 {@link Style}。
 * 对于自定义格式化，将 {@link #pattern} 属性设为所需的数字模式，例如 {@code "#,###.##"}。
 *
 * <p>各属性互斥，因此每个注解只应设置一个属性（选择最符合您格式化需求的那个）。
 * 指定 {@link #pattern} 属性时，其优先级高于 {@link #style} 属性。
 * 未指定任何注解属性时，默认应用基于样式的格式：对大多数被注解类型为通用数字格式，
 * 对货币类型则为货币格式，具体取决于被注解字段或方法参数的类型。
 *
 * @author Keith Donald
 * @author Juergen Hoeller
 * @since 3.0
 * @see java.text.NumberFormat
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
public @interface NumberFormat {

	/**
	 * 用于格式化字段或方法参数的样式模式。
	 * <p>对大多数被注解类型默认为 {@link Style#DEFAULT}（通用数字格式化），
	 * 货币类型除外，其默认为货币格式化。
	 * <p>若希望按不同于默认样式的通用样式格式化字段或方法参数，请设置此属性。
	 */
	Style style() default Style.DEFAULT;

	/**
	 * 用于格式化字段或方法参数的自定义模式。
	 * <p>默认为空字符串，表示未指定自定义模式。
	 * <p>若希望按样式无法表示的自定义数字模式格式化字段或方法参数，请设置此属性。
	 */
	String pattern() default "";


	/**
	 * 常用的数字格式样式。
	 */
	enum Style {

		/**
		 * 被注解类型的默认格式：通常为 'number'，货币类型（例如 {@code javax.money.MonetaryAmount}）可能为 'currency'。
		 * @since 4.2
		 */
		DEFAULT,

		/**
		 * 当前区域设置的通用数字格式。
		 */
		NUMBER,

		/**
		 * 当前区域设置的百分比格式。
		 */
		PERCENT,

		/**
		 * 当前区域设置的货币格式。
		 */
		CURRENCY
	}

}
