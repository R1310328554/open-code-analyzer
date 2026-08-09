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
 * 回调接口：使 bean 能够获知 bean 的
 * {@link ClassLoader 类加载器}，即当前 bean 工厂用于加载 bean 类的 ClassLoader。
 *
 * <p>主要供框架类实现：即便自身可能由共享 ClassLoader 加载，
 * 仍需按名称加载应用类。
 *
 * <p>全部 bean 生命周期方法列表见
 * {@link BeanFactory BeanFactory javadocs}。
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 2.0
 * @see BeanNameAware
 * @see BeanFactoryAware
 * @see InitializingBean
 */
public interface BeanClassLoaderAware extends Aware {

	/**
	 * 向 bean 实例提供 bean {@link ClassLoader 类加载器} 的回调。
	 * <p>在填充普通 bean 属性之<i>后</i>、
	 * 初始化回调（如 {@link InitializingBean InitializingBean} 的
	 * {@link InitializingBean#afterPropertiesSet()} 或自定义 init-method）之
	 * <i>前</i>调用。
	 * @param classLoader 所属的类加载器
	 */
	void setBeanClassLoader(ClassLoader classLoader);

}
