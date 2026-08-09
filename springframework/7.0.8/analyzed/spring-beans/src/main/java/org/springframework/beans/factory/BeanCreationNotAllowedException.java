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
 * 在当前不允许创建 bean 时仍有请求发出所抛出的异常
 * （例如 BeanFactory 处于关闭阶段）。
 *
 * @author Juergen Hoeller
 * @since 2.0
 */
@SuppressWarnings("serial")
public class BeanCreationNotAllowedException extends BeanCreationException {

	/**
	 * 创建一个新的 {@code BeanCreationNotAllowedException}。
	 * @param beanName 被请求的 bean 名称
	 * @param msg 详细消息
	 */
	public BeanCreationNotAllowedException(String beanName, String msg) {
		super(beanName, msg);
	}

}
