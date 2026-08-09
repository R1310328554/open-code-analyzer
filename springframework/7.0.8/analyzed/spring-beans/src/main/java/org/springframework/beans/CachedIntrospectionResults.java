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
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.util.ClassUtils;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.StringUtils;

/**
 * 内部类：缓存某个 Java 类的 JavaBeans {@link java.beans.PropertyDescriptor} 信息。
 * 不打算由应用代码直接使用。
 *
 * <p>Spring 需要在应用自身的 {@link ClassLoader} 范围内缓存 bean 描述符，
 * 而不是依赖 JDK 全局的 {@link BeanInfo} 缓存（以免共享 JVM 中某个应用关闭时发生泄漏）。
 *
 * <p>信息以静态方式缓存，因此不必为每个操作的 JavaBean 都新建本类实例。
 * 本类采用工厂模式：私有构造函数，通过静态 {@link #forClass(Class)} 工厂方法获取实例。
 *
 * <p>要使缓存真正有效，通常应让 Spring jar 与应用类处于同一 ClassLoader，
 * 这样缓存生命周期可随应用一起干净地清理。
 *
 * <p>自 6.0 起，Spring 默认通过高效的方法反射发现基本 JavaBeans 属性。
 * 若需完整 JavaBeans 内省（含索引属性及 JDK 支持的全部定制器），可在
 * {@code META-INF/spring.factories} 中配置：
 * {@code org.springframework.beans.BeanInfoFactory=org.springframework.beans.StandardBeanInfoFactory}
 * 若需与 Spring 5.3 兼容的扩展内省（含非 void 的 setter）：
 * {@code org.springframework.beans.BeanInfoFactory=org.springframework.beans.ExtendedBeanInfoFactory}
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 05 May 2001
 * @see #acceptClassLoader(ClassLoader)
 * @see #clearClassLoader(ClassLoader)
 * @see #forClass(Class)
 */
public final class CachedIntrospectionResults {

	/** 通过 SpringFactoriesLoader 加载的 BeanInfoFactory 列表。 */
	private static final List<BeanInfoFactory> beanInfoFactories = SpringFactoriesLoader.loadFactories(
			BeanInfoFactory.class, CachedIntrospectionResults.class.getClassLoader());

	/** 默认使用的简易 BeanInfoFactory。 */
	private static final SimpleBeanInfoFactory simpleBeanInfoFactory = new SimpleBeanInfoFactory();

	private static final Log logger = LogFactory.getLog(CachedIntrospectionResults.class);

	/**
	 * 始终接受其类进入缓存的 ClassLoader 集合，
	 * 即使这些类本身并不满足“缓存安全”条件。
	 */
	static final Set<ClassLoader> acceptedClassLoaders = ConcurrentHashMap.newKeySet(16);

	/**
	 * 以 Class 为键、CachedIntrospectionResults 为值的强引用缓存。
	 * 用于缓存安全的 bean 类。
	 */
	static final ConcurrentMap<Class<?>, CachedIntrospectionResults> strongClassCache =
			new ConcurrentHashMap<>(64);

	/**
	 * 以 Class 为键、CachedIntrospectionResults 为值的软引用缓存。
	 * 用于非缓存安全的 bean 类。
	 */
	static final ConcurrentMap<Class<?>, CachedIntrospectionResults> softClassCache =
			new ConcurrentReferenceHashMap<>(64);


	/**
	 * 将给定 ClassLoader 视为缓存安全，即使其类在本 CachedIntrospectionResults
	 * 中本不满足缓存安全条件。
	 * <p>仅在 Spring 类位于“公共”ClassLoader（例如系统 ClassLoader）、
	 * 且其生命周期与应用解耦时才有意义。此时若默认缓存应用类，
	 * 会在公共 ClassLoader 中造成泄漏。
	 * <p>应用启动时调用 {@code acceptClassLoader}，关闭时应配对调用 {@link #clearClassLoader}。
	 * @param classLoader 要接受的 ClassLoader
	 */
	public static void acceptClassLoader(@Nullable ClassLoader classLoader) {
		if (classLoader != null) {
			acceptedClassLoaders.add(classLoader);
		}
	}

	/**
	 * 清除给定 ClassLoader 的内省缓存：移除其下所有类的内省结果，
	 * 并将该 ClassLoader（及其子加载器）从接受列表中移除。
	 * @param classLoader 要清除缓存的 ClassLoader
	 */
	public static void clearClassLoader(@Nullable ClassLoader classLoader) {
		acceptedClassLoaders.removeIf(registeredLoader ->
				isUnderneathClassLoader(registeredLoader, classLoader));
		strongClassCache.keySet().removeIf(beanClass ->
				isUnderneathClassLoader(beanClass.getClassLoader(), classLoader));
		softClassCache.keySet().removeIf(beanClass ->
				isUnderneathClassLoader(beanClass.getClassLoader(), classLoader));
	}

	/**
	 * 为给定 bean 类创建 CachedIntrospectionResults。
	 * @param beanClass 要分析的 bean 类
	 * @return 对应的 CachedIntrospectionResults
	 * @throws BeansException 内省失败时
	 */
	static CachedIntrospectionResults forClass(Class<?> beanClass) throws BeansException {
		CachedIntrospectionResults results = strongClassCache.get(beanClass);
		if (results != null) {
			return results;
		}
		results = softClassCache.get(beanClass);
		if (results != null) {
			return results;
		}

		results = new CachedIntrospectionResults(beanClass);
		ConcurrentMap<Class<?>, CachedIntrospectionResults> classCacheToUse;

		if (ClassUtils.isCacheSafe(beanClass, CachedIntrospectionResults.class.getClassLoader()) ||
				isClassLoaderAccepted(beanClass.getClassLoader())) {
			classCacheToUse = strongClassCache;
		}
		else {
			if (logger.isDebugEnabled()) {
				logger.debug("Not strongly caching class [" + beanClass.getName() + "] because it is not cache-safe");
			}
			classCacheToUse = softClassCache;
		}

		CachedIntrospectionResults existing = classCacheToUse.putIfAbsent(beanClass, results);
		return (existing != null ? existing : results);
	}

	/**
	 * 检查本 CachedIntrospectionResults 是否已配置为接受给定 ClassLoader。
	 * @param classLoader 要检查的 ClassLoader
	 * @return 是否接受该 ClassLoader
	 * @see #acceptClassLoader
	 */
	private static boolean isClassLoaderAccepted(ClassLoader classLoader) {
		for (ClassLoader acceptedLoader : acceptedClassLoaders) {
			if (isUnderneathClassLoader(classLoader, acceptedLoader)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 检查候选 ClassLoader 是否位于给定父 ClassLoader 之下，
	 * 即父加载器是否在候选加载器的层级链中。
	 * @param candidate 要检查的候选 ClassLoader
	 * @param parent 要匹配的父 ClassLoader
	 */
	private static boolean isUnderneathClassLoader(@Nullable ClassLoader candidate, @Nullable ClassLoader parent) {
		if (candidate == parent) {
			return true;
		}
		if (candidate == null) {
			return false;
		}
		ClassLoader classLoaderToCheck = candidate;
		while (classLoaderToCheck != null) {
			classLoaderToCheck = classLoaderToCheck.getParent();
			if (classLoaderToCheck == parent) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 获取给定目标类的 {@link BeanInfo} 描述符。
	 * @param beanClass 要内省的目标类
	 * @return 得到的 {@code BeanInfo}（永不为 {@code null}）
	 * @throws IntrospectionException 内省给定 bean 类失败时
	 */
	private static BeanInfo getBeanInfo(Class<?> beanClass) throws IntrospectionException {
		for (BeanInfoFactory beanInfoFactory : beanInfoFactories) {
			BeanInfo beanInfo = beanInfoFactory.getBeanInfo(beanClass);
			if (beanInfo != null) {
				return beanInfo;
			}
		}
		return simpleBeanInfoFactory.getBeanInfo(beanClass);
	}


	/** 已内省 bean 类对应的 BeanInfo。 */
	private final BeanInfo beanInfo;

	/** 以属性名字符串为键的 PropertyDescriptor 映射。 */
	private final Map<String, PropertyDescriptor> propertyDescriptors;


	/**
	 * 为给定类创建新的 CachedIntrospectionResults 实例。
	 * @param beanClass 要分析的 bean 类
	 * @throws BeansException 内省失败时
	 */
	private CachedIntrospectionResults(Class<?> beanClass) throws BeansException {
		try {
			if (logger.isTraceEnabled()) {
				logger.trace("Getting BeanInfo for class [" + beanClass.getName() + "]");
			}
			this.beanInfo = getBeanInfo(beanClass);

			if (logger.isTraceEnabled()) {
				logger.trace("Caching PropertyDescriptors for class [" + beanClass.getName() + "]");
			}
			this.propertyDescriptors = new LinkedHashMap<>();

			Set<String> readMethodNames = new HashSet<>();

			// 该调用较慢，因此只执行一次。
			PropertyDescriptor[] pds = this.beanInfo.getPropertyDescriptors();
			for (PropertyDescriptor pd : pds) {
				if (Class.class == beanClass && !("name".equals(pd.getName()) ||
						(pd.getName().endsWith("Name") && String.class == pd.getPropertyType()))) {
					// 对 Class 仅允许各类 name 变体属性
					continue;
				}
				if (URL.class == beanClass && "content".equals(pd.getName())) {
					// 对 URL 仅允许属性内省，不允许解析 content
					continue;
				}
				if (pd.getWriteMethod() == null && isInvalidReadOnlyPropertyType(pd.getPropertyType(), beanClass)) {
					// 忽略 ClassLoader 等只读属性——无需绑定
					continue;
				}
				if (logger.isTraceEnabled()) {
					logger.trace("Found bean property '" + pd.getName() + "'" +
							(pd.getPropertyType() != null ? " of type [" + pd.getPropertyType().getName() + "]" : "") +
							(pd.getPropertyEditorClass() != null ?
									"; editor [" + pd.getPropertyEditorClass().getName() + "]" : ""));
				}
				pd = buildGenericTypeAwarePropertyDescriptor(beanClass, pd);
				this.propertyDescriptors.put(pd.getName(), pd);
				Method readMethod = pd.getReadMethod();
				if (readMethod != null) {
					readMethodNames.add(readMethod.getName());
				}
			}

			// 显式检查已实现接口上的 setter/getter，尤其是接口默认方法。
			Class<?> currClass = beanClass;
			while (currClass != null && currClass != Object.class) {
				introspectInterfaces(beanClass, currClass, readMethodNames);
				currClass = currClass.getSuperclass();
			}

			// 检查无前缀的 record 风格访问器，例如 "lastName()"
			// - 访问方法直接对应同名实例字段
			// - 与 Java 15 record 组件访问器约定一致
			introspectPlainAccessors(beanClass, readMethodNames);
		}
		catch (IntrospectionException ex) {
			throw new FatalBeanException("Failed to obtain BeanInfo for class [" + beanClass.getName() + "]", ex);
		}
	}

	/**
	 * 内省当前类所实现接口上的属性描述符。
	 */
	private void introspectInterfaces(Class<?> beanClass, Class<?> currClass, Set<String> readMethodNames)
			throws IntrospectionException {

		for (Class<?> ifc : currClass.getInterfaces()) {
			if (!ClassUtils.isJavaLanguageInterface(ifc)) {
				for (PropertyDescriptor pd : getBeanInfo(ifc).getPropertyDescriptors()) {
					PropertyDescriptor existingPd = this.propertyDescriptors.get(pd.getName());
					if (existingPd == null ||
							(existingPd.getReadMethod() == null && pd.getReadMethod() != null)) {
						// GenericTypeAwarePropertyDescriptor 会宽松地根据已声明的读方法解析 set* 写方法，
						// 因此这里优先保留带读方法的描述符。
						pd = buildGenericTypeAwarePropertyDescriptor(beanClass, pd);
						if (pd.getWriteMethod() == null &&
								isInvalidReadOnlyPropertyType(pd.getPropertyType(), beanClass)) {
							// 忽略 ClassLoader 等只读属性——无需绑定
							continue;
						}
						this.propertyDescriptors.put(pd.getName(), pd);
						Method readMethod = pd.getReadMethod();
						if (readMethod != null) {
							readMethodNames.add(readMethod.getName());
						}
					}
				}
				introspectInterfaces(ifc, ifc, readMethodNames);
			}
		}
	}

	/**
	 * 内省无前缀的普通访问器方法（如 record 风格）。
	 */
	private void introspectPlainAccessors(Class<?> beanClass, Set<String> readMethodNames)
			throws IntrospectionException {

		for (Method method : beanClass.getMethods()) {
			if (!this.propertyDescriptors.containsKey(method.getName()) &&
					!readMethodNames.contains(method.getName()) && isPlainAccessor(method)) {
				this.propertyDescriptors.put(method.getName(),
						new GenericTypeAwarePropertyDescriptor(beanClass, method.getName(), method, null, null));
				readMethodNames.add(method.getName());
			}
		}
	}

	/**
	 * 判断方法是否为“普通访问器”：无参、有返回值、且存在同名实例字段。
	 */
	private boolean isPlainAccessor(Method method) {
		if (Modifier.isStatic(method.getModifiers()) ||
				method.getDeclaringClass() == Object.class || method.getDeclaringClass() == Class.class ||
				method.getParameterCount() > 0 || method.getReturnType() == void.class ||
				isInvalidReadOnlyPropertyType(method.getReturnType(), method.getDeclaringClass())) {
			return false;
		}
		try {
			// 访问方法是否对应同名实例字段？
			method.getDeclaringClass().getDeclaredField(method.getName());
			return true;
		}
		catch (Exception ex) {
			return false;
		}
	}

	/**
	 * 判断只读属性类型是否无效（如 ClassLoader、ProtectionDomain，
	 * 或 bean 本身并非 AutoCloseable 时的 AutoCloseable）。
	 */
	private boolean isInvalidReadOnlyPropertyType(@Nullable Class<?> returnType, Class<?> beanClass) {
		return (returnType != null && (ClassLoader.class.isAssignableFrom(returnType) ||
				ProtectionDomain.class.isAssignableFrom(returnType) ||
				(AutoCloseable.class.isAssignableFrom(returnType) &&
						!AutoCloseable.class.isAssignableFrom(beanClass))));
	}


	/** 返回已缓存的 BeanInfo。 */
	BeanInfo getBeanInfo() {
		return this.beanInfo;
	}

	/** 返回所内省的 bean 类。 */
	Class<?> getBeanClass() {
		return this.beanInfo.getBeanDescriptor().getBeanClass();
	}

	/**
	 * 按名称查找 PropertyDescriptor；名称大小写可做宽松回退匹配。
	 */
	@Nullable PropertyDescriptor getPropertyDescriptor(String name) {
		PropertyDescriptor pd = this.propertyDescriptors.get(name);
		if (pd == null && StringUtils.hasLength(name)) {
			// 与 Property 中相同的宽松回退检查……
			pd = this.propertyDescriptors.get(StringUtils.uncapitalize(name));
			if (pd == null) {
				pd = this.propertyDescriptors.get(StringUtils.capitalize(name));
			}
		}
		return pd;
	}

	/** 返回全部已缓存的 PropertyDescriptor。 */
	PropertyDescriptor[] getPropertyDescriptors() {
		return this.propertyDescriptors.values().toArray(PropertyDescriptorUtils.EMPTY_PROPERTY_DESCRIPTOR_ARRAY);
	}

	/**
	 * 将普通 PropertyDescriptor 包装为感知泛型的 GenericTypeAwarePropertyDescriptor。
	 */
	private PropertyDescriptor buildGenericTypeAwarePropertyDescriptor(Class<?> beanClass, PropertyDescriptor pd) {
		try {
			return new GenericTypeAwarePropertyDescriptor(beanClass, pd.getName(), pd.getReadMethod(),
					pd.getWriteMethod(), pd.getPropertyEditorClass());
		}
		catch (IntrospectionException ex) {
			throw new FatalBeanException("Failed to re-introspect class [" + beanClass.getName() + "]", ex);
		}
	}

}
