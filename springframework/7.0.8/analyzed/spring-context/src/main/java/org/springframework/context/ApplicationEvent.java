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

package org.springframework.context;

import java.time.Clock;
import java.util.EventObject;

/**
 * 所有应用事件应继承的基类。为抽象类，因为通用事件不应直接发布。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.context.ApplicationListener
 * @see org.springframework.context.event.EventListener
 */
public abstract class ApplicationEvent extends EventObject {

	/** 为与 Spring 1.2 互操作而使用的 serialVersionUID。 */
	private static final long serialVersionUID = 7099057708183571937L;

	/** 事件发生时的系统时间。 */
	private final long timestamp;


	/**
	 * 创建新的 {@code ApplicationEvent}，其 {@link #getTimestamp() 时间戳}
	 * 设为 {@link System#currentTimeMillis()}。
	 * @param source the object on which the event initially occurred or with
	 * which the event is associated (never {@code null})
	 * @see #ApplicationEvent(Object, Clock)
	 */
	public ApplicationEvent(Object source) {
		super(source);
		this.timestamp = System.currentTimeMillis();
	}

	/**
	 * 创建新的 {@code ApplicationEvent}，其 {@link #getTimestamp() 时间戳}
	 * 设为所提供 {@link Clock} 的 {@link Clock#millis()} 返回值。
	 * <p>此构造器通常用于测试场景。
	 * @param source the object on which the event initially occurred or with
	 * which the event is associated (never {@code null})
	 * @param clock a clock which will provide the timestamp
	 * @since 5.3.8
	 * @see #ApplicationEvent(Object)
	 */
	public ApplicationEvent(Object source, Clock clock) {
		super(source);
		this.timestamp = clock.millis();
	}


	/**
	 * 返回事件发生时的毫秒时间戳。
	 * @see #ApplicationEvent(Object)
	 * @see #ApplicationEvent(Object, Clock)
	 */
	public final long getTimestamp() {
		return this.timestamp;
	}

}
