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

package org.springframework.jmx.support;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.MBeanServerNotFoundException;

/**
 * 通过标准 JMX 1.2 {@link javax.management.MBeanServerFactory} API
 * 获取 {@link javax.management.MBeanServer} 引用的 {@link FactoryBean}。
 *
 * <p>将 {@code MBeanServer} 暴露为 Bean 引用。
 *
 * <p>默认 {@code MBeanServerFactoryBean} 即使已有运行中的 {@code MBeanServer} 也会创建新实例。
 * 若希望先尝试定位运行中的 {@code MBeanServer}，将 {@code locateExistingServerIfPossible} 设为 {@code true}。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 1.2
 * @see #setLocateExistingServerIfPossible
 * @see #locateMBeanServer
 * @see javax.management.MBeanServer
 * @see javax.management.MBeanServerFactory#findMBeanServer
 * @see javax.management.MBeanServerFactory#createMBeanServer
 * @see javax.management.MBeanServerFactory#newMBeanServer
 * @see MBeanServerConnectionFactoryBean
 * @see ConnectorServerFactoryBean
 */
public class MBeanServerFactoryBean implements FactoryBean<MBeanServer>, InitializingBean, DisposableBean {

	protected final Log logger = LogFactory.getLog(getClass());

	private boolean locateExistingServerIfPossible = false;

	private @Nullable String agentId;

	private @Nullable String defaultDomain;

	private boolean registerWithFactory = true;

	private @Nullable MBeanServer server;

	private boolean newlyRegistered = false;


	/**
	 * 设置是否先尝试定位运行中的 {@code MBeanServer} 再创建。
	 * <p>默认为 {@code false}。
	 */
	public void setLocateExistingServerIfPossible(boolean locateExistingServerIfPossible) {
		this.locateExistingServerIfPossible = locateExistingServerIfPossible;
	}

	/**
	 * 设置要定位的 {@code MBeanServer} 的 agent id。
	 * <p>默认为无。若指定，将自动尝试定位对应 MBeanServer；若找不到则<b>不会</b>创建新实例
	 * （解析时抛出 {@code MBeanServerNotFoundException}）。
	 * <p>空字符串表示平台 MBeanServer。
	 * @see javax.management.MBeanServerFactory#findMBeanServer(String)
	 */
	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}

	/**
	 * 设置 {@code MBeanServer} 的默认域，传递给
	 * {@code MBeanServerFactory.createMBeanServer()} 或 {@code findMBeanServer()}。
	 * <p>默认为无。
	 * @see javax.management.MBeanServerFactory#createMBeanServer(String)
	 * @see javax.management.MBeanServerFactory#findMBeanServer(String)
	 */
	public void setDefaultDomain(String defaultDomain) {
		this.defaultDomain = defaultDomain;
	}

	/**
	 * 设置是否将 {@code MBeanServer} 注册到 {@code MBeanServerFactory}，
	 * 使其可通过 {@code MBeanServerFactory.findMBeanServer()} 找到。
	 * <p>默认为 {@code true}。
	 * @see javax.management.MBeanServerFactory#createMBeanServer
	 * @see javax.management.MBeanServerFactory#findMBeanServer
	 */
	public void setRegisterWithFactory(boolean registerWithFactory) {
		this.registerWithFactory = registerWithFactory;
	}


	/** 创建 {@code MBeanServer} 实例。 */
	@Override
	public void afterPropertiesSet() throws MBeanServerNotFoundException {
		// Try to locate existing MBeanServer, if desired.
		if (this.locateExistingServerIfPossible || this.agentId != null) {
			try {
				this.server = locateMBeanServer(this.agentId);
			}
			catch (MBeanServerNotFoundException ex) {
				// If agentId was specified, we were only supposed to locate that
				// specific MBeanServer; so let's bail if we can't find it.
				if (this.agentId != null) {
					throw ex;
				}
				logger.debug("No existing MBeanServer found - creating new one");
			}
		}

		// Create a new MBeanServer and register it, if desired.
		if (this.server == null) {
			this.server = createMBeanServer(this.defaultDomain, this.registerWithFactory);
			this.newlyRegistered = this.registerWithFactory;
		}
	}

	/**
	 * 尝试定位现有 {@code MBeanServer}。
	 * 在 {@code locateExistingServerIfPossible} 为 {@code true} 时调用。
	 * <p>默认使用标准查找。子类可重写以添加额外定位逻辑。
	 * @param agentId MBeanServer 代理标识；{@code null} 时考虑全部已注册服务器
	 * @return 找到的 {@code MBeanServer}
	 * @throws org.springframework.jmx.MBeanServerNotFoundException 找不到时
	 * @see #setLocateExistingServerIfPossible
	 * @see JmxUtils#locateMBeanServer(String)
	 * @see javax.management.MBeanServerFactory#findMBeanServer(String)
	 */
	protected MBeanServer locateMBeanServer(@Nullable String agentId) throws MBeanServerNotFoundException {
		return JmxUtils.locateMBeanServer(agentId);
	}

	/**
	 * 创建新 {@code MBeanServer} 实例并按需注册到 {@code MBeanServerFactory}。
	 * @param defaultDomain 默认域，或 {@code null}
	 * @param registerWithFactory 是否注册到 {@code MBeanServerFactory}
	 * @see javax.management.MBeanServerFactory#createMBeanServer
	 * @see javax.management.MBeanServerFactory#newMBeanServer
	 */
	protected MBeanServer createMBeanServer(@Nullable String defaultDomain, boolean registerWithFactory) {
		if (registerWithFactory) {
			return MBeanServerFactory.createMBeanServer(defaultDomain);
		}
		else {
			return MBeanServerFactory.newMBeanServer(defaultDomain);
		}
	}


	@Override
	public @Nullable MBeanServer getObject() {
		return this.server;
	}

	@Override
	public Class<? extends MBeanServer> getObjectType() {
		return (this.server != null ? this.server.getClass() : MBeanServer.class);
	}

	@Override
	public boolean isSingleton() {
		return true;
	}


	/** 按需注销 {@code MBeanServer} 实例。 */
	@Override
	public void destroy() {
		if (this.newlyRegistered) {
			MBeanServerFactory.releaseMBeanServer(this.server);
		}
	}

}
