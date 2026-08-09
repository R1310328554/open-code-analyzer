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

package org.springframework.jmx.export.assembler;

import javax.management.JMException;
import javax.management.modelmbean.ModelMBeanInfo;

/**
 * 所有可为受管资源创建管理接口元数据的类均需实现的接口。
 *
 * <p>供 {@code MBeanExporter} 为任意非 MBean 类型的 Bean 生成管理接口。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 1.2
 * @see org.springframework.jmx.export.MBeanExporter
 */
public interface MBeanInfoAssembler {

	/**
	 * 为给定受管资源创建 {@code ModelMBeanInfo}。
	 * @param managedBean 待暴露的 Bean（可能是 AOP 代理）
	 * @param beanKey 与该受管 Bean 关联的键
	 * @return ModelMBeanInfo 元数据对象
	 * @throws JMException 发生错误时
	 */
	ModelMBeanInfo getMBeanInfo(Object managedBean, String beanKey) throws JMException;

}
