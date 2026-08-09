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

package org.springframework.jca.endpoint;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ActivationSpec;
import jakarta.resource.spi.ResourceAdapter;
import jakarta.resource.spi.endpoint.MessageEndpointFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.SmartLifecycle;
import org.springframework.util.Assert;

/**
 * 在 Spring 应用上下文中管理 JCA 1.7 消息端点的通用 Bean，
 * 作为应用上下文生命周期的一部分激活和停用端点。
 *
 * <p>本类完全通用，可与任意 ResourceAdapter、
 * MessageEndpointFactory 和 ActivationSpec 配合工作。
 * 可按标准 Bean 风格配置，例如通过 Spring XML Bean 定义格式，如下所示：
 *
 * <pre class="code">
 * &lt;bean class="org.springframework.jca.endpoint.GenericMessageEndpointManager"&gt;
 *  &lt;property name="resourceAdapter" ref="resourceAdapter"/&gt;
 *  &lt;property name="messageEndpointFactory"&gt;
 *    &lt;bean class="org.springframework.jca.endpoint.GenericMessageEndpointFactory"&gt;
 *      &lt;property name="messageListener" ref="messageListener"/&gt;
 *    &lt;/bean&gt;
 *  &lt;/property&gt;
 *  &lt;property name="activationSpec"&gt;
 *    &lt;bean class="org.apache.activemq.ra.ActiveMQActivationSpec"&gt;
 *      &lt;property name="destination" value="myQueue"/&gt;
 *      &lt;property name="destinationType" value="jakarta.jms.Queue"/&gt;
 *    &lt;/bean&gt;
 *  &lt;/property&gt;
 * &lt;/bean&gt;
 * </pre>
 *
 * <p>In this example, Spring's own {@link GenericMessageEndpointFactory} is used
 * to point to a standard message listener object that happens to be supported
 * by the specified target ResourceAdapter: in this case, a JMS
 * {@link jakarta.jms.MessageListener} object as supported by the ActiveMQ
 * message broker, defined as a Spring bean:
 *
 * <pre class="code">
 * &lt;bean id="messageListener" class="com.myorg.messaging.myMessageListener"&gt;
 *   &lt;!-- ... --&gt;
 * &lt;/bean&gt;
 * </pre>
 *
 * <p>The target ResourceAdapter may be configured as a local Spring bean as well
 * (the typical case) or obtained from JNDI. For the example above, a local
 * ResourceAdapter bean could be defined as follows (matching the "resourceAdapter"
 * bean reference above):
 *
 * <pre class="code">
 * &lt;bean id="resourceAdapter" class="org.springframework.jca.support.ResourceAdapterFactoryBean"&gt;
 *  &lt;property name="resourceAdapter"&gt;
 *    &lt;bean class="org.apache.activemq.ra.ActiveMQResourceAdapter"&gt;
 *      &lt;property name="serverUrl" value="tcp://localhost:61616"/&gt;
 *    &lt;/bean&gt;
 *  &lt;/property&gt;
 *  &lt;property name="workManager"&gt;
 *    &lt;bean class="..."/&gt;
 *  &lt;/property&gt;
 * &lt;/bean&gt;
 * </pre>
 *
 * <p>For a different target resource, the configuration would simply point to a
 * different ResourceAdapter and a different ActivationSpec object (which are
 * both specific to the resource provider), and possibly a different message
 * listener (for example, a CCI {@link jakarta.resource.cci.MessageListener} for a
 * resource adapter which is based on the JCA Common Client Interface).
 *
 * <p>The asynchronous execution strategy can be customized through the
 * "workManager" property on the ResourceAdapterFactoryBean as shown above,
 * where {@code <bean class="..."/>} should be replaced with configuration for
 * any JCA-compliant {@code WorkManager}.
 *
 * <p>Transactional execution is a responsibility of the concrete message endpoint,
 * as built by the specified MessageEndpointFactory. {@link GenericMessageEndpointFactory}
 * supports XA transaction participation through its "transactionManager" property,
 * typically with a Spring {@link org.springframework.transaction.jta.JtaTransactionManager}
 * or a plain {@link jakarta.transaction.TransactionManager} implementation specified there.
 *
 * <pre class="code">
 * &lt;bean class="org.springframework.jca.endpoint.GenericMessageEndpointManager"&gt;
 *  &lt;property name="resourceAdapter" ref="resourceAdapter"/&gt;
 *  &lt;property name="messageEndpointFactory"&gt;
 *    &lt;bean class="org.springframework.jca.endpoint.GenericMessageEndpointFactory"&gt;
 *      &lt;property name="messageListener" ref="messageListener"/&gt;
 *      &lt;property name="transactionManager" ref="transactionManager"/&gt;
 *    &lt;/bean&gt;
 *  &lt;/property&gt;
 *  &lt;property name="activationSpec"&gt;
 *    &lt;bean class="org.apache.activemq.ra.ActiveMQActivationSpec"&gt;
 *      &lt;property name="destination" value="myQueue"/&gt;
 *      &lt;property name="destinationType" value="jakarta.jms.Queue"/&gt;
 *    &lt;/bean&gt;
 *  &lt;/property&gt;
 * &lt;/bean&gt;
 *
 * &lt;bean id="transactionManager" class="org.springframework.transaction.jta.JtaTransactionManager"/&gt;
 * </pre>
 *
 * <p>Alternatively, check out your resource provider's ActivationSpec object,
 * which should support local transactions through a provider-specific config flag,
 * for example, ActiveMQActivationSpec's "useRAManagedTransaction" bean property.
 *
 * <pre class="code">
 * &lt;bean class="org.springframework.jca.endpoint.GenericMessageEndpointManager"&gt;
 *  &lt;property name="resourceAdapter" ref="resourceAdapter"/&gt;
 *  &lt;property name="messageEndpointFactory"&gt;
 *    &lt;bean class="org.springframework.jca.endpoint.GenericMessageEndpointFactory"&gt;
 *      &lt;property name="messageListener" ref="messageListener"/&gt;
 *    &lt;/bean&gt;
 *  &lt;/property&gt;
 *  &lt;property name="activationSpec"&gt;
 *    &lt;bean class="org.apache.activemq.ra.ActiveMQActivationSpec"&gt;
 *      &lt;property name="destination" value="myQueue"/&gt;
 *      &lt;property name="destinationType" value="jakarta.jms.Queue"/&gt;
 *      &lt;property name="useRAManagedTransaction" value="true"/&gt;
 *    &lt;/bean&gt;
 *  &lt;/property&gt;
 * &lt;/bean&gt;
 * </pre>
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see jakarta.resource.spi.ResourceAdapter#endpointActivation
 * @see jakarta.resource.spi.ResourceAdapter#endpointDeactivation
 * @see jakarta.resource.spi.endpoint.MessageEndpointFactory
 * @see jakarta.resource.spi.ActivationSpec
 */
public class GenericMessageEndpointManager implements SmartLifecycle, InitializingBean, DisposableBean {

	private @Nullable ResourceAdapter resourceAdapter;

	private @Nullable MessageEndpointFactory messageEndpointFactory;

	private @Nullable ActivationSpec activationSpec;

	private boolean autoStartup = true;

	private int phase = DEFAULT_PHASE;

	private volatile boolean running;

	private final Object lifecycleMonitor = new Object();


	/**
	 * 设置要管理端点的 JCA ResourceAdapter。
	 */
	public void setResourceAdapter(@Nullable ResourceAdapter resourceAdapter) {
		this.resourceAdapter = resourceAdapter;
	}

	/**
	 * 返回要管理端点的 JCA ResourceAdapter。
	 */
	public @Nullable ResourceAdapter getResourceAdapter() {
		return this.resourceAdapter;
	}

	/**
	 * 设置要激活的 JCA MessageEndpointFactory，指向端点将委托的 MessageListener 对象。
	 * <p>A MessageEndpointFactory instance may be shared across multiple
	 * endpoints (i.e. multiple GenericMessageEndpointManager instances),
	 * with different {@link #setActivationSpec ActivationSpec} objects applied.
	 * @see GenericMessageEndpointFactory#setMessageListener
	 */
	public void setMessageEndpointFactory(@Nullable MessageEndpointFactory messageEndpointFactory) {
		this.messageEndpointFactory = messageEndpointFactory;
	}

	/**
	 * 返回要激活的 JCA MessageEndpointFactory。
	 */
	public @Nullable MessageEndpointFactory getMessageEndpointFactory() {
		return this.messageEndpointFactory;
	}

	/**
	 * 设置用于激活端点的 JCA ActivationSpec。
	 * <p>注意，此 ActivationSpec 实例不应在多个 ResourceAdapter 实例间共享。
	 */
	public void setActivationSpec(@Nullable ActivationSpec activationSpec) {
		this.activationSpec = activationSpec;
	}

	/**
	 * 返回用于激活端点的 JCA ActivationSpec。
	 */
	public @Nullable ActivationSpec getActivationSpec() {
		return this.activationSpec;
	}

	/**
	 * 设置本端点管理器初始化且上下文刷新后是否自动启动端点激活。
	 * <p>默认为 "true"。关闭此标志可推迟端点激活，
	 * 直至显式调用 {@link #start()}。
	 */
	public void setAutoStartup(boolean autoStartup) {
		this.autoStartup = autoStartup;
	}

	/**
	 * 返回 'autoStartup' 属性值。若为 "true"，
	 * 本端点管理器将在 ContextRefreshedEvent 时启动。
	 */
	@Override
	public boolean isAutoStartup() {
		return this.autoStartup;
	}

	/**
	 * 指定本端点管理器应启动和停止的阶段。
	 * 启动顺序从低到高，关闭顺序相反。
	 * 默认值为 {@code Integer.MAX_VALUE}，表示尽可能晚启动、尽可能早停止。
	 */
	public void setPhase(int phase) {
		this.phase = phase;
	}

	/**
	 * 返回本端点管理器将启动和停止的阶段。
	 */
	@Override
	public int getPhase() {
		return this.phase;
	}

	/**
	 * 准备消息端点，若 "autoStartup" 标志为 "true" 则自动激活。
	 */
	@Override
	public void afterPropertiesSet() throws ResourceException {
		if (getResourceAdapter() == null) {
			throw new IllegalArgumentException("Property 'resourceAdapter' is required");
		}
		if (getMessageEndpointFactory() == null) {
			throw new IllegalArgumentException("Property 'messageEndpointFactory' is required");
		}
		ActivationSpec activationSpec = getActivationSpec();
		if (activationSpec == null) {
			throw new IllegalArgumentException("Property 'activationSpec' is required");
		}

		if (activationSpec.getResourceAdapter() == null) {
			activationSpec.setResourceAdapter(getResourceAdapter());
		}
		else if (activationSpec.getResourceAdapter() != getResourceAdapter()) {
			throw new IllegalArgumentException("ActivationSpec [" + activationSpec +
					"] is associated with a different ResourceAdapter: " + activationSpec.getResourceAdapter());
		}
	}

	/**
	 * 激活已配置的消息端点。
	 */
	@Override
	public void start() {
		synchronized (this.lifecycleMonitor) {
			if (!this.running) {
				ResourceAdapter resourceAdapter = getResourceAdapter();
				Assert.state(resourceAdapter != null, "No ResourceAdapter set");
				try {
					resourceAdapter.endpointActivation(getMessageEndpointFactory(), getActivationSpec());
				}
				catch (ResourceException ex) {
					throw new IllegalStateException("Could not activate message endpoint", ex);
				}
				this.running = true;
			}
		}
	}

	/**
	 * 停用已配置的消息端点。
	 */
	@Override
	public void stop() {
		synchronized (this.lifecycleMonitor) {
			if (this.running) {
				ResourceAdapter resourceAdapter = getResourceAdapter();
				Assert.state(resourceAdapter != null, "No ResourceAdapter set");
				resourceAdapter.endpointDeactivation(getMessageEndpointFactory(), getActivationSpec());
				this.running = false;
			}
		}
	}

	@Override
	public void stop(Runnable callback) {
		synchronized (this.lifecycleMonitor) {
			stop();
			callback.run();
		}
	}

	/**
	 * 返回已配置的消息端点当前是否处于活动状态。
	 */
	@Override
	public boolean isRunning() {
		return this.running;
	}

	/**
	 * 停用消息端点，为关闭做准备。
	 */
	@Override
	public void destroy() {
		stop();
	}

}
