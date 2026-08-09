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
 * 定义应用运行期间供应用开发者访问的 MBean 导出操作集合的接口。
 *
 * <p>该接口用于借助 Spring 的管理接口生成能力（以及可选的 {@link ObjectName}
 * 生成能力）将应用资源导出到 JMX。
 *
 * @author Rob Harrop
 * @since 2.0
 * @see MBeanExporter
 */
public interface MBeanExportOperations {

	/**
	 * 将提供的资源注册到 JMX。若资源尚非有效 MBean，Spring 会为其生成管理接口；
	 * 具体生成的接口取决于实现及其配置。本调用还会为受管资源生成 {@link ObjectName}
	 * 并返回给调用方。
	 * @param managedResource 要通过 JMX 暴露的资源
	 * @return 资源被暴露时使用的 {@link ObjectName}
	 * @throws MBeanExportException 若 Spring 无法生成 {@link ObjectName} 或注册 MBean
	 */
	ObjectName registerManagedResource(Object managedResource) throws MBeanExportException;

	/**
	 * 将提供的资源注册到 JMX。若资源尚非有效 MBean，Spring 会为其生成管理接口；
	 * 具体生成的接口取决于实现及其配置。
	 * @param managedResource 要通过 JMX 暴露的资源
	 * @param objectName 用于暴露资源的 {@link ObjectName}
	 * @throws MBeanExportException 若 Spring 无法注册 MBean
	 */
	void registerManagedResource(Object managedResource, ObjectName objectName) throws MBeanExportException;

	/**
	 * 从底层 MBeanServer 注册表中移除指定的 MBean。
	 * @param objectName 要移除资源的 {@link ObjectName}
	 */
	void unregisterManagedResource(ObjectName objectName);

}
