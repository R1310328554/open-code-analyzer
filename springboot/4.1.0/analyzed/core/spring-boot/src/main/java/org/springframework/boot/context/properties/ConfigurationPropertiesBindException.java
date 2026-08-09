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

package org.springframework.boot.context.properties;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.util.ClassUtils;

/**
 * {@link ConfigurationProperties @ConfigurationProperties} 绑定失败时抛出的异常。
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @since 2.0.0
 */
public class ConfigurationPropertiesBindException extends BeanCreationException {

	private final ConfigurationPropertiesBean bean;

	ConfigurationPropertiesBindException(ConfigurationPropertiesBean bean, Exception cause) {
		super(bean.getName(), getMessage(bean), cause);
		this.bean = bean;
	}

	/**
	 * 返回正在绑定的 Bean 类型。
	 *
	 * @return Bean 类型
	 */
	public Class<?> getBeanType() {
		return this.bean.getType();
	}

	/**
	 * 返回触发绑定的配置属性注解。
	 *
	 * @return 配置属性注解
	 */
	public ConfigurationProperties getAnnotation() {
		return this.bean.getAnnotation();
	}

	private static String getMessage(ConfigurationPropertiesBean bean) {
		ConfigurationProperties annotation = bean.getAnnotation();
		StringBuilder message = new StringBuilder();
		message.append("Could not bind properties to '");
		message.append(ClassUtils.getShortName(bean.getType())).append("' : ");
		message.append("prefix=").append(annotation.prefix());
		message.append(", ignoreInvalidFields=").append(annotation.ignoreInvalidFields());
		message.append(", ignoreUnknownFields=").append(annotation.ignoreUnknownFields());
		return message.toString();
	}

}
