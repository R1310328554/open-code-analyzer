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
 * 基于 Spring {@link org.springframework.beans.factory.BeanFactory} 的
 * {@link org.springframework.aop.TargetSource} 实现基类，
 * 委托给 Spring 管理的 bean 实例。
 *
 * <p>子类可创建原型实例或延迟访问单例目标等。
 * 具体策略见 {@link LazyInitTargetSource} 及
 * {@link AbstractPrototypeBasedTargetSource} 的子类。
 *
 * <p>基于 BeanFactory 的 TargetSource 可序列化。
 * 这涉及断开当前目标并转为 {@link SingletonTargetSource}。
 *
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

	/** 使用 Spring 1.2.7 的 serialVersionUID 以保持互操作性。 */
	private static final long serialVersionUID = -4721607536018568393L;


	/** 子类可用的 Logger。 */
	protected final transient Log logger = LogFactory.getLog(getClass());

	/** 每次调用时将创建的目标 bean 名称。 */
	protected @Nullable String targetBeanName;

	/** 目标的类。 */
	private volatile @Nullable Class<?> targetClass;

	/**
	 * 拥有本 TargetSource 的 BeanFactory。
	 * 须持有此引用以便必要时创建新原型实例。
	 */
	@SuppressWarnings("serial")
	private @Nullable BeanFactory beanFactory;


	/**
	 * 设置工厂中目标 bean 的名称。
	 * <p>目标 bean 不应为单例，否则始终从工厂获得同一实例，
	 * 行为与 {@link SingletonTargetSource} 相同。
	 * @param targetBeanName 拥有本拦截器的 BeanFactory 中目标 bean 的名称
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
	 * 显式指定目标类，避免任何形式访问目标 bean
	 * （例如避免初始化 FactoryBean 实例）。
	 * <p>默认通过 BeanFactory 的 {@code getType} 调用
	 * （或作为回退的完整 {@code getBean} 调用）自动检测类型。
	 */
	public void setTargetClass(Class<?> targetClass) {
		this.targetClass = targetClass;
	}

	/**
	 * 设置所属的 BeanFactory。
	 * 须保存引用以便每次调用时使用 {@code getBean} 方法。
	 */
	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		if (this.targetBeanName == null) {
			throw new IllegalStateException("Property 'targetBeanName' is required");
		}
		this.beanFactory = beanFactory;
	}

	/**
	 * 返回所属的 BeanFactory。
	 */
	public BeanFactory getBeanFactory() {
		Assert.state(this.beanFactory != null, "BeanFactory not set");
		return this.beanFactory;
	}


	@Override
	public @Nullable Class<?> getTargetClass() {
		Class<?> targetClass = this.targetClass;
		if (targetClass != null) {
			return targetClass;
		}
		synchronized (this) {
			// 在同步块内完整检查，仅一次进入 BeanFactory 交互算法...
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
	 * 从其他 AbstractBeanFactoryBasedTargetSource 对象复制配置。
	 * 若子类希望暴露此方法，应覆盖。
	 * @param other 要复制配置的对象
	 */
	protected void copyFrom(AbstractBeanFactoryBasedTargetSource other) {
		this.targetBeanName = other.targetBeanName;
		this.targetClass = other.targetClass;
		this.beanFactory = other.beanFactory;
	}


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

	@Override
	public int hashCode() {
		return Objects.hash(getClass(), this.targetBeanName);
	}

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
