
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

package org.springframework.jmx.export.assembler;

import java.lang.reflect.Method;

/**
 * {@code AbstractReflectiveMBeanInfoAssembler} 的简单子类，
 * 对方法与属性的纳入始终返回 {@code true}，从而将所有 public 方法与属性
 * 分别暴露为 JMX 操作与属性。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 1.2
 */
public class SimpleReflectiveMBeanInfoAssembler extends AbstractConfigurableMBeanInfoAssembler {

	/**
	 * 始终返回 {@code true}。
	 */
	@Override
	protected boolean includeReadAttribute(Method method, String beanKey) {
		return true;
	}

	/**
	 * 始终返回 {@code true}。
	 */
	@Override
	protected boolean includeWriteAttribute(Method method, String beanKey) {
		return true;
	}

	/**
	 * 始终返回 {@code true}。
	 */
	@Override
	protected boolean includeOperation(Method method, String beanKey) {
		return true;
	}

}
