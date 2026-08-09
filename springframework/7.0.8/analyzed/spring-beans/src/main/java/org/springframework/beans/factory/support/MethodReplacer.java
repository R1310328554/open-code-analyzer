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

package org.springframework.beans.factory.support;

import java.lang.reflect.Method;

/**
 * 由类实现的接口，用于重新实现 IoC 受管对象上的任意方法：
 * 即依赖注入的<b>方法注入</b>形式。
 *
 * <p>此类方法可以是（但不必是）抽象方法；若为抽象方法，
 * 容器将创建具体子类来完成实例化。
 *
 * @author Rod Johnson
 * @since 1.1
 */
public interface MethodReplacer {

	/**
	 * 重新实现给定方法。
	 * @param obj 要为其重新实现方法的实例
	 * @param method 要重新实现的方法
	 * @param args 方法参数
	 * @return 方法的返回值
	 */
	Object reimplement(Object obj, Method method, Object[] args) throws Throwable;

}
