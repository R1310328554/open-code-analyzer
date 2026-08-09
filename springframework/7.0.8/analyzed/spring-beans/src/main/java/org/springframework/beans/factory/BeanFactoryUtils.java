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

package org.springframework.beans.factory;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeansException;
import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * 操作 Bean 工厂的便捷方法，尤其针对 {@link ListableBeanFactory} 接口。
 *
 * <p>返回 Bean 数量、Bean 名称或 Bean 实例，并考虑 Bean 工厂的嵌套层次
 * （{@link ListableBeanFactory} 接口上的方法不像 {@link BeanFactory} 接口那样考虑层次）。
 *
 * <p><b>注意：</b>通常更推荐使用 {@link BeanFactory#getBeanProvider} 配合
 * {@link ObjectProvider#stream()}，而非本工具类。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 04.07.2003
 * @see BeanFactory#getBeanProvider
 */
public abstract class BeanFactoryUtils {

	/**
	 * 生成 Bean 名称的分隔符。若类名或父名不唯一，将追加 "#1"、"#2" 等直至唯一。
	 */
	public static final String GENERATED_BEAN_NAME_SEPARATOR = "#";

	/**
	 * 从带 FactoryBean 前缀的名称到剥离后名称的缓存。
	 * @since 5.1
	 * @see BeanFactory#FACTORY_BEAN_PREFIX
	 */
	private static final Map<String, String> transformedBeanNameCache = new ConcurrentHashMap<>();


	/**
	 * 判断给定名称是否为 Factory 解引用（以 Factory 解引用前缀开头）。
	 * @param name Bean 名称
	 * @return 是否为 Factory 解引用
	 * @see BeanFactory#FACTORY_BEAN_PREFIX
	 */
	public static boolean isFactoryDereference(@Nullable String name) {
		return (name != null && !name.isEmpty() && name.charAt(0) == BeanFactory.FACTORY_BEAN_PREFIX_CHAR);
	}

	/**
	 * 返回实际 Bean 名称，剥离 Factory 解引用前缀（若有，并剥离重复的 Factory 前缀）。
	 * @param name Bean 名称
	 * @return 转换后的名称
	 * @see BeanFactory#FACTORY_BEAN_PREFIX
	 */
	public static String transformedBeanName(String name) {
		Assert.notNull(name, "'name' must not be null");
		if (name.isEmpty() || name.charAt(0) != BeanFactory.FACTORY_BEAN_PREFIX_CHAR) {
			return name;
		}
		return transformedBeanNameCache.computeIfAbsent(name, beanName -> {
			do {
				beanName = beanName.substring(1);  // '&' 的长度
			}
			while (beanName.charAt(0) == BeanFactory.FACTORY_BEAN_PREFIX_CHAR);
			return beanName;
		});
	}

	/**
	 * 判断给定名称是否为默认命名策略生成的 Bean 名称（包含 "#..." 部分）。
	 * @param name Bean 名称
	 * @return 是否为生成的 Bean 名称
	 * @see #GENERATED_BEAN_NAME_SEPARATOR
	 * @see org.springframework.beans.factory.support.BeanDefinitionReaderUtils#generateBeanName
	 * @see org.springframework.beans.factory.support.DefaultBeanNameGenerator
	 */
	public static boolean isGeneratedBeanName(@Nullable String name) {
		return (name != null && name.contains(GENERATED_BEAN_NAME_SEPARATOR));
	}

	/**
	 * 从给定（可能生成的）Bean 名称提取「原始」名称，排除为唯一性追加的 "#..." 后缀。
	 * @param name 可能生成的 Bean 名称
	 * @return 原始 Bean 名称
	 * @see #GENERATED_BEAN_NAME_SEPARATOR
	 */
	public static String originalBeanName(String name) {
		Assert.notNull(name, "'name' must not be null");
		int separatorIndex = name.indexOf(GENERATED_BEAN_NAME_SEPARATOR);
		return (separatorIndex != -1 ? name.substring(0, separatorIndex) : name);
	}


	// Bean 名称检索

	/**
	 * 统计本工厂参与层次中所有 Bean 的数量，含祖先工厂定义的 Bean。
	 * <p>同名被覆盖（在子工厂以相同名称指定）的 Bean 只计一次。
	 * @param lbf Bean 工厂
	 * @return Bean 数量（含祖先工厂）
	 * @see #beanNamesIncludingAncestors
	 */
	public static int countBeansIncludingAncestors(ListableBeanFactory lbf) {
		return beanNamesIncludingAncestors(lbf).length;
	}

	/**
	 * 返回工厂中所有 Bean 名称，含祖先工厂。
	 * @param lbf Bean 工厂
	 * @return 匹配的 Bean 名称数组，无则返回空数组
	 * @see #beanNamesForTypeIncludingAncestors
	 */
	public static String[] beanNamesIncludingAncestors(ListableBeanFactory lbf) {
		return beanNamesForTypeIncludingAncestors(lbf, Object.class);
	}

	/**
	 * 获取匹配给定类型的所有 Bean 名称，含祖先工厂定义的。
	 * 若 Bean 定义被覆盖则返回唯一名称。
	 * <p>会考虑 FactoryBean 创建的对象，即会初始化 FactoryBean。
	 * 若 FactoryBean 创建的对象不匹配，则用 FactoryBean 本身匹配类型。
	 * <p>本版本自动包含原型与 FactoryBean。
	 * @param lbf Bean 工厂
	 * @param type Bean 必须匹配的类型（{@code ResolvableType}）
	 * @return 匹配的 Bean 名称数组，无则返回空数组
	 * @since 4.2
	 * @see ListableBeanFactory#getBeanNamesForType(ResolvableType)
	 */
	public static String[] beanNamesForTypeIncludingAncestors(ListableBeanFactory lbf, ResolvableType type) {
		Assert.notNull(lbf, "ListableBeanFactory must not be null");
		String[] result = lbf.getBeanNamesForType(type);
		if (lbf instanceof HierarchicalBeanFactory hbf) {
			if (hbf.getParentBeanFactory() instanceof ListableBeanFactory pbf) {
				String[] parentResult = beanNamesForTypeIncludingAncestors(pbf, type);
				result = mergeNamesWithParent(result, parentResult, hbf);
			}
		}
		return result;
	}

	/**
	 * 获取匹配给定类型的所有 Bean 名称，含祖先工厂定义的。
	 * <p>若 {@code allowEagerInit} 为 true，会考虑 FactoryBean 创建的对象并初始化 FactoryBean。
	 * 若创建的对象不匹配，则用 FactoryBean 本身匹配；{@code allowEagerInit} 为 false 时
	 * 仅检查 raw FactoryBean（无需初始化每个 FactoryBean）。
	 * @param lbf Bean 工厂
	 * @param type Bean 必须匹配的类型（{@code ResolvableType}）
	 * @param includeNonSingletons 是否包含原型或作用域 Bean，还是仅单例（同样适用于 FactoryBean）
	 * @param allowEagerInit 类型检查时是否可初始化 <i>lazy-init 单例</i> 与
	 * <i>FactoryBean 创建的对象</i>（或带 "factory-bean" 引用的工厂方法）。
	 * 传入 {@code true} 会初始化 FactoryBean 与 "factory-bean" 引用。
	 * @return 匹配的 Bean 名称数组，无则返回空数组
	 * @since 5.2
	 * @see ListableBeanFactory#getBeanNamesForType(ResolvableType, boolean, boolean)
	 */
	public static String[] beanNamesForTypeIncludingAncestors(
			ListableBeanFactory lbf, ResolvableType type, boolean includeNonSingletons, boolean allowEagerInit) {

		Assert.notNull(lbf, "ListableBeanFactory must not be null");
		String[] result = lbf.getBeanNamesForType(type, includeNonSingletons, allowEagerInit);
		if (lbf instanceof HierarchicalBeanFactory hbf) {
			if (hbf.getParentBeanFactory() instanceof ListableBeanFactory pbf) {
				String[] parentResult = beanNamesForTypeIncludingAncestors(
						pbf, type, includeNonSingletons, allowEagerInit);
				result = mergeNamesWithParent(result, parentResult, hbf);
			}
		}
		return result;
	}

	/**
	 * 获取匹配给定类型的所有 Bean 名称，含祖先工厂定义的。
	 * <p>会考虑 FactoryBean 创建的对象；本版本自动包含原型与 FactoryBean。
	 * @param lbf Bean 工厂
	 * @param type Bean 必须匹配的类型（{@code Class}）
	 * @return 匹配的 Bean 名称数组，无则返回空数组
	 * @see ListableBeanFactory#getBeanNamesForType(Class)
	 */
	public static String[] beanNamesForTypeIncludingAncestors(ListableBeanFactory lbf, Class<?> type) {
		Assert.notNull(lbf, "ListableBeanFactory must not be null");
		String[] result = lbf.getBeanNamesForType(type);
		if (lbf instanceof HierarchicalBeanFactory hbf) {
			if (hbf.getParentBeanFactory() instanceof ListableBeanFactory pbf) {
				String[] parentResult = beanNamesForTypeIncludingAncestors(pbf, type);
				result = mergeNamesWithParent(result, parentResult, hbf);
			}
		}
		return result;
	}

	/**
	 * 获取匹配给定类型的所有 Bean 名称，含祖先工厂定义的。
	 * @param lbf Bean 工厂
	 * @param includeNonSingletons 是否包含原型或作用域 Bean，还是仅单例（同样适用于 FactoryBean）
	 * @param allowEagerInit 类型检查时是否可初始化 lazy-init 单例与 FactoryBean 创建的对象
	 * @param type Bean 必须匹配的类型
	 * @return 匹配的 Bean 名称数组，无则返回空数组
	 * @see ListableBeanFactory#getBeanNamesForType(Class, boolean, boolean)
	 */
	public static String[] beanNamesForTypeIncludingAncestors(
			ListableBeanFactory lbf, Class<?> type, boolean includeNonSingletons, boolean allowEagerInit) {

		Assert.notNull(lbf, "ListableBeanFactory must not be null");
		String[] result = lbf.getBeanNamesForType(type, includeNonSingletons, allowEagerInit);
		if (lbf instanceof HierarchicalBeanFactory hbf) {
			if (hbf.getParentBeanFactory() instanceof ListableBeanFactory pbf) {
				String[] parentResult = beanNamesForTypeIncludingAncestors(
						pbf, type, includeNonSingletons, allowEagerInit);
				result = mergeNamesWithParent(result, parentResult, hbf);
			}
		}
		return result;
	}

	/**
	 * 获取 {@code Class} 上带有指定 {@link Annotation} 的所有 Bean 名称，
	 * 含祖先工厂定义的，且尚未创建 Bean 实例。覆盖定义时返回唯一名称。
	 * @param lbf Bean 工厂
	 * @param annotationType 要查找的注解类型
	 * @return 匹配的 Bean 名称数组，无则返回空数组
	 * @since 5.0
	 * @see ListableBeanFactory#getBeanNamesForAnnotation(Class)
	 */
	public static String[] beanNamesForAnnotationIncludingAncestors(
			ListableBeanFactory lbf, Class<? extends Annotation> annotationType) {

		Assert.notNull(lbf, "ListableBeanFactory must not be null");
		String[] result = lbf.getBeanNamesForAnnotation(annotationType);
		if (lbf instanceof HierarchicalBeanFactory hbf) {
			if (hbf.getParentBeanFactory() instanceof ListableBeanFactory pbf) {
				String[] parentResult = beanNamesForAnnotationIncludingAncestors(pbf, annotationType);
				result = mergeNamesWithParent(result, parentResult, hbf);
			}
		}
		return result;
	}


	// Bean 实例检索

	/**
	 * 返回给定类型或子类型的所有 Bean，若当前工厂为 HierarchicalBeanFactory 则也包含祖先工厂中的 Bean。
	 * 返回的 Map 仅包含该类型的 Bean。
	 * <p>会考虑 FactoryBean 创建的对象。
	 * <p><b>注意：同名 Bean 在「最低」工厂层级优先，即子工厂中的 Bean 会隐藏祖先工厂中的同名 Bean，
	 * 按类型查找时也看不见祖先中的对应 Bean。</b> 这样可在子工厂中显式选择相同 Bean 名称以「替换」Bean。
	 * @param lbf Bean 工厂
	 * @param type 要匹配的 Bean 类型
	 * @return 匹配的 Bean 实例 Map，无则返回空 Map
	 * @throws BeansException 若 Bean 无法创建
	 * @see ListableBeanFactory#getBeansOfType(Class)
	 */
	public static <T> Map<String, T> beansOfTypeIncludingAncestors(ListableBeanFactory lbf, Class<T> type)
			throws BeansException {

		Assert.notNull(lbf, "ListableBeanFactory must not be null");
		Map<String, T> result = new LinkedHashMap<>(4);
		result.putAll(lbf.getBeansOfType(type));
		if (lbf instanceof HierarchicalBeanFactory hbf) {
			if (hbf.getParentBeanFactory() instanceof ListableBeanFactory pbf) {
				Map<String, T> parentResult = beansOfTypeIncludingAncestors(pbf, type);
				parentResult.forEach((beanName, beanInstance) -> {
					if (!result.containsKey(beanName) && !hbf.containsLocalBean(beanName)) {
						result.put(beanName, beanInstance);
					}
				});
			}
		}
		return result;
	}

	/**
	 * 返回给定类型或子类型的所有 Bean，含祖先工厂（若为 HierarchicalBeanFactory）。
	 * <p>若 {@code allowEagerInit} 为 true 会考虑 FactoryBean 创建的对象。
	 * <p><b>注意：同名 Bean 在最低工厂层级优先，子工厂 Bean 隐藏祖先工厂同名 Bean。</b>
	 * @param lbf Bean 工厂
	 * @param type 要匹配的 Bean 类型
	 * @param includeNonSingletons 是否包含原型或作用域 Bean，还是仅单例（同样适用于 FactoryBean）
	 * @param allowEagerInit 类型检查时是否可初始化 lazy-init 单例与 FactoryBean 创建的对象
	 * @return 匹配的 Bean 实例 Map，无则返回空 Map
	 * @throws BeansException 若 Bean 无法创建
	 * @see ListableBeanFactory#getBeansOfType(Class, boolean, boolean)
	 */
	public static <T> Map<String, T> beansOfTypeIncludingAncestors(
			ListableBeanFactory lbf, Class<T> type, boolean includeNonSingletons, boolean allowEagerInit)
			throws BeansException {

		Assert.notNull(lbf, "ListableBeanFactory must not be null");
		Map<String, T> result = new LinkedHashMap<>(4);
		result.putAll(lbf.getBeansOfType(type, includeNonSingletons, allowEagerInit));
		if (lbf instanceof HierarchicalBeanFactory hbf) {
			if (hbf.getParentBeanFactory() instanceof ListableBeanFactory pbf) {
				Map<String, T> parentResult = beansOfTypeIncludingAncestors(pbf, type, includeNonSingletons, allowEagerInit);
				parentResult.forEach((beanName, beanInstance) -> {
					if (!result.containsKey(beanName) && !hbf.containsLocalBean(beanName)) {
						result.put(beanName, beanInstance);
					}
				});
			}
		}
		return result;
	}

	/**
	 * 返回唯一匹配给定类型或子类型的 Bean，含祖先工厂；不关心 Bean 名称时的便捷方法。
	 * <p>本版本自动包含原型与 FactoryBean。
	 * <p><b>注意：同名 Bean 在最低工厂层级优先。</b>
	 * @param lbf Bean 工厂
	 * @param type 要匹配的 Bean 类型
	 * @return 匹配的 Bean 实例
	 * @throws NoSuchBeanDefinitionException 未找到给定类型的 Bean
	 * @throws NoUniqueBeanDefinitionException 找到多个给定类型的 Bean
	 * @throws BeansException 若 Bean 无法创建
	 * @see #beansOfTypeIncludingAncestors(ListableBeanFactory, Class)
	 */
	public static <T> T beanOfTypeIncludingAncestors(ListableBeanFactory lbf, Class<T> type)
			throws BeansException {

		Map<String, T> beansOfType = beansOfTypeIncludingAncestors(lbf, type);
		return uniqueBean(type, beansOfType);
	}

	/**
	 * 返回唯一匹配给定类型或子类型的 Bean，含祖先工厂；不关心 Bean 名称时的便捷方法。
	 * <p><b>注意：同名 Bean 在最低工厂层级优先。</b>
	 * @param lbf Bean 工厂
	 * @param type 要匹配的 Bean 类型
	 * @param includeNonSingletons 是否包含原型或作用域 Bean，还是仅单例（同样适用于 FactoryBean）
	 * @param allowEagerInit 类型检查时是否可初始化 lazy-init 单例与 FactoryBean 创建的对象
	 * @return 匹配的 Bean 实例
	 * @throws NoSuchBeanDefinitionException 未找到给定类型的 Bean
	 * @throws NoUniqueBeanDefinitionException 找到多个给定类型的 Bean
	 * @throws BeansException 若 Bean 无法创建
	 * @see #beansOfTypeIncludingAncestors(ListableBeanFactory, Class, boolean, boolean)
	 */
	public static <T> T beanOfTypeIncludingAncestors(
			ListableBeanFactory lbf, Class<T> type, boolean includeNonSingletons, boolean allowEagerInit)
			throws BeansException {

		Map<String, T> beansOfType = beansOfTypeIncludingAncestors(lbf, type, includeNonSingletons, allowEagerInit);
		return uniqueBean(type, beansOfType);
	}

	/**
	 * 返回唯一匹配给定类型或子类型的 Bean，不查找祖先工厂；不关心 Bean 名称时的便捷方法。
	 * <p>本版本自动包含原型与 FactoryBean。
	 * @param lbf Bean 工厂
	 * @param type 要匹配的 Bean 类型
	 * @return 匹配的 Bean 实例
	 * @throws NoSuchBeanDefinitionException 未找到给定类型的 Bean
	 * @throws NoUniqueBeanDefinitionException 找到多个给定类型的 Bean
	 * @throws BeansException 若 Bean 无法创建
	 * @see ListableBeanFactory#getBeansOfType(Class)
	 */
	public static <T> T beanOfType(ListableBeanFactory lbf, Class<T> type) throws BeansException {
		Assert.notNull(lbf, "ListableBeanFactory must not be null");
		Map<String, T> beansOfType = lbf.getBeansOfType(type);
		return uniqueBean(type, beansOfType);
	}

	/**
	 * 返回唯一匹配给定类型或子类型的 Bean，不查找祖先工厂。
	 * @param lbf Bean 工厂
	 * @param type 要匹配的 Bean 类型
	 * @param includeNonSingletons 是否包含原型或作用域 Bean，还是仅单例（同样适用于 FactoryBean）
	 * @param allowEagerInit 类型检查时是否可初始化 lazy-init 单例与 FactoryBean 创建的对象
	 * @return 匹配的 Bean 实例
	 * @throws NoSuchBeanDefinitionException 未找到给定类型的 Bean
	 * @throws NoUniqueBeanDefinitionException 找到多个给定类型的 Bean
	 * @throws BeansException 若 Bean 无法创建
	 * @see ListableBeanFactory#getBeansOfType(Class, boolean, boolean)
	 */
	public static <T> T beanOfType(
			ListableBeanFactory lbf, Class<T> type, boolean includeNonSingletons, boolean allowEagerInit)
			throws BeansException {

		Assert.notNull(lbf, "ListableBeanFactory must not be null");
		Map<String, T> beansOfType = lbf.getBeansOfType(type, includeNonSingletons, allowEagerInit);
		return uniqueBean(type, beansOfType);
	}


	/**
	 * 将本地 Bean 名称结果与父级结果合并。
	 * @param result 本地 Bean 名称结果
	 * @param parentResult 父级 Bean 名称结果（可能为空）
	 * @param hbf 本地 Bean 工厂
	 * @return 合并结果（可能直接为本地结果）
	 * @since 4.3.15
	 */
	private static String[] mergeNamesWithParent(String[] result, String[] parentResult, HierarchicalBeanFactory hbf) {
		if (parentResult.length == 0) {
			return result;
		}
		List<String> merged = new ArrayList<>(result.length + parentResult.length);
		merged.addAll(Arrays.asList(result));
		for (String beanName : parentResult) {
			if (!merged.contains(beanName) && !hbf.containsLocalBean(beanName)) {
				merged.add(beanName);
			}
		}
		return StringUtils.toStringArray(merged);
	}

	/**
	 * 从匹配 Bean 的 Map 中提取给定类型的唯一 Bean。
	 * @param type 要匹配的 Bean 类型
	 * @param matchingBeans 找到的所有匹配 Bean
	 * @return 唯一 Bean 实例
	 * @throws NoSuchBeanDefinitionException 未找到给定类型的 Bean
	 * @throws NoUniqueBeanDefinitionException 找到多个给定类型的 Bean
	 */
	private static <T> T uniqueBean(Class<T> type, Map<String, T> matchingBeans) {
		int count = matchingBeans.size();
		if (count == 1) {
			return matchingBeans.values().iterator().next();
		}
		else if (count > 1) {
			throw new NoUniqueBeanDefinitionException(type, matchingBeans.keySet());
		}
		else {
			throw new NoSuchBeanDefinitionException(type);
		}
	}

}
