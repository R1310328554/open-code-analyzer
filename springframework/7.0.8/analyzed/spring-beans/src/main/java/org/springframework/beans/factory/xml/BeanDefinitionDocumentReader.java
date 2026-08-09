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

package org.springframework.beans.factory.xml;

import org.w3c.dom.Document;

import org.springframework.beans.factory.BeanDefinitionStoreException;

/**
 * 解析包含 Spring Bean 定义的 XML 文档的 SPI。
 * 由 {@link XmlBeanDefinitionReader} 用于实际解析 DOM 文档。
 *
 * <p>每个文档创建一个实例：实现可在 {@code registerBeanDefinitions} 执行期间
 * 在实例字段中保存状态，例如文档内所有 Bean 定义共用的全局设置。
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 18.12.2003
 * @see XmlBeanDefinitionReader#setDocumentReaderClass
 */
public interface BeanDefinitionDocumentReader {

	/**
	 * 从给定 DOM 文档读取 Bean 定义，并注册到 readerContext 中的注册表。
	 * @param doc DOM 文档
	 * @param readerContext 当前读取器上下文（含目标注册表及正在解析的资源）
	 * @throws BeanDefinitionStoreException 解析出错时
	 */
	void registerBeanDefinitions(Document doc, XmlReaderContext readerContext)
			throws BeanDefinitionStoreException;

}
