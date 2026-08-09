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

package org.springframework.aop.aspectj.annotation;

import org.springframework.aop.framework.AopConfigException;

/**
 * 尝试对非 AspectJ 注释样式方面的类执行顾问程序生成操作时抛出的 AopConfigException 扩展。
 * @author Rod Johnson
 * @since 2.0
 */
@SuppressWarnings("serial")
public class NotAnAtAspectException extends AopConfigException {

	/** 类相关状态（`nonAspectClass`）。 */
	private final Class<?> nonAspectClass;


	/**
	 * 为给定类创建一个新的 NotAnAtAspectException。
	 * @param nonAspectClass 违规班级
	 */
	public NotAnAtAspectException(Class<?> nonAspectClass) {
		super(nonAspectClass.getName() + " is not an @AspectJ aspect");
		this.nonAspectClass = nonAspectClass;
	}

	/**
	 * 返回有问题的类。
	 */
	public Class<?> getNonAspectClass() {
		return this.nonAspectClass;
	}

}
