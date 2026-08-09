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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.parsing.EmptyReaderEventListener;
import org.springframework.beans.factory.parsing.FailFastProblemReporter;
import org.springframework.beans.factory.parsing.NullSourceExtractor;
import org.springframework.beans.factory.parsing.ProblemReporter;
import org.springframework.beans.factory.parsing.ReaderEventListener;
import org.springframework.beans.factory.parsing.SourceExtractor;
import org.springframework.beans.factory.support.AbstractBeanDefinitionReader;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.NamedThreadLocal;
import org.springframework.core.io.DescriptiveResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.util.Assert;
import org.springframework.util.xml.SimpleSaxErrorHandler;
import org.springframework.util.xml.XmlValidationModeDetector;

/**
 * XML Bean 定义的 Bean 定义读取器。
 * 将实际的 XML 文档读取委托给 {@link BeanDefinitionDocumentReader} 接口的实现。
 *
 * <p>通常应用于
 * {@link org.springframework.beans.factory.support.DefaultListableBeanFactory}
 * 或 {@link org.springframework.context.support.GenericApplicationContext}。
 *
 * <p>本类加载 DOM 文档并应用 BeanDefinitionDocumentReader。
 * 文档读取器将每个 Bean 定义注册到给定 Bean 工厂，
 * 与后者的
 * {@link org.springframework.beans.factory.support.BeanDefinitionRegistry} 接口实现交互。
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Chris Beams
 * @author Sam Brannen
 * @since 26.11.2003
 * @see #setDocumentReaderClass
 * @see BeanDefinitionDocumentReader
 * @see DefaultBeanDefinitionDocumentReader
 * @see BeanDefinitionRegistry
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory
 * @see org.springframework.context.support.GenericApplicationContext
 */
public class XmlBeanDefinitionReader extends AbstractBeanDefinitionReader {

	/**
	 * 表示应禁用验证。
	 */
	public static final int VALIDATION_NONE = XmlValidationModeDetector.VALIDATION_NONE;

	/**
	 * 表示应自动检测验证模式。
	 */
	public static final int VALIDATION_AUTO = XmlValidationModeDetector.VALIDATION_AUTO;

	/**
	 * 表示应使用 DTD 验证。
	 */
	public static final int VALIDATION_DTD = XmlValidationModeDetector.VALIDATION_DTD;

	/**
	 * 表示应使用 XSD 验证。
	 */
	public static final int VALIDATION_XSD = XmlValidationModeDetector.VALIDATION_XSD;


	/**
	 * 本类定义的验证常量名到常量值的映射。
	 */
	private static final Map<String, Integer> constants = Map.of(
			"VALIDATION_NONE", VALIDATION_NONE,
			"VALIDATION_AUTO", VALIDATION_AUTO,
			"VALIDATION_DTD", VALIDATION_DTD,
			"VALIDATION_XSD", VALIDATION_XSD
		);

	/** XML 验证模式。 */
	private int validationMode = VALIDATION_AUTO;

	/** XML 解析器是否启用命名空间感知。 */
	private boolean namespaceAware = false;

	/** Bean 定义文档读取器实现类。 */
	private Class<? extends BeanDefinitionDocumentReader> documentReaderClass =
			DefaultBeanDefinitionDocumentReader.class;

	/** 问题报告器。 */
	private ProblemReporter problemReporter = new FailFastProblemReporter();

	/** 读取事件监听器。 */
	private ReaderEventListener eventListener = new EmptyReaderEventListener();

	/** 源提取器。 */
	private SourceExtractor sourceExtractor = new NullSourceExtractor();

	/** 命名空间处理器解析器。 */
	private @Nullable NamespaceHandlerResolver namespaceHandlerResolver;

	/** XML 文档加载器。 */
	private DocumentLoader documentLoader = new DefaultDocumentLoader();

	/** SAX 实体解析器。 */
	private @Nullable EntityResolver entityResolver;

	/** SAX 错误处理器。 */
	private ErrorHandler errorHandler = new SimpleSaxErrorHandler(logger);

	/** XML 验证模式检测器。 */
	private final XmlValidationModeDetector validationModeDetector = new XmlValidationModeDetector();

	/** 当前正在加载的 XML Bean 定义资源（线程局部变量）。 */
	private final ThreadLocal<Set<EncodedResource>> resourcesCurrentlyBeingLoaded = NamedThreadLocal.withInitial(
			"XML bean definition resources currently being loaded", () -> new HashSet<>(4));


	/**
	 * 为给定 Bean 工厂创建新的 XmlBeanDefinitionReader。
	 * @param registry 用于加载 Bean 定义的 BeanFactory，以 BeanDefinitionRegistry 形式提供
	 */
	public XmlBeanDefinitionReader(BeanDefinitionRegistry registry) {
		super(registry);
	}


	/**
	 * 设置是否使用 XML 验证。默认为 {@code true}。
	 * <p>若关闭验证，本方法会启用命名空间感知，
	 * 以便在此场景下仍能正确处理 schema 命名空间。
	 * @see #setValidationMode
	 * @see #setNamespaceAware
	 */
	public void setValidating(boolean validating) {
		this.validationMode = (validating ? VALIDATION_AUTO : VALIDATION_NONE);
		this.namespaceAware = !validating;
	}

	/**
	 * 按名称设置要使用的验证模式。默认为 {@link #VALIDATION_AUTO}。
	 * @see #setValidationMode
	 */
	public void setValidationModeName(String validationModeName) {
		Assert.hasText(validationModeName, "'validationModeName' must not be null or blank");
		Integer validationMode = constants.get(validationModeName);
		Assert.notNull(validationMode, "Only validation mode constants allowed");
		this.validationMode = validationMode;
	}

	/**
	 * 设置要使用的验证模式。默认为 {@link #VALIDATION_AUTO}。
	 * <p>注意，这仅激活或停用验证本身。
	 * 若对 schema 文件关闭验证，可能需要显式启用 schema 命名空间支持：
	 * 参见 {@link #setNamespaceAware}。
	 */
	public void setValidationMode(int validationMode) {
		Assert.isTrue(constants.containsValue(validationMode),
				"Only values of validation mode constants allowed");
		this.validationMode = validationMode;
	}

	/**
	 * 返回要使用的验证模式。
	 */
	public int getValidationMode() {
		return this.validationMode;
	}

	/**
	 * 设置 XML 解析器是否应感知 XML 命名空间。
	 * 默认为 "false"。
	 * <p>启用 schema 验证时通常不需要。
	 * 但在无验证时，需切换为 "true" 以正确处理 schema 命名空间。
	 */
	public void setNamespaceAware(boolean namespaceAware) {
		this.namespaceAware = namespaceAware;
	}

	/**
	 * 返回 XML 解析器是否应感知 XML 命名空间。
	 */
	public boolean isNamespaceAware() {
		return this.namespaceAware;
	}

	/**
	 * 指定要使用的 {@link org.springframework.beans.factory.parsing.ProblemReporter}。
	 * <p>默认实现为 {@link org.springframework.beans.factory.parsing.FailFastProblemReporter}，
	 * 采用快速失败行为。外部工具可提供替代实现，在工具 UI 中汇总错误和警告。
	 */
	public void setProblemReporter(@Nullable ProblemReporter problemReporter) {
		this.problemReporter = (problemReporter != null ? problemReporter : new FailFastProblemReporter());
	}

	/**
	 * 指定要使用的 {@link ReaderEventListener}。
	 * <p>默认实现为 EmptyReaderEventListener，丢弃所有事件通知。
	 * 外部工具可提供替代实现以监控注册到 BeanFactory 的组件。
	 */
	public void setEventListener(@Nullable ReaderEventListener eventListener) {
		this.eventListener = (eventListener != null ? eventListener : new EmptyReaderEventListener());
	}

	/**
	 * 指定要使用的 {@link SourceExtractor}。
	 * <p>默认实现为 {@link NullSourceExtractor}，简单返回 {@code null} 作为源对象。
	 * 这意味着在正常运行时执行期间，不会向 Bean 配置元数据附加额外源元数据。
	 */
	public void setSourceExtractor(@Nullable SourceExtractor sourceExtractor) {
		this.sourceExtractor = (sourceExtractor != null ? sourceExtractor : new NullSourceExtractor());
	}

	/**
	 * 指定要使用的 {@link NamespaceHandlerResolver}。
	 * <p>若未指定，将通过 {@link #createDefaultNamespaceHandlerResolver()} 创建默认实例。
	 */
	public void setNamespaceHandlerResolver(@Nullable NamespaceHandlerResolver namespaceHandlerResolver) {
		this.namespaceHandlerResolver = namespaceHandlerResolver;
	}

	/**
	 * 指定要使用的 {@link DocumentLoader}。
	 * <p>默认实现为 {@link DefaultDocumentLoader}，使用 JAXP 加载 {@link Document} 实例。
	 */
	public void setDocumentLoader(@Nullable DocumentLoader documentLoader) {
		this.documentLoader = (documentLoader != null ? documentLoader : new DefaultDocumentLoader());
	}

	/**
	 * 设置用于解析的 SAX 实体解析器。
	 * <p>默认使用 {@link ResourceEntityResolver}。可覆盖以进行自定义实体解析，
	 * 例如相对于特定基路径。
	 */
	public void setEntityResolver(@Nullable EntityResolver entityResolver) {
		this.entityResolver = entityResolver;
	}

	/**
	 * 返回要使用的 EntityResolver，若未指定则构建默认解析器。
	 */
	protected EntityResolver getEntityResolver() {
		if (this.entityResolver == null) {
			// 确定要使用的默认 EntityResolver
			ResourceLoader resourceLoader = getResourceLoader();
			if (resourceLoader != null) {
				this.entityResolver = new ResourceEntityResolver(resourceLoader);
			}
			else {
				this.entityResolver = new DelegatingEntityResolver(getBeanClassLoader());
			}
		}
		return this.entityResolver;
	}

	/**
	 * 设置 {@code org.xml.sax.ErrorHandler} 接口实现，用于自定义处理 XML 解析错误和警告。
	 * <p>若未设置，使用默认 SimpleSaxErrorHandler，仅以视图类的 logger 记录警告，
	 * 并重新抛出错误以中止 XML 转换。
	 * @see SimpleSaxErrorHandler
	 */
	public void setErrorHandler(ErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}

	/**
	 * 指定用于实际读取 XML Bean 定义文档的 {@link BeanDefinitionDocumentReader} 实现。
	 * <p>默认为 {@link DefaultBeanDefinitionDocumentReader}。
	 * @param documentReaderClass 所需的 BeanDefinitionDocumentReader 实现类
	 */
	public void setDocumentReaderClass(Class<? extends BeanDefinitionDocumentReader> documentReaderClass) {
		this.documentReaderClass = documentReaderClass;
	}


	/**
	 * 从指定 XML 文件加载 Bean 定义。
	 * @param resource XML 文件的资源描述符
	 * @return 找到的 Bean 定义数量
	 * @throws BeanDefinitionStoreException 加载或解析出错时
	 */
	@Override
	public int loadBeanDefinitions(Resource resource) throws BeanDefinitionStoreException {
		return loadBeanDefinitions(new EncodedResource(resource));
	}

	/**
	 * 从指定 XML 文件加载 Bean 定义。
	 * @param encodedResource XML 文件的资源描述符，可指定解析编码
	 * @return 找到的 Bean 定义数量
	 * @throws BeanDefinitionStoreException 加载或解析出错时
	 */
	public int loadBeanDefinitions(EncodedResource encodedResource) throws BeanDefinitionStoreException {
		Assert.notNull(encodedResource, "EncodedResource must not be null");
		if (logger.isTraceEnabled()) {
			logger.trace("Loading XML bean definitions from " + encodedResource);
		}

		Set<EncodedResource> currentResources = this.resourcesCurrentlyBeingLoaded.get();

		// 检测循环加载（如 import 定义错误）
		if (!currentResources.add(encodedResource)) {
			throw new BeanDefinitionStoreException(
					"Detected cyclic loading of " + encodedResource + " - check your import definitions!");
		}

		try (InputStream inputStream = encodedResource.getResource().getInputStream()) {
			InputSource inputSource = new InputSource(inputStream);
			if (encodedResource.getEncoding() != null) {
				inputSource.setEncoding(encodedResource.getEncoding());
			}
			return doLoadBeanDefinitions(inputSource, encodedResource.getResource());
		}
		catch (IOException ex) {
			throw new BeanDefinitionStoreException(
					"IOException parsing XML document from " + encodedResource.getResource(), ex);
		}
		finally {
			currentResources.remove(encodedResource);
			if (currentResources.isEmpty()) {
				this.resourcesCurrentlyBeingLoaded.remove();
			}
		}
	}

	/**
	 * 从指定 XML 文件加载 Bean 定义。
	 * @param inputSource 待读取的 SAX InputSource
	 * @return 找到的 Bean 定义数量
	 * @throws BeanDefinitionStoreException 加载或解析出错时
	 */
	public int loadBeanDefinitions(InputSource inputSource) throws BeanDefinitionStoreException {
		return loadBeanDefinitions(inputSource, "resource loaded through SAX InputSource");
	}

	/**
	 * 从指定 XML 文件加载 Bean 定义。
	 * @param inputSource 待读取的 SAX InputSource
	 * @param resourceDescription 资源描述（可为 {@code null} 或空）
	 * @return 找到的 Bean 定义数量
	 * @throws BeanDefinitionStoreException 加载或解析出错时
	 */
	public int loadBeanDefinitions(InputSource inputSource, @Nullable String resourceDescription)
			throws BeanDefinitionStoreException {

		return doLoadBeanDefinitions(inputSource, new DescriptiveResource(resourceDescription));
	}


	/**
	 * 实际从指定 XML 文件加载 Bean 定义。
	 * @param inputSource 待读取的 SAX InputSource
	 * @param resource XML 文件的资源描述符
	 * @return 找到的 Bean 定义数量
	 * @throws BeanDefinitionStoreException 加载或解析出错时
	 * @see #doLoadDocument
	 * @see #registerBeanDefinitions
	 */
	protected int doLoadBeanDefinitions(InputSource inputSource, Resource resource)
			throws BeanDefinitionStoreException {

		try {
			Document doc = doLoadDocument(inputSource, resource);
			int count = registerBeanDefinitions(doc, resource);
			if (logger.isDebugEnabled()) {
				logger.debug("Loaded " + count + " bean definitions from " + resource);
			}
			return count;
		}
		catch (BeanDefinitionStoreException ex) {
			throw ex;
		}
		catch (SAXParseException ex) {
			throw new XmlBeanDefinitionStoreException(resource.getDescription(),
					"Line " + ex.getLineNumber() + " in XML document from " + resource + " is invalid", ex);
		}
		catch (SAXException ex) {
			throw new XmlBeanDefinitionStoreException(resource.getDescription(),
					"XML document from " + resource + " is invalid", ex);
		}
		catch (ParserConfigurationException ex) {
			throw new BeanDefinitionStoreException(resource.getDescription(),
					"Parser configuration exception parsing XML from " + resource, ex);
		}
		catch (IOException ex) {
			throw new BeanDefinitionStoreException(resource.getDescription(),
					"IOException parsing XML document from " + resource, ex);
		}
		catch (Throwable ex) {
			throw new BeanDefinitionStoreException(resource.getDescription(),
					"Unexpected exception parsing XML document from " + resource, ex);
		}
	}

	/**
	 * 使用配置的 DocumentLoader 实际加载指定文档。
	 * @param inputSource 待读取的 SAX InputSource
	 * @param resource XML 文件的资源描述符
	 * @return DOM Document
	 * @throws Exception DocumentLoader 抛出时
	 * @see #setDocumentLoader
	 * @see DocumentLoader#loadDocument
	 */
	protected Document doLoadDocument(InputSource inputSource, Resource resource) throws Exception {
		return this.documentLoader.loadDocument(inputSource, getEntityResolver(), this.errorHandler,
				getValidationModeForResource(resource), isNamespaceAware());
	}

	/**
	 * 确定指定 {@link Resource} 的验证模式。
	 * 若未配置显式验证模式，则从给定资源 {@link #detectValidationMode 检测}验证模式。
	 * <p>若需完全控制验证模式（即使设置的不是 {@link #VALIDATION_AUTO}），可覆盖此方法。
	 * @see #detectValidationMode
	 */
	protected int getValidationModeForResource(Resource resource) {
		int validationModeToUse = getValidationMode();
		if (validationModeToUse != VALIDATION_AUTO) {
			return validationModeToUse;
		}
		int detectedMode = detectValidationMode(resource);
		if (detectedMode != VALIDATION_AUTO) {
			return detectedMode;
		}
		// 未获得明确指示，假定 XSD：
		// 检测停止前（找到文档根标签之前）显然未发现 DTD 声明
		return VALIDATION_XSD;
	}

	/**
	 * 检测应对所供 {@link Resource} 标识的 XML 文件执行何种验证。
	 * 若文件有 {@code DOCTYPE} 定义则使用 DTD 验证，否则假定 XSD 验证。
	 * <p>若需自定义 {@link #VALIDATION_AUTO} 模式的解析，可覆盖此方法。
	 */
	protected int detectValidationMode(Resource resource) {
		if (resource.isOpen()) {
			throw new BeanDefinitionStoreException(
					"Passed-in Resource [" + resource + "] contains an open stream: " +
					"cannot determine validation mode automatically. Either pass in a Resource " +
					"that is able to create fresh streams, or explicitly specify the validationMode " +
					"on your XmlBeanDefinitionReader instance.");
		}

		InputStream inputStream;
		try {
			inputStream = resource.getInputStream();
		}
		catch (IOException ex) {
			throw new BeanDefinitionStoreException(
					"Unable to determine validation mode for [" + resource + "]: cannot open InputStream. " +
					"Did you attempt to load directly from a SAX InputSource without specifying the " +
					"validationMode on your XmlBeanDefinitionReader instance?", ex);
		}

		try {
			return this.validationModeDetector.detectValidationMode(inputStream);
		}
		catch (IOException ex) {
			throw new BeanDefinitionStoreException("Unable to determine validation mode for [" +
					resource + "]: an error occurred whilst reading from the InputStream.", ex);
		}
	}

	/**
	 * 注册给定 DOM 文档中包含的 Bean 定义。
	 * 由 {@code loadBeanDefinitions} 调用。
	 * <p>创建解析器类的新实例并调用其 {@code registerBeanDefinitions}。
	 * @param doc DOM 文档
	 * @param resource 资源描述符（用于上下文信息）
	 * @return 找到的 Bean 定义数量
	 * @throws BeanDefinitionStoreException 解析出错时
	 * @see #loadBeanDefinitions
	 * @see #setDocumentReaderClass
	 * @see BeanDefinitionDocumentReader#registerBeanDefinitions
	 */
	public int registerBeanDefinitions(Document doc, Resource resource) throws BeanDefinitionStoreException {
		BeanDefinitionDocumentReader documentReader = createBeanDefinitionDocumentReader();
		int countBefore = getRegistry().getBeanDefinitionCount();
		documentReader.registerBeanDefinitions(doc, createReaderContext(resource));
		return getRegistry().getBeanDefinitionCount() - countBefore;
	}

	/**
	 * 创建用于从 XML 文档实际读取 Bean 定义的 {@link BeanDefinitionDocumentReader}。
	 * <p>默认实现实例化指定的 "documentReaderClass"。
	 * @see #setDocumentReaderClass
	 */
	protected BeanDefinitionDocumentReader createBeanDefinitionDocumentReader() {
		return BeanUtils.instantiateClass(this.documentReaderClass);
	}

	/**
	 * 创建传递给文档读取器的 {@link XmlReaderContext}。
	 */
	public XmlReaderContext createReaderContext(Resource resource) {
		return new XmlReaderContext(resource, this.problemReporter, this.eventListener,
				this.sourceExtractor, this, getNamespaceHandlerResolver());
	}

	/**
	 * 懒加载创建默认 NamespaceHandlerResolver（若此前未设置）。
	 * @see #createDefaultNamespaceHandlerResolver()
	 */
	public NamespaceHandlerResolver getNamespaceHandlerResolver() {
		if (this.namespaceHandlerResolver == null) {
			this.namespaceHandlerResolver = createDefaultNamespaceHandlerResolver();
		}
		return this.namespaceHandlerResolver;
	}

	/**
	 * 创建默认的 {@link NamespaceHandlerResolver} 实现（未指定时使用）。
	 * <p>默认实现返回 {@link DefaultNamespaceHandlerResolver} 实例。
	 * @see DefaultNamespaceHandlerResolver#DefaultNamespaceHandlerResolver(ClassLoader)
	 */
	protected NamespaceHandlerResolver createDefaultNamespaceHandlerResolver() {
		ResourceLoader resourceLoader = getResourceLoader();
		ClassLoader cl = (resourceLoader != null ? resourceLoader.getClassLoader() : getBeanClassLoader());
		return new DefaultNamespaceHandlerResolver(cl);
	}

}
