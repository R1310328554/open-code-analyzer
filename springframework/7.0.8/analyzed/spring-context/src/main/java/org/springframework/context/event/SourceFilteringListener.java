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

import org.jspecify.annotations.Nullable;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;

/**
 * 过滤指定事件源的 {@link org.springframework.context.ApplicationListener} 装饰器，
 * 仅对匹配的 {@link org.springframework.context.ApplicationEvent} 对象调用其委托监听器。
 *
 * <p>也可作为基类使用，重写 {@link #onApplicationEventInternal} 方法，
 * 而非指定委托监听器。
 *
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @since 2.0.5
 */
public class SourceFilteringListener implements GenericApplicationListener {

	/** 本监听器只处理来自该源的事件。 */
	private final Object source;

	/** 实际处理事件的委托监听器（子类可省略）。 */
	private @Nullable GenericApplicationListener delegate;


	/**
	 * 为给定事件源创建 SourceFilteringListener。
	 * @param source 本监听器过滤的事件源，仅处理来自该源的事件
	 * @param delegate 对来自指定源的事件进行调用的委托监听器
	 */
	public SourceFilteringListener(Object source, ApplicationListener<?> delegate) {
		this.source = source;
		this.delegate = (delegate instanceof GenericApplicationListener gal ? gal :
				new GenericApplicationListenerAdapter(delegate));
	}

	/**
	 * 为给定事件源创建 SourceFilteringListener，
	 * 期望子类重写 {@link #onApplicationEventInternal} 方法（而非指定委托监听器）。
	 * @param source 本监听器过滤的事件源，仅处理来自该源的事件
	 */
	protected SourceFilteringListener(Object source) {
		this.source = source;
	}


	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (event.getSource() == this.source) {
			onApplicationEventInternal(event);
		}
	}

	@Override
	public boolean supportsEventType(ResolvableType eventType) {
		return (this.delegate == null || this.delegate.supportsEventType(eventType));
	}

	@Override
	public boolean supportsSourceType(@Nullable Class<?> sourceType) {
		return (sourceType != null && sourceType.isInstance(this.source));
	}

	@Override
	public int getOrder() {
		return (this.delegate != null ? this.delegate.getOrder() : Ordered.LOWEST_PRECEDENCE);
	}

	@Override
	public String getListenerId() {
		return (this.delegate != null ? this.delegate.getListenerId() : "");
	}


	/**
	 * 在已按期望事件源过滤后，实际处理事件。
	 * <p>默认实现调用指定的委托监听器（若有）。
	 * @param event 要处理的事件（匹配指定源）
	 */
	protected void onApplicationEventInternal(ApplicationEvent event) {
		if (this.delegate == null) {
			throw new IllegalStateException(
					"Must specify a delegate object or override the onApplicationEventInternal method");
		}
		this.delegate.onApplicationEvent(event);
	}

}
