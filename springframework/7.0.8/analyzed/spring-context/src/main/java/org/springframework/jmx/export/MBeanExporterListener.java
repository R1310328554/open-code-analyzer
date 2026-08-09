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

import javax.management.ObjectName;

/**
 * 监听器，使应用代码能在通过 {@link MBeanExporter} 注册或注销 MBean 时收到通知。
 *
 * @author Rob Harrop
 * @since 1.2.2
 * @see org.springframework.jmx.export.MBeanExporter#setListeners
 */
public interface MBeanExporterListener {

	/**
	 * 在 MBean 已<i>成功</i>注册到 {@link javax.management.MBeanServer} 后，
	 * 由 {@link MBeanExporter} 调用。
	 * @param objectName 已注册 MBean 的 {@code ObjectName}
	 */
	void mbeanRegistered(ObjectName objectName);

	/**
	 * 在 MBean 已从 {@link javax.management.MBeanServer} <i>成功</i>注销后，
	 * 由 {@link MBeanExporter} 调用。
	 * @param objectName 已注销 MBean 的 {@code ObjectName}
	 */
	void mbeanUnregistered(ObjectName objectName);

}
