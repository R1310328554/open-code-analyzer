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

package org.springframework.beans;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;

import org.springframework.core.Ordered;
import org.springframework.core.SpringProperties;

/**
 * 执行标准 {@link java.beans.Introspector} 检查的 {@link BeanInfoFactory} 实现。
 *
 * <p>通过 {@code META-INF/spring.factories} 配置如下内容启用：
 *
 * <p>{@code org.springframework.beans.BeanInfoFactory=org.springframework.beans.StandardBeanInfoFactory}
 *
 * <p>排序为 {@link Ordered#LOWEST_PRECEDENCE}，以便其他用户定义的
 * {@link BeanInfoFactory} 可以优先。
 *
 * @author Juergen Hoeller
 * @since 6.0
 * @see ExtendedBeanInfoFactory
 * @see CachedIntrospectionResults
 * @see Introspector#getBeanInfo(Class)
 */
public class StandardBeanInfoFactory implements BeanInfoFactory, Ordered {

	/**
	 * 指示 Spring 在调用 JavaBeans {@link Introspector} 时使用
	 * {@link Introspector#IGNORE_ALL_BEANINFO} 模式的系统属性：
	 * {@code "spring.beaninfo.ignore"}。值为 {@code "true"} 时跳过对
	 * {@code BeanInfo} 类的搜索（常见于应用本身并未定义此类 BeanInfo 的场景）。
	 * <p>默认为 {@code "false"}，会考虑全部 {@code BeanInfo} 元数据类，
	 * 行为与标准 {@link Introspector#getBeanInfo(Class)} 调用一致。
	 * 若反复访问不存在的 {@code BeanInfo} 类导致启动或懒加载开销过大，可考虑设为 {@code "true"}。
	 * <p>此类现象也可能说明缓存未有效工作：优先让 Spring jar 与应用类处于同一 ClassLoader，
	 * 以便缓存随应用生命周期干净清理。Web 应用若为多 ClassLoader 布局，
	 * 可在 {@code web.xml} 中声明本地
	 * {@link org.springframework.web.util.IntrospectorCleanupListener} 以获得有效缓存。
	 * @see Introspector#getBeanInfo(Class, int)
	 */
	public static final String IGNORE_BEANINFO_PROPERTY_NAME = "spring.beaninfo.ignore";

	/** 是否让 Introspector 忽略 BeanInfo 类。 */
	private static final boolean shouldIntrospectorIgnoreBeaninfoClasses =
			SpringProperties.getFlag(IGNORE_BEANINFO_PROPERTY_NAME);


	/**
	 * 使用标准 Introspector 获取 BeanInfo，并立即从 JDK 缓存中冲刷相关类，
	 * 以便由 CachedIntrospectionResults 以更利于 GC 的方式缓存。
	 */
	@Override
	public BeanInfo getBeanInfo(Class<?> beanClass) throws IntrospectionException {
		BeanInfo beanInfo = (shouldIntrospectorIgnoreBeaninfoClasses ?
				Introspector.getBeanInfo(beanClass, Introspector.IGNORE_ALL_BEANINFO) :
				Introspector.getBeanInfo(beanClass));

		// 立即将类从 Introspector 缓存中移除，以便 ClassLoader 关闭时可正常 GC；
		// 我们在 CachedIntrospectionResults 中以利于 GC 的方式自行缓存。
		// 这对 JDK 的 ClassInfo 缓存同样必要。
		Class<?> classToFlush = beanClass;
		do {
			Introspector.flushFromCaches(classToFlush);
			classToFlush = classToFlush.getSuperclass();
		}
		while (classToFlush != null && classToFlush != Object.class);

		return beanInfo;
	}

	/**
	 * 返回最低优先级，以便其他 BeanInfoFactory 可覆盖本实现。
	 */
	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}

}
