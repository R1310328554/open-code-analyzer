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
 * {@link BeanNameAware} 的对偶接口。用于返回某个对象的 Bean 名称。
 *
 * <p>引入本接口可避免在与 Spring IoC 和 Spring AOP 配合使用的对象中，
 * 对 Bean 名称产生脆弱的依赖。
 *
 * @author Rod Johnson
 * @since 2.0
 * @see BeanNameAware
 */
public interface NamedBean {

	/**
	 * 返回本 Bean 在 Spring BeanFactory 中的名称（若已知）。
	 */
	String getBeanName();

}
