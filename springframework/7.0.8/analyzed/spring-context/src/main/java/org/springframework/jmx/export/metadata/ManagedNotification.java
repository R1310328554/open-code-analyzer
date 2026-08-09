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

package org.springframework.jmx.export.metadata;

import org.jspecify.annotations.Nullable;

import org.springframework.util.StringUtils;

/**
 * 指示 Bean 发出的 JMX 通知的元数据。
 *
 * @author Rob Harrop
 * @since 2.0
 */
public class ManagedNotification {

	private String @Nullable [] notificationTypes;

	private @Nullable String name;

	private @Nullable String description;


	/**
	 * 设置单个通知类型，或以逗号分隔字符串形式设置多个通知类型。
	 */
	public void setNotificationType(String notificationType) {
		this.notificationTypes = StringUtils.commaDelimitedListToStringArray(notificationType);
	}

	/**
	 * 设置通知类型列表。
	 */
	public void setNotificationTypes(String @Nullable ... notificationTypes) {
		this.notificationTypes = notificationTypes;
	}

	/**
	 * 返回通知类型列表。
	 */
	public String @Nullable [] getNotificationTypes() {
		return this.notificationTypes;
	}

	/**
	 * 设置该通知的名称。
	 */
	public void setName(@Nullable String name) {
		this.name = name;
	}

	/**
	 * 返回该通知的名称。
	 */
	public @Nullable String getName() {
		return this.name;
	}

	/**
	 * 设置该通知的描述信息。
	 */
	public void setDescription(@Nullable String description) {
		this.description = description;
	}

	/**
	 * 返回该通知的描述信息。
	 */
	public @Nullable String getDescription() {
		return this.description;
	}

}
