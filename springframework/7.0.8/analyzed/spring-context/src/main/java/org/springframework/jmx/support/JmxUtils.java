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

package org.springframework.jmx.support;

import java.beans.PropertyDescriptor;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.List;

import javax.management.DynamicMBean;
import javax.management.JMX;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.jmx.MBeanServerNotFoundException;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * 支持 Spring JMX 的通用工具方法集合。
 * 包含定位 {@code MBeanServer} 的便捷方法。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 1.2
 * @see #locateMBeanServer
 */
public abstract class JmxUtils {

	/**
	 * 扩展现有 {@link ObjectName} 时使用的键，
	 * 值为对应受管资源的 identity hash code。
	 */
	public static final String IDENTITY_OBJECT_NAME_KEY = "identity";

	/** 用于识别 MBean 接口的后缀。 */
	private static final String MBEAN_SUFFIX = "MBean";


	private static final Log logger = LogFactory.getLog(JmxUtils.class);


	/**
	 * 尝试查找本地运行的 {@code MBeanServer}。若找不到则失败。
	 * 若找到多个则记录警告并返回列表中第一个。
	 * @return 找到的 {@code MBeanServer}
	 * @throws MBeanServerNotFoundException 若找不到 {@code MBeanServer}
	 * @see javax.management.MBeanServerFactory#findMBeanServer
	 */
	public static MBeanServer locateMBeanServer() throws MBeanServerNotFoundException {
		return locateMBeanServer(null);
	}

	/**
	 * 尝试查找本地运行的 {@code MBeanServer}。若找不到则失败。
	 * 若找到多个则记录警告并返回列表中第一个。
	 * @param agentId 要检索的 MBeanServer 代理标识。
	 * 为 {@code null} 时考虑全部已注册 MBeanServer；
	 * 为空字符串时返回平台 MBeanServer。
	 * @return 找到的 {@code MBeanServer}
	 * @throws MBeanServerNotFoundException 若找不到 {@code MBeanServer}
	 * @see javax.management.MBeanServerFactory#findMBeanServer(String)
	 */
	public static MBeanServer locateMBeanServer(@Nullable String agentId) throws MBeanServerNotFoundException {
		MBeanServer server = null;

		// null means any registered server, but "" specifically means the platform server
		if (!"".equals(agentId)) {
			List<MBeanServer> servers = MBeanServerFactory.findMBeanServer(agentId);
			if (!CollectionUtils.isEmpty(servers)) {
				// Check to see if an MBeanServer is registered.
				if (servers.size() > 1 && logger.isInfoEnabled()) {
					logger.info("Found more than one MBeanServer instance" +
							(agentId != null ? " with agent id [" + agentId + "]" : "") +
							". Returning first from list.");
				}
				server = servers.get(0);
			}
		}

		if (server == null && !StringUtils.hasLength(agentId)) {
			// Attempt to load the PlatformMBeanServer.
			try {
				server = ManagementFactory.getPlatformMBeanServer();
			}
			catch (SecurityException ex) {
				throw new MBeanServerNotFoundException("No specific MBeanServer found, " +
						"and not allowed to obtain the Java platform MBeanServer", ex);
			}
		}

		if (server == null) {
			throw new MBeanServerNotFoundException(
					"Unable to locate an MBeanServer instance" +
					(agentId != null ? " with agent id [" + agentId + "]" : ""));
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Found MBeanServer: " + server);
		}
		return server;
	}

	/**
	 * 将 {@code MBeanParameterInfo} 数组转换为对应参数的 {@code Class} 数组。
	 * @param paramInfo JMX 参数信息
	 * @return 参数类型 Class 数组
	 * @throws ClassNotFoundException 若参数类型无法解析
	 */
	public static Class<?> @Nullable [] parameterInfoToTypes(MBeanParameterInfo @Nullable [] paramInfo)
			throws ClassNotFoundException {

		return parameterInfoToTypes(paramInfo, ClassUtils.getDefaultClassLoader());
	}

	/**
	 * 将 {@code MBeanParameterInfo} 数组转换为对应参数的 {@code Class} 数组。
	 * @param paramInfo JMX 参数信息
	 * @param classLoader 加载参数类型所用的 ClassLoader
	 * @return 参数类型 Class 数组
	 * @throws ClassNotFoundException 若参数类型无法解析
	 */
	public static Class<?> @Nullable [] parameterInfoToTypes(
			MBeanParameterInfo @Nullable [] paramInfo, @Nullable ClassLoader classLoader)
			throws ClassNotFoundException {

		Class<?>[] types = null;
		if (paramInfo != null && paramInfo.length > 0) {
			types = new Class<?>[paramInfo.length];
			for (int x = 0; x < paramInfo.length; x++) {
				types[x] = ClassUtils.forName(paramInfo[x].getType(), classLoader);
			}
		}
		return types;
	}

	/**
	 * 创建表示方法参数签名的 {@code String[]}。
	 * 数组每个元素为方法签名中对应参数的全限定类名。
	 * @param method 要构建参数签名的方法
	 * @return 参数类型签名数组
	 */
	public static String[] getMethodSignature(Method method) {
		Class<?>[] types = method.getParameterTypes();
		String[] signature = new String[types.length];
		for (int x = 0; x < types.length; x++) {
			signature[x] = types[x].getName();
		}
		return signature;
	}

	/**
	 * 返回给定 JavaBeans 属性应使用的 JMX 属性名。
	 * <p>严格大小写模式下，{@code getFoo()} 对应属性 {@code Foo}；
	 * 非严格模式下对应 {@code foo}。
	 * @param property JavaBeans 属性描述符
	 * @param useStrictCasing 是否使用严格大小写
	 * @return 应使用的 JMX 属性名
	 */
	public static String getAttributeName(PropertyDescriptor property, boolean useStrictCasing) {
		if (useStrictCasing) {
			return StringUtils.capitalize(property.getName());
		}
		else {
			return property.getName();
		}
	}

	/**
	 * 向现有 {@link ObjectName} 追加键值对：键为静态值 {@code identity}，
	 * 值为所暴露受管资源的 identity hash code。
	 * 可用于为某 Bean 或类的每个实例生成唯一 {@link ObjectName}。
	 * @param objectName 原始 JMX ObjectName
	 * @param managedResource MBean 实例
	 * @return 追加 identity 后的 ObjectName
	 * @throws MalformedObjectNameException 对象名无效时
	 * @see org.springframework.util.ObjectUtils#getIdentityHexString(Object)
	 */
	public static ObjectName appendIdentityToObjectName(ObjectName objectName, Object managedResource)
			throws MalformedObjectNameException {

		Hashtable<String, String> keyProperties = objectName.getKeyPropertyList();
		keyProperties.put(IDENTITY_OBJECT_NAME_KEY, ObjectUtils.getIdentityHexString(managedResource));
		return ObjectNameManager.getInstance(objectName.getDomain(), keyProperties);
	}

	/**
	 * 返回给定 Bean 应暴露的类或接口（用于搜索属性与操作，如检查注解）。
	 * <p>CGLIB 代理返回超类，否则返回 Bean 类（JDK 代理或普通 Bean）。
	 * @param managedBean Bean 实例（可能是 AOP 代理）
	 * @return 要暴露的 Bean 类
	 * @see org.springframework.util.ClassUtils#getUserClass(Object)
	 */
	public static Class<?> getClassToExpose(Object managedBean) {
		return ClassUtils.getUserClass(managedBean);
	}

	/**
	 * 返回给定 Bean 类应暴露的类或接口。
	 * @param clazz Bean 类（可能是 AOP 代理类）
	 * @return 要暴露的 Bean 类
	 * @see org.springframework.util.ClassUtils#getUserClass(Class)
	 */
	public static Class<?> getClassToExpose(Class<?> clazz) {
		return ClassUtils.getUserClass(clazz);
	}

	/**
	 * 判断给定 Bean 类是否可直接作为 MBean。
	 * <p>检查 {@link javax.management.DynamicMBean}、对应 "*MBean" 接口（Standard MBean）
	 * 或 "*MXBean" 接口（Java MXBean）。
	 * @param clazz 要分析的 Bean 类
	 * @return 是否为 MBean
	 * @see org.springframework.jmx.export.MBeanExporter#isMBean(Class)
	 */
	public static boolean isMBean(@Nullable Class<?> clazz) {
		return (clazz != null &&
				(DynamicMBean.class.isAssignableFrom(clazz) ||
						(getMBeanInterface(clazz) != null || getMXBeanInterface(clazz) != null)));
	}

	/**
	 * 返回给定类的 Standard MBean 接口（若有），
	 * 即类名加后缀 "MBean" 的接口。
	 * @param clazz 要检查的类
	 * @return Standard MBean 接口，或 {@code null}
	 */
	public static @Nullable Class<?> getMBeanInterface(@Nullable Class<?> clazz) {
		if (clazz == null || clazz.getSuperclass() == null) {
			return null;
		}
		String mbeanInterfaceName = clazz.getName() + MBEAN_SUFFIX;
		Class<?>[] implementedInterfaces = clazz.getInterfaces();
		for (Class<?> iface : implementedInterfaces) {
			if (iface.getName().equals(mbeanInterfaceName)) {
				return iface;
			}
		}
		return getMBeanInterface(clazz.getSuperclass());
	}

	/**
	 * 返回给定类的 Java MXBean 接口（若有），
	 * 即名称以 "MXBean" 结尾或带 MXBean 注解的接口。
	 * @param clazz 要检查的类
	 * @return MXBean 接口，或 {@code null}
	 */
	public static @Nullable Class<?> getMXBeanInterface(@Nullable Class<?> clazz) {
		if (clazz == null || clazz.getSuperclass() == null) {
			return null;
		}
		Class<?>[] implementedInterfaces = clazz.getInterfaces();
		for (Class<?> iface : implementedInterfaces) {
			if (JMX.isMXBeanInterface(iface)) {
				return iface;
			}
		}
		return getMXBeanInterface(clazz.getSuperclass());
	}

}
