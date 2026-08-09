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

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeansException;

/**
 * {@link org.springframework.aop.TargetSource} 从 {@link
 * org.springframework.beans.factory.BeanFactory} 延迟访问单例 bean。
 * <p> 当初始化时需要代理引用但实际目标对象在第一次使用之前不应初始化时非常有用。当目标 bean 在 {@link
 * org.springframework.context.ApplicationContext}（或急切预实例化单例 bean 的 {@code
 * BeanFactory}）中定义时，它也必须标记为“lazy-init”，否则它将在启动时由所述 {@code ApplicationContext}（或 {@code
 * BeanFactory}）实例化。 <p>例如：
 * <pre class="code"> <bean id =“serviceTarget”class =“example.MyService”lazy-init
 * =“true”> ...&lt;/bean&gt;
 * &lt;bean id="service" class="org.springframework.aop.framework.ProxyFactoryBean"&gt;
 * <属性名称=“目标源”> &lt;bean class="org.springframework.aop.target.LazyInitTargetSource"&gt;
 * &lt;属性名称=“targetBeanName”&gt;&lt;idref local=“serviceTarget”/&gt;&lt;/property&gt;
 * &lt;/豆&gt; &lt;/属性&gt; </bean></pre>
 * 在调用“service”代理上的方法之前，“serviceTarget”bean 不会被初始化。
 * <p>子类可以扩展此类并覆盖 {@link #postProcessTargetObject(Object)}，以便在首次加载目标对象时对目标对象执行一些附加处理。
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 1.1.4
 * @see org.springframework.beans.factory.BeanFactory#getBean
 * @see #postProcessTargetObject
 */
@SuppressWarnings("serial")
public class LazyInitTargetSource extends AbstractBeanFactoryBasedTargetSource {

	/** 目标相关状态（`target`）。 */
	private @Nullable Object target;


	/**
	 * 获取 Target（`Target`）。
	 */
	@Override
	public synchronized Object getTarget() throws BeansException {
		if (this.target == null) {
			this.target = getBeanFactory().getBean(getTargetBeanName());
			postProcessTargetObject(this.target);
		}
		return this.target;
	}

	/**
	 * 子类可以重写此方法，以便在首次加载目标对象时对其执行附加处理。
	 * @param targetObject 刚刚实例化（和配置）的目标对象
	 */
	protected void postProcessTargetObject(Object targetObject) {
	}

}
