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

import org.springframework.beans.FatalBeanException;

/**
 * 当 Bean 尚未完全初始化（例如因循环引用）时，
 * 从 FactoryBean 的 {@code getObject()} 方法抛出的异常。
 *
 * <p>注意：涉及 FactoryBean 的循环引用，不能像普通 Bean 那样通过急切缓存单例实例来解决。
 * 原因在于：<i>每一个</i> FactoryBean 都必须先完全初始化，才能返回所创建的 Bean；
 * 而普通 Bean 只有在<i>特定</i>情况下才需要初始化——也就是协作 Bean 在初始化时
 * 真正调用了它们，而不仅仅是保存引用。
 *
 * @author Juergen Hoeller
 * @since 30.10.2003
 * @see FactoryBean#getObject()
 */
@SuppressWarnings("serial")
public class FactoryBeanNotInitializedException extends FatalBeanException {

	/**
	 * 使用默认消息创建新的 FactoryBeanNotInitializedException。
	 */
	public FactoryBeanNotInitializedException() {
		super("FactoryBean is not fully initialized yet");
	}

	/**
	 * 使用给定消息创建新的 FactoryBeanNotInitializedException。
	 * @param msg 详细消息
	 */
	public FactoryBeanNotInitializedException(String msg) {
		super(msg);
	}

}
