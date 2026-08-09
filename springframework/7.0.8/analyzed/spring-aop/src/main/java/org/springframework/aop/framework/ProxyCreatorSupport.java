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
 * 代理工厂的基类。提供对可配置 AopProxyFactory 的便捷访问。
 * @author Juergen Hoeller
 * @since 2.0.3
 * @see #createAopProxy()
 */
@SuppressWarnings("serial")
public class ProxyCreatorSupport extends AdvisedSupport {

	/** 工厂相关状态（`aopProxyFactory`）。 */
	private AopProxyFactory aopProxyFactory;

	private final List<AdvisedSupportListener> listeners = new ArrayList<>();

	/**
	 */
	private boolean active = false;


	/**
	 * 创建一个新的 ProxyCreatorSupport 实例。
	 */
	public ProxyCreatorSupport() {
		this.aopProxyFactory = DefaultAopProxyFactory.INSTANCE;
	}

	/**
	 * 创建一个新的 ProxyCreatorSupport 实例。
	 * @param aopProxyFactory 要使用的 AopProxyFactory
	 */
	public ProxyCreatorSupport(AopProxyFactory aopProxyFactory) {
		Assert.notNull(aopProxyFactory, "AopProxyFactory must not be null");
		this.aopProxyFactory = aopProxyFactory;
	}


	/**
	 * 自定义AopProxyFactory，允许在不改变核心框架的情况下放入不同的策略。 <p>Default 为 {@link DefaultAopProxyFactory}，根据
	 * 需求使用动态 JDK 代理或 CGLIB 代理。
	 */
	public void setAopProxyFactory(AopProxyFactory aopProxyFactory) {
		Assert.notNull(aopProxyFactory, "AopProxyFactory must not be null");
		this.aopProxyFactory = aopProxyFactory;
	}

	/**
	 * 返回此 ProxyConfig 使用的 AopProxyFactory。
	 */
	public AopProxyFactory getAopProxyFactory() {
		return this.aopProxyFactory;
	}

	/**
	 * 将给定的 AdvisedSupportListener 添加到此代理配置中。
	 * @param listener 监听者注册
	 */
	public void addListener(AdvisedSupportListener listener) {
		Assert.notNull(listener, "AdvisedSupportListener must not be null");
		this.listeners.add(listener);
	}

	/**
	 * 从此代理配置中删除给定的 AdvisedSupportListener。
	 * @param listener 要删除的侦听器
	 */
	public void removeListener(AdvisedSupportListener listener) {
		Assert.notNull(listener, "AdvisedSupportListener must not be null");
		this.listeners.remove(listener);
	}


	/**
	 * 子类应该调用它来获取新的 AOP 代理。他们应该 <b>not</b> 创建一个以 {@code this} 作为参数的 AOP 代理。
	 */
	protected final synchronized AopProxy createAopProxy() {
		if (!this.active) {
			activate();
		}
		return getAopProxyFactory().createAopProxy(this);
	}

	/**
	 * 激活此代理配置。
	 * @see AdvisedSupportListener#activated
	 */
	private void activate() {
		this.active = true;
		for (AdvisedSupportListener listener : this.listeners) {
			listener.activated(this);
		}
	}

	/**
	 * 将建议更改事件传播到所有 AdvisedSupportListener。
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
	 * 子类可以调用它来检查是否已创建任何 AOP 代理。
	 */
	protected final synchronized boolean isActive() {
		return this.active;
	}

}
