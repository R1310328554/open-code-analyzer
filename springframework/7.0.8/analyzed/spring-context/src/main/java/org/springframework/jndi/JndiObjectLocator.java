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

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * 基于 JNDI 的服务定位器便捷超类，提供可配置的特定 JNDI 资源查找。
 *
 * <p>暴露 {@link #setJndiName "jndiName"} 属性。名称可含也可不含 Jakarta EE 本地映射资源
 * 期望的 {@code java:comp/env/} 前缀；若不含且 {@code resourceRef} 为 {@code true}
 * （默认 <strong>false</strong>）且未指定其他 scheme，则自动添加。
 *
 * <p>子类可在适当时机调用 {@link #lookup()}：有的在初始化时查找，有的按需查找，
 * 后者更灵活，允许在 JNDI 对象可用前完成定位器初始化。
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see #setJndiName
 * @see #setJndiTemplate
 * @see #setJndiEnvironment
 * @see #setResourceRef
 * @see #lookup()
 */
public abstract class JndiObjectLocator extends JndiLocatorSupport implements InitializingBean {

	private @Nullable String jndiName;

	private @Nullable Class<?> expectedType;


	/**
	 * 设置要查找的 JNDI 名称。若不以 {@code java:comp/env/} 开头且 {@code resourceRef}
	 * 为 {@code true}，则自动添加前缀。
	 * @param jndiName 要查找的 JNDI 名称
	 * @see #setResourceRef
	 */
	public void setJndiName(@Nullable String jndiName) {
		this.jndiName = jndiName;
	}

	/** 返回要查找的 JNDI 名称。 */
	public @Nullable String getJndiName() {
		return this.jndiName;
	}

	/**
	 * 指定查找到的 JNDI 对象应可赋值到的类型（若有）。
	 */
	public void setExpectedType(@Nullable Class<?> expectedType) {
		this.expectedType = expectedType;
	}

	/** 返回查找到的 JNDI 对象应可赋值到的类型（若有）。 */
	public @Nullable Class<?> getExpectedType() {
		return this.expectedType;
	}

	@Override
	public void afterPropertiesSet() throws IllegalArgumentException, NamingException {
		if (!StringUtils.hasLength(getJndiName())) {
			throw new IllegalArgumentException("Property 'jndiName' is required");
		}
	}


	/**
	 * 对本定位器目标资源执行实际 JNDI 查找。
	 * @return 查找到的目标对象
	 * @throws NamingException 查找失败或对象不可赋值到期望类型时
	 * @see #setJndiName
	 * @see #setExpectedType
	 * @see #lookup(String, Class)
	 */
	protected Object lookup() throws NamingException {
		String jndiName = getJndiName();
		Assert.state(jndiName != null, "No JNDI name specified");
		return lookup(jndiName, getExpectedType());
	}

}
