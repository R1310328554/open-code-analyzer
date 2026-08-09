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
 * 动态 {@link org.springframework.aop.TargetSource} 实现的基类，用于创建新的原型 bean 实例以支持池或每次调用新实例策略。
 * <p>这样的 TargetSource 必须在 {@link BeanFactory} 中运行，因为它需要调用 {@code getBean}
 * 方法来创建新的原型实例。因此，这个基类扩展了{@link AbstractBeanFactoryBasedTargetSource}。
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.beans.factory.BeanFactory#getBean
 * @see PrototypeTargetSource
 * @see ThreadLocalTargetSource
 * @see CommonsPool2TargetSource
 */
@SuppressWarnings("serial")
public abstract class AbstractPrototypeBasedTargetSource extends AbstractBeanFactoryBasedTargetSource {

	/**
	 * 设置 Bean Factory（`BeanFactory`）。
	 */
	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		super.setBeanFactory(beanFactory);

		// 检查目标bean是否被定义为prototype。
		if (!beanFactory.isPrototype(getTargetBeanName())) {
			throw new BeanDefinitionStoreException(
					"Cannot use prototype-based TargetSource against non-prototype bean with name '" +
					this.targetBeanName + "': instances would not be independent");
		}
	}

	/**
	 * 子类应该调用此方法来创建新的原型实例。
	 * @throws BeansException 如果bean创建失败
	 */
	protected Object newPrototypeInstance() throws BeansException {
		if (logger.isDebugEnabled()) {
			logger.debug("Creating new instance of bean '" + this.targetBeanName + "'");
		}
		return getBeanFactory().getBean(getTargetBeanName());
	}

	/**
	 * 子类应该调用此方法来销毁过时的原型实例。
	 * @param target 要销毁的 bean 实例
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

	/**
	 * 方法 `readObject`：完成本类中与「read Object」相关的职责。
	 */
	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		throw new NotSerializableException("A prototype-based TargetSource itself is not deserializable - " +
				"just a disconnected SingletonTargetSource or EmptyTargetSource is");
	}

	/**
	 * 在序列化时将此对象替换为 SingletonTargetSource。受保护，否则子类不会调用它。 （{@code writeReplace()} 方法必须对正在序列化的类可见
	 * 。） <p> 通过此方法的实现，无需将此类或子类中的不可序列化字段标记为瞬态。
	 */
	protected Object writeReplace() throws ObjectStreamException {
		if (logger.isDebugEnabled()) {
			logger.debug("Disconnecting TargetSource [" + this + "]");
		}
		try {
			// 创建断开连接的 SingletonTargetSource/EmptyTargetSource。
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
