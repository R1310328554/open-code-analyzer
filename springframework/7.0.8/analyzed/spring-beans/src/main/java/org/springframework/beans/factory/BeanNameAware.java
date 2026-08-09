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
 * 希望感知自身在 BeanFactory 中的 bean 名称的 bean 所实现的接口。
 * 注意：通常不建议对象依赖自己的 bean 名称，因为这往往意味着对外部配置的脆弱依赖，
 * 以及可能不必要的对 Spring API 的依赖。
 *
 * <p>完整的 bean 生命周期方法列表，参见
 * {@link BeanFactory BeanFactory javadocs}。
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 01.11.2003
 * @see BeanClassLoaderAware
 * @see BeanFactoryAware
 * @see InitializingBean
 */
public interface BeanNameAware extends Aware {

	/**
	 * 设置创建本 bean 的工厂中该 bean 的名称。
	 * <p>在填充常规 bean 属性之后、
	 * {@link InitializingBean#afterPropertiesSet()} 或自定义 init-method
	 * 等初始化回调之前调用。
	 * @param name 工厂中的 bean 名称。
	 * 注意：这是工厂中实际使用的 bean 名称，可能与最初指定的名称不同：
	 * 尤其对内部 bean，实际名称可能通过追加 {@code "#..."} 后缀保证唯一。
	 * 如需原始名称（不含后缀），可使用 {@link BeanFactoryUtils#originalBeanName(String)}。
	 */
	void setBeanName(String name);

}
