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

package org.springframework.context.aot;

import org.jspecify.annotations.Nullable;

import org.springframework.aot.generate.GenerationContext;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.beans.factory.aot.BeanRegistrationAotContribution;
import org.springframework.beans.factory.aot.BeanRegistrationAotProcessor;
import org.springframework.beans.factory.aot.BeanRegistrationCode;
import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.core.KotlinDetector;

/**
 * 为 Kotlin 反射添加额外运行时提示的 AOT {@code BeanRegistrationAotProcessor}。
 *
 * @author Sebastien Deleuze
 * @since 6.0.4
 */
class KotlinReflectionBeanRegistrationAotProcessor implements BeanRegistrationAotProcessor {

	@Override
	public @Nullable BeanRegistrationAotContribution processAheadOfTime(RegisteredBean registeredBean) {
		Class<?> beanClass = registeredBean.getBeanClass();
		if (KotlinDetector.isKotlinType(beanClass)) {
			return new AotContribution(beanClass);
		}
		return null;
	}


	private static class AotContribution implements BeanRegistrationAotContribution {

		/** 需要注册反射提示的 Kotlin Bean 类。 */
		private final Class<?> beanClass;

		public AotContribution(Class<?> beanClass) {
			this.beanClass = beanClass;
		}

		@Override
		public void applyTo(GenerationContext generationContext, BeanRegistrationCode beanRegistrationCode) {
			registerHints(this.beanClass, generationContext.getRuntimeHints());
		}

		/** 递归为 Kotlin 类型及其父类、外部类注册反射提示。 */
		private void registerHints(Class<?> type, RuntimeHints runtimeHints) {
			if (KotlinDetector.isKotlinType(type)) {
				runtimeHints.reflection().registerType(type);
			}
			Class<?> superClass = type.getSuperclass();
			if (superClass != null) {
				registerHints(superClass, runtimeHints);
			}
			Class<?> enclosingClass = type.getEnclosingClass();
			if (enclosingClass != null) {
				runtimeHints.reflection().registerType(enclosingClass);
			}
		}
	}

}
