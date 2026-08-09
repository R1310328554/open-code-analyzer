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

package org.springframework.aop.framework;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.Assert;

/**
 * 代理工厂基类。
 * 提供对可配置 AopProxyFactory 的便捷访问。
 *
 * @author Juergen Hoeller
 * @since 2.0.3
 * @see #createAopProxy()
 */
@SuppressWarnings("serial")
public class ProxyCreatorSupport extends AdvisedSupport {

	private AopProxyFactory aopProxyFactory;

	private final List<AdvisedSupportListener> listeners = new ArrayList<>();

	/** 创建首个 AOP 代理时设为 true。 */
	private boolean active = false;


	/**
	 * 创建新的 ProxyCreatorSupport 实例。
	 */
	public ProxyCreatorSupport() {
		this.aopProxyFactory = DefaultAopProxyFactory.INSTANCE;
	}

	/**
	 * 创建新的 ProxyCreatorSupport 实例。
	 * @param aopProxyFactory 要使用的 AopProxyFactory
	 */
	public ProxyCreatorSupport(AopProxyFactory aopProxyFactory) {
		Assert.notNull(aopProxyFactory, "AopProxyFactory must not be null");
		this.aopProxyFactory = aopProxyFactory;
	}


	/**
	 * 自定义 AopProxyFactory，允许在不改动核心框架的情况下
	 * 插入不同策略。
	 * <p>默认为 {@link DefaultAopProxyFactory}，
	 * 根据需求使用动态 JDK 或 CGLIB 代理。
	 */
	public void setAopProxyFactory(AopProxyFactory aopProxyFactory) {
		Assert.notNull(aopProxyFactory, "AopProxyFactory must not be null");
		this.aopProxyFactory = aopProxyFactory;
	}

	/**
	 * 返回本 ProxyConfig 使用的 AopProxyFactory。
	 */
	public AopProxyFactory getAopProxyFactory() {
		return this.aopProxyFactory;
	}

	/**
	 * 向本代理配置添加给定 AdvisedSupportListener。
	 * @param listener 要注册的监听器
	 */
	public void addListener(AdvisedSupportListener listener) {
		Assert.notNull(listener, "AdvisedSupportListener must not be null");
		this.listeners.add(listener);
	}

	/**
	 * 从本代理配置移除给定 AdvisedSupportListener。
	 * @param listener 要移除的监听器
	 */
	public void removeListener(AdvisedSupportListener listener) {
		Assert.notNull(listener, "AdvisedSupportListener must not be null");
		this.listeners.remove(listener);
	}


	/**
	 * 子类应调用此方法获取新 AOP 代理。
	 * 子类<b>不应</b>以 {@code this} 为参数创建 AOP 代理。
	 */
	protected final synchronized AopProxy createAopProxy() {
		if (!this.active) {
			activate();
		}
		return getAopProxyFactory().createAopProxy(this);
	}

	/**
	 * 激活本代理配置。
	 * @see AdvisedSupportListener#activated
	 */
	private void activate() {
		this.active = true;
		for (AdvisedSupportListener listener : this.listeners) {
			listener.activated(this);
		}
	}

	/**
	 * 将 Advice 变更事件传播给所有 AdvisedSupportListener。
	 * @see AdvisedSupportListener#adviceChanged
	 */
	@Override
	protected void adviceChanged() {
		super.adviceChanged();
		synchronized (this) {
			if (this.active) {
				for (AdvisedSupportListener listener : this.listeners) {
					listener.adviceChanged(this);
				}
			}
		}
	}

	/**
	 * 子类可调用此方法检查是否已创建任何 AOP 代理。
	 */
	protected final synchronized boolean isActive() {
		return this.active;
	}

}
