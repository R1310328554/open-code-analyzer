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

package org.aopalliance.intercept;

import java.lang.reflect.AccessibleObject;

import org.jspecify.annotations.Nullable;

/**
 * 该接口表示通用运行时连接点（在 AOP 术语中）。
 * <p>A 运行时连接点是发生在静态连接点（即程序中的位置）上的 <i>event</i>。例如，调用是方法上的运行时连接点（静态连接点）。一般可以使用 {@link #getS
 * taticPart()} 方法检索给定连接点的静态部分。
 * <p>在拦截框架的上下文中，运行时连接点是对可访问对象（方法、构造函数、字段）的访问的具体化，即连接点的静态部分。它被传递到安装在静态连接点上的拦截器。
 * @author Rod Johnson
 * @see Interceptor
 */
public interface Joinpoint {

	/**
	* 继续处理链中的下一个拦截器。 <p> 该方法的实现和语义取决于实际的连接点类型（请参阅子接口）。
	* @return 子接口的进程定义
	* @throws Throwable 如果连接点抛出异常
	*/
	@Nullable Object proceed() throws Throwable;

	/**
	* 返回保存当前连接点静态部分的对象。 <p>例如，调用的目标对象。
	* @return 对象（如果可访问对象是静态的，则可以为 null）
	*/
	@Nullable Object getThis();

	/**
	* 返回此连接点的静态部分。 <p>静态部分是一个可访问的对象，上面安装了一系列拦截器。
	*/
	AccessibleObject getStaticPart();

}
