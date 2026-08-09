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

package org.springframework.beans.factory;

/**
 * 希望在销毁时释放资源的 Bean 所实现的接口。
 * {@link BeanFactory} 在销毁某个作用域 Bean 时会调用其 destroy 方法。
 * {@link org.springframework.context.ApplicationContext} 则应在关闭时，
 * 由应用生命周期驱动，销毁其中所有的单例 Bean。
 *
 * <p>出于同样目的，Spring 管理的 Bean 也可以实现 Java 的 {@link AutoCloseable} 接口。
 * 不实现接口的另一种方式是指定自定义销毁方法，例如在 XML Bean 定义中声明。
 * 完整的 Bean 生命周期方法列表见 {@link BeanFactory BeanFactory javadocs}。
 *
 * @author Juergen Hoeller
 * @since 12.08.2003
 * @see InitializingBean
 * @see org.springframework.beans.factory.support.RootBeanDefinition#getDestroyMethodName()
 * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#destroySingletons()
 * @see org.springframework.context.ConfigurableApplicationContext#close()
 */
public interface DisposableBean {

	/**
	 * 由所属 {@code BeanFactory} 在销毁 Bean 时调用。
	 * @throws Exception 关闭过程中出错时抛出。异常会被记录日志，
	 * 但不会再次抛出，以便其他 Bean 也能继续释放各自的资源。
	 */
	void destroy() throws Exception;

}
