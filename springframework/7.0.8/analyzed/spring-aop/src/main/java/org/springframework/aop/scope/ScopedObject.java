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

package org.springframework.aop.scope;

import org.springframework.aop.RawTargetAccess;

/**
 * 作用域对象的 AOP 引入接口。
 *
 * <p>由 {@link ScopedProxyFactoryBean} 创建的对象可转型为本接口，
 * 从而访问原始目标对象并以编程方式移除目标对象。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see ScopedProxyFactoryBean
 */
public interface ScopedObject extends RawTargetAccess {

	/**
	 * 返回本作用域对象代理背后的当前目标对象，
	 * 以原始形式（即目标作用域中存储的形式）。
	 * <p>例如，原始目标对象可传给无法处理作用域代理对象的持久化提供者。
	 * @return 本作用域对象代理背后的当前目标对象
	 */
	Object getTargetObject();

	/**
	 * 从目标作用域（例如底层 session）中移除此对象。
	 * <p>注意：之后不得再调用该作用域对象
	 * （至少在当前线程内、目标作用域中仍是同一目标对象时如此）。
	 */
	void removeFromScope();

}
