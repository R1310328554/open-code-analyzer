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

import java.lang.reflect.Method;
import java.util.Properties;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.core.ResolvableType;
import org.springframework.util.ClassUtils;

/**
 * 基础的 {@link AutowireCandidateResolver}：当依赖声明为泛型类型时
 * （例如 {@code Repository<Customer>}），对候选 Bean 的类型执行完整泛型匹配。
 *
 * <p>这是 {@link org.springframework.beans.factory.annotation.QualifierAnnotationAutowireCandidateResolver}
 * 的基类，在本层提供所有非基于注解的解析步骤实现。
 *
 * @author Juergen Hoeller
 * @since 4.0
 */
public class GenericTypeAwareAutowireCandidateResolver extends SimpleAutowireCandidateResolver
		implements BeanFactoryAware, Cloneable {

	/** 所属的 BeanFactory，用于解析候选 Bean 类型。 */
	private @Nullable BeanFactory beanFactory;


	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	protected final @Nullable BeanFactory getBeanFactory() {
		return this.beanFactory;
	}


	@Override
	public boolean isAutowireCandidate(BeanDefinitionHolder bdHolder, DependencyDescriptor descriptor) {
		if (!super.isAutowireCandidate(bdHolder, descriptor)) {
			// 若明确为 false，则不再进行其他检查
			return false;
		}
		return checkGenericTypeMatch(bdHolder, descriptor);
	}

	/**
	 * 将给定依赖类型及其泛型信息与候选 Bean 定义进行匹配。
	 */
	@SuppressWarnings("NullAway") // Dataflow analysis limitation
	protected boolean checkGenericTypeMatch(BeanDefinitionHolder bdHolder, DependencyDescriptor descriptor) {
		ResolvableType dependencyType = descriptor.getResolvableType();
		if (dependencyType.getType() instanceof Class) {
			// 无泛型参数 -> 已知为 Class 类型匹配，无需再次检查
			return true;
		}

		ResolvableType targetType = null;
		boolean cacheType = false;
		RootBeanDefinition rbd = null;
		if (bdHolder.getBeanDefinition() instanceof RootBeanDefinition rootBeanDef) {
			rbd = rootBeanDef;
		}
		if (rbd != null) {
			targetType = rbd.targetType;
			if (targetType == null) {
				cacheType = true;
				// 首先检查工厂方法返回类型（如适用）
				targetType = getReturnTypeForFactoryMethod(rbd, descriptor);
				if (targetType == null) {
					RootBeanDefinition dbd = getResolvedDecoratedDefinition(rbd);
					if (dbd != null) {
						targetType = dbd.targetType;
						if (targetType == null) {
							targetType = getReturnTypeForFactoryMethod(dbd, descriptor);
						}
					}
				}
			}
		}

		if (targetType == null) {
			// 常规情况：普通 Bean 实例，且 BeanFactory 可用
			if (this.beanFactory != null) {
				Class<?> beanType = this.beanFactory.getType(bdHolder.getBeanName());
				if (beanType != null) {
					targetType = ResolvableType.forClass(ClassUtils.getUserClass(beanType));
				}
			}
			// 回退：未设置 BeanFactory，或无法通过其解析类型
			// -> 在适用时尽力与目标类匹配
			if (targetType == null && rbd != null && rbd.hasBeanClass() && rbd.getFactoryMethodName() == null) {
				Class<?> beanClass = rbd.getBeanClass();
				if (!FactoryBean.class.isAssignableFrom(beanClass)) {
					targetType = ResolvableType.forClass(ClassUtils.getUserClass(beanClass));
				}
			}
		}

		if (targetType == null) {
			return true;
		}
		if (cacheType) {
			rbd.targetType = targetType;
		}

		// 预声明的目标类型：对于泛型 FactoryBean 类型，
		// 在匹配非 FactoryBean 类型时展开嵌套泛型
		Class<?> targetClass = targetType.resolve();
		if (targetClass != null && FactoryBean.class.isAssignableFrom(targetClass)) {
			Class<?> classToMatch = dependencyType.resolve();
			if (classToMatch != null && !FactoryBean.class.isAssignableFrom(classToMatch) &&
					!classToMatch.isAssignableFrom(targetClass)) {
				targetType = targetType.getGeneric();
				if (descriptor.fallbackMatchAllowed()) {
					// 与 FactoryBean 对象的基于 Class 的延迟类型判定路径保持一致
					targetType = ResolvableType.forClass(targetType.resolve());
				}
			}
		}

		if (descriptor.fallbackMatchAllowed()) {
			// 回退匹配允许不可解析的泛型，例如普通 HashMap 匹配 Map<String,String>；
			// 也务实允许 java.util.Properties 匹配任意 Map（尽管形式上为 Map<Object,Object>，
			// 但通常被视为 Map<String,String>）
			if (targetType.hasUnresolvableGenerics()) {
				return dependencyType.isAssignableFromResolvedPart(targetType);
			}
			else if (targetType.resolve() == Properties.class) {
				return true;
			}
		}
		// 执行复杂泛型类型的完整匹配
		return dependencyType.isAssignableFrom(targetType);
	}

	protected @Nullable RootBeanDefinition getResolvedDecoratedDefinition(RootBeanDefinition rbd) {
		BeanDefinitionHolder decDef = rbd.getDecoratedDefinition();
		if (decDef != null && this.beanFactory instanceof ConfigurableListableBeanFactory clbf) {
			if (clbf.containsBeanDefinition(decDef.getBeanName())) {
				BeanDefinition dbd = clbf.getMergedBeanDefinition(decDef.getBeanName());
				if (dbd instanceof RootBeanDefinition rootBeanDef) {
					return rootBeanDef;
				}
			}
		}
		return null;
	}

	protected @Nullable ResolvableType getReturnTypeForFactoryMethod(RootBeanDefinition rbd, DependencyDescriptor descriptor) {
		// 通常应为各类工厂方法设置，因为 BeanFactory 在调用 AutowireCandidateResolver 前会预解析
		ResolvableType returnType = rbd.factoryMethodReturnType;
		if (returnType == null) {
			Method factoryMethod = rbd.getResolvedFactoryMethod();
			if (factoryMethod != null) {
				returnType = ResolvableType.forMethodReturnType(factoryMethod);
			}
		}
		if (returnType != null) {
			Class<?> resolvedClass = returnType.resolve();
			if (resolvedClass != null && descriptor.getDependencyType().isAssignableFrom(resolvedClass)) {
				// 仅当返回类型足以表达依赖时才使用工厂方法元数据；
				// 否则容器可能已注册的单例实例类型反而更匹配
				return returnType;
			}
		}
		return null;
	}


	/**
	 * 本实现通过标准 {@link Cloneable} 机制克隆所有实例字段，
	 * 允许克隆后通过新的 {@link #setBeanFactory} 调用重新配置。
	 * @see #clone()
	 */
	@Override
	public AutowireCandidateResolver cloneIfNecessary() {
		try {
			return (AutowireCandidateResolver) clone();
		}
		catch (CloneNotSupportedException ex) {
			throw new IllegalStateException(ex);
		}
	}

}
