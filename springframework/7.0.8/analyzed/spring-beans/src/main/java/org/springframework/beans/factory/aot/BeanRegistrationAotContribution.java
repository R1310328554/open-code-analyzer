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

package org.springframework.beans.factory.aot;

import java.util.function.UnaryOperator;

import org.jspecify.annotations.Nullable;

import org.springframework.aot.generate.GenerationContext;
import org.springframework.util.Assert;

/**
 * 来自 {@link BeanRegistrationAotProcessor} 的 AOT 贡献，
 * 用于注册单个 Bean 定义。
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @since 6.0
 * @see BeanRegistrationAotProcessor
 */
@FunctionalInterface
public interface BeanRegistrationAotContribution {

	/**
	 * 自定义将用于生成 Bean 注册代码的 {@link BeanRegistrationCodeFragments}。
	 * 若默认代码生成不适用，可使用自定义代码片段。
	 * @param generationContext 生成上下文
	 * @param codeFragments 现有代码片段
	 * @return 要使用的代码片段，可为原实例或包装器
	 */
	default BeanRegistrationCodeFragments customizeBeanRegistrationCodeFragments(
			GenerationContext generationContext, BeanRegistrationCodeFragments codeFragments) {

		return codeFragments;
	}

	/**
	 * 将本贡献应用到给定的 {@link BeanRegistrationCode}。
	 * @param generationContext 生成上下文
	 * @param beanRegistrationCode 生成的注册代码
	 */
	void applyTo(GenerationContext generationContext, BeanRegistrationCode beanRegistrationCode);

	/**
	 * 创建自定义 {@link BeanRegistrationCodeFragments} 的
	 * {@link BeanRegistrationAotContribution}。通常与
	 * {@link BeanRegistrationCodeFragmentsDecorator} 的扩展配合使用，
	 * 以覆盖特定回调。
	 * @param defaultCodeFragments 默认代码片段
	 * @return 新的 {@link BeanRegistrationAotContribution} 实例
	 * @see BeanRegistrationCodeFragmentsDecorator
	 */
	static BeanRegistrationAotContribution withCustomCodeFragments(
			UnaryOperator<BeanRegistrationCodeFragments> defaultCodeFragments) {

		Assert.notNull(defaultCodeFragments, "'defaultCodeFragments' must not be null");

		return new BeanRegistrationAotContribution() {
			@Override
			public BeanRegistrationCodeFragments customizeBeanRegistrationCodeFragments(
					GenerationContext generationContext, BeanRegistrationCodeFragments codeFragments) {
				return defaultCodeFragments.apply(codeFragments);
			}
			@Override
			public void applyTo(GenerationContext generationContext, BeanRegistrationCode beanRegistrationCode) {
			}
		};
	}

	/**
	 * 创建依次应用第一个贡献和第二个贡献的合并贡献。
	 * 任一贡献可为 {@code null} 以被忽略；若两个输入均为 {@code null}，
	 * 则合并结果为 {@code null}。
	 * @param a 第一个贡献
	 * @param b 第二个贡献
	 * @return 两个贡献的串联结果；若两者均为 {@code null} 则返回 {@code null}
	 * @since 6.1
	 */
	static @Nullable BeanRegistrationAotContribution concat(@Nullable BeanRegistrationAotContribution a,
			@Nullable BeanRegistrationAotContribution b) {

		if (a == null) {
			return b;
		}
		if (b == null) {
			return a;
		}
		return (generationContext, beanRegistrationCode) -> {
			a.applyTo(generationContext, beanRegistrationCode);
			b.applyTo(generationContext, beanRegistrationCode);
		};
	}

}
