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

import org.springframework.util.Assert;

/**
 * 可定位任意数量 JNDI 对象的便捷超类。
 * 继承 {@link JndiAccessor}，拥有 {@code jndiTemplate} 与 {@code jndiEnvironment} Bean 属性。
 *
 * <p>JNDI 名称可含也可不含 Jakarta EE 访问本地映射（ENC）资源时期望的
 * {@code java:comp/env/} 前缀。若不含且 {@code resourceRef} 为 {@code true}
 *（默认 <strong>false</strong>）且未指定其他 scheme（如 {@code java:}），
 * 则会自动添加该前缀。
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see #setJndiTemplate
 * @see #setJndiEnvironment
 * @see #setResourceRef
 */
public abstract class JndiLocatorSupport extends JndiAccessor {

	/** Jakarta EE 容器中使用的 JNDI 前缀。 */
	public static final String CONTAINER_PREFIX = "java:comp/env/";


	private boolean resourceRef = false;


	/**
	 * 设置查找是否发生在 Jakarta EE 容器中，即 JNDI 名称不含
	 * {@code java:comp/env/} 时是否需自动添加。默认为 {@code false}。
	 * <p>仅当未指定其他 scheme（如 {@code java:}）时生效。
	 */
	public void setResourceRef(boolean resourceRef) {
		this.resourceRef = resourceRef;
	}

	/** 返回查找是否发生在 Jakarta EE 容器中。 */
	public boolean isResourceRef() {
		return this.resourceRef;
	}


	/**
	 * 通过 {@link JndiTemplate} 执行实际 JNDI 查找。
	 * <p>若名称不以 {@code java:comp/env/} 开头且 {@code resourceRef} 为 {@code true}，则添加前缀。
	 * @param jndiName 要查找的 JNDI 名称
	 * @return 查找到的对象
	 * @throws NamingException JNDI 查找失败时
	 * @see #setResourceRef
	 */
	protected Object lookup(String jndiName) throws NamingException {
		return lookup(jndiName, null);
	}

	/**
	 * 通过 {@link JndiTemplate} 执行实际 JNDI 查找。
	 * @param jndiName 要查找的 JNDI 名称
	 * @param requiredType 期望的对象类型
	 * @return 查找到的对象
	 * @throws NamingException JNDI 查找失败时
	 * @see #setResourceRef
	 */
	protected <T> T lookup(String jndiName, @Nullable Class<T> requiredType) throws NamingException {
		Assert.notNull(jndiName, "'jndiName' must not be null");
		String convertedName = convertJndiName(jndiName);
		T jndiObject;
		try {
			jndiObject = getJndiTemplate().lookup(convertedName, requiredType);
		}
		catch (NamingException ex) {
			if (!convertedName.equals(jndiName)) {
				// Try fallback to originally specified name...
				if (logger.isDebugEnabled()) {
					logger.debug("Converted JNDI name [" + convertedName +
							"] not found - trying original name [" + jndiName + "]. " + ex);
				}
				jndiObject = getJndiTemplate().lookup(jndiName, requiredType);
			}
			else {
				throw ex;
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Located object with JNDI name [" + convertedName + "]");
		}
		return jndiObject;
	}

	/**
	 * 将给定 JNDI 名称转换为实际使用的 JNDI 名称。
	 * <p>默认在 {@code resourceRef} 为 {@code true} 且未指定其他 scheme 时添加 {@code java:comp/env/} 前缀。
	 * @param jndiName 原始 JNDI 名称
	 * @return 实际使用的 JNDI 名称
	 * @see #CONTAINER_PREFIX
	 * @see #setResourceRef
	 */
	protected String convertJndiName(String jndiName) {
		// Prepend container prefix if not already specified and no other scheme given.
		if (isResourceRef() && !jndiName.startsWith(CONTAINER_PREFIX) && jndiName.indexOf(':') == -1) {
			jndiName = CONTAINER_PREFIX + jndiName;
		}
		return jndiName;
	}

}
