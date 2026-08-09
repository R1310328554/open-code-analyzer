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
 * 基于 BeanFactory 的抽象 PointcutAdvisor，允许将任何 Advice 配置为对 BeanFactory 中的 Advice bean 的引用。
 * <p>指定通知 bean 的名称而不是通知对象本身（如果在 BeanFactory 中运行）会增加初始化时的松散耦合，以便在切入点实际匹配之前不初始化通知对象。
 * @author Juergen Hoeller
 * @since 2.0.2
 * @see #setAdviceBeanName
 * @see DefaultBeanFactoryPointcutAdvisor
 */
@SuppressWarnings("serial")
public abstract class AbstractBeanFactoryPointcutAdvisor extends AbstractPointcutAdvisor implements BeanFactoryAware {

	/** 名称相关状态（`adviceBeanName`）。 */
	private @Nullable String adviceBeanName;

	/** 底层 BeanFactory 引用。 */
	private @Nullable BeanFactory beanFactory;

	/** 通知相关状态（`advice`）。 */
	private transient volatile @Nullable Advice advice;

	/**
	 * 方法 `Object`：完成本类中与「Object」相关的职责。
	 */
	private transient Object adviceMonitor = new Object();


	/**
	 * 指定该顾问程序应引用的建议 bean 的名称。 <p>A 指定 bean 的实例将在第一次访问该顾问的建议时获得。该顾问程序最多只能获取一个建议 bean 实例，并在顾问程序的
	 * 生命周期内缓存该实例。
	 * @see #getAdvice()
	 */
	public void setAdviceBeanName(@Nullable String adviceBeanName) {
		this.adviceBeanName = adviceBeanName;
	}

	/**
	 * 返回该顾问程序引用的建议 bean 的名称（如果有）。
	 */
	public @Nullable String getAdviceBeanName() {
		return this.adviceBeanName;
	}

	/**
	 * 设置 Bean Factory（`BeanFactory`）。
	 */
	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	/**
	 * 直接指定目标建议的特定实例，避免 {@link #getAdvice()} 中的延迟解析。
	 * @since 3.1
	 */
	public void setAdvice(Advice advice) {
		synchronized (this.adviceMonitor) {
			this.advice = advice;
		}
	}

	/**
	 * 获取 Advice（`Advice`）。
	 */
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
			// 工厂没有单例保证 -> 让我们在本地锁定。
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

	/**
	 * 返回字符串表示。
	 */
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

	/**
	 * 方法 `readObject`：完成本类中与「read Object」相关的职责。
	 */
	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		// 依赖默认序列化，只需在反序列化后初始化状态即可。
		ois.defaultReadObject();

		// 初始化瞬态字段。
		this.adviceMonitor = new Object();
	}

}
