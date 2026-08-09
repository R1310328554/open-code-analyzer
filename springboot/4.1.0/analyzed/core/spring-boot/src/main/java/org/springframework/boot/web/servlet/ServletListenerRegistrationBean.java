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

package org.springframework.boot.web.servlet;

import java.util.Collections;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Set;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextAttributeListener;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletRequestAttributeListener;
import jakarta.servlet.ServletRequestListener;
import jakarta.servlet.http.HttpSessionAttributeListener;
import jakarta.servlet.http.HttpSessionIdListener;
import jakarta.servlet.http.HttpSessionListener;
import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * 在 Servlet 3.0+ 容器中注册 {@link EventListener} 的 {@link ServletContextInitializer}。
 * 功能类似 {@link ServletContext} 提供的 {@link ServletContext#addListener(EventListener) 注册}能力，
 * 但采用对 Spring Bean 更友好的设计。
 * <p>
 * 此 Bean 可用于注册以下类型的监听器：
 * <ul>
 * <li>{@link ServletContextAttributeListener}</li>
 * <li>{@link ServletRequestListener}</li>
 * <li>{@link ServletRequestAttributeListener}</li>
 * <li>{@link HttpSessionAttributeListener}</li>
 * <li>{@link HttpSessionIdListener}</li>
 * <li>{@link HttpSessionListener}</li>
 * <li>{@link ServletContextListener}</li>
 * </ul>
 *
 * @param <T> the type of listener 监听器类型
 * @author Dave Syer
 * @author Phillip Webb
 * @since 1.4.0
 */
public class ServletListenerRegistrationBean<T extends EventListener> extends RegistrationBean {

	private static final Set<Class<?>> SUPPORTED_TYPES;

	static {
		Set<Class<?>> types = new HashSet<>();
		types.add(ServletContextAttributeListener.class);
		types.add(ServletRequestListener.class);
		types.add(ServletRequestAttributeListener.class);
		types.add(HttpSessionAttributeListener.class);
		types.add(HttpSessionIdListener.class);
		types.add(HttpSessionListener.class);
		types.add(ServletContextListener.class);
		SUPPORTED_TYPES = Collections.unmodifiableSet(types);
	}

	private @Nullable T listener;

	/**
	 * 创建新的 {@link ServletListenerRegistrationBean} 实例。
	 */
	public ServletListenerRegistrationBean() {
	}

	/**
	 * 创建新的 {@link ServletListenerRegistrationBean} 实例。
	 *
	 * @param listener the listener to register 待注册的监听器
	 */
	public ServletListenerRegistrationBean(T listener) {
		Assert.notNull(listener, "'listener' must not be null");
		Assert.isTrue(isSupportedType(listener), "'listener' is not of a supported type");
		this.listener = listener;
	}

	/**
	 * 设置待注册的监听器。
	 *
	 * @param listener the listener to register 待注册的监听器
	 */
	public void setListener(T listener) {
		Assert.notNull(listener, "'listener' must not be null");
		Assert.isTrue(isSupportedType(listener), "'listener' is not of a supported type");
		this.listener = listener;
	}

	/**
	 * 返回待注册的监听器。
	 *
	 * @return the listener to be registered 待注册的监听器
	 */
	public @Nullable T getListener() {
		return this.listener;
	}

	@Override
	protected String getDescription() {
		Assert.notNull(this.listener, "'listener' must not be null");
		return "listener " + this.listener;
	}

	@Override
	protected void register(String description, ServletContext servletContext) {
		try {
			servletContext.addListener(this.listener);
		}
		catch (RuntimeException ex) {
			throw new IllegalStateException("Failed to add listener '" + this.listener + "' to servlet context", ex);
		}
	}

	/**
	 * 若指定监听器属于支持的类型则返回 {@code true}。
	 *
	 * @param listener the listener to test 待检测的监听器
	 * @return if the listener is of a supported type 是否为支持的类型
	 */
	public static boolean isSupportedType(EventListener listener) {
		for (Class<?> type : SUPPORTED_TYPES) {
			if (ClassUtils.isAssignableValue(type, listener)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 返回此注册支持的监听器类型集合。
	 *
	 * @return the supported types 支持的类型集合
	 */
	public static Set<Class<?>> getSupportedTypes() {
		return SUPPORTED_TYPES;
	}

}
