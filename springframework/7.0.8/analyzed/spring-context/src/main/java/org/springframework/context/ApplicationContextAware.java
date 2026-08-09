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

package org.springframework.context;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.Aware;

/**
 * 任何希望获知其所运行 {@link ApplicationContext} 的对象应实现的接口。
 *
 * <p>例如，当对象需要访问一组协作 Bean 时，实现本接口是合理的。
 * 注意：仅为查找 Bean 而实现本接口，不如通过 Bean 引用配置来得合适。
 *
 * <p>若对象需要访问文件资源（即调用 {@code getResource}）、发布应用事件，
 * 或访问 MessageSource，也可实现本接口。但在此类具体场景下，
 * 更宜实现更专用的 {@link ResourceLoaderAware}、
 * {@link ApplicationEventPublisherAware} 或 {@link MessageSourceAware} 接口。
 *
 * <p>注意：文件资源依赖也可作为类型为
 * {@link org.springframework.core.io.Resource} 的 Bean 属性暴露，
 * 通过字符串由 Bean 工厂自动类型转换填充。这样无需仅为访问特定文件资源
 * 而实现任何回调接口。
 *
 * <p>{@link org.springframework.context.support.ApplicationObjectSupport} 是
 * 实现本接口的应用对象便捷基类。
 *
 * <p>所有 Bean 生命周期方法列表见
 * {@link org.springframework.beans.factory.BeanFactory BeanFactory javadocs}。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Chris Beams
 * @see ResourceLoaderAware
 * @see ApplicationEventPublisherAware
 * @see MessageSourceAware
 * @see org.springframework.context.support.ApplicationObjectSupport
 * @see org.springframework.beans.factory.BeanFactoryAware
 */
public interface ApplicationContextAware extends Aware {

	/**
	 * 设置本对象所运行的 ApplicationContext。
	 * 通常用于初始化该对象。
	 * <p>在普通 Bean 属性填充之后、init 回调（如
	 * {@link org.springframework.beans.factory.InitializingBean#afterPropertiesSet()}
	 * 或自定义 init-method）之前调用。若适用，在
	 * {@link ResourceLoaderAware#setResourceLoader}、
	 * {@link ApplicationEventPublisherAware#setApplicationEventPublisher} 和
	 * {@link MessageSourceAware} 之后调用。
	 * @param applicationContext the ApplicationContext object to be used by this object
	 * @throws ApplicationContextException in case of context initialization errors
	 * @throws BeansException if thrown by application context methods
	 * @see org.springframework.beans.factory.BeanInitializationException
	 */
	void setApplicationContext(ApplicationContext applicationContext) throws BeansException;

}
