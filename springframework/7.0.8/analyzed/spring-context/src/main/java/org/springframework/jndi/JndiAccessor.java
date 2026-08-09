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

package org.springframework.jndi;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

/**
 * JNDI 访问器的便捷超类，提供 {@code jndiTemplate} 与 {@code jndiEnvironment} Bean 属性。
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see #setJndiTemplate
 * @see #setJndiEnvironment
 */
public class JndiAccessor {

	/** 子类可用的日志记录器。 */
	protected final Log logger = LogFactory.getLog(getClass());

	private JndiTemplate jndiTemplate = new JndiTemplate();


	/**
	 * 设置用于 JNDI 查找的 {@link JndiTemplate}。
	 * <p>也可通过 {@code jndiEnvironment} 指定 JNDI 环境设置。
	 * @see #setJndiEnvironment
	 */
	public void setJndiTemplate(@Nullable JndiTemplate jndiTemplate) {
		this.jndiTemplate = (jndiTemplate != null ? jndiTemplate : new JndiTemplate());
	}

	/** 返回用于 JNDI 查找的 {@link JndiTemplate}。 */
	public JndiTemplate getJndiTemplate() {
		return this.jndiTemplate;
	}

	/**
	 * 设置用于 JNDI 查找的 JNDI 环境。
	 * <p>使用给定环境设置创建 {@link JndiTemplate}。
	 * @see #setJndiTemplate
	 */
	public void setJndiEnvironment(@Nullable Properties jndiEnvironment) {
		this.jndiTemplate = new JndiTemplate(jndiEnvironment);
	}

	/** 返回用于 JNDI 查找的 JNDI 环境。 */
	public @Nullable Properties getJndiEnvironment() {
		return this.jndiTemplate.getEnvironment();
	}

}
