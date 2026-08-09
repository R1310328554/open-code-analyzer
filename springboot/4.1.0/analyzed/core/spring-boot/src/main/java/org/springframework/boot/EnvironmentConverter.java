/*
 * Copyright 2012-present the original author or authors.
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

package org.springframework.boot;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.context.support.StandardServletEnvironment;

/**
 * 将一种 {@link Environment} 类型转换为另一种的工具类。
 * <p>
 * 复制活动 Profile、转换服务及属性源，并在转换为 Servlet 环境时
 * 保留 Servlet 相关属性源。
 *
 * @author Ethan Rubinson
 * @author Andy Wilkinson
 * @author Madhura Bhave
 */
final class EnvironmentConverter {

	private static final String CONFIGURABLE_WEB_ENVIRONMENT_CLASS = "org.springframework.web.context.ConfigurableWebEnvironment";

	private static final Set<String> SERVLET_ENVIRONMENT_SOURCE_NAMES;

	static {
		Set<String> names = new HashSet<>();
		names.add(StandardServletEnvironment.SERVLET_CONTEXT_PROPERTY_SOURCE_NAME);
		names.add(StandardServletEnvironment.SERVLET_CONFIG_PROPERTY_SOURCE_NAME);
		names.add(StandardServletEnvironment.JNDI_PROPERTY_SOURCE_NAME);
		SERVLET_ENVIRONMENT_SOURCE_NAMES = Collections.unmodifiableSet(names);
	}

	private final ClassLoader classLoader;

	/**
	 * 创建新的 {@link EnvironmentConverter}，转换过程中使用给定的 {@code classLoader}。
	 *
	 * @param classLoader 要使用的类加载器
	 */
	EnvironmentConverter(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	/**
	 * 将给定的 {@code environment} 转换为指定的 {@link StandardEnvironment} 类型。
	 * 若环境已是相同类型，则不执行转换，直接返回原实例。
	 *
	 * @param environment 要转换的环境
	 * @param type 目标环境类型
	 * @return 转换后的环境
	 */
	ConfigurableEnvironment convertEnvironmentIfNecessary(ConfigurableEnvironment environment,
			Class<? extends ConfigurableEnvironment> type) {
		if (type.equals(environment.getClass())) {
			return environment;
		}
		return convertEnvironment(environment, type);
	}

	private ConfigurableEnvironment convertEnvironment(ConfigurableEnvironment environment,
			Class<? extends ConfigurableEnvironment> type) {
		ConfigurableEnvironment result = createEnvironment(type);
		result.setActiveProfiles(environment.getActiveProfiles());
		result.setConversionService(environment.getConversionService());
		copyPropertySources(environment, result);
		return result;
	}

	private ConfigurableEnvironment createEnvironment(Class<? extends ConfigurableEnvironment> type) {
		try {
			Constructor<? extends ConfigurableEnvironment> constructor = type.getDeclaredConstructor();
			ReflectionUtils.makeAccessible(constructor);
			return constructor.newInstance();
		}
		catch (Exception ex) {
			return new ApplicationEnvironment();
		}
	}

	private void copyPropertySources(ConfigurableEnvironment source, ConfigurableEnvironment target) {
		removePropertySources(target.getPropertySources(), isServletEnvironment(target.getClass(), this.classLoader));
		for (PropertySource<?> propertySource : source.getPropertySources()) {
			if (!SERVLET_ENVIRONMENT_SOURCE_NAMES.contains(propertySource.getName())) {
				target.getPropertySources().addLast(propertySource);
			}
		}
	}

	private boolean isServletEnvironment(Class<?> conversionType, ClassLoader classLoader) {
		try {
			Class<?> webEnvironmentClass = ClassUtils.forName(CONFIGURABLE_WEB_ENVIRONMENT_CLASS, classLoader);
			return webEnvironmentClass.isAssignableFrom(conversionType);
		}
		catch (Throwable ex) {
			return false;
		}
	}

	private void removePropertySources(MutablePropertySources propertySources, boolean isServletEnvironment) {
		Set<String> names = new HashSet<>();
		for (PropertySource<?> propertySource : propertySources) {
			names.add(propertySource.getName());
		}
		for (String name : names) {
			if (!isServletEnvironment || !SERVLET_ENVIRONMENT_SOURCE_NAMES.contains(name)) {
				propertySources.remove(name);
			}
		}
	}

}
