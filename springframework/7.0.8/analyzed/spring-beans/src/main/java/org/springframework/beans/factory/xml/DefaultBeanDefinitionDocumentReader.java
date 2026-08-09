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
import java.net.URISyntaxException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

/**
 * {@link BeanDefinitionDocumentReader} 的默认实现，按 "spring-beans" DTD/XSD 格式
 * （Spring 默认 XML Bean 定义格式）读取 Bean 定义。
 *
 * <p>所需 XML 文档的结构、元素与属性名在本类中硬编码（必要时可先转换格式）。
 * {@code <beans>} 不必是文档根元素：本类会解析文件中所有 Bean 定义元素，与根元素无关。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Erik Wiersma
 * @since 18.12.2003
 */
public class DefaultBeanDefinitionDocumentReader implements BeanDefinitionDocumentReader {

	/** {@code <bean>} 元素本地名。 */
	public static final String BEAN_ELEMENT = BeanDefinitionParserDelegate.BEAN_ELEMENT;

	/** 嵌套 {@code <beans>} 元素名。 */
	public static final String NESTED_BEANS_ELEMENT = "beans";

	/** {@code <alias>} 元素名。 */
	public static final String ALIAS_ELEMENT = "alias";

	/** {@code name} 属性名。 */
	public static final String NAME_ATTRIBUTE = "name";

	/** {@code alias} 属性名。 */
	public static final String ALIAS_ATTRIBUTE = "alias";

	/** {@code <import>} 元素名。 */
	public static final String IMPORT_ELEMENT = "import";

	/** {@code resource} 属性名。 */
	public static final String RESOURCE_ATTRIBUTE = "resource";

	/** {@code profile} 属性名。 */
	public static final String PROFILE_ATTRIBUTE = "profile";


	protected final Log logger = LogFactory.getLog(getClass());

	/** 当前 XML 读取器上下文。 */
	private @Nullable XmlReaderContext readerContext;

	/** 当前 Bean 定义解析委托对象。 */
	private @Nullable BeanDefinitionParserDelegate delegate;


	/**
	 * 按 "spring-beans" XSD（或历史上的 DTD）解析 Bean 定义。
	 * <p>打开 DOM 文档后，先初始化 {@code <beans/>} 级别的默认设置，再解析其中的 Bean 定义。
	 */
	@Override
	public void registerBeanDefinitions(Document doc, XmlReaderContext readerContext) {
		this.readerContext = readerContext;
		doRegisterBeanDefinitions(doc.getDocumentElement());
	}

	/**
	 * 返回本解析器所处理的 XML 资源的读取器上下文。
	 */
	protected final XmlReaderContext getReaderContext() {
		Assert.state(this.readerContext != null, "No XmlReaderContext available");
		return this.readerContext;
	}

	/**
	 * 调用 {@link org.springframework.beans.factory.parsing.SourceExtractor}
	 * 从给定 {@link Element} 提取源元数据。
	 */
	protected @Nullable Object extractSource(Element ele) {
		return getReaderContext().extractSource(ele);
	}


	/**
	 * 注册给定根 {@code <beans/>} 元素内的各 Bean 定义。
	 */
	@SuppressWarnings("deprecation")  // for Environment.acceptsProfiles(String...)
	protected void doRegisterBeanDefinitions(Element root) {
		// 嵌套 <beans> 会递归进入本方法。为正确传播 <beans> 的 default-* 属性，
		// 用父 delegate（可能为 null）创建子 delegate 作为回退，最后恢复父引用（模拟 delegate 栈）
		BeanDefinitionParserDelegate parent = this.delegate;
		BeanDefinitionParserDelegate current = createDelegate(getReaderContext(), root, parent);
		this.delegate = current;

		if (current.isDefaultNamespace(root)) {
			String profileSpec = root.getAttribute(PROFILE_ATTRIBUTE);
			if (StringUtils.hasText(profileSpec)) {
				String[] specifiedProfiles = StringUtils.tokenizeToStringArray(
						profileSpec, BeanDefinitionParserDelegate.MULTI_VALUE_ATTRIBUTE_DELIMITERS);
				// XML 配置不支持 profile 表达式，不能用 Profiles.of(...)
				if (!getReaderContext().getEnvironment().acceptsProfiles(specifiedProfiles)) {
					if (logger.isDebugEnabled()) {
						logger.debug("Skipped XML bean definition file due to specified profiles [" + profileSpec +
								"] not matching: " + getReaderContext().getResource());
					}
					return;
				}
			}
		}

		preProcessXml(root);
		parseBeanDefinitions(root, current);
		postProcessXml(root);

		this.delegate = parent;
	}

	/**
	 * 创建 Bean 定义解析委托，并继承父 delegate 的默认设置。
	 */
	protected BeanDefinitionParserDelegate createDelegate(
			XmlReaderContext readerContext, Element root, @Nullable BeanDefinitionParserDelegate parentDelegate) {

		BeanDefinitionParserDelegate delegate = new BeanDefinitionParserDelegate(readerContext);
		delegate.initDefaults(root, parentDelegate);
		return delegate;
	}

	/**
	 * 解析文档根级别的元素：{@code import}、{@code alias}、{@code bean}。
	 * @param root 文档 DOM 根元素
	 */
	protected void parseBeanDefinitions(Element root, BeanDefinitionParserDelegate delegate) {
		if (delegate.isDefaultNamespace(root)) {
			NodeList nl = root.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				Node node = nl.item(i);
				if (node instanceof Element ele) {
					if (delegate.isDefaultNamespace(ele)) {
						parseDefaultElement(ele, delegate);
					}
					else {
						delegate.parseCustomElement(ele);
					}
				}
			}
		}
		else {
			delegate.parseCustomElement(root);
		}
	}

	/** 按元素类型分发到 import、alias、bean 或嵌套 beans 处理。 */
	private void parseDefaultElement(Element ele, BeanDefinitionParserDelegate delegate) {
		if (delegate.nodeNameEquals(ele, IMPORT_ELEMENT)) {
			importBeanDefinitionResource(ele);
		}
		else if (delegate.nodeNameEquals(ele, ALIAS_ELEMENT)) {
			processAliasRegistration(ele);
		}
		else if (delegate.nodeNameEquals(ele, BEAN_ELEMENT)) {
			processBeanDefinition(ele, delegate);
		}
		else if (delegate.nodeNameEquals(ele, NESTED_BEANS_ELEMENT)) {
			// 递归处理嵌套 <beans>
			doRegisterBeanDefinitions(ele);
		}
	}

	/**
	 * 解析 {@code import} 元素，将给定资源中的 Bean 定义加载到 Bean 工厂。
	 */
	protected void importBeanDefinitionResource(Element ele) {
		String location = ele.getAttribute(RESOURCE_ATTRIBUTE);
		if (!StringUtils.hasText(location)) {
			getReaderContext().error("Resource location must not be empty", ele);
			return;
		}

		// 解析系统属性占位符，例如 "${user.dir}"
		location = getReaderContext().getEnvironment().resolveRequiredPlaceholders(location);

		Set<Resource> actualResources = new LinkedHashSet<>(4);

		// 判断 location 是绝对还是相对 URI
		boolean absoluteLocation = false;
		try {
			absoluteLocation = ResourcePatternUtils.isUrl(location) || ResourceUtils.toURI(location).isAbsolute();
		}
		catch (URISyntaxException ex) {
			// 无法转为 URI 时视为相对路径（classpath*: 前缀除外）
		}

		if (absoluteLocation) {
			try {
				int importCount = getReaderContext().getReader().loadBeanDefinitions(location, actualResources);
				if (logger.isTraceEnabled()) {
					logger.trace("Imported " + importCount + " bean definitions from URL location [" + location + "]");
				}
			}
			catch (BeanDefinitionStoreException ex) {
				getReaderContext().error(
						"Failed to import bean definitions from URL location [" + location + "]", ele, ex);
			}
		}
		else {
			// 非 URL：相对于当前文件解析资源路径
			try {
				int importCount;
				Resource relativeResource = getReaderContext().getResource().createRelative(location);
				if (relativeResource.exists()) {
					importCount = getReaderContext().getReader().loadBeanDefinitions(relativeResource);
					actualResources.add(relativeResource);
				}
				else {
					String baseLocation = getReaderContext().getResource().getURL().toString();
					importCount = getReaderContext().getReader().loadBeanDefinitions(
							StringUtils.applyRelativePath(baseLocation, location), actualResources);
				}
				if (logger.isTraceEnabled()) {
					logger.trace("Imported " + importCount + " bean definitions from relative location [" + location + "]");
				}
			}
			catch (IOException ex) {
				getReaderContext().error("Failed to resolve current resource location", ele, ex);
			}
			catch (BeanDefinitionStoreException ex) {
				getReaderContext().error(
						"Failed to import bean definitions from relative location [" + location + "]", ele, ex);
			}
		}
		Resource[] actResArray = actualResources.toArray(new Resource[0]);
		getReaderContext().fireImportProcessed(location, actResArray, extractSource(ele));
	}

	/**
	 * 处理给定 alias 元素，向注册表注册别名。
	 */
	protected void processAliasRegistration(Element ele) {
		String name = ele.getAttribute(NAME_ATTRIBUTE);
		String alias = ele.getAttribute(ALIAS_ATTRIBUTE);
		boolean valid = true;
		if (!StringUtils.hasText(name)) {
			getReaderContext().error("Name must not be empty", ele);
			valid = false;
		}
		if (!StringUtils.hasText(alias)) {
			getReaderContext().error("Alias must not be empty", ele);
			valid = false;
		}
		if (valid) {
			try {
				getReaderContext().getRegistry().registerAlias(name, alias);
			}
			catch (Exception ex) {
				getReaderContext().error("Failed to register alias '" + alias +
						"' for bean with name '" + name + "'", ele, ex);
			}
			getReaderContext().fireAliasRegistered(name, alias, extractSource(ele));
		}
	}

	/**
	 * 处理给定 bean 元素：解析 Bean 定义并注册到注册表。
	 */
	protected void processBeanDefinition(Element ele, BeanDefinitionParserDelegate delegate) {
		BeanDefinitionHolder bdHolder = delegate.parseBeanDefinitionElement(ele);
		if (bdHolder != null) {
			bdHolder = delegate.decorateBeanDefinitionIfRequired(ele, bdHolder);
			try {
				// 注册最终装饰后的实例
				BeanDefinitionReaderUtils.registerBeanDefinition(bdHolder, getReaderContext().getRegistry());
			}
			catch (BeanDefinitionStoreException ex) {
				getReaderContext().error("Failed to register bean definition with name '" +
						bdHolder.getBeanName() + "'", ele, ex);
			}
			// 触发组件注册事件
			getReaderContext().fireComponentRegistered(new BeanComponentDefinition(bdHolder));
		}
	}


	/**
	 * 在解析 Bean 定义之前处理自定义元素类型的扩展点。
	 * <p>默认无操作。子类可覆盖以将自定义元素转为标准 Spring Bean 定义等。
	 * @see #getReaderContext()
	 */
	protected void preProcessXml(Element root) {
	}

	/**
	 * 在 Bean 定义解析完成之后的扩展点。
	 * <p>默认无操作。子类可覆盖以进行自定义后处理。
	 * @see #getReaderContext()
	 */
	protected void postProcessXml(Element root) {
	}

}
