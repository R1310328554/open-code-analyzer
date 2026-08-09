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

package org.springframework.boot.autoconfigure.condition;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Conditional;
import org.springframework.core.env.Environment;

/**
 * 检查指定属性是否具有特定布尔值的 {@link Conditional @Conditional}。
 * 默认情况下，属性必须存在于 {@link Environment} 中且等于 {@code true}。
 * 可通过 {@link #havingValue()} 和 {@link #matchIfMissing()} 属性进一步定制。
 * <p>
 * 若属性完全不在 {@link Environment} 中，则参考 {@link #matchIfMissing()} 属性。
 * 默认情况下缺失的属性不匹配。
 *
 * @author Phillip Webb
 * @since 3.5.0
 * @see ConditionalOnProperty
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Documented
@Conditional(OnPropertyCondition.class)
@Repeatable(ConditionalOnBooleanProperties.class)
public @interface ConditionalOnBooleanProperty {

	/**
	 * {@link #name()} 的别名。
	 * @return 属性名
	 */
	String[] value() default {};

	/**
	 * 应用于每个属性的前缀。若未指定，前缀会自动以点结尾。
	 * 有效前缀由一个或多个以点分隔的单词组成（例如 {@code "acme.system.feature"}）。
	 * @return 前缀
	 */
	String prefix() default "";

	/**
	 * 要测试的属性名。若已定义前缀，则应用于计算每个属性的完整键。
	 * 例如前缀为 {@code app.config}、某值为 {@code my-value} 时，完整键为
	 * {@code app.config.my-value}。
	 * <p>
	 * 使用短横线命名法指定每个属性，即全小写并以 {@code -} 分隔单词
	 * （例如 {@code my-long-property}）。
	 * <p>
	 * 若指定多个名称，所有属性都必须通过测试条件才匹配。
	 * @return 属性名
	 */
	String[] name() default {};

	/**
	 * 属性的期望值。若未指定，属性必须等于 {@code true}。
	 * @return 期望值
	 */
	boolean havingValue() default true;

	/**
	 * 指定属性未设置时条件是否应匹配。默认为 {@code false}。
	 * @return 属性缺失时是否匹配
	 */
	boolean matchIfMissing() default false;

}
