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
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * {@code AbstractReflectiveMBeanInfoAssembler} 的子类，允许使用任意接口定义
 * Bean 的管理接口。这些接口中声明的方法与属性将分别暴露为 MBean 操作与属性。
 *
 * <p>默认情况下，本类根据 Bean 类实现的接口来决定是否纳入各操作或属性。
 * 也可通过 {@code managedInterfaces} 属性提供接口数组替代默认行为。
 * 若有多个 Bean 且各自需要不同接口集，可使用 {@code interfaceMappings} 将
 * Bean 键映射到接口名列表。
 *
 * <p>若同时指定 {@code interfaceMappings} 与 {@code managedInterfaces}，
 * Spring 会先在映射中查找；若未找到，则使用 {@code managedInterfaces} 定义的接口。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 1.2
 * @see #setManagedInterfaces
 * @see #setInterfaceMappings
 * @see MethodNameBasedMBeanInfoAssembler
 * @see SimpleReflectiveMBeanInfoAssembler
 * @see org.springframework.jmx.export.MBeanExporter
 */
public class InterfaceBasedMBeanInfoAssembler extends AbstractConfigurableMBeanInfoAssembler
		implements BeanClassLoaderAware, InitializingBean {

	/** 用于创建管理信息的接口数组。 */
	private Class<?> @Nullable [] managedInterfaces;

	/** Bean 键到接口类数组的映射。 */
	/** Bean 键到接口类数组的原始 Properties 映射。 */
	private @Nullable Properties interfaceMappings;

	/** 用于解析接口类名的 ClassLoader。 */
	private @Nullable ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

	/** Bean 键到接口类数组的映射。 */
	/** Bean 键到已解析接口类数组的映射。 */
	private @Nullable Map<String, Class<?>[]> resolvedInterfaceMappings;


	/**
	 * 设置用于创建管理信息的接口数组。
	 * <p>若某 Bean 在 {@code interfaceMappings} 中无对应条目，则使用这些接口。
	 * @param managedInterfaces 接口类数组；每项<strong>必须</strong>是接口
	 * @see #setInterfaceMappings
	 */
	public void setManagedInterfaces(Class<?> @Nullable ... managedInterfaces) {
		if (managedInterfaces != null) {
			for (Class<?> ifc : managedInterfaces) {
				if (!ifc.isInterface()) {
					throw new IllegalArgumentException(
							"Management interface [" + ifc.getName() + "] is not an interface");
				}
			}
		}
		this.managedInterfaces = managedInterfaces;
	}

	/**
	 * 设置 Bean 键到逗号分隔接口名列表的映射。
	 * <p>属性键须与 Bean 键匹配，属性值为接口名列表。查找时 Spring 优先检查这些映射。
	 * @param mappings Bean 键到接口名的映射
	 */
	public void setInterfaceMappings(@Nullable Properties mappings) {
		this.interfaceMappings = mappings;
	}

	@Override
	public void setBeanClassLoader(@Nullable ClassLoader beanClassLoader) {
		this.beanClassLoader = beanClassLoader;
	}


	@Override
	public void afterPropertiesSet() {
		if (this.interfaceMappings != null) {
			this.resolvedInterfaceMappings = resolveInterfaceMappings(this.interfaceMappings);
		}
	}

	/**
	 * 解析接口映射，将类名字符串转换为 {@code Class} 对象。
	 * @param mappings 指定的接口映射
	 * @return 已解析的接口映射（值为 Class 数组）
	 */
	private Map<String, Class<?>[]> resolveInterfaceMappings(Properties mappings) {
		Map<String, Class<?>[]> resolvedMappings = CollectionUtils.newHashMap(mappings.size());
		for (Enumeration<?> en = mappings.propertyNames(); en.hasMoreElements();) {
			String beanKey = (String) en.nextElement();
			String[] classNames = StringUtils.commaDelimitedListToStringArray(mappings.getProperty(beanKey));
			Class<?>[] classes = resolveClassNames(classNames, beanKey);
			resolvedMappings.put(beanKey, classes);
		}
		return resolvedMappings;
	}

	/**
	 * 将类名字符串解析为 {@code Class} 对象。
	 * @param classNames 待解析的类名
	 * @param beanKey 关联的 Bean 键
	 * @return 已解析的 Class 数组
	 */
	private Class<?>[] resolveClassNames(String[] classNames, String beanKey) {
		Class<?>[] classes = new Class<?>[classNames.length];
		for (int x = 0; x < classes.length; x++) {
			Class<?> cls = ClassUtils.resolveClassName(classNames[x].trim(), this.beanClassLoader);
			if (!cls.isInterface()) {
				throw new IllegalArgumentException(
						"Class [" + classNames[x] + "] mapped to bean key [" + beanKey + "] is no interface");
			}
			classes[x] = cls;
		}
		return classes;
	}


	/**
	 * 检查 {@code Method} 是否在已配置接口中声明且为 public。
	 * @param method 访问器 {@code Method}
	 * @param beanKey beans 映射中与该 MBean 关联的键
	 * @return 若在已配置接口中声明则返回 {@code true}
	 */
	@Override
	protected boolean includeReadAttribute(Method method, String beanKey) {
		return isPublicInInterface(method, beanKey);
	}

	/**
	 * 检查 {@code Method} 是否在已配置接口中声明且为 public。
	 * @param method 修改器 {@code Method}
	 * @param beanKey beans 映射中与该 MBean 关联的键
	 * @return 若在已配置接口中声明则返回 {@code true}
	 */
	@Override
	protected boolean includeWriteAttribute(Method method, String beanKey) {
		return isPublicInInterface(method, beanKey);
	}

	/**
	 * 检查 {@code Method} 是否在已配置接口中声明且为 public。
	 * @param method 操作 {@code Method}
	 * @param beanKey beans 映射中与该 MBean 关联的键
	 * @return 若在已配置接口中声明则返回 {@code true}
	 */
	@Override
	protected boolean includeOperation(Method method, String beanKey) {
		return isPublicInInterface(method, beanKey);
	}

	/**
	 * 检查 {@code Method} 是否同时为 public 且在已配置接口中声明。
	 * @param method 待检查的 {@code Method}
	 * @param beanKey beans 映射中的 Bean 键
	 * @return 若满足条件则返回 {@code true}
	 */
	private boolean isPublicInInterface(Method method, String beanKey) {
		return Modifier.isPublic(method.getModifiers()) && isDeclaredInInterface(method, beanKey);
	}

	/**
	 * 检查给定方法是否在指定 Bean 的管理接口中声明。
	 * 按 beanKey 查找映射接口，否则回退到 managedInterfaces 或 Bean 类全部接口。
	 */
	private boolean isDeclaredInInterface(Method method, String beanKey) {
		Class<?>[] ifaces = null;

		if (this.resolvedInterfaceMappings != null) {
			ifaces = this.resolvedInterfaceMappings.get(beanKey);
		}

		if (ifaces == null) {
			ifaces = this.managedInterfaces;
			if (ifaces == null) {
				ifaces = ClassUtils.getAllInterfacesForClass(method.getDeclaringClass());
			}
		}

		for (Class<?> ifc : ifaces) {
			for (Method ifcMethod : ifc.getMethods()) {
				if (ifcMethod.getName().equals(method.getName()) &&
						ifcMethod.getParameterCount() == method.getParameterCount() &&
						Arrays.equals(ifcMethod.getParameterTypes(), method.getParameterTypes())) {
					return true;
				}
			}
		}

		return false;
	}

}
