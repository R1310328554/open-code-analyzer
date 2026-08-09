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

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

/**
 * 动态 {@link org.springframework.aop.TargetSource} 实现的基类，
 * 创建新的原型 Bean 实例以支持池化或每次调用新建实例的策略。
 *
 * <p>此类 TargetSource 须在 {@link BeanFactory} 中运行，
 * 因需调用 {@code getBean} 创建新原型实例。
 * 因此本基类继承 {@link AbstractBeanFactoryBasedTargetSource}。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.beans.factory.BeanFactory#getBean
 * @see PrototypeTargetSource
 * @see ThreadLocalTargetSource
 * @see CommonsPool2TargetSource
 */
@SuppressWarnings("serial")
public abstract class AbstractPrototypeBasedTargetSource extends AbstractBeanFactoryBasedTargetSource {

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		super.setBeanFactory(beanFactory);

		// 检查目标 Bean 是否定义为 prototype。
		if (!beanFactory.isPrototype(getTargetBeanName())) {
			throw new BeanDefinitionStoreException(
					"Cannot use prototype-based TargetSource against non-prototype bean with name '" +
					this.targetBeanName + "': instances would not be independent");
		}
	}

	/**
	 * 子类应调用本方法创建新原型实例。
	 * @throws BeansException 若 Bean 创建失败
	 */
	protected Object newPrototypeInstance() throws BeansException {
		if (logger.isDebugEnabled()) {
			logger.debug("Creating new instance of bean '" + this.targetBeanName + "'");
		}
		return getBeanFactory().getBean(getTargetBeanName());
	}

	/**
	 * 子类应调用本方法销毁过时的原型实例。
	 * @param target 待销毁的 Bean 实例
	 */
	protected void destroyPrototypeInstance(Object target) {
		if (logger.isDebugEnabled()) {
			logger.debug("Destroying instance of bean '" + this.targetBeanName + "'");
		}
		if (getBeanFactory() instanceof ConfigurableBeanFactory cbf) {
			cbf.destroyBean(getTargetBeanName(), target);
		}
		else if (target instanceof DisposableBean disposableBean) {
			try {
				disposableBean.destroy();
			}
			catch (Throwable ex) {
				logger.warn("Destroy method on bean with name '" + this.targetBeanName + "' threw an exception", ex);
			}
		}
	}


	//---------------------------------------------------------------------
	// 序列化支持
	//---------------------------------------------------------------------

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		throw new NotSerializableException("A prototype-based TargetSource itself is not deserializable - " +
				"just a disconnected SingletonTargetSource or EmptyTargetSource is");
	}

	/**
	 * 序列化时用 SingletonTargetSource 替换本对象。
	 * 设为 protected，否则子类无法调用。
	 * （{@code writeReplace()} 须对正在序列化的类可见。）
	 * <p>采用本实现后，无需将本类或子类中不可序列化字段标记为 transient。
	 */
	protected Object writeReplace() throws ObjectStreamException {
		if (logger.isDebugEnabled()) {
			logger.debug("Disconnecting TargetSource [" + this + "]");
		}
		try {
			// 创建断开的 SingletonTargetSource/EmptyTargetSource。
			Object target = getTarget();
			return (target != null ? new SingletonTargetSource(target) :
					EmptyTargetSource.forClass(getTargetClass()));
		}
		catch (Exception ex) {
			String msg = "Cannot get target for disconnecting TargetSource [" + this + "]";
			logger.error(msg, ex);
			throw new NotSerializableException(msg + ": " + ex);
		}
	}

}
