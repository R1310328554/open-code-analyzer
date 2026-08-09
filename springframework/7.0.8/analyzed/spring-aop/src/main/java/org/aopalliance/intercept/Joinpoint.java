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
 * 表示通用运行时连接点的接口（AOP 术语）。
 *
 * <p>运行时连接点是发生在静态连接点（即程序中的某个位置）上的<i>事件</i>。
 * 例如，调用是方法上的运行时连接点（静态连接点）。
 * 给定连接点的静态部分可通过 {@link #getStaticPart()} 方法获取。
 *
 * <p>在拦截框架的上下文中，运行时连接点是对可访问对象
 * （方法、构造函数、字段）访问的具体化，即连接点的静态部分。
 * 它会传递给安装在静态连接点上的拦截器。
 *
 * @author Rod Johnson
 * @see Interceptor
 */
public interface Joinpoint {

	/**
	 * 继续执行拦截器链中的下一个拦截器。
	 * <p>本方法的实现与语义取决于实际连接点类型（见子接口）。
	 * @return 见各子接口对 proceed 的定义
	 * @throws Throwable 若连接点抛出异常
	 */
	@Nullable Object proceed() throws Throwable;

	/**
	 * 返回持有当前连接点静态部分的对象。
	 * <p>例如，调用场景下的目标对象。
	 * @return 对象（若可访问对象为 static 则可能为 null）
	 */
	@Nullable Object getThis();

	/**
	 * 返回本连接点的静态部分。
	 * <p>静态部分是可访问对象，其上安装了一串拦截器。
	 */
	AccessibleObject getStaticPart();

}
