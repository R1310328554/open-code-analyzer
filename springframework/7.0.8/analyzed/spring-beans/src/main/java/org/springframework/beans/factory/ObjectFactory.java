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

import org.springframework.beans.BeansException;

/**
 * 定义在被调用时可返回一个 Object 实例（可能是共享的，也可能是独立的）的工厂。
 *
 * <p>本接口通常用于封装通用工厂：每次调用都返回某个目标对象的新实例（原型）。
 *
 * <p>本接口与 {@link FactoryBean} 类似，但后者的实现通常作为 SPI 实例
 * 定义在 {@link BeanFactory} 中，而本接口的实现通常作为 API
 * （通过注入）提供给其他 Bean。因此，{@code getObject()} 方法的异常处理行为也不同。
 *
 * @author Colin Sampaleanu
 * @since 1.0.2
 * @param <T> 对象类型
 * @see FactoryBean
 */
@FunctionalInterface
public interface ObjectFactory<T> {

	/**
	 * 返回本工厂所管理对象的一个实例（可能是共享的，也可能是独立的）。
	 * @return 得到的实例
	 * @throws BeansException 创建出错时
	 */
	T getObject() throws BeansException;

}
