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
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;

/**
 * 加载 XML {@link Document} 的策略接口。
 *
 * @author Rob Harrop
 * @since 2.0
 * @see DefaultDocumentLoader
 */
public interface DocumentLoader {

	/**
	 * 从提供的 {@link InputSource source} 加载 {@link Document document}。
	 * @param inputSource 待加载文档的来源
	 * @param entityResolver 用于解析任意实体的解析器
	 * @param errorHandler 文档加载过程中报告错误的处理器
	 * @param validationMode 校验类型：{@link org.springframework.util.xml.XmlValidationModeDetector#VALIDATION_DTD DTD}
	 * 或 {@link org.springframework.util.xml.XmlValidationModeDetector#VALIDATION_XSD XSD}
	 * @param namespaceAware 若为 {@code true} 则提供 XML 命名空间支持
	 * @return 已加载的 {@link Document document}
	 * @throws Exception 发生错误时
	 */
	Document loadDocument(
			InputSource inputSource, EntityResolver entityResolver,
			ErrorHandler errorHandler, int validationMode, boolean namespaceAware)
			throws Exception;

}
