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

package org.springframework.aop.target;

import java.io.Serializable;
import java.util.Objects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.aop.TargetSource;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * 基于 Spring {@link org.springframework.beans.factory.BeanFactory} 的 {@link
 * org.springframework.aop.TargetSource} 实现的基类，委托给 Spring 管理的 bean 实例。
 * 例如，<p>子类可以创建原型实例或延迟访问单例目标。具体策略请参见 {@link LazyInitTargetSource} 和 {@link AbstractPrototyp
 * eBasedTargetSource} 的子类。
 * 基于 <p>BeanFactory 的 TargetSource 是可序列化的。这涉及断开当前目标并转变为 {@link SingletonTargetSource}。
 * @author Juergen Hoeller
 * @author Rod Johnson
 * @since 1.1.4
 * @see org.springframework.beans.factory.BeanFactory#getBean
 * @see LazyInitTargetSource
 * @see PrototypeTargetSource
 * @see ThreadLocalTargetSource
 * @see CommonsPool2TargetSource
 */
public abstract class AbstractBeanFactoryBasedTargetSource implements TargetSource, BeanFactoryAware, Serializable {

	/**
	 */
	private static final long serialVersionUID = -4721607536018568393L;


	/**
	 */
	protected final transient Log logger = LogFactory.getLog(getClass());

	/**
	 */
	protected @Nullable String targetBeanName;

	/**
	 */
	private volatile @Nullable Class<?> targetClass;

	/**
	 * 拥有此 TargetSource 的 BeanFactory。我们需要保留这个引用，以便我们可以根据需要创建新的原型实例。
	 */
	@SuppressWarnings("serial")
	private @Nullable BeanFactory beanFactory;


	/**
	 * 在工厂中设置目标 bean 的名称。 <p> 目标 bean 不应该是单例，否则将始终从工厂获取相同的实例，从而导致与 {@link SingletonTargetSource
	 * } 提供的行为相同。
	 * @param targetBeanName 拥有此拦截器的 BeanFactory 中目标 bean 的名称
	 * @see SingletonTargetSource
	 */
	public void setTargetBeanName(String targetBeanName) {
		this.targetBeanName = targetBeanName;
	}

	/**
	 * 返回工厂中目标 bean 的名称。
	 */
	public String getTargetBeanName() {
		Assert.state(this.targetBeanName != null, "Target bean name not set");
		return this.targetBeanName;
	}

	/**
	 * 显式指定目标类，以避免对目标 bean 进行任何类型的访问（例如，避免初始化 FactoryBean 实例）。 <p>Default 是通过 BeanFactory 上的 {@
	 * code getType} 调用（或者甚至作为后备的完整 {@code getBean} 调用）自动检测类型。
	 */
	public void setTargetClass(Class<?> targetClass) {
		this.targetClass = targetClass;
	}

	/**
	 * 设置所属的 BeanFactory。我们需要保存一个引用，以便我们可以在每次调用时使用 {@code getBean} 方法。
	 */
	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		if (this.targetBeanName == null) {
			throw new IllegalStateException("Property 'targetBeanName' is required");
		}
		this.beanFactory = beanFactory;
	}

	/**
	 * 返回拥有的 BeanFactory。
	 */
	public BeanFactory getBeanFactory() {
		Assert.state(this.beanFactory != null, "BeanFactory not set");
		return this.beanFactory;
	}


	/**
	 * 获取 Target Class（`TargetClass`）。
	 */
	@Override
	public @Nullable Class<?> getTargetClass() {
		Class<?> targetClass = this.targetClass;
		if (targetClass != null) {
			return targetClass;
		}
		synchronized (this) {
			// 同步内全面检查，仅进入一次BeanFactory交互算法...
			targetClass = this.targetClass;
			if (targetClass == null && this.beanFactory != null && this.targetBeanName != null) {
				// 确定目标 bean 的类型。
				targetClass = this.beanFactory.getType(this.targetBeanName);
				if (targetClass == null) {
					if (logger.isTraceEnabled()) {
						logger.trace("Getting bean with name '" + this.targetBeanName + "' for type determination");
					}
					Object beanInstance = this.beanFactory.getBean(this.targetBeanName);
					targetClass = beanInstance.getClass();
				}
				this.targetClass = targetClass;
			}
			return targetClass;
		}
	}


	/**
	 * 从其他 AbstractBeanFactoryBasedTargetSource 对象复制配置。如果子类希望公开它，则应该覆盖它。
	 * @param other 从中复制配置的对象
	 */
	protected void copyFrom(AbstractBeanFactoryBasedTargetSource other) {
		this.targetBeanName = other.targetBeanName;
		this.targetClass = other.targetClass;
		this.beanFactory = other.beanFactory;
	}


	/**
	 * 比较是否相等。
	 */
	@Override
	public boolean equals(@Nullable Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}
		AbstractBeanFactoryBasedTargetSource otherTargetSource = (AbstractBeanFactoryBasedTargetSource) other;
		return (ObjectUtils.nullSafeEquals(this.beanFactory, otherTargetSource.beanFactory) &&
				ObjectUtils.nullSafeEquals(this.targetBeanName, otherTargetSource.targetBeanName));
	}

	/**
	 * 判断是否包含/具备 h Code。
	 */
	@Override
	public int hashCode() {
		return Objects.hash(getClass(), this.targetBeanName);
	}

	/**
	 * 返回字符串表示。
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(getClass().getSimpleName());
		sb.append(" for target bean '").append(this.targetBeanName).append('\'');
		Class<?> targetClass = this.targetClass;
		if (targetClass != null) {
			sb.append(" of type [").append(targetClass.getName()).append(']');
		}
		return sb.toString();
	}

}
