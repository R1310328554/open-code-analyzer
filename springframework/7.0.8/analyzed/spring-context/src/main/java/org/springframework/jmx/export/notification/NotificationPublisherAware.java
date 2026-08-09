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

package org.springframework.jmx.export.notification;

import org.springframework.beans.factory.Aware;

/**
 * 任何需注册到 {@link javax.management.MBeanServer} 并希望发送
 * JMX {@link javax.management.Notification javax.management.Notifications} 的
 * Spring 管理资源应实现的接口。
 *
 * <p>在 Spring 创建的受管资源注册到 {@link javax.management.MBeanServer} 后，
 * 立即为其提供 {@link NotificationPublisher}。
 *
 * <p><b>注意：</b>该接口仅适用于通过 Spring 的
 * {@link org.springframework.jmx.export.MBeanExporter} 导出的简单 Spring 管理 Bean。
 * 不适用于未导出的 Bean，也不适用于 Spring 导出的标准 MBean。
 * 对于标准 JMX MBean，请考虑实现
 * {@link javax.management.modelmbean.ModelMBeanNotificationBroadcaster} 接口
 * （或实现完整的 {@link javax.management.modelmbean.ModelMBean}）。
 *
 * @author Rob Harrop
 * @author Chris Beams
 * @since 2.0
 * @see NotificationPublisher
 */
public interface NotificationPublisherAware extends Aware {

	/**
	 * 为当前受管资源实例设置 {@link NotificationPublisher} 实例。
	 */
	void setNotificationPublisher(NotificationPublisher notificationPublisher);

}
