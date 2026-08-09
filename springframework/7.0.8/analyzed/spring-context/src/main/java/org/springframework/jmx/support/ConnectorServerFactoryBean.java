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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.MBeanServerForwarder;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.JmxException;
import org.springframework.util.CollectionUtils;

/**
 * 创建 JSR-160 {@link JMXConnectorServer} 的 {@link FactoryBean}，
 * 可选地将其注册到 {@link MBeanServer}，然后启动它。
 *
 * <p>将 {@code threaded} 属性设为 {@code true} 可在独立线程中启动 {@code JMXConnectorServer}。
 * 将 {@code daemon} 属性设为 {@code true} 可将该线程配置为守护线程。
 *
 * <p>当包含的 {@code ApplicationContext} 关闭导致本类实例销毁时，
 * {@code JMXConnectorServer} 会被正确关闭。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 1.2
 * @see JMXConnectorServer
 * @see MBeanServer
 */
public class ConnectorServerFactoryBean extends MBeanRegistrationSupport
		implements FactoryBean<JMXConnectorServer>, InitializingBean, DisposableBean {

	/** 默认服务 URL。 */
	public static final String DEFAULT_SERVICE_URL = "service:jmx:jmxmp://localhost:9875";


	private String serviceUrl = DEFAULT_SERVICE_URL;

	private final Map<String, Object> environment = new HashMap<>();

	private @Nullable MBeanServerForwarder forwarder;

	private @Nullable ObjectName objectName;

	private boolean threaded = false;

	private boolean daemon = false;

	private @Nullable JMXConnectorServer connectorServer;


	/**
	 * 设置 {@code JMXConnectorServer} 的服务 URL。
	 */
	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}

	/**
	 * 以 {@code java.util.Properties}（字符串键值对）形式设置
	 * 构造 {@code JMXConnectorServer} 所用的环境属性。
	 */
	public void setEnvironment(@Nullable Properties environment) {
		CollectionUtils.mergePropertiesIntoMap(environment, this.environment);
	}

	/**
	 * 以字符串键与任意 Object 值的 {@code Map} 形式设置
	 * 构造 {@code JMXConnector} 所用的环境属性。
	 */
	public void setEnvironmentMap(@Nullable Map<String, ?> environment) {
		if (environment != null) {
			this.environment.putAll(environment);
		}
	}

	/**
	 * 设置要应用于 {@code JMXConnectorServer} 的 MBeanServerForwarder。
	 */
	public void setForwarder(MBeanServerForwarder forwarder) {
		this.forwarder = forwarder;
	}

	/**
	 * 设置用于将 {@code JMXConnectorServer} 自身注册到 {@code MBeanServer} 的
	 * {@code ObjectName}，可为 {@code ObjectName} 实例或 {@code String}。
	 * @throws MalformedObjectNameException {@code ObjectName} 格式错误时
	 */
	public void setObjectName(Object objectName) throws MalformedObjectNameException {
		this.objectName = ObjectNameManager.getInstance(objectName);
	}

	/**
	 * 设置 {@code JMXConnectorServer} 是否应在独立线程中启动。
	 */
	public void setThreaded(boolean threaded) {
		this.threaded = threaded;
	}

	/**
	 * 设置为 {@code JMXConnectorServer} 启动的线程是否应作为守护线程启动。
	 */
	public void setDaemon(boolean daemon) {
		this.daemon = daemon;
	}


	/**
	 * 启动连接器服务器。若 {@code threaded} 标志为 {@code true}，
	 * {@code JMXConnectorServer} 将在独立线程中启动。
	 * 若 {@code daemon} 标志为 {@code true}，该线程将作为守护线程启动。
	 * @throws JMException 向 {@code MBeanServer} 注册连接器服务器时发生问题时
	 * @throws IOException 启动连接器服务器时发生问题时
	 */
	@Override
	public void afterPropertiesSet() throws JMException, IOException {
		if (this.server == null) {
			this.server = JmxUtils.locateMBeanServer();
		}

		// Create the JMX service URL.
		JMXServiceURL url = new JMXServiceURL(this.serviceUrl);

		// Create the connector server now.
		this.connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, this.environment, this.server);

		// Set the given MBeanServerForwarder, if any.
		if (this.forwarder != null) {
			this.connectorServer.setMBeanServerForwarder(this.forwarder);
		}

		// Do we want to register the connector with the MBean server?
		if (this.objectName != null) {
			doRegister(this.connectorServer, this.objectName);
		}

		try {
			if (this.threaded) {
				// Start the connector server asynchronously (in a separate thread).
				final JMXConnectorServer serverToStart = this.connectorServer;
				Thread connectorThread = new Thread() {
					@Override
					public void run() {
						try {
							serverToStart.start();
						}
						catch (IOException ex) {
							throw new JmxException("Could not start JMX connector server after delay", ex);
						}
					}
				};

				connectorThread.setName("JMX Connector Thread [" + this.serviceUrl + "]");
				connectorThread.setDaemon(this.daemon);
				connectorThread.start();
			}
			else {
				// Start the connector server in the same thread.
				this.connectorServer.start();
			}

			if (logger.isInfoEnabled()) {
				logger.info("JMX connector server started: " + this.connectorServer);
			}
		}

		catch (IOException ex) {
			// Unregister the connector server if startup failed.
			unregisterBeans();
			throw ex;
		}
	}


	@Override
	public @Nullable JMXConnectorServer getObject() {
		return this.connectorServer;
	}

	@Override
	public Class<? extends JMXConnectorServer> getObjectType() {
		return (this.connectorServer != null ? this.connectorServer.getClass() : JMXConnectorServer.class);
	}

	@Override
	public boolean isSingleton() {
		return true;
	}


	/**
	 * 停止本类实例管理的 {@code JMXConnectorServer}。
	 * 在 {@code ApplicationContext} 关闭时自动调用。
	 * @throws IOException 停止连接器服务器时发生错误
	 */
	@Override
	public void destroy() throws IOException {
		try {
			if (this.connectorServer != null) {
				if (logger.isInfoEnabled()) {
					logger.info("Stopping JMX connector server: " + this.connectorServer);
				}
				this.connectorServer.stop();
			}
		}
		finally {
			unregisterBeans();
		}
	}

}
