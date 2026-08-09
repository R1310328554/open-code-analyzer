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

import org.springframework.beans.factory.Aware;

/**
 * 任何希望获知其所运行的 {@link MessageSource}（通常是 ApplicationContext）
 * 的对象应实现的接口。
 *
 * <p>注意，{@code MessageSource} 通常也可作为 Bean 引用传入
 *（通过任意 Bean 属性或构造器参数），因为它在应用上下文中
 * 以名为 {@code "messageSource"} 的 Bean 形式定义。
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 1.1.1
 * @see ApplicationContextAware
 */
public interface MessageSourceAware extends Aware {

	/**
	 * 设置此对象所使用的 {@link MessageSource}。
	 * <p>在填充普通 Bean 属性之后、初始化回调（如 InitializingBean 的 afterPropertiesSet
	 * 或自定义 init-method）之前调用。
	 * 在 ApplicationContextAware 的 setApplicationContext 之前调用。
	 * @param messageSource message source to be used by this object
	 */
	void setMessageSource(MessageSource messageSource);

}
