/*
 * Copyright 2012-present the original author or authors.
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

package org.springframework.boot.autoconfigure.jmx;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.jmx.support.RegistrationPolicy;

/**
 * JMX 的配置属性。
 *
 * @author Scott Frederick
 * @since 2.7.0
 */
@ConfigurationProperties("spring.jmx")
public class JmxProperties {

	/**
	 * 将 Spring 的管理 Bean 暴露到 JMX 域。
	 */
	private boolean enabled;

	/**
	 * 是否应确保运行时对象名称唯一。
	 */
	private boolean uniqueNames;

	/**
	 * MBeanServer Bean 名称。
	 */
	private String server = "mbeanServer";

	/**
	 * JMX 域名。
	 */
	private @Nullable String defaultDomain;

	/**
	 * JMX 注册策略。
	 */
	private RegistrationPolicy registrationPolicy = RegistrationPolicy.FAIL_ON_EXISTING;

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isUniqueNames() {
		return this.uniqueNames;
	}

	public void setUniqueNames(boolean uniqueNames) {
		this.uniqueNames = uniqueNames;
	}

	public String getServer() {
		return this.server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public @Nullable String getDefaultDomain() {
		return this.defaultDomain;
	}

	public void setDefaultDomain(@Nullable String defaultDomain) {
		this.defaultDomain = defaultDomain;
	}

	public RegistrationPolicy getRegistrationPolicy() {
		return this.registrationPolicy;
	}

	public void setRegistrationPolicy(RegistrationPolicy registrationPolicy) {
		this.registrationPolicy = registrationPolicy;
	}

}
