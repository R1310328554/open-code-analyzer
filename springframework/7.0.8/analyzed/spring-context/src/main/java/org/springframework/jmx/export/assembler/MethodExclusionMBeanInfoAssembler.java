
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

package org.springframework.jmx.export.assembler;

import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.springframework.util.StringUtils;

/**
 * {@code AbstractReflectiveMBeanInfoAssembler} 的子类，允许显式排除某些方法名，
 * 使其不作为 MBean 操作或属性暴露。
 *
 * <p>未显式排除的方法均会暴露给 JMX。JavaBean getter/setter 会自动暴露为 JMX 属性。
 *
 * <p>可通过 {@code ignoredMethods} 属性提供要排除的方法名数组。若有多个 Bean 且各自
 * 需要不同的排除列表，可使用 {@code ignoredMethodMappings} 将 Bean 键映射到方法名列表。
 *
 * <p>若同时指定 {@code ignoredMethodMappings} 与 {@code ignoredMethods}，
 * Spring 会先在映射中查找；若未找到，则使用 {@code ignoredMethods} 定义的方法名。
 *
 * @author Rob Harrop
 * @author Seth Ladd
 * @since 1.2.5
 * @see #setIgnoredMethods
 * @see #setIgnoredMethodMappings
 * @see InterfaceBasedMBeanInfoAssembler
 * @see SimpleReflectiveMBeanInfoAssembler
 * @see MethodNameBasedMBeanInfoAssembler
 * @see org.springframework.jmx.export.MBeanExporter
 */
public class MethodExclusionMBeanInfoAssembler extends AbstractConfigurableMBeanInfoAssembler {

	/** 全局要忽略的方法名集合。 */
	private @Nullable Set<String> ignoredMethods;

	/** Bean 键到要忽略的方法名集合的映射。 */
	private @Nullable Map<String, Set<String>> ignoredMethodMappings;


	/**
	 * 设置创建管理接口时要<b>忽略</b>的方法名数组。
	 * <p>若某 Bean 在 {@code ignoredMethodMappings} 中无对应条目，则使用这些方法名。
	 * @param ignoredMethodNames 要忽略的方法名
	 * @see #setIgnoredMethodMappings(java.util.Properties)
	 */
	public void setIgnoredMethods(String... ignoredMethodNames) {
		this.ignoredMethods = Set.of(ignoredMethodNames);
	}

	/**
	 * 设置 Bean 键到逗号分隔方法名列表的映射。
	 * <p>这些方法名在创建管理接口时会被<b>忽略</b>。
	 * <p>属性键须与 Bean 键匹配，属性值为方法名列表。查找时 Spring 优先检查这些映射。
	 * @param mappings Bean 键到方法名的映射
	 */
	public void setIgnoredMethodMappings(Properties mappings) {
		this.ignoredMethodMappings = new HashMap<>();
		for (Enumeration<?> en = mappings.keys(); en.hasMoreElements();) {
			String beanKey = (String) en.nextElement();
			String[] methodNames = StringUtils.commaDelimitedListToStringArray(mappings.getProperty(beanKey));
			this.ignoredMethodMappings.put(beanKey, Set.of(methodNames));
		}
	}


	@Override
	protected boolean includeReadAttribute(Method method, String beanKey) {
		return isNotIgnored(method, beanKey);
	}

	@Override
	protected boolean includeWriteAttribute(Method method, String beanKey) {
		return isNotIgnored(method, beanKey);
	}

	@Override
	protected boolean includeOperation(Method method, String beanKey) {
		return isNotIgnored(method, beanKey);
	}

	/**
	 * 判断给定方法是否应纳入管理接口，即未被配置为忽略。
	 * @param method 操作方法
	 * @param beanKey {@code MBeanExporter} 的 beans 映射中与该 MBean 关联的键
	 * @return 若方法未被忽略则返回 {@code true}
	 */
	protected boolean isNotIgnored(Method method, String beanKey) {
		if (this.ignoredMethodMappings != null) {
			Set<String> methodNames = this.ignoredMethodMappings.get(beanKey);
			if (methodNames != null) {
				return !methodNames.contains(method.getName());
			}
		}
		if (this.ignoredMethods != null) {
			return !this.ignoredMethods.contains(method.getName());
		}
		return true;
	}

}
