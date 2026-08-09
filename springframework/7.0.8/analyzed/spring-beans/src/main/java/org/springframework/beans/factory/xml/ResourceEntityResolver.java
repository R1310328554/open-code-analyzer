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

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ResourceUtils;

/**
 * {@code EntityResolver} 实现，在适用时尝试通过 {@link org.springframework.core.io.ResourceLoader}
 *（通常相对于 {@code ApplicationContext} 的资源基路径）解析实体引用。
 * 继承 {@link DelegatingEntityResolver} 以同时提供 DTD 与 XSD 查找。
 *
 * <p>允许使用标准 XML 实体将 XML 片段包含到应用上下文定义中，例如将大型 XML 文件
 * 拆分为多个模块。include 路径可按惯例相对于应用上下文的资源基路径，
 * 而非相对于 JVM 工作目录（XML 解析器的默认行为）。
 *
 * <p>注意：除相对路径外，指定当前系统根（即 JVM 工作目录）中文件的每个 URL，
 * 也将按应用上下文进行相对解析。
 *
 * @author Juergen Hoeller
 * @since 31.07.2003
 * @see org.springframework.core.io.ResourceLoader
 * @see org.springframework.context.ApplicationContext
 */
public class ResourceEntityResolver extends DelegatingEntityResolver {

	private static final Log logger = LogFactory.getLog(ResourceEntityResolver.class);

	private final ResourceLoader resourceLoader;


	/**
	 * 为指定的 ResourceLoader（通常为 ApplicationContext）创建 ResourceEntityResolver。
	 * @param resourceLoader 用于加载 XML 实体 include 的 ResourceLoader（或 ApplicationContext）
	 */
	public ResourceEntityResolver(ResourceLoader resourceLoader) {
		super(resourceLoader.getClassLoader());
		this.resourceLoader = resourceLoader;
	}


	@Override
	public @Nullable InputSource resolveEntity(@Nullable String publicId, @Nullable String systemId)
			throws SAXException, IOException {

		InputSource source = super.resolveEntity(publicId, systemId);

		if (source == null && systemId != null) {
			String resourcePath = null;
			try {
				String decodedSystemId = URLDecoder.decode(systemId, StandardCharsets.UTF_8);
				String givenUrl = ResourceUtils.toURL(decodedSystemId).toString();
				String systemRootUrl = new File("").toURI().toURL().toString();
				// 若当前位于系统根目录，则尝试相对于资源基路径解析
				if (givenUrl.startsWith(systemRootUrl)) {
					resourcePath = givenUrl.substring(systemRootUrl.length());
				}
			}
			catch (Exception ex) {
				// 通常为 MalformedURLException 或 AccessControlException
				if (logger.isDebugEnabled()) {
					logger.debug("Could not resolve XML entity [" + systemId + "] against system root URL", ex);
				}
				// 无 URL（或无法解析的 URL）→ 尝试相对于资源基路径
				resourcePath = systemId;
			}
			if (resourcePath != null) {
				if (logger.isTraceEnabled()) {
					logger.trace("Trying to locate XML entity [" + systemId + "] as resource [" + resourcePath + "]");
				}
				Resource resource = this.resourceLoader.getResource(resourcePath);
				source = new InputSource(resource.getInputStream());
				source.setPublicId(publicId);
				source.setSystemId(systemId);
				if (logger.isDebugEnabled()) {
					logger.debug("Found XML entity [" + systemId + "]: " + resource);
				}
			}
			else if (systemId.endsWith(DTD_SUFFIX) || systemId.endsWith(XSD_SUFFIX)) {
				source = resolveSchemaEntity(publicId, systemId);
			}
		}

		return source;
	}

	/**
	 * {@link #resolveEntity(String, String)} 的回退方法，在无法将 "schema" 实体（DTD 或 XSD）
	 * 解析为本地资源时使用。默认行为是通过 HTTPS 执行远程解析。
	 * <p>子类可覆盖此方法以更改默认行为。
	 * <ul>
	 * <li>返回 {@code null} 以回退到解析器的
	 * {@linkplain org.xml.sax.EntityResolver#resolveEntity(String, String) 默认行为}。</li>
	 * <li>抛出异常以阻止 DTD 或 XSD 的远程解析。</li>
	 * </ul>
	 * @param publicId 所引用外部实体的公共标识符，若无则传入 null
	 * @param systemId 所引用外部实体的系统标识符，表示 DTD 或 XSD 的 URL
	 * @return 描述新输入源的 InputSource 对象，或返回 null 以请求解析器打开指向系统标识符的常规 URI 连接
	 * @since 6.0.4
	 */
	protected @Nullable InputSource resolveSchemaEntity(@Nullable String publicId, String systemId) {
		InputSource source;
		// 即使规范声明为 http，也通过 https 进行外部 dtd/xsd 查找
		String url = systemId;
		if (url.startsWith("http:")) {
			url = "https:" + url.substring(5);
		}
		if (logger.isWarnEnabled()) {
			logger.warn("DTD/XSD XML entity [" + systemId + "] not found, falling back to remote https resolution");
		}
		try {
			source = new InputSource(ResourceUtils.toURL(url).openStream());
			source.setPublicId(publicId);
			source.setSystemId(systemId);
		}
		catch (IOException ex) {
			if (logger.isDebugEnabled()) {
				logger.debug("Could not resolve XML entity [" + systemId + "] through URL [" + url + "]", ex);
			}
			// 回退到解析器默认行为
			source = null;
		}
		return source;
	}

}
