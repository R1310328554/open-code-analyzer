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
 * 引用了当前正在创建中的 bean 时抛出的异常。
 * 典型场景：构造器自动装配匹配到了正在构造的同一个 bean。
 *
 * @author Juergen Hoeller
 * @since 1.1
 */
@SuppressWarnings("serial")
public class BeanCurrentlyInCreationException extends BeanCreationException {

	/**
	 * 创建一个新的 {@code BeanCurrentlyInCreationException}，
	 * 使用提示可能存在循环引用的默认错误消息。
	 * @param beanName 被请求的 bean 名称
	 */
	public BeanCurrentlyInCreationException(String beanName) {
		super(beanName, "Requested bean is currently in creation: "+
				"Is there an unresolvable circular reference or an asynchronous initialization dependency?");
	}

	/**
	 * 创建一个新的 {@code BeanCurrentlyInCreationException}。
	 * @param beanName 被请求的 bean 名称
	 * @param msg 详细消息
	 */
	public BeanCurrentlyInCreationException(String beanName, String msg) {
		super(beanName, msg);
	}

}
