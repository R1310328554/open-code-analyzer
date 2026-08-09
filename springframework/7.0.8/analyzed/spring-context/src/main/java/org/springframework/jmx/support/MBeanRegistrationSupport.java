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

import java.util.LinkedHashSet;
import java.util.Set;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;

/**
 * 为向 {@link javax.management.MBeanServer} 注册 MBean 提供基础设施支持。
 * 在指定 {@link ObjectName} 已存在 MBean 时的行为完全可配置。
 *
 * <p>跟踪全部已注册 MBean，可通过 {@link #unregisterBeans()} 注销。
 *
 * <p>子类可重写 {@link #onRegister(ObjectName)} 与 {@link #onUnregister(ObjectName)}
 * 在注册/注销时接收通知。
 *
 * <p>默认情况下，若 {@link javax.management.ObjectName} 已被使用则注册失败。
 *
 * <p>将 {@link #setRegistrationPolicy(RegistrationPolicy) registrationPolicy} 设为
 * {@link RegistrationPolicy#IGNORE_EXISTING} 时，忽略已存在 MBean，适用于多应用共享 MBeanServer 的场景。
 *
 * <p>设为 {@link RegistrationPolicy#REPLACE_EXISTING} 时，必要时替换已存在 MBean，
 * 适用于无法保证 {@link MBeanServer} 状态的场景。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Phillip Webb
 * @since 2.0
 * @see #setServer
 * @see #setRegistrationPolicy
 * @see org.springframework.jmx.export.MBeanExporter
 */
public class MBeanRegistrationSupport {

	/** 本类的 {@code Log} 实例。 */
	protected final Log logger = LogFactory.getLog(getClass());

	/** 用于注册 Bean 的 {@code MBeanServer} 实例。 */
	protected @Nullable MBeanServer server;

	/** 本导出器已注册的 Bean。 */
	private final Set<ObjectName> registeredBeans = new LinkedHashSet<>();

	/**
	 * 注册 MBean 时发现已存在同名 MBean 时使用的策略。
	 * 默认抛出异常。
	 */
	private RegistrationPolicy registrationPolicy = RegistrationPolicy.FAIL_ON_EXISTING;


	/**
	 * 指定注册全部 Bean 时使用的 {@code MBeanServer} 实例。
	 * 若未提供，{@code MBeanExporter} 会尝试定位现有 {@code MBeanServer}。
	 */
	public void setServer(@Nullable MBeanServer server) {
		this.server = server;
	}

	/** 返回 Bean 将注册到的 {@code MBeanServer}。 */
	public final @Nullable MBeanServer getServer() {
		return this.server;
	}

	/**
	 * 设置尝试以已存在的 {@link javax.management.ObjectName} 注册 MBean 时使用的策略。
	 * @param registrationPolicy 要使用的策略
	 * @since 3.2
	 */
	public void setRegistrationPolicy(RegistrationPolicy registrationPolicy) {
		Assert.notNull(registrationPolicy, "RegistrationPolicy must not be null");
		this.registrationPolicy = registrationPolicy;
	}


	/**
	 * 实际向服务器注册 MBean。遇到已存在 MBean 时的行为由 {@link #setRegistrationPolicy} 配置。
	 * @param mbean MBean 实例
	 * @param objectName 建议的 ObjectName
	 * @throws JMException 注册失败时
	 */
	protected void doRegister(Object mbean, ObjectName objectName) throws JMException {
		Assert.state(this.server != null, "No MBeanServer set");
		ObjectName actualObjectName;

		synchronized (this.registeredBeans) {
			ObjectInstance registeredBean = null;
			try {
				registeredBean = this.server.registerMBean(mbean, objectName);
			}
			catch (InstanceAlreadyExistsException ex) {
				if (this.registrationPolicy == RegistrationPolicy.IGNORE_EXISTING) {
					if (logger.isDebugEnabled()) {
						logger.debug("Ignoring existing MBean at [" + objectName + "]");
					}
				}
				else if (this.registrationPolicy == RegistrationPolicy.REPLACE_EXISTING) {
					try {
						if (logger.isDebugEnabled()) {
							logger.debug("Replacing existing MBean at [" + objectName + "]");
						}
						this.server.unregisterMBean(objectName);
						registeredBean = this.server.registerMBean(mbean, objectName);
					}
					catch (InstanceNotFoundException ex2) {
						if (logger.isInfoEnabled()) {
							logger.info("Unable to replace existing MBean at [" + objectName + "]", ex2);
						}
						throw ex;
					}
				}
				else {
					throw ex;
				}
			}

			// Track registration and notify listeners.
			actualObjectName = (registeredBean != null ? registeredBean.getObjectName() : null);
			if (actualObjectName == null) {
				actualObjectName = objectName;
			}
			this.registeredBeans.add(actualObjectName);
		}

		onRegister(actualObjectName, mbean);
	}

	/** 注销本类实例注册的全部 Bean。 */
	protected void unregisterBeans() {
		Set<ObjectName> snapshot;
		synchronized (this.registeredBeans) {
			snapshot = new LinkedHashSet<>(this.registeredBeans);
		}
		if (!snapshot.isEmpty()) {
			logger.debug("Unregistering JMX-exposed beans");
			for (ObjectName objectName : snapshot) {
				doUnregister(objectName);
			}
		}
	}

	/**
	 * 实际从服务器注销指定 MBean。
	 * @param objectName 建议的 ObjectName
	 */
	protected void doUnregister(ObjectName objectName) {
		Assert.state(this.server != null, "No MBeanServer set");
		boolean actuallyUnregistered = false;

		synchronized (this.registeredBeans) {
			if (this.registeredBeans.remove(objectName)) {
				try {
					// MBean might already have been unregistered by an external process
					if (this.server.isRegistered(objectName)) {
						this.server.unregisterMBean(objectName);
						actuallyUnregistered = true;
					}
					else {
						if (logger.isInfoEnabled()) {
							logger.info("Could not unregister MBean [" + objectName + "] as said MBean " +
									"is not registered (perhaps already unregistered by an external process)");
						}
					}
				}
				catch (JMException ex) {
					if (logger.isInfoEnabled()) {
						logger.info("Could not unregister MBean [" + objectName + "]", ex);
					}
				}
			}
		}

		if (actuallyUnregistered) {
			onUnregister(objectName);
		}
	}

	/** 返回全部已注册 Bean 的 {@link ObjectName} 数组。 */
	protected final ObjectName[] getRegisteredObjectNames() {
		synchronized (this.registeredBeans) {
			return this.registeredBeans.toArray(new ObjectName[0]);
		}
	}


	/**
	 * MBean 以给定 {@link ObjectName} 注册时调用。子类可执行额外处理。
	 * <p>默认委托给 {@link #onRegister(ObjectName)}。
	 * @param objectName MBean 实际注册使用的 {@link ObjectName}
	 * @param mbean 已注册的 MBean 实例
	 */
	protected void onRegister(ObjectName objectName, Object mbean) {
		onRegister(objectName);
	}

	/**
	 * MBean 以给定 {@link ObjectName} 注册时调用。默认实现为空，子类可重写。
	 * @param objectName MBean 实际注册使用的 {@link ObjectName}
	 */
	protected void onRegister(ObjectName objectName) {
	}

	/**
	 * MBean 以给定 {@link ObjectName} 注销时调用。默认实现为空，子类可重写。
	 * @param objectName MBean 注册时使用的 {@link ObjectName}
	 */
	protected void onUnregister(ObjectName objectName) {
	}

}
