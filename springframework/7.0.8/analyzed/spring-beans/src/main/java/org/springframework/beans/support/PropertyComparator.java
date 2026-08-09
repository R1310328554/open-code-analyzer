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

package org.springframework.beans.support;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.util.StringUtils;

/**
 * PropertyComparator 通过 BeanWrapper 读取指定 Bean 属性，对两个 Bean 进行比较。
 *
 * @author Juergen Hoeller
 * @author Jean-Pierre Pawlak
 * @since 19.05.2003
 * @param <T> 此比较器可比较的对象类型
 * @see org.springframework.beans.BeanWrapper
 * @deprecated as severely outdated and superseded by more modern solutions,
 * for example in Spring Data Commons
 */
@Deprecated(since = "7.0.3", forRemoval = true)
@SuppressWarnings("removal")
public class PropertyComparator<T> implements Comparator<T> {

	protected final Log logger = LogFactory.getLog(getClass());

	private final SortDefinition sortDefinition;


	/**
	 * 根据给定 SortDefinition 创建新的 PropertyComparator。
	 * @see MutableSortDefinition
	 */
	public PropertyComparator(SortDefinition sortDefinition) {
		this.sortDefinition = sortDefinition;
	}

	/**
	 * 根据给定设置创建 PropertyComparator。
	 * @param property 用于比较的属性
	 * @param ignoreCase 比较 String 值时是否忽略大小写
	 * @param ascending 是否升序（{@code true}）或降序（{@code false}）
	 */
	public PropertyComparator(String property, boolean ignoreCase, boolean ascending) {
		this.sortDefinition = new MutableSortDefinition(property, ignoreCase, ascending);
	}

	/**
	 * 返回此比较器使用的 SortDefinition。
	 */
	public final SortDefinition getSortDefinition() {
		return this.sortDefinition;
	}


	@Override
	@SuppressWarnings("unchecked")
	public int compare(T o1, T o2) {
		Object v1 = getPropertyValue(o1);
		Object v2 = getPropertyValue(o2);
		if (this.sortDefinition.isIgnoreCase() && (v1 instanceof String text1) && (v2 instanceof String text2)) {
			v1 = text1.toLowerCase(Locale.ROOT);
			v2 = text2.toLowerCase(Locale.ROOT);
		}

		int result;

		// 属性为 null 的对象排在排序结果末尾
		try {
			if (v1 != null) {
				result = (v2 != null ? ((Comparable<Object>) v1).compareTo(v2) : -1);
			}
			else {
				result = (v2 != null ? 1 : 0);
			}
		}
		catch (RuntimeException ex) {
			if (logger.isDebugEnabled()) {
				logger.debug("Could not sort objects [" + o1 + "] and [" + o2 + "]", ex);
			}
			return 0;
		}

		return (this.sortDefinition.isAscending() ? result : -result);
	}

	/**
	 * 获取给定对象在 SortDefinition 中指定属性的值。
	 * @param obj 要读取属性值的对象
	 * @return 属性值
	 */
	private @Nullable Object getPropertyValue(Object obj) {
		// 若嵌套属性无法读取，直接返回 null（类似 JSTL EL）。
		// 若属性本身不存在，则让异常抛出。
		try {
			BeanWrapperImpl beanWrapper = new BeanWrapperImpl(false);
			beanWrapper.setWrappedInstance(obj);
			return beanWrapper.getPropertyValue(this.sortDefinition.getProperty());
		}
		catch (BeansException ex) {
			logger.debug("PropertyComparator could not access property - treating as null for sorting", ex);
			return null;
		}
	}


	/**
	 * 根据给定排序定义对 List 进行排序。
	 * <p>注意：列表中的对象必须以 Bean 属性形式提供给定属性，即具备 getXXX 方法。
	 * @param source 输入 List
	 * @param sortDefinition 排序参数
	 * @throws java.lang.IllegalArgumentException 缺少 propertyName 时
	 */
	public static void sort(List<?> source, SortDefinition sortDefinition) throws BeansException {
		if (StringUtils.hasText(sortDefinition.getProperty())) {
			source.sort(new PropertyComparator<>(sortDefinition));
		}
	}

	/**
	 * 根据给定排序定义对数组进行排序。
	 * <p>注意：数组中的对象必须以 Bean 属性形式提供给定属性，即具备 getXXX 方法。
	 * @param source 输入数组
	 * @param sortDefinition 排序参数
	 * @throws java.lang.IllegalArgumentException 缺少 propertyName 时
	 */
	public static void sort(Object[] source, SortDefinition sortDefinition) throws BeansException {
		if (StringUtils.hasText(sortDefinition.getProperty())) {
			Arrays.sort(source, new PropertyComparator<>(sortDefinition));
		}
	}

}
