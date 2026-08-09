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

package org.springframework.aop.support;

import java.io.IOException;
import java.io.ObjectInputStream;

import org.aopalliance.aop.Advice;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.util.Assert;

/**
 * 基于 BeanFactory 的抽象 PointcutAdvisor，
 * 允许将任意 Advice 配置为 BeanFactory 中 Advice Bean 的引用。
 *
 * <p>在 BeanFactory 环境中指定 advice Bean 名称而非 advice 对象本身，
 * 可在初始化时提高松耦合，直到切入点实际匹配时才初始化 advice 对象。
 *
 * @author Juergen Hoeller
 * @since 2.0.2
 * @see #setAdviceBeanName
 * @see DefaultBeanFactoryPointcutAdvisor
 */
@SuppressWarnings("serial")
public abstract class AbstractBeanFactoryPointcutAdvisor extends AbstractPointcutAdvisor implements BeanFactoryAware {

	private @Nullable String adviceBeanName;

	private @Nullable BeanFactory beanFactory;

	private transient volatile @Nullable Advice advice;

	private transient Object adviceMonitor = new Object();


	/**
	 * 指定本 advisor 应引用的 advice Bean 名称。
	 * <p>首次访问本 advisor 的 advice 时将获取指定 Bean 的实例。
	 * 本 advisor 最多只获取一个 advice Bean 实例，
	 * 并在 advisor 生命周期内缓存该实例。
	 * @see #getAdvice()
	 */
	public void setAdviceBeanName(@Nullable String adviceBeanName) {
		this.adviceBeanName = adviceBeanName;
	}

	/**
	 * 返回本 advisor 引用的 advice Bean 名称（若有）。
	 */
	public @Nullable String getAdviceBeanName() {
		return this.adviceBeanName;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	/**
	 * 直接指定目标 advice 的特定实例，
	 * 避免在 {@link #getAdvice()} 中进行懒解析。
	 * @since 3.1
	 */
	public void setAdvice(Advice advice) {
		synchronized (this.adviceMonitor) {
			this.advice = advice;
		}
	}

	@Override
	public Advice getAdvice() {
		Advice advice = this.advice;
		if (advice != null) {
			return advice;
		}

		Assert.state(this.adviceBeanName != null, "'adviceBeanName' must be specified");
		Assert.state(this.beanFactory != null, "BeanFactory must be set to resolve 'adviceBeanName'");

		if (this.beanFactory.isSingleton(this.adviceBeanName)) {
			// 依赖工厂提供的单例语义。
			advice = this.beanFactory.getBean(this.adviceBeanName, Advice.class);
			this.advice = advice;
			return advice;
		}
		else {
			// 工厂无单例保证 -> 在本地加锁。
			synchronized (this.adviceMonitor) {
				advice = this.advice;
				if (advice == null) {
					advice = this.beanFactory.getBean(this.adviceBeanName, Advice.class);
					this.advice = advice;
				}
				return advice;
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(getClass().getName());
		sb.append(": advice ");
		if (this.adviceBeanName != null) {
			sb.append("bean '").append(this.adviceBeanName).append('\'');
		}
		else {
			sb.append(this.advice);
		}
		return sb.toString();
	}


	//---------------------------------------------------------------------
	// 序列化支持
	//---------------------------------------------------------------------

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		// 依赖默认序列化，反序列化后仅初始化状态。
		ois.defaultReadObject();

		// 初始化 transient 字段。
		this.adviceMonitor = new Object();
	}

}
