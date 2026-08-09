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

package org.springframework.jmx.access;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXServiceURL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.JmxException;
import org.springframework.jmx.MBeanServerNotFoundException;
import org.springframework.jmx.support.NotificationListenerHolder;
import org.springframework.util.CollectionUtils;

/**
 * 注册器对象，将特定的 {@link javax.management.NotificationListener}
 * 与一个或多个 {@link javax.management.MBeanServer} 中的 MBean 关联
 * （通常通过 {@link javax.management.MBeanServerConnection}）。
 *
 * @author Juergen Hoeller
 * @since 2.5.2
 * @see #setServer
 * @see #setMappedObjectNames
 * @see #setNotificationListener
 */
public class NotificationListenerRegistrar extends NotificationListenerHolder
		implements InitializingBean, DisposableBean {

	/** 子类可用的 Logger。 */
	protected final Log logger = LogFactory.getLog(getClass());

	private final ConnectorDelegate connector = new ConnectorDelegate();

	private @Nullable MBeanServerConnection server;

	private @Nullable JMXServiceURL serviceUrl;

	private @Nullable Map<String, ?> environment;

	private @Nullable String agentId;

	private ObjectName @Nullable [] actualObjectNames;


	/**
	 * 设置用于连接所有调用所路由到的 MBean 的 {@code MBeanServerConnection}。
	 */
	public void setServer(MBeanServerConnection server) {
		this.server = server;
	}

	/**
	 * 指定 JMX 连接器的环境。
	 * @see javax.management.remote.JMXConnectorFactory#connect(javax.management.remote.JMXServiceURL, java.util.Map)
	 */
	public void setEnvironment(@Nullable Map<String, ?> environment) {
		this.environment = environment;
	}

	/**
	 * 允许以 {@code Map} 方式访问要为连接器设置的环境，并可选择添加或覆盖特定条目。
	 * <p>适用于直接指定条目，例如通过 {@code environment[myKey]}；
	 * 在子 bean 定义中添加或覆盖条目时尤其有用。
	 */
	public @Nullable Map<String, ?> getEnvironment() {
		return this.environment;
	}

	/**
	 * 设置远程 {@code MBeanServer} 的服务 URL。
	 */
	public void setServiceUrl(String url) throws MalformedURLException {
		this.serviceUrl = new JMXServiceURL(url);
	}

	/**
	 * 设置要定位的 {@code MBeanServer} 的 agent id。
	 * <p>默认为无。若指定，将尝试定位对应的 MBeanServer，除非已设置
	 * {@link #setServiceUrl "serviceUrl"} 属性。
	 * @see javax.management.MBeanServerFactory#findMBeanServer(String)
	 * <p>指定空字符串表示平台 MBeanServer。
	 */
	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}


	@Override
	public void afterPropertiesSet() {
		if (getNotificationListener() == null) {
			throw new IllegalArgumentException("Property 'notificationListener' is required");
		}
		if (CollectionUtils.isEmpty(this.mappedObjectNames)) {
			throw new IllegalArgumentException("Property 'mappedObjectName' is required");
		}
		prepare();
	}

	/**
	 * 注册指定的 {@code NotificationListener}。
	 * <p>确保已配置 {@code MBeanServerConnection}，若未提供则尝试检测本地连接。
	 */
	public void prepare() {
		if (this.server == null) {
			this.server = this.connector.connect(this.serviceUrl, this.environment, this.agentId);
		}
		try {
			this.actualObjectNames = getResolvedObjectNames();
			if (this.actualObjectNames != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Registering NotificationListener for MBeans " + Arrays.toString(this.actualObjectNames));
				}
				for (ObjectName actualObjectName : this.actualObjectNames) {
					this.server.addNotificationListener(
							actualObjectName, getNotificationListener(), getNotificationFilter(), getHandback());
				}
			}
		}
		catch (IOException ex) {
			throw new MBeanServerNotFoundException(
					"Could not connect to remote MBeanServer at URL [" + this.serviceUrl + "]", ex);
		}
		catch (Exception ex) {
			throw new JmxException("Unable to register NotificationListener", ex);
		}
	}

	/**
	 * 注销指定的 {@code NotificationListener}。
	 */
	@Override
	public void destroy() {
		try {
			if (this.server != null && this.actualObjectNames != null) {
				for (ObjectName actualObjectName : this.actualObjectNames) {
					try {
						this.server.removeNotificationListener(
								actualObjectName, getNotificationListener(), getNotificationFilter(), getHandback());
					}
					catch (Exception ex) {
						if (logger.isDebugEnabled()) {
							logger.debug("Unable to unregister NotificationListener", ex);
						}
					}
				}
			}
		}
		finally {
			this.connector.close();
		}
	}

}
