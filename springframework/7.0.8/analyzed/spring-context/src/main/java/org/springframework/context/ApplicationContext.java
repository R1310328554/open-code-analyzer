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

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * 为应用程序提供配置的核心接口。
 * 应用运行期间为只读，若实现支持则可重新加载。
 *
 * <p>ApplicationContext 提供：
 * <ul>
 * <li>访问应用组件的 Bean 工厂方法。
 * 继承自 {@link org.springframework.beans.factory.ListableBeanFactory}。
 * <li>以通用方式加载文件资源的能力。
 * 继承自 {@link org.springframework.core.io.ResourceLoader} 接口。
 * <li>向已注册监听器发布事件的能力。
 * 继承自 {@link ApplicationEventPublisher} 接口。
 * <li>解析消息并支持国际化的能力。
 * 继承自 {@link MessageSource} 接口。
 * <li>从父上下文继承。子上下文中的定义始终优先。
 * 例如，整个 Web 应用可共用一个父上下文，而每个 Servlet 拥有彼此独立的子上下文。
 * </ul>
 *
 * <p>除标准 {@link org.springframework.beans.factory.BeanFactory}
 * 生命周期能力外，ApplicationContext 实现还会检测并调用
 * {@link ApplicationContextAware} Bean，以及
 * {@link ResourceLoaderAware}、{@link ApplicationEventPublisherAware}
 * 和 {@link MessageSourceAware} Bean。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see ConfigurableApplicationContext
 * @see org.springframework.beans.factory.BeanFactory
 * @see org.springframework.core.io.ResourceLoader
 */
public interface ApplicationContext extends EnvironmentCapable, ListableBeanFactory, HierarchicalBeanFactory,
		MessageSource, ApplicationEventPublisher, ResourcePatternResolver {

	/**
	 * 返回本应用上下文的唯一 ID。
	 * @return the unique id of the context (never null as of 7.0.2)
	 */
	String getId();

	/**
	 * 返回本上下文所属已部署应用的名称。
	 * @return a name for the deployed application, or the empty String by default
	 */
	String getApplicationName();

	/**
	 * 返回本上下文的友好名称。
	 * @return a display name for this context (never {@code null})
	 */
	String getDisplayName();

	/**
	 * 返回本上下文首次加载时的时间戳。
	 * @return the timestamp (ms) when this context was first loaded
	 */
	long getStartupDate();

	/**
	 * 返回父上下文；若无父级且本上下文为层次根节点则返回 {@code null}。
	 * @return the parent context, or {@code null} if there is no parent
	 */
	@Nullable ApplicationContext getParent();

	/**
	 * 暴露本上下文的 AutowireCapableBeanFactory 功能。
	 * <p>应用代码通常不直接使用，除非需要为容器外的 Bean 实例
	 * 初始化并（完全或部分）应用 Spring Bean 生命周期。
	 * <p>或者，{@link ConfigurableApplicationContext} 暴露的内部 BeanFactory
	 * 也可访问 {@link AutowireCapableBeanFactory}。本方法主要作为
	 * ApplicationContext 接口上的便捷专用入口。
	 * <p><b>注意：自 4.2 起，应用上下文关闭后本方法将一致抛出 IllegalStateException。</b>
	 * 在当前 Spring Framework 版本中，仅可刷新的应用上下文如此行为；
	 * 自 4.2 起，所有应用上下文实现均需遵守。
	 * @return the AutowireCapableBeanFactory for this context
	 * @throws IllegalStateException if the context does not support the
	 * {@link AutowireCapableBeanFactory} interface, or does not hold an
	 * autowire-capable bean factory yet (for example, if {@code refresh()} has
	 * never been called), or if the context has been closed already
	 * @see ConfigurableApplicationContext#refresh()
	 * @see ConfigurableApplicationContext#getBeanFactory()
	 */
	AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws IllegalStateException;

}
