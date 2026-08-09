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

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;

/**
 * 为 {@link ApplicationContext} 引发的事件提供的基类。
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 2.5
 */
@SuppressWarnings("serial")
public abstract class ApplicationContextEvent extends ApplicationEvent {

	/**
	 * 创建新的 {@code ApplicationContextEvent}。
	 * @param source 事件所针对的 {@link ApplicationContext}
	 * （不得为 {@code null}）
	 */
	public ApplicationContextEvent(ApplicationContext source) {
		super(source);
	}

	/**
	 * 获取事件所针对的 {@link ApplicationContext}。
	 * @return 事件所针对的 {@code ApplicationContext}
	 * @since 7.0
	 * @see #getApplicationContext()
	 */
	@Override
	public ApplicationContext getSource() {
		return getApplicationContext();
	}

	/**
	 * 获取事件所针对的 {@link ApplicationContext}。
	 * @see #getSource()
	 */
	public final ApplicationContext getApplicationContext() {
		return (ApplicationContext) super.getSource();
	}

}
