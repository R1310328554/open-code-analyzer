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

package org.springframework.aop.framework.autoproxy.target;

import org.jspecify.annotations.Nullable;

import org.springframework.aop.target.AbstractBeanFactoryBasedTargetSource;
import org.springframework.aop.target.LazyInitTargetSource;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * {@code TargetSourceCreator} 为定义为“lazy-init”的每个 bean 强制执行 {@link
 * LazyInitTargetSource}。这将导致为每个 bean 创建一个代理，允许获取对此类 bean 的引用，而无需实际初始化目标 bean 实例。
 * <p> 注册为自动代理创建者的自定义 {@code TargetSourceCreator}，与特定 bean 的自定义拦截器结合使用或仅用于创建惰性初始化代理。例如，作为 X
 * ML 应用程序上下文定义中自动检测的基础设施 bean：
 * <pre class="code"> <bean
 * class="org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator">;
 * &lt;属性名称=“beanNames”值=“*”/> <!-- 适用于所有豆类 --> <属性名称=“customTargetSourceCreators”> <列表>
 * <bean
 * class="org.springframework.aop.framework.autoproxy.target.LazyInitTargetSourceCreator"/>/>
 * &lt;/列表&gt; &lt;/属性&gt; &lt;/豆&gt;
 * <bean id =“myLazyInitBean”class =“mypackage.MyBeanClass”lazy-init =“true”> <!-- ... -->
 * </bean></pre>
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 1.2
 * @see org.springframework.beans.factory.config.BeanDefinition#isLazyInit
 * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator#setCustomTargetSourceCreators
 * @see org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator
 */
public class LazyInitTargetSourceCreator extends AbstractBeanFactoryBasedTargetSourceCreator {

	/**
	 * 判断是否 Prototype Based。
	 */
	@Override
	protected boolean isPrototypeBased() {
		return false;
	}

	/**
	 * 创建：Bean Factory Based Target Source（方法 `createBeanFactoryBasedTargetSource`）。
	 */
	@Override
	protected @Nullable AbstractBeanFactoryBasedTargetSource createBeanFactoryBasedTargetSource(
			Class<?> beanClass, String beanName) {

		if (getBeanFactory() instanceof ConfigurableListableBeanFactory clbf) {
			BeanDefinition definition = clbf.getBeanDefinition(beanName);
			if (definition.isLazyInit()) {
				return new LazyInitTargetSource();
			}
		}
		return null;
	}

}
