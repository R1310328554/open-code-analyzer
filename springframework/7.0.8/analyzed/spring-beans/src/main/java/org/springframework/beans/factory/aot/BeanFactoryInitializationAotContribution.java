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

import org.springframework.aot.generate.GenerationContext;

/**
 * 来自 {@link BeanFactoryInitializationAotProcessor} 的 AOT 贡献，用于初始化 bean 工厂。
 *
 * <p>注意：实现此接口的 bean 在 AOT 处理期间不会生成注册方法，
 * 除非它们同时实现了
 * {@link org.springframework.beans.factory.aot.BeanRegistrationExcludeFilter}。
 *
 * @author Phillip Webb
 * @since 6.0
 * @see BeanFactoryInitializationAotProcessor
 */
@FunctionalInterface
public interface BeanFactoryInitializationAotContribution {

	/**
	 * 将此贡献应用到给定的 {@link BeanFactoryInitializationCode}。
	 * @param generationContext 当前活跃的生成上下文
	 * @param beanFactoryInitializationCode bean 工厂初始化代码
	 */
	void applyTo(GenerationContext generationContext,
			BeanFactoryInitializationCode beanFactoryInitializationCode);

}
