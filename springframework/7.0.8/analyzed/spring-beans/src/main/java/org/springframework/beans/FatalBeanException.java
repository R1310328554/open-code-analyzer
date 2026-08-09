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

package org.springframework.beans;

import org.jspecify.annotations.Nullable;

/**
 * 在 beans 包或其子包中遇到不可恢复问题时抛出，例如类或字段不合法。
 *
 * @author Rod Johnson
 */
@SuppressWarnings("serial")
public class FatalBeanException extends BeansException {

	/**
	 * 使用指定消息创建 FatalBeanException。
	 * @param msg 详细消息
	 */
	public FatalBeanException(String msg) {
		super(msg);
	}

	/**
	 * 使用指定消息和根因创建 FatalBeanException。
	 * @param msg 详细消息
	 * @param cause 根因
	 */
	public FatalBeanException(String msg, @Nullable Throwable cause) {
		super(msg, cause);
	}

}
