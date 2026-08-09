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

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * 通过处理 {@link ConfigurableListableBeanFactory} 实例，
 * 为 Bean 工厂初始化提供 AOT 贡献的处理器。
 *
 * <p>{@code BeanFactoryInitializationAotProcessor} 实现类可注册在
 * {@value AotServices#FACTORIES_RESOURCE_LOCATION} 资源中，或作为 Bean 注册。
 *
 * <p>在已注册的 Bean 上使用本接口，会导致该 Bean <em>及其</em>所有依赖
 * 在 AOT 处理期间被初始化。通常建议仅将本接口用于基础设施 Bean，
 * 例如依赖较少且已在 Bean 工厂生命周期早期完成初始化的
 * {@link BeanFactoryPostProcessor}。若此类 Bean 通过工厂方法注册，
 * 请确保将其声明为 {@code static}，以免必须初始化其所在封闭类。
 *
 * <p>实现本接口的组件本身不会被贡献。
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @since 6.0
 * @see BeanFactoryInitializationAotContribution
 */
@FunctionalInterface
public interface BeanFactoryInitializationAotProcessor {

	/**
	 * 提前处理给定的 {@link ConfigurableListableBeanFactory} 实例，
	 * 并返回贡献或 {@code null}。
	 * <p>处理器可自由采用任何技术分析给定的 Bean 工厂。最常见的是通过反射
	 * 查找用于贡献的字段或方法。贡献通常生成源代码或资源文件，
	 * 供 AOT 优化后的应用运行时使用。
	 * <p>若给定的 Bean 工厂与处理器无关，本方法应返回 {@code null} 贡献。
	 * @param beanFactory 待处理的 Bean 工厂
	 * @return {@link BeanFactoryInitializationAotContribution} 或 {@code null}
	 */
	@Nullable BeanFactoryInitializationAotContribution processAheadOfTime(ConfigurableListableBeanFactory beanFactory);

}
