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

package org.springframework.beans.factory.annotation;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;

/**
 * 与 Spring 特有注解相关的 Bean 查找便捷方法，
 * 例如 Spring 的 {@link Qualifier @Qualifier}。
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 3.1.2
 * @see BeanFactoryUtils
 */
public abstract class BeanFactoryAnnotationUtils {

	/**
	 * 从给定 {@code BeanFactory} 中检索类型为 {@code T}、且声明了匹配给定限定符的全部 Bean
	 *（例如通过 {@code <qualifier>} 或 {@code @Qualifier}），
	 * 或者 Bean 名称本身与该限定符匹配的 Bean。
	 * @param beanFactory 获取目标 Bean 的工厂（也会搜索祖先）
	 * @param beanType 要检索的 Bean 类型
	 * @param qualifier 在全部类型匹配中用于筛选的限定符
	 * @return 匹配的 {@code T} 类型 Bean
	 * @throws BeansException 若任一匹配 Bean 无法创建
	 * @since 5.1.1
	 * @see BeanFactoryUtils#beansOfTypeIncludingAncestors(ListableBeanFactory, Class)
	 */
	public static <T> Map<String, T> qualifiedBeansOfType(
			ListableBeanFactory beanFactory, Class<T> beanType, String qualifier) throws BeansException {

		String[] candidateBeans = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(beanFactory, beanType);
		Map<String, T> result = new LinkedHashMap<>(4);
		for (String beanName : candidateBeans) {
			if (isQualifierMatch(qualifier::equals, beanName, beanFactory)) {
				result.put(beanName, beanFactory.getBean(beanName, beanType));
			}
		}
		return result;
	}

	/**
	 * 从给定 {@code BeanFactory} 中获取类型为 {@code T}、且声明了匹配给定限定符的单个 Bean
	 *（例如通过 {@code <qualifier>} 或 {@code @Qualifier}），
	 * 或者 Bean 名称本身与该限定符匹配的 Bean。
	 * @param beanFactory 获取目标 Bean 的工厂（也会搜索祖先）
	 * @param beanType 要检索的 Bean 类型
	 * @param qualifier 在多个类型匹配中用于筛选的限定符
	 * @return 匹配的 {@code T} 类型 Bean（永不为 {@code null}）
	 * @throws NoUniqueBeanDefinitionException 若找到多个匹配的 {@code T} 类型 Bean
	 * @throws NoSuchBeanDefinitionException 若找不到匹配的 {@code T} 类型 Bean
	 * @throws BeansException 若 Bean 无法创建
	 * @see BeanFactoryUtils#beanOfTypeIncludingAncestors(ListableBeanFactory, Class)
	 */
	public static <T> T qualifiedBeanOfType(BeanFactory beanFactory, Class<T> beanType, String qualifier)
			throws BeansException {

		Assert.notNull(beanFactory, "BeanFactory must not be null");

		if (beanFactory instanceof ListableBeanFactory lbf) {
			// 支持完整的限定符匹配
			return qualifiedBeanOfType(lbf, beanType, qualifier);
		}
		else if (beanFactory.containsBean(qualifier) && beanFactory.isTypeMatch(qualifier, beanType)) {
			// 回退：至少能按 Bean 名称找到目标
			return beanFactory.getBean(qualifier, beanType);
		}
		else {
			throw new NoSuchBeanDefinitionException(qualifier, "No matching " + beanType.getSimpleName() +
					" bean found for bean name '" + qualifier +
					"'! (Note: Qualifier matching not supported because given " +
					"BeanFactory does not implement ConfigurableListableBeanFactory.)");
		}
	}

	/**
	 * 从给定 {@code ListableBeanFactory} 中获取类型为 {@code T}、
	 * 且声明了匹配给定限定符的 Bean（例如 {@code <qualifier>} 或 {@code @Qualifier}）。
	 * @param beanFactory 获取目标 Bean 的工厂
	 * @param beanType 要检索的 Bean 类型
	 * @param qualifier 在多个类型匹配中用于筛选的限定符
	 * @return 匹配的 {@code T} 类型 Bean（永不为 {@code null}）
	 */
	private static <T> T qualifiedBeanOfType(ListableBeanFactory beanFactory, Class<T> beanType, String qualifier) {
		String[] candidateBeans = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(beanFactory, beanType);
		String matchingBean = null;
		for (String beanName : candidateBeans) {
			if (isQualifierMatch(qualifier::equals, beanName, beanFactory)) {
				if (matchingBean != null) {
					throw new NoUniqueBeanDefinitionException(beanType, matchingBean, beanName);
				}
				matchingBean = beanName;
			}
		}
		if (matchingBean != null) {
			return beanFactory.getBean(matchingBean, beanType);
		}
		else if (beanFactory.containsBean(qualifier) && beanFactory.isTypeMatch(qualifier, beanType)) {
			// 回退：至少能按 Bean 名称找到目标——多半是手动注册的单例
			return beanFactory.getBean(qualifier, beanType);
		}
		else {
			throw new NoSuchBeanDefinitionException(qualifier, "No matching " + beanType.getSimpleName() +
					" bean found for qualifier '" + qualifier + "' - neither qualifier match nor bean name match!");
		}
	}

	/**
	 * 获取给定带注解元素上的 {@link Qualifier#value() 限定符值}。
	 * @param annotatedElement 要内省的类、方法或参数
	 * @return 关联的限定符值；没有则为 {@code null}
	 * @since 6.2
	 */
	public static @Nullable String getQualifierValue(AnnotatedElement annotatedElement) {
		Qualifier qualifier = AnnotationUtils.getAnnotation(annotatedElement, Qualifier.class);
		return (qualifier != null ? qualifier.value() : null);
	}

	/**
	 * 检查指定名称的 Bean 是否声明了给定名称的限定符。
	 * @param qualifier 要匹配的限定符
	 * @param beanName 候选 Bean 的名称
	 * @param beanFactory 用于检索该命名 Bean 的工厂
	 * @return 若 Bean 定义（XML 场景）或 Bean 的工厂方法（{@code @Bean} 场景）
	 * 通过 {@code <qualifier>} / {@code @Qualifier} 定义了匹配的限定符值，则为 {@code true}
	 * @since 5.0
	 */
	public static boolean isQualifierMatch(
			Predicate<String> qualifier, String beanName, @Nullable BeanFactory beanFactory) {

		// 先快速匹配 Bean 名称或别名……
		if (qualifier.test(beanName)) {
			return true;
		}
		if (beanFactory != null) {
			for (String alias : beanFactory.getAliases(beanName)) {
				if (qualifier.test(alias)) {
					return true;
				}
			}
			try {
				Class<?> beanType = beanFactory.getType(beanName);
				if (beanFactory instanceof ConfigurableBeanFactory cbf) {
					BeanDefinition bd = cbf.getMergedBeanDefinition(beanName);
					// Bean 定义上是否有显式限定符元数据？（通常来自 XML）
					if (bd instanceof AbstractBeanDefinition abd) {
						AutowireCandidateQualifier candidate = abd.getQualifier(Qualifier.class.getName());
						if (candidate != null) {
							Object value = candidate.getAttribute(AutowireCandidateQualifier.VALUE_KEY);
							if (value != null && qualifier.test(value.toString())) {
								return true;
							}
						}
					}
					// 工厂方法上是否有对应限定符？（通常来自配置类）
					if (bd instanceof RootBeanDefinition rbd) {
						Method factoryMethod = rbd.getResolvedFactoryMethod();
						if (factoryMethod != null) {
							Qualifier targetAnnotation = AnnotationUtils.getAnnotation(factoryMethod, Qualifier.class);
							if (targetAnnotation != null) {
								return qualifier.test(targetAnnotation.value());
							}
						}
					}
				}
				// Bean 实现类上是否有对应限定符？（自定义用户类型）
				if (beanType != null) {
					Qualifier targetAnnotation = AnnotationUtils.getAnnotation(beanType, Qualifier.class);
					if (targetAnnotation != null) {
						return qualifier.test(targetAnnotation.value());
					}
				}
			}
			catch (NoSuchBeanDefinitionException ignored) {
				// 手动注册的单例对象无法比较限定符
			}
		}
		return false;
	}

}
