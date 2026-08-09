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

package org.springframework.context.event;

import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.ResolvableType;

/**
 * 由可管理多个 {@link ApplicationListener} 对象并向其发布事件的对象实现的接口。
 *
 * <p>{@link org.springframework.context.ApplicationEventPublisher}（通常为 Spring
 * {@link org.springframework.context.ApplicationContext}）可将
 * {@code ApplicationEventMulticaster} 作为实际发布事件的委托。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @see ApplicationListener
 */
public interface ApplicationEventMulticaster {

	/**
	 * 添加监听所有事件的监听器。
	 * @param listener 要添加的监听器
	 * @see #removeApplicationListener(ApplicationListener)
	 * @see #removeApplicationListeners(Predicate)
	 */
	void addApplicationListener(ApplicationListener<?> listener);

	/**
	 * 添加监听所有事件的监听器 Bean。
	 * @param listenerBeanName 要添加的监听器 Bean 名称
	 * @see #removeApplicationListenerBean(String)
	 * @see #removeApplicationListenerBeans(Predicate)
	 */
	void addApplicationListenerBean(String listenerBeanName);

	/**
	 * 从通知列表中移除监听器。
	 * @param listener 要移除的监听器
	 * @see #addApplicationListener(ApplicationListener)
	 * @see #removeApplicationListeners(Predicate)
	 */
	void removeApplicationListener(ApplicationListener<?> listener);

	/**
	 * 从通知列表中移除监听器 Bean。
	 * @param listenerBeanName 要移除的监听器 Bean 名称
	 * @see #addApplicationListenerBean(String)
	 * @see #removeApplicationListenerBeans(Predicate)
	 */
	void removeApplicationListenerBean(String listenerBeanName);

	/**
	 * 从已注册的 {@code ApplicationListener} 实例集合中移除所有匹配的监听器
	 * （包括适配器类，例如用于带 {@link EventListener} 注解方法的
	 * {@link ApplicationListenerMethodAdapter}）。
	 * <p>注意：仅适用于实例注册，不适用于按 Bean 名称注册的监听器。
	 * @param predicate 用于识别待移除监听器实例的谓词，
	 * 例如检查 {@link SmartApplicationListener#getListenerId()}
	 * @since 5.3.5
	 * @see #addApplicationListener(ApplicationListener)
	 * @see #removeApplicationListener(ApplicationListener)
	 */
	void removeApplicationListeners(Predicate<ApplicationListener<?>> predicate);

	/**
	 * 从已注册的监听器 Bean 名称集合中移除所有匹配的监听器 Bean
	 * （这些名称指向直接实现 {@link ApplicationListener} 接口的 Bean 类）。
	 * <p>注意：仅适用于 Bean 名称注册，不适用于以编程方式注册的
	 * {@code ApplicationListener} 实例。
	 * @param predicate 用于识别待移除监听器 Bean 名称的谓词
	 * @since 5.3.5
	 * @see #addApplicationListenerBean(String)
	 * @see #removeApplicationListenerBean(String)
	 */
	void removeApplicationListenerBeans(Predicate<String> predicate);

	/**
	 * 移除向本多播器注册的所有监听器。
	 * <p>调用移除后，在新监听器注册之前，多播器不会对事件通知执行任何操作。
	 * @see #removeApplicationListeners(Predicate)
	 */
	void removeAllListeners();

	/**
	 * 将给定应用事件多播给合适的监听器。
	 * <p>如有可能，请考虑使用
	 * {@link #multicastEvent(ApplicationEvent, ResolvableType)}，
	 * 因其对基于泛型的事件支持更好。
	 * <p>若匹配的 {@code ApplicationListener} 不支持异步执行，
	 * 则必须在本次多播调用的调用线程中运行。
	 * @param event 要多播的事件
	 * @see ApplicationListener#supportsAsyncExecution()
	 */
	void multicastEvent(ApplicationEvent event);

	/**
	 * 将给定应用事件多播给合适的监听器。
	 * <p>若 {@code eventType} 为 {@code null}，则根据 {@code event}
	 * 实例构建默认类型。
	 * <p>若匹配的 {@code ApplicationListener} 不支持异步执行，
	 * 则必须在本次多播调用的调用线程中运行。
	 * @param event 要多播的事件
	 * @param eventType 事件类型（可为 {@code null}）
	 * @since 4.2
	 * @see ApplicationListener#supportsAsyncExecution()
	 */
	void multicastEvent(ApplicationEvent event, @Nullable ResolvableType eventType);

}
