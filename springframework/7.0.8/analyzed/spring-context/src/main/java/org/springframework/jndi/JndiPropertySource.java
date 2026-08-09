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

import javax.naming.NamingException;

import org.jspecify.annotations.Nullable;

import org.springframework.core.env.PropertySource;

/**
 * 从底层 Spring {@link JndiLocatorDelegate} 读取属性的 {@link PropertySource} 实现。
 *
 * <p>默认底层 {@code JndiLocatorDelegate} 的
 * {@link JndiLocatorDelegate#setResourceRef(boolean) "resourceRef"} 为 {@code true}，
 * 查找名称将自动前缀 {@code java:comp/env/}，符合
 * <a href="https://download.oracle.com/javase/jndi/tutorial/beyond/misc/policy.html">JNDI 命名约定</a>。
 * 若要覆盖或更改前缀，手动配置 {@code JndiLocatorDelegate} 并通过接受它的构造器传入。
 * 自定义 JNDI 属性应通过 {@link JndiLocatorDelegate#setJndiEnvironment(java.util.Properties)}
 * 在构造 {@code JndiPropertySource} 前指定。
 *
 * <p>{@link org.springframework.web.context.support.StandardServletEnvironment
 * StandardServletEnvironment} 默认包含 {@code JndiPropertySource}；
 * 可在 {@link org.springframework.context.ApplicationContextInitializer
 * ApplicationContextInitializer} 或 {@link org.springframework.web.WebApplicationInitializer
 * WebApplicationInitializer} 中定制底层 {@link JndiLocatorDelegate}。
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 * @see JndiLocatorDelegate
 * @see org.springframework.context.ApplicationContextInitializer
 * @see org.springframework.web.WebApplicationInitializer
 * @see org.springframework.web.context.support.StandardServletEnvironment
 */
public class JndiPropertySource extends PropertySource<JndiLocatorDelegate> {

	/**
	 * 使用给定名称创建 {@code JndiPropertySource}，
	 * 底层 {@link JndiLocatorDelegate} 配置为名称前缀 {@code java:comp/env/}。
	 */
	public JndiPropertySource(String name) {
		this(name, JndiLocatorDelegate.createDefaultResourceRefLocator());
	}

	/**
	 * 使用给定名称与 {@code JndiLocatorDelegate} 创建 {@code JndiPropertySource}。
	 */
	public JndiPropertySource(String name, JndiLocatorDelegate jndiLocator) {
		super(name, jndiLocator);
	}


	/**
	 * 从底层 {@link JndiLocatorDelegate} 查找并返回与给定名称关联的值。
	 * 若 {@link JndiLocatorDelegate#lookup(String)} 抛出 {@link NamingException}，
	 * 返回 {@code null} 并以 DEBUG 级别记录异常消息。
	 */
	@Override
	public @Nullable Object getProperty(String name) {
		if (getSource().isResourceRef() && name.indexOf(':') != -1) {
			// We're in resource-ref (prefixing with "java:comp/env") mode. Let's not bother
			// with property names with a colon it since they're probably just containing a
			// default value clause, very unlikely to match including the colon part even in
			// a textual property source, and effectively never meant to match that way in
			// JNDI where a colon indicates a separator between JNDI scheme and actual name.
			return null;
		}

		try {
			Object value = this.source.lookup(name);
			if (logger.isDebugEnabled()) {
				logger.debug("JNDI lookup for name [" + name + "] returned: [" + value + "]");
			}
			return value;
		}
		catch (NamingException ex) {
			if (logger.isDebugEnabled()) {
				logger.debug("JNDI lookup for name [" + name + "] threw NamingException " +
						"with message: " + ex.getMessage() + ". Returning null.");
			}
			return null;
		}
	}

}
