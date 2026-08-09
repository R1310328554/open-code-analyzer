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

import javax.management.Notification;

/**
 * 简单接口，使 Spring 管理的 MBean 能够发布 JMX 通知，
 * 而无需了解通知如何传输到 {@link javax.management.MBeanServer}。
 *
 * <p>受管资源可通过实现 {@link NotificationPublisherAware} 接口访问 {@code NotificationPublisher}。
 * 当特定受管资源实例注册到 {@link javax.management.MBeanServer} 后，
 * 若该资源实现了 {@link NotificationPublisherAware} 接口，Spring 将向其注入
 * {@code NotificationPublisher} 实例。
 *
 * <p>每个受管资源实例拥有独立的 {@code NotificationPublisher} 实现实例，
 * 该实例跟踪为该受管资源注册的全部
 * {@link javax.management.NotificationListener NotificationListeners}。
 *
 * <p>现有用户自定义 MBean 应使用标准 JMX API 发布通知；
 * 本接口仅供 Spring 创建的 MBean 使用。
 *
 * @author Rob Harrop
 * @since 2.0
 * @see NotificationPublisherAware
 * @see org.springframework.jmx.export.MBeanExporter
 */
@FunctionalInterface
public interface NotificationPublisher {

	/**
	 * 将指定 {@link javax.management.Notification} 发送给所有已注册的
	 * {@link javax.management.NotificationListener NotificationListeners}。
	 * 受管资源<strong>不</strong>负责管理已注册
	 * {@link javax.management.NotificationListener NotificationListeners} 的列表；
	 * 该工作由框架自动完成。
	 * @param notification 要发送的 JMX 通知
	 * @throws UnableToSendNotificationException 发送失败时
	 */
	void sendNotification(Notification notification) throws UnableToSendNotificationException;

}
