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

package org.springframework.scheduling.concurrent;

import java.util.Properties;
import java.util.concurrent.Executor;

import javax.naming.NamingException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jndi.JndiLocatorDelegate;
import org.springframework.jndi.JndiTemplate;

/**
 * 基于 JNDI 的 {@link ConcurrentTaskExecutor} 变体，
 * 在 Jakarta EE/8 环境中默认查找 JSR-236 的 "java:comp/DefaultManagedExecutorService"。
 *
 * <p>注意：本类并非严格基于 JSR-236；可与 JNDI 中找到的任意常规
 * {@link java.util.concurrent.Executor} 配合工作。
 * 实际适配 {@link jakarta.enterprise.concurrent.ManagedExecutorService}
 * 在基类 {@link ConcurrentTaskExecutor} 自身中完成。
 *
 * @author Juergen Hoeller
 * @since 4.0
 * @see jakarta.enterprise.concurrent.ManagedExecutorService
 */
public class DefaultManagedTaskExecutor extends ConcurrentTaskExecutor implements InitializingBean {

	private final JndiLocatorDelegate jndiLocator = new JndiLocatorDelegate();

	private String jndiName = "java:comp/DefaultManagedExecutorService";


	public DefaultManagedTaskExecutor() {
		// Executor initialization happens in afterPropertiesSet
		super(null);
	}


	/**
	 * 设置用于 JNDI 查找的 JNDI 模板。
	 * @see org.springframework.jndi.JndiAccessor#setJndiTemplate
	 */
	public void setJndiTemplate(JndiTemplate jndiTemplate) {
		this.jndiLocator.setJndiTemplate(jndiTemplate);
	}

	/**
	 * 设置用于 JNDI 查找的 JNDI 环境。
	 * @see org.springframework.jndi.JndiAccessor#setJndiEnvironment
	 */
	public void setJndiEnvironment(Properties jndiEnvironment) {
		this.jndiLocator.setJndiEnvironment(jndiEnvironment);
	}

	/**
	 * 设置查找是否发生在 Jakarta EE 容器中，即若 JNDI 名称尚未包含前缀
	 * "java:comp/env/" 是否需要添加。PersistenceAnnotationBeanPostProcessor 默认为 "true"。
	 * @see org.springframework.jndi.JndiLocatorSupport#setResourceRef
	 */
	public void setResourceRef(boolean resourceRef) {
		this.jndiLocator.setResourceRef(resourceRef);
	}

	/**
	 * 指定要委托的 {@link java.util.concurrent.Executor} 的 JNDI 名称，
	 * 替换默认 JNDI 名称 "java:comp/DefaultManagedExecutorService"。
	 * <p>可以是完全限定 JNDI 名称，或在 "resourceRef" 为 "true" 时
	 * 相对于当前环境命名上下文的 JNDI 名称。
	 * @see #setConcurrentExecutor
	 * @see #setResourceRef
	 */
	public void setJndiName(String jndiName) {
		this.jndiName = jndiName;
	}

	@Override
	public void afterPropertiesSet() throws NamingException {
		setConcurrentExecutor(this.jndiLocator.lookup(this.jndiName, Executor.class));
	}

}
