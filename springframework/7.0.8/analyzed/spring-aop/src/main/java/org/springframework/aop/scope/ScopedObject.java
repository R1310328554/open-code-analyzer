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
 * <p> 从 {@link ScopedProxyFactoryBean} 创建的对象可以转换为此接口，从而能够访问原始目标对象并以编程方式删除目标对象。
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see ScopedProxyFactoryBean
 */
public interface ScopedObject extends RawTargetAccess {

	/**
	 * 返回此作用域对象代理后面的当前目标对象，以其原始形式（存储在目标作用域中）。 <p> 例如，原始目标对象可以传递给无法处理作用域代理对象的持久性提供程序。
	 * @return 此作用域对象代理后面的当前目标对象
	 */
	Object getTargetObject();

	/**
	 * 从其目标范围（例如从支持会话）中删除此对象。 <p>请注意，此后不能对作用域对象进行进一步的调用（至少在当前线程内，即在目标作用域中具有完全相同的目标对象）。
	 */
	void removeFromScope();

}
