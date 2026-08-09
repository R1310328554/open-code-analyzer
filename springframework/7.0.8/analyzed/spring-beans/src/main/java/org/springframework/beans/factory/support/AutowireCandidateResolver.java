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

package org.springframework.beans.factory.support;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.DependencyDescriptor;

/**
 * 判断特定 Bean 定义是否可作为特定依赖的自动装配候选者的策略接口。
 *
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @since 2.5
 */
public interface AutowireCandidateResolver {

	/**
	 * 判断给定 Bean 定义是否可作为给定依赖的自动装配候选者。
	 * <p>默认实现检查
	 * {@link org.springframework.beans.factory.config.BeanDefinition#isAutowireCandidate()}。
	 * @param bdHolder 包含 Bean 名称和别名的 Bean 定义
	 * @param descriptor 目标方法参数或字段的描述符
	 * @return 该 Bean 定义是否可作为自动装配候选者
	 * @see org.springframework.beans.factory.config.BeanDefinition#isAutowireCandidate()
	 */
	default boolean isAutowireCandidate(BeanDefinitionHolder bdHolder, DependencyDescriptor descriptor) {
		return bdHolder.getBeanDefinition().isAutowireCandidate();
	}

	/**
	 * 判断给定描述符是否实际上为必需依赖。
	 * <p>默认实现检查 {@link DependencyDescriptor#isRequired()}。
	 * @param descriptor 目标方法参数或字段的描述符
	 * @return 描述符是否标记为必需，或以其他方式（如参数注解）表明非必需
	 * @since 5.0
	 * @see DependencyDescriptor#isRequired()
	 */
	default boolean isRequired(DependencyDescriptor descriptor) {
		return descriptor.isRequired();
	}

	/**
	 * 判断给定描述符是否声明了超出类型的限定符
	 *（通常为某种注解，但不一定）。
	 * <p>默认实现返回 {@code false}。
	 * @param descriptor 目标方法参数或字段的描述符
	 * @return 描述符是否声明了限定符，从而在类型匹配之外进一步缩小候选范围
	 * @since 5.1
	 * @see org.springframework.beans.factory.annotation.QualifierAnnotationAutowireCandidateResolver#hasQualifier
	 */
	default boolean hasQualifier(DependencyDescriptor descriptor) {
		return false;
	}

	/**
	 * 判断是否为给定依赖建议了目标 Bean 名称
	 *（通常——但不一定——由单值限定符声明）。
	 * @param descriptor 目标方法参数或字段的描述符
	 * @return 限定符值（若有）
	 * @since 6.2
	 */
	default @Nullable String getSuggestedName(DependencyDescriptor descriptor) {
		return null;
	}

	/**
	 * 判断是否为给定依赖建议了默认值。
	 * <p>默认实现直接返回 {@code null}。
	 * @param descriptor 目标方法参数或字段的描述符
	 * @return 建议的值（通常为表达式字符串），未找到则返回 {@code null}
	 * @since 3.0
	 */
	default @Nullable Object getSuggestedValue(DependencyDescriptor descriptor) {
		return null;
	}

	/**
	 * 若注入点要求延迟解析，则构建用于延迟解析实际依赖目标的代理。
	 * <p>默认实现直接返回 {@code null}。
	 * @param descriptor 目标方法参数或字段的描述符
	 * @param beanName 包含该注入点的 Bean 名称
	 * @return 用于延迟解析实际依赖目标的代理，若应直接解析则返回 {@code null}
	 * @since 4.0
	 */
	default @Nullable Object getLazyResolutionProxyIfNecessary(DependencyDescriptor descriptor, @Nullable String beanName) {
		return null;
	}

	/**
	 * 若注入点要求延迟解析，则确定依赖目标的代理类。
	 * <p>默认实现直接返回 {@code null}。
	 * @param descriptor 目标方法参数或字段的描述符
	 * @param beanName 包含该注入点的 Bean 名称
	 * @return 用于延迟解析依赖目标的代理类（若有）
	 * @since 6.0
	 */
	default @Nullable Class<?> getLazyResolutionProxyClass(DependencyDescriptor descriptor, @Nullable String beanName) {
		return null;
	}

	/**
	 * 如有必要，返回本解析器实例的克隆，保留其本地配置，
	 * 并允许克隆实例关联到新的 Bean 工厂；若无此类状态则返回本实例。
	 * <p>默认实现通过默认类构造器创建独立实例，假定无特定配置状态需要复制。
	 * 子类可覆盖此方法以处理自定义配置状态，或通过标准 {@link Cloneable} 支持
	 *（如 Spring 自带可配置的 {@code AutowireCandidateResolver} 变体），
	 * 或直接返回 {@code this}（如 {@link SimpleAutowireCandidateResolver}）。
	 * @since 5.2.7
	 * @see GenericTypeAwareAutowireCandidateResolver#cloneIfNecessary()
	 * @see DefaultListableBeanFactory#copyConfigurationFrom
	 */
	default AutowireCandidateResolver cloneIfNecessary() {
		return BeanUtils.instantiateClass(getClass());
	}

}
