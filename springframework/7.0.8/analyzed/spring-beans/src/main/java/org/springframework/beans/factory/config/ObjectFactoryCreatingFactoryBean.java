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

package org.springframework.beans.factory.config;

import java.io.Serializable;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.util.Assert;

/**
 * {@link org.springframework.beans.factory.FactoryBean} 实现，其返回值为
 * {@link org.springframework.beans.factory.ObjectFactory}，该 ObjectFactory 又从
 * {@link org.springframework.beans.factory.BeanFactory} 获取 Bean。
 *
 * <p>因此，可避免客户端对象直接调用
 * {@link org.springframework.beans.factory.BeanFactory#getBean(String)} 从
 * {@link org.springframework.beans.factory.BeanFactory} 获取（通常为原型）Bean，
 * 这违反了控制反转原则。借助本类，可将
 * {@link org.springframework.beans.factory.ObjectFactory} 实例作为属性注入客户端对象，
 * 该实例直接返回唯一的目标 Bean（同样，通常为原型 Bean）。
 *
 * <p>基于 XML 的 {@link org.springframework.beans.factory.BeanFactory} 配置示例：
 *
 * <pre class="code">&lt;beans&gt;
 *
 *   &lt;!-- 原型 Bean，因存在状态 --&gt;
 *   &lt;bean id="myService" class="a.b.c.MyService" scope="prototype"/&gt;
 *
 *   &lt;bean id="myServiceFactory"
 *       class="org.springframework.beans.factory.config.ObjectFactoryCreatingFactoryBean"&gt;
 *     &lt;property name="targetBeanName"&gt;&lt;idref local="myService"/&gt;&lt;/property&gt;
 *   &lt;/bean&gt;
 *
 *   &lt;bean id="clientBean" class="a.b.c.MyClientBean"&gt;
 *     &lt;property name="myServiceFactory" ref="myServiceFactory"/&gt;
 *   &lt;/bean&gt;
 *
 *&lt;/beans&gt;</pre>
 *
 * <p>配套的 {@code MyClientBean} 类实现可能如下：
 *
 * <pre class="code">package a.b.c;
 *
 * import org.springframework.beans.factory.ObjectFactory;
 *
 * public class MyClientBean {
 *
 *   private ObjectFactory&lt;MyService&gt; myServiceFactory;
 *
 *   public void setMyServiceFactory(ObjectFactory&lt;MyService&gt; myServiceFactory) {
 *     this.myServiceFactory = myServiceFactory;
 *   }
 *
 *   public void someBusinessMethod() {
 *     // 获取全新的 MyService 实例
 *     MyService service = this.myServiceFactory.getObject();
 *     // 使用 service 对象执行业务逻辑...
 *   }
 * }</pre>
 *
 * <p>对象创建模式此应用场景的另一种做法是使用 {@link ServiceLocatorFactoryBean}
 * 获取（原型）Bean。{@link ServiceLocatorFactoryBean} 的优势在于不必依赖
 * {@link org.springframework.beans.factory.ObjectFactory} 等 Spring 专用接口，
 * 但缺点是需要运行时类生成。请参阅
 * {@link ServiceLocatorFactoryBean ServiceLocatorFactoryBean JavaDoc} 以全面了解此问题。
 *
 * @author Colin Sampaleanu
 * @author Juergen Hoeller
 * @since 1.0.2
 * @see org.springframework.beans.factory.ObjectFactory
 * @see ServiceLocatorFactoryBean
 */
public class ObjectFactoryCreatingFactoryBean extends AbstractFactoryBean<ObjectFactory<Object>> {

	/** 目标 Bean 名称。 */
	private @Nullable String targetBeanName;


	/**
	 * 设置目标 Bean 的名称。
	 * <p>目标<i>不必</i>是非单例 Bean，但实际几乎总是如此（因为若目标 Bean 为单例，
	 * 可直接将单例 Bean 注入依赖对象，从而无需本工厂方式提供的额外间接层）。
	 */
	public void setTargetBeanName(String targetBeanName) {
		this.targetBeanName = targetBeanName;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.hasText(this.targetBeanName, "Property 'targetBeanName' is required");
		super.afterPropertiesSet();
	}


	@Override
	public Class<?> getObjectType() {
		return ObjectFactory.class;
	}

	@Override
	protected ObjectFactory<Object> createInstance() {
		BeanFactory beanFactory = getBeanFactory();
		Assert.state(beanFactory != null, "No BeanFactory available");
		Assert.state(this.targetBeanName != null, "No target bean name specified");
		return new TargetBeanObjectFactory(beanFactory, this.targetBeanName);
	}


	/**
	 * 独立内部类——用于序列化。
	 */
	@SuppressWarnings("serial")
	private static class TargetBeanObjectFactory implements ObjectFactory<Object>, Serializable {

		/** 用于获取目标 Bean 的 Bean 工厂。 */
		private final BeanFactory beanFactory;

		/** 目标 Bean 名称。 */
		private final String targetBeanName;

		public TargetBeanObjectFactory(BeanFactory beanFactory, String targetBeanName) {
			this.beanFactory = beanFactory;
			this.targetBeanName = targetBeanName;
		}

		@Override
		public Object getObject() throws BeansException {
			return this.beanFactory.getBean(this.targetBeanName);
		}
	}

}
