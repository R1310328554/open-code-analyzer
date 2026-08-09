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

import org.springframework.aop.TargetSource;

/**
 * 为 {@code getTarget()} 调用提供可配置 JNDI 查找的 AOP {@link org.springframework.aop.TargetSource}。
 *
 * <p>可作为 {@link JndiObjectFactoryBean} 的替代，支持懒加载或每次操作重新查找 JNDI 对象
 * （见 {@code lookupOnStartup} 与 {@code cache} 属性），开发时便于热重启 JNDI 服务器（如远程 JMS）。
 *
 * <p>示例：
 *
 * <pre class="code">
 * &lt;bean id="queueConnectionFactoryTarget" class="org.springframework.jndi.JndiObjectTargetSource"&gt;
 *   &lt;property name="jndiName" value="JmsQueueConnectionFactory"/&gt;
 *   &lt;property name="lookupOnStartup" value="false"/&gt;
 * &lt;/bean&gt;
 *
 * &lt;bean id="queueConnectionFactory" class="org.springframework.aop.framework.ProxyFactoryBean"&gt;
 *   &lt;property name="proxyInterfaces" value="jakarta.jms.QueueConnectionFactory"/&gt;
 *   &lt;property name="targetSource" ref="queueConnectionFactoryTarget"/&gt;
 * &lt;/bean&gt;</pre>
 *
 * 对 {@code queueConnectionFactory} 代理调用 {@code createQueueConnection} 将懒加载
 * {@code JmsQueueConnectionFactory} 并委托给检索到的 QueueConnectionFactory。
 *
 * <p><b>或者使用带 {@code proxyInterface} 的 {@link JndiObjectFactoryBean}。</b>
 * 可在 JndiObjectFactoryBean 上指定 {@code lookupOnStartup} 与 {@code cache}，
 * 底层自动创建 JndiObjectTargetSource。
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see #setLookupOnStartup
 * @see #setCache
 * @see org.springframework.aop.framework.ProxyFactoryBean#setTargetSource
 * @see JndiObjectFactoryBean#setProxyInterface
 */
public class JndiObjectTargetSource extends JndiObjectLocator implements TargetSource {

	private boolean lookupOnStartup = true;

	private boolean cache = true;

	private @Nullable Object cachedObject;

	private @Nullable Class<?> targetClass;


	/**
	 * 设置是否在启动时查找 JNDI 对象。默认为 {@code true}。
	 * <p>可关闭以允许 JNDI 对象延迟可用，首次访问时才获取。
	 * @see #setCache
	 */
	public void setLookupOnStartup(boolean lookupOnStartup) {
		this.lookupOnStartup = lookupOnStartup;
	}

	/**
	 * 设置定位后是否缓存 JNDI 对象。默认为 {@code true}。
	 * <p>可关闭以支持 JNDI 对象热部署，每次调用重新获取。
	 * @see #setLookupOnStartup
	 */
	public void setCache(boolean cache) {
		this.cache = cache;
	}

	@Override
	public void afterPropertiesSet() throws NamingException {
		super.afterPropertiesSet();
		if (this.lookupOnStartup) {
			Object object = lookup();
			if (this.cache) {
				this.cachedObject = object;
			}
			else {
				this.targetClass = object.getClass();
			}
		}
	}


	@Override
	public @Nullable Class<?> getTargetClass() {
		if (this.cachedObject != null) {
			return this.cachedObject.getClass();
		}
		else if (this.targetClass != null) {
			return this.targetClass;
		}
		else {
			return getExpectedType();
		}
	}

	@Override
	public boolean isStatic() {
		return (this.cachedObject != null);
	}

	@Override
	public @Nullable Object getTarget() {
		try {
			if (this.lookupOnStartup || !this.cache) {
				return (this.cachedObject != null ? this.cachedObject : lookup());
			}
			else {
				synchronized (this) {
					if (this.cachedObject == null) {
						this.cachedObject = lookup();
					}
					return this.cachedObject;
				}
			}
		}
		catch (NamingException ex) {
			throw new JndiLookupFailureException("JndiObjectTargetSource failed to obtain new target object", ex);
		}
	}

}
