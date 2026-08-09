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
import java.util.concurrent.ThreadFactory;

import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jndi.JndiLocatorDelegate;
import org.springframework.jndi.JndiTemplate;

/**
 * 基于 JNDI 的 {@link CustomizableThreadFactory} 变体，
 * 在 Jakarta EE 环境中默认查找 JSR-236 的 "java:comp/DefaultManagedThreadFactory"，
 * 未找到时回退到本地 {@link CustomizableThreadFactory} 配置。
 *
 * <p>这是在 Jakarta EE 环境中使用受管线程的便捷方式，
 * 否则直接使用常规本地线程，无需条件配置（即无需 profile）。
 *
 * <p>注意：本类并非严格基于 JSR-236；可与 JNDI 中找到的任何常规
 * {@link java.util.concurrent.ThreadFactory} 配合工作。
 * 因此可通过 {@link #setJndiName "jndiName"} Bean 属性自定义默认 JNDI 名称
 * "java:comp/DefaultManagedThreadFactory"。
 *
 * @author Juergen Hoeller
 * @since 4.0
 */
@SuppressWarnings("serial")
public class DefaultManagedAwareThreadFactory extends CustomizableThreadFactory implements InitializingBean {

	protected final Log logger = LogFactory.getLog(getClass());

	private final JndiLocatorDelegate jndiLocator = new JndiLocatorDelegate();

	private @Nullable String jndiName = "java:comp/DefaultManagedThreadFactory";

	private @Nullable ThreadFactory threadFactory;


	/**
	 * 设置 JNDI 查找使用的 JNDI 模板。
	 * @see org.springframework.jndi.JndiAccessor#setJndiTemplate
	 */
	public void setJndiTemplate(JndiTemplate jndiTemplate) {
		this.jndiLocator.setJndiTemplate(jndiTemplate);
	}

	/**
	 * 设置 JNDI 查找使用的 JNDI 环境。
	 * @see org.springframework.jndi.JndiAccessor#setJndiEnvironment
	 */
	public void setJndiEnvironment(Properties jndiEnvironment) {
		this.jndiLocator.setJndiEnvironment(jndiEnvironment);
	}

	/**
	 * 设置查找是否发生在 Jakarta EE 容器中，
	 * 即 JNDI 名称尚未包含时是否需添加前缀 "java:comp/env/"。
	 * PersistenceAnnotationBeanPostProcessor 默认为 "true"。
	 * @see org.springframework.jndi.JndiLocatorSupport#setResourceRef
	 */
	public void setResourceRef(boolean resourceRef) {
		this.jndiLocator.setResourceRef(resourceRef);
	}

	/**
	 * 指定委托的 {@link java.util.concurrent.ThreadFactory} 的 JNDI 名称，
	 * 替换默认 JNDI 名称 "java:comp/DefaultManagedThreadFactory"。
	 * <p>可为完全限定 JNDI 名称，或在 "resourceRef" 为 "true" 时
	 * 相对于当前环境命名上下文的 JNDI 名称。
	 * @see #setResourceRef
	 */
	public void setJndiName(String jndiName) {
		this.jndiName = jndiName;
	}

	@Override
	public void afterPropertiesSet() throws NamingException {
		if (this.jndiName != null) {
			try {
				this.threadFactory = this.jndiLocator.lookup(this.jndiName, ThreadFactory.class);
			}
			catch (NamingException ex) {
				if (logger.isTraceEnabled()) {
					logger.trace("Failed to retrieve [" + this.jndiName + "] from JNDI", ex);
				}
				logger.info("Could not find default managed thread factory in JNDI - " +
						"proceeding with default local thread factory");
			}
		}
	}


	@Override
	public Thread newThread(Runnable runnable) {
		if (this.threadFactory != null) {
			return this.threadFactory.newThread(runnable);
		}
		else {
			return super.newThread(runnable);
		}
	}

}
