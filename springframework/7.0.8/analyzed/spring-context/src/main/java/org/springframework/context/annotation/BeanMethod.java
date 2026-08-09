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

/* ===== [OCA 中文解析] =====
文件意图总览

表示配置类中标注了 {@link Bean} 的方法，并在解析阶段执行语义校验。
===== [OCA 中文解析结束] ===== */
package org.springframework.context.annotation;

import java.util.Map;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.parsing.Problem;
import org.springframework.beans.factory.parsing.ProblemReporter;
import org.springframework.core.type.MethodMetadata;

/* ===== [OCA 中文解析] =====
class BeanMethod — 意图说明

{@link ConfigurationMethod} 子类，校验 {@code @Bean} 方法是否符合配置类语义约束。

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
===== [OCA 中文解析结束] ===== */
/**
 * Represents a {@link Configuration @Configuration} class method annotated with
 * {@link Bean @Bean}.
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 3.0
 * @see ConfigurationClass
 * @see ConfigurationClassParser
 * @see ConfigurationClassBeanDefinitionReader
 */
final class BeanMethod extends ConfigurationMethod {

	BeanMethod(MethodMetadata metadata, ConfigurationClass configurationClass) {
		super(metadata, configurationClass);
	}


	@Override
	@SuppressWarnings("NullAway") // Reflection
	public void validate(ProblemReporter problemReporter) {
		if (getMetadata().getAnnotationAttributes(Autowired.class.getName()) != null) {
			// [OCA] 声明了 @Autowired：语义冲突，@Bean 方法参数本就自动装配，而非 setter 式注入。
			problemReporter.error(new AutowiredDeclaredMethodError());
		}

		if ("void".equals(getMetadata().getReturnTypeName())) {
			// [OCA] 声明为 void：可能误用 @Bean，本意或许是初始化方法？
			problemReporter.error(new VoidDeclaredMethodError());
		}

		if (getMetadata().isStatic()) {
			// [OCA] 静态 @Bean 方法无进一步约束，直接返回。
			return;
		}

		Map<String, @Nullable Object> attributes =
				getConfigurationClass().getMetadata().getAnnotationAttributes(Configuration.class.getName());
		if (attributes != null && (Boolean) attributes.get("proxyBeanMethods") && !getMetadata().isOverridable()) {
			// [OCA] 启用 CGLIB 代理时，实例 @Bean 方法必须可覆盖（不能 private/final）。
			problemReporter.error(new NonOverridableMethodError());
		}
	}

	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof BeanMethod that &&
				this.configurationClass.equals(that.configurationClass) &&
				getLocalMethodIdentifier(this.metadata).equals(getLocalMethodIdentifier(that.metadata))));
	}

	@Override
	public int hashCode() {
		return this.configurationClass.hashCode() * 31 + getLocalMethodIdentifier(this.metadata).hashCode();
	}

	@Override
	public String toString() {
		return "BeanMethod: " + this.metadata;
	}


	private static String getLocalMethodIdentifier(MethodMetadata metadata) {
		String metadataString = metadata.toString();
		int index = metadataString.indexOf(metadata.getDeclaringClassName());
		return (index >= 0 ? metadataString.substring(index + metadata.getDeclaringClassName().length()) :
				metadataString);
	}


	private class AutowiredDeclaredMethodError extends Problem {

		AutowiredDeclaredMethodError() {
			super("@Bean method '%s' must not be declared as autowired; remove the method-level @Autowired annotation."
					.formatted(getMetadata().getMethodName()), getResourceLocation());
		}
	}


	private class VoidDeclaredMethodError extends Problem {

		VoidDeclaredMethodError() {
			super("@Bean method '%s' must not be declared as void; change the method's return type or its annotation."
					.formatted(getMetadata().getMethodName()), getResourceLocation());
		}
	}


	private class NonOverridableMethodError extends Problem {

		NonOverridableMethodError() {
			super("@Bean method '%s' must not be private or final; change the method's modifiers to continue."
					.formatted(getMetadata().getMethodName()), getResourceLocation());
		}
	}

}
