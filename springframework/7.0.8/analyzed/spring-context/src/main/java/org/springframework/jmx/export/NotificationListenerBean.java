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

package org.springframework.jmx.export;

import javax.management.NotificationListener;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.support.NotificationListenerHolder;
import org.springframework.util.Assert;

/**
 * 辅助类，聚合 {@link javax.management.NotificationListener}、
 * {@link javax.management.NotificationFilter} 以及任意 handback 对象。
 *
 * <p>还通过 {@link #setMappedObjectNames mappedObjectNames} 属性支持将
 * 封装的 {@link javax.management.NotificationListener} 与任意数量的 MBean 关联，
 * 以便从这些 MBean 接收 {@link javax.management.Notification Notification}。
 *
 * <p>注意：本类也支持将 Spring bean 名称作为
 * {@link #setMappedObjectNames "mappedObjectNames"} 的替代 JMX 对象名称。
 * 注意只有同一 {@link MBeanExporter} 导出的 bean 才支持此类 bean 名称。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 * @see MBeanExporter#setNotificationListeners
 */
public class NotificationListenerBean extends NotificationListenerHolder implements InitializingBean {

	/**
	 * 创建 {@link NotificationListenerBean} 的新实例。
	 */
	public NotificationListenerBean() {
	}

	/**
	 * 创建 {@link NotificationListenerBean} 的新实例。
	 * @param notificationListener 封装的监听器
	 */
	public NotificationListenerBean(NotificationListener notificationListener) {
		Assert.notNull(notificationListener, "NotificationListener must not be null");
		setNotificationListener(notificationListener);
	}


	@Override
	public void afterPropertiesSet() {
		if (getNotificationListener() == null) {
			throw new IllegalArgumentException("Property 'notificationListener' is required");
		}
	}

	void replaceObjectName(Object originalName, Object newName) {
		if (this.mappedObjectNames != null && this.mappedObjectNames.contains(originalName)) {
			this.mappedObjectNames.remove(originalName);
			this.mappedObjectNames.add(newName);
		}
	}

}
