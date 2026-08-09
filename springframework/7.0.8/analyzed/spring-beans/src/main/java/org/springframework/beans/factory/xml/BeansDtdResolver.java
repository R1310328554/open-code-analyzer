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

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Spring beans DTD 的 {@link EntityResolver} 实现，从 Spring 类路径（或 JAR）加载 DTD。
 *
 * <p>无论 systemId 是含 {@code spring-beans} 的本地 URL，还是
 * {@code https://www.springframework.org/dtd/spring-beans-2.0.dtd}，
 * 均从类路径资源 {@code /org/springframework/beans/factory/xml/spring-beans.dtd}
 * 加载 {@code spring-beans.dtd}。
 *
 * @author Juergen Hoeller
 * @author Colin Sampaleanu
 * @since 04.06.2003
 * @see ResourceEntityResolver
 */
public class BeansDtdResolver implements EntityResolver {

	/** DTD 文件扩展名。 */
	private static final String DTD_EXTENSION = ".dtd";

	/** Spring beans DTD 名称片段。 */
	private static final String DTD_NAME = "spring-beans";

	private static final Log logger = LogFactory.getLog(BeansDtdResolver.class);


	@Override
	public @Nullable InputSource resolveEntity(@Nullable String publicId, @Nullable String systemId) throws IOException {
		if (logger.isTraceEnabled()) {
			logger.trace("Trying to resolve XML entity with public ID [" + publicId +
					"] and system ID [" + systemId + "]");
		}

		if (systemId != null && systemId.endsWith(DTD_EXTENSION)) {
			int lastPathSeparator = systemId.lastIndexOf('/');
			int dtdNameStart = systemId.indexOf(DTD_NAME, lastPathSeparator);
			if (dtdNameStart != -1) {
				String dtdFile = DTD_NAME + DTD_EXTENSION;
				if (logger.isTraceEnabled()) {
					logger.trace("Trying to locate [" + dtdFile + "] in Spring jar on classpath");
				}
				try {
					Resource resource = new ClassPathResource(dtdFile, getClass());
					InputSource source = new InputSource(resource.getInputStream());
					source.setPublicId(publicId);
					source.setSystemId(systemId);
					if (logger.isTraceEnabled()) {
						logger.trace("Found beans DTD [" + systemId + "] in classpath: " + dtdFile);
					}
					return source;
				}
				catch (FileNotFoundException ex) {
					if (logger.isDebugEnabled()) {
						logger.debug("Could not resolve beans DTD [" + systemId + "]: not found in classpath", ex);
					}
				}
			}
		}

		// 回退到解析器默认行为
		return null;
	}


	@Override
	public String toString() {
		return "EntityResolver for spring-beans DTD";
	}

}
