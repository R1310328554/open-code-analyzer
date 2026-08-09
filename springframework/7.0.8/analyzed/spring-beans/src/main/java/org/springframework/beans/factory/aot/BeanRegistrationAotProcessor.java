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

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.RegisteredBean;

/**
 * 通过处理 {@link RegisteredBean} 实例，为 Bean 注册提供 AOT 贡献的处理器。
 *
 * <p>{@code BeanRegistrationAotProcessor} 实现类可注册在
 * {@value AotServices#FACTORIES_RESOURCE_LOCATION} 资源中，或作为 Bean 注册。
 *
 * <p>在已注册的 Bean 上使用本接口，会导致该 Bean <em>及其</em>所有依赖
 * 在 AOT 处理期间被初始化。通常建议仅将本接口用于基础设施 Bean，
 * 例如依赖较少且已在 Bean 工厂生命周期早期完成初始化的
 * {@link BeanPostProcessor}。若此类 Bean 通过工厂方法注册，
 * 请确保将其声明为 {@code static}，以免必须初始化其所在封闭类。
 *
 * <p>AOT 处理器会以优化后的编排（通常为生成代码）替代其常规运行时行为。
 * 因此，默认情况下，实现本接口的组件不会被贡献。若实现本接口的组件
 * 仍需在运行时被调用，可重写 {@link #isBeanExcludedFromAotProcessing}。
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @since 6.0
 * @see BeanRegistrationAotContribution
 */
@FunctionalInterface
public interface BeanRegistrationAotProcessor {

	/**
	 * 可在 {@link org.springframework.beans.factory.config.BeanDefinition} 上
	 * {@link org.springframework.core.AttributeAccessor#setAttribute 设置}的属性名，
	 * 用于标记其注册不应被处理。
	 * @since 6.2
	 */
	String IGNORE_REGISTRATION_ATTRIBUTE = "aotProcessingIgnoreRegistration";

	/**
	 * 提前处理给定的 {@link RegisteredBean} 实例，并返回贡献或 {@code null}。
	 * <p>
	 * 处理器可自由采用任何技术分析给定实例。最常见的是通过反射
	 * 查找用于贡献的字段或方法。贡献通常生成源代码或资源文件，
	 * 供 AOT 优化后的应用运行时使用。
	 * <p>
	 * 若给定实例与处理器无关，应返回 {@code null} 贡献。
	 * @param registeredBean 待处理的已注册 Bean
	 * @return {@link BeanRegistrationAotContribution} 或 {@code null}
	 */
	@Nullable BeanRegistrationAotContribution processAheadOfTime(RegisteredBean registeredBean);

	/**
	 * 返回与此处理器关联的 Bean 实例是否应从 AOT 处理中排除。
	 * 默认返回 {@code true} 以自动排除该 Bean；若需要写入其定义，
	 * 可重写本方法返回 {@code false}。
	 * @return 是否应从 AOT 处理中排除该 Bean
	 * @see BeanRegistrationExcludeFilter
	 */
	default boolean isBeanExcludedFromAotProcessing() {
		return true;
	}

}
