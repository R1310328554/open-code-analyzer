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

package org.springframework.boot.builder;

import java.lang.ref.WeakReference;

import org.springframework.beans.BeansException;
import org.springframework.boot.builder.ParentContextApplicationContextInitializer.ParentContextAvailableEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.Ordered;
import org.springframework.util.ObjectUtils;

/**
 * 当父上下文关闭时关闭应用上下文的监听器。监听刷新事件获取当前上下文，
 * 再监听关闭事件并沿层次结构向下传播。
 *
 * @author Dave Syer
 * @author Eric Bottard
 * @since 1.0.0
 */
public class ParentContextCloserApplicationListener
		implements ApplicationListener<ParentContextAvailableEvent>, ApplicationContextAware, Ordered {

	private static final int ORDER = Ordered.LOWEST_PRECEDENCE - 10;

	@SuppressWarnings("NullAway.Init")
	private ApplicationContext context;

	@Override
	public int getOrder() {
		return ORDER;
	}

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		this.context = context;
	}

	@Override
	public void onApplicationEvent(ParentContextAvailableEvent event) {
		maybeInstallListenerInParent(event.getApplicationContext());
	}

	private void maybeInstallListenerInParent(ConfigurableApplicationContext child) {
		if (child == this.context && child.getParent() instanceof ConfigurableApplicationContext parent) {
			parent.addApplicationListener(createContextCloserListener(child));
		}
	}

	/**
	 * 子类可覆盖以创建自定义 ContextCloserListener 子类，仍强制使用弱引用。
	 * @param child 子上下文
	 * @return 要使用的 {@link ContextCloserListener}
	 */
	protected ContextCloserListener createContextCloserListener(ConfigurableApplicationContext child) {
		return new ContextCloserListener(child);
	}

	/**
	 * 用于关闭上下文的 {@link ApplicationListener}。
	 */
	protected static class ContextCloserListener implements ApplicationListener<ContextClosedEvent> {

		private final WeakReference<ConfigurableApplicationContext> childContext;

		public ContextCloserListener(ConfigurableApplicationContext childContext) {
			this.childContext = new WeakReference<>(childContext);
		}

		@Override
		public void onApplicationEvent(ContextClosedEvent event) {
			ConfigurableApplicationContext context = this.childContext.get();
			if ((context != null) && (event.getApplicationContext() == context.getParent()) && context.isActive()) {
				context.close();
			}
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (obj instanceof ContextCloserListener other) {
				return ObjectUtils.nullSafeEquals(this.childContext.get(), other.childContext.get());
			}
			return super.equals(obj);
		}

		@Override
		public int hashCode() {
			return ObjectUtils.nullSafeHashCode(this.childContext.get());
		}

	}

}
