
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
 * {@code AbstractReflectiveMBeanInfoAssembler} 的子类，允许指定要作为
 * MBean 操作与属性暴露的方法名。JavaBean getter/setter 会自动暴露为 JMX 属性。
 *
 * <p>可通过 {@code managedMethods} 属性提供方法名数组。若有多个 Bean 且各自
 * 需要不同的方法列表，可使用 {@code methodMappings} 将 Bean 键映射到方法名列表。
 *
 * <p>若同时指定 {@code methodMappings} 与 {@code managedMethods}，
 * Spring 会先在映射中查找；若未找到，则使用 {@code managedMethods} 定义的方法名。
 *
 * @author Juergen Hoeller
 * @since 1.2
 * @see #setManagedMethods
 * @see #setMethodMappings
 * @see InterfaceBasedMBeanInfoAssembler
 * @see SimpleReflectiveMBeanInfoAssembler
 * @see MethodExclusionMBeanInfoAssembler
 * @see org.springframework.jmx.export.MBeanExporter
 */
public class MethodNameBasedMBeanInfoAssembler extends AbstractConfigurableMBeanInfoAssembler {

	/** 用于创建管理接口的方法名集合。 */
	private @Nullable Set<String> managedMethods;

	/** Bean 键到方法名集合的映射。 */
	private @Nullable Map<String, Set<String>> methodMappings;


	/**
	 * 设置用于创建管理接口的方法名数组。
	 * <p>若某 Bean 在 {@code methodMappings} 中无对应条目，则使用这些方法名。
	 * @param methodNames 要暴露的方法名
	 * @see #setMethodMappings
	 */
	public void setManagedMethods(String... methodNames) {
		this.managedMethods = Set.of(methodNames);
	}

	/**
	 * 设置 Bean 键到逗号分隔方法名列表的映射。
	 * <p>属性键须与 Bean 键匹配，属性值为方法名列表。查找时 Spring 优先检查这些映射。
	 * @param mappings Bean 键到方法名的映射
	 */
	public void setMethodMappings(Properties mappings) {
		this.methodMappings = new HashMap<>();
		for (Enumeration<?> en = mappings.keys(); en.hasMoreElements();) {
			String beanKey = (String) en.nextElement();
			String[] methodNames = StringUtils.commaDelimitedListToStringArray(mappings.getProperty(beanKey));
			this.methodMappings.put(beanKey, Set.of(methodNames));
		}
	}


	@Override
	protected boolean includeReadAttribute(Method method, String beanKey) {
		return isMatch(method, beanKey);
	}

	@Override
	protected boolean includeWriteAttribute(Method method, String beanKey) {
		return isMatch(method, beanKey);
	}

	@Override
	protected boolean includeOperation(Method method, String beanKey) {
		return isMatch(method, beanKey);
	}

	/**
	 * 判断给定方法名是否在允许暴露的集合中。
	 * 优先查找 {@code methodMappings} 中该 Bean 的条目，否则回退到 {@code managedMethods}。
	 * @param method 待检查的方法
	 * @param beanKey Bean 键
	 * @return 方法名匹配则返回 {@code true}
	 */
	protected boolean isMatch(Method method, String beanKey) {
		if (this.methodMappings != null) {
			Set<String> methodNames = this.methodMappings.get(beanKey);
			if (methodNames != null) {
				return methodNames.contains(method.getName());
			}
		}
		return (this.managedMethods != null && this.managedMethods.contains(method.getName()));
	}

}
