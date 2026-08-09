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

import java.lang.reflect.Method;

import org.springframework.context.ApplicationListener;

/**
 * 为标注了 {@link EventListener} 的方法创建 {@link ApplicationListener} 的策略接口。
 *
 * @author Stephane Nicoll
 * @since 4.2
 */
public interface EventListenerFactory {

	/**
	 * 指定本工厂是否支持给定 {@link Method}。
	 * @param method 标注了 {@link EventListener} 的方法
	 * @return 若本工厂支持该方法则为 {@code true}
	 */
	boolean supportsMethod(Method method);

	/**
	 * 为指定方法创建 {@link ApplicationListener}。
	 * @param beanName Bean 名称
	 * @param type 实例的目标类型
	 * @param method 标注了 {@link EventListener} 的方法
	 * @return 应用监听器，适合调用指定方法
	 */
	ApplicationListener<?> createApplicationListener(String beanName, Class<?> type, Method method);

}
