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
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 当 {@code ApplicationContext} 重启时引发的事件。
 *
 * <p>注意：{@code ContextRestartedEvent} 是 {@link ContextStartedEvent} 的特化。
 *
 * @author Sam Brannen
 * @since 7.0
 * @see ConfigurableApplicationContext#restart()
 * @see ContextPausedEvent
 * @see ContextStartedEvent
 */
@SuppressWarnings("serial")
public class ContextRestartedEvent extends ContextStartedEvent {

	/**
	 * 创建新的 {@code ContextRestartedEvent}。
	 * @param source 已重启的 {@code ApplicationContext}
	 * （不得为 {@code null}）
	 */
	public ContextRestartedEvent(ApplicationContext source) {
		super(source);
	}

}
