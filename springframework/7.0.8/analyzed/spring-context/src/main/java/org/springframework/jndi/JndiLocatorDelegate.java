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

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jspecify.annotations.Nullable;

import org.springframework.core.SpringProperties;

/**
 * 具有公开 lookup 方法的 {@link JndiLocatorSupport} 子类，便于作为委托使用。
 *
 * @author Juergen Hoeller
 * @since 3.0.1
 */
public class JndiLocatorDelegate extends JndiLocatorSupport {

	/**
	 * 系统属性：指示 Spring 忽略默认 JNDI 环境，
	 * 即 {@link #isDefaultJndiEnvironmentAvailable()} 始终返回 {@code false}。
	 * <p>默认为 {@code false}，允许常规默认 JNDI 访问（如 {@link JndiPropertySource}）。
	 * 设为 {@code true} 可优化无需 JNDI 回退搜索的场景，避免重复查找开销。
	 * <p>仅影响 JNDI 回退搜索，不影响显式配置的 JNDI 查找（如 {@code DataSource}）。
	 * @since 4.3
	 * @see #isDefaultJndiEnvironmentAvailable()
	 * @see JndiPropertySource
	 */
	public static final String IGNORE_JNDI_PROPERTY_NAME = "spring.jndi.ignore";


	private static final boolean shouldIgnoreDefaultJndiEnvironment =
			SpringProperties.getFlag(IGNORE_JNDI_PROPERTY_NAME);


	@Override
	public Object lookup(String jndiName) throws NamingException {
		return super.lookup(jndiName);
	}

	@Override
	public <T> T lookup(String jndiName, @Nullable Class<T> requiredType) throws NamingException {
		return super.lookup(jndiName, requiredType);
	}


	/**
	 * 配置 {@code resourceRef} 为 {@code true} 的 {@code JndiLocatorDelegate}，
	 * 即所有名称将前缀 {@code java:comp/env/}。
	 * @see #setResourceRef
	 */
	public static JndiLocatorDelegate createDefaultResourceRefLocator() {
		JndiLocatorDelegate jndiLocator = new JndiLocatorDelegate();
		jndiLocator.setResourceRef(true);
		return jndiLocator;
	}

	/**
	 * 检查当前 JVM 是否存在默认 JNDI 环境（如 Jakarta EE 环境）。
	 * @return 若可使用默认 InitialContext 则为 {@code true}，否则 {@code false}
	 */
	public static boolean isDefaultJndiEnvironmentAvailable() {
		if (shouldIgnoreDefaultJndiEnvironment) {
			return false;
		}
		try {
			new InitialContext().getEnvironment();
			return true;
		}
		catch (Throwable ex) {
			return false;
		}
	}

}
