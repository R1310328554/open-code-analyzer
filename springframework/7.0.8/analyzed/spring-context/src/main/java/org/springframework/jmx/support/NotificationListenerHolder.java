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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.management.MalformedObjectNameException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.jspecify.annotations.Nullable;

import org.springframework.util.ObjectUtils;

/**
 * 聚合 {@link javax.management.NotificationListener}、
 * {@link javax.management.NotificationFilter} 及任意 handback 对象的辅助类，
 * 以及监听器希望接收 {@link javax.management.Notification} 的 MBean 名称集合。
 *
 * @author Juergen Hoeller
 * @since 2.5.2
 * @see org.springframework.jmx.export.NotificationListenerBean
 * @see org.springframework.jmx.access.NotificationListenerRegistrar
 */
public class NotificationListenerHolder {

	private @Nullable NotificationListener notificationListener;

	private @Nullable NotificationFilter notificationFilter;

	private @Nullable Object handback;

	protected @Nullable Set<Object> mappedObjectNames;


	/** 设置 {@link javax.management.NotificationListener}。 */
	public void setNotificationListener(@Nullable NotificationListener notificationListener) {
		this.notificationListener = notificationListener;
	}

	/** 获取 {@link javax.management.NotificationListener}。 */
	public @Nullable NotificationListener getNotificationListener() {
		return this.notificationListener;
	}

	/**
	 * 设置与封装 {@link #getNotificationFilter() NotificationFilter} 关联的
	 * {@link javax.management.NotificationFilter}。
	 * <p>可为 {@code null}。
	 */
	public void setNotificationFilter(@Nullable NotificationFilter notificationFilter) {
		this.notificationFilter = notificationFilter;
	}

	/**
	 * 返回与封装 {@link #getNotificationListener() NotificationListener} 关联的
	 * {@link javax.management.NotificationFilter}。
	 * <p>可为 {@code null}。
	 */
	public @Nullable NotificationFilter getNotificationFilter() {
		return this.notificationFilter;
	}

	/**
	 * 设置 {@link javax.management.NotificationBroadcaster} 通知
	 * {@link javax.management.NotificationListener} 时原样回传的任意对象。
	 * @param handback handback 对象（可为 {@code null}）
	 * @see javax.management.NotificationListener#handleNotification(javax.management.Notification, Object)
	 */
	public void setHandback(@Nullable Object handback) {
		this.handback = handback;
	}

	/**
	 * 返回通知时原样回传的 handback 对象。
	 * @return handback 对象（可为 {@code null}）
	 * @see javax.management.NotificationListener#handleNotification(javax.management.Notification, Object)
	 */
	public @Nullable Object getHandback() {
		return this.handback;
	}

	/**
	 * 设置封装 NotificationFilter 将注册监听 Notification 的单个 MBean 的 ObjectName 风格名称。
	 * 可为 {@code ObjectName} 或 {@code String}。
	 * @see #setMappedObjectNames
	 */
	public void setMappedObjectName(@Nullable Object mappedObjectName) {
		this.mappedObjectNames = (mappedObjectName != null ?
				new LinkedHashSet<>(Collections.singleton(mappedObjectName)) : null);
	}

	/**
	 * 设置封装 NotificationFilter 将注册监听的多个 MBean 的 ObjectName 风格名称数组。
	 * 可为 {@code ObjectName} 实例或 {@code String}。
	 * @see #setMappedObjectName
	 */
	public void setMappedObjectNames(Object... mappedObjectNames) {
		this.mappedObjectNames = new LinkedHashSet<>(Arrays.asList(mappedObjectNames));
	}

	/**
	 * 返回封装 NotificationFilter 将注册为 Notification 监听器的
	 * {@link javax.management.ObjectName} 字符串表示列表。
	 * @throws MalformedObjectNameException 若 {@code ObjectName} 格式错误
	 */
	public ObjectName @Nullable [] getResolvedObjectNames() throws MalformedObjectNameException {
		if (this.mappedObjectNames == null) {
			return null;
		}
		ObjectName[] resolved = new ObjectName[this.mappedObjectNames.size()];
		int i = 0;
		for (Object objectName : this.mappedObjectNames) {
			resolved[i] = ObjectNameManager.getInstance(objectName);
			i++;
		}
		return resolved;
	}


	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof NotificationListenerHolder that &&
				ObjectUtils.nullSafeEquals(this.notificationListener, that.notificationListener) &&
				ObjectUtils.nullSafeEquals(this.notificationFilter, that.notificationFilter) &&
				ObjectUtils.nullSafeEquals(this.handback, that.handback) &&
				ObjectUtils.nullSafeEquals(this.mappedObjectNames, that.mappedObjectNames)));
	}

	@Override
	public int hashCode() {
		return ObjectUtils.nullSafeHash(this.notificationListener, this.notificationFilter,
				this.handback, this.mappedObjectNames);
	}

}
