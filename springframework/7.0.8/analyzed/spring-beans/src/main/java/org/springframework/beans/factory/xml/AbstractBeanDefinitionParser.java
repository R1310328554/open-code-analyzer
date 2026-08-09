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

import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.util.StringUtils;

/**
 * {@link BeanDefinitionParser} 的抽象实现，提供若干便捷方法及子类必须实现的
 * {@link AbstractBeanDefinitionParser#parseInternal 模板方法}。
 *
 * <p>当需要将任意复杂 XML 解析为一个或多个 {@link BeanDefinition} 时使用本解析器。
 * 若只需将 XML 解析为单个 {@code BeanDefinition}，可考虑更简单的子类
 * {@link AbstractSingleBeanDefinitionParser} 和 {@link AbstractSimpleBeanDefinitionParser}。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Rick Evans
 * @author Dave Syer
 * @since 2.0
 */
public abstract class AbstractBeanDefinitionParser implements BeanDefinitionParser {

	/** {@code id} 属性名常量。 */
	public static final String ID_ATTRIBUTE = "id";

	/** {@code name} 属性名常量。 */
	public static final String NAME_ATTRIBUTE = "name";


	@Override
	@SuppressWarnings("NullAway") // Dataflow analysis limitation
	public final @Nullable BeanDefinition parse(Element element, ParserContext parserContext) {
		AbstractBeanDefinition definition = parseInternal(element, parserContext);
		if (definition != null && !parserContext.isNested()) {
			try {
				String id = resolveId(element, definition, parserContext);
				if (!StringUtils.hasText(id)) {
					parserContext.getReaderContext().error(
							"Id is required for element '" + parserContext.getDelegate().getLocalName(element) +
							"' when used as a top-level tag", element);
				}
				String[] aliases = null;
				if (shouldParseNameAsAliases()) {
					String name = element.getAttribute(NAME_ATTRIBUTE);
					if (StringUtils.hasLength(name)) {
						aliases = StringUtils.trimArrayElements(StringUtils.commaDelimitedListToStringArray(name));
					}
				}
				BeanDefinitionHolder holder = new BeanDefinitionHolder(definition, id, aliases);
				registerBeanDefinition(holder, parserContext.getRegistry());
				if (shouldFireEvents()) {
					BeanComponentDefinition componentDefinition = new BeanComponentDefinition(holder);
					postProcessComponentDefinition(componentDefinition);
					parserContext.registerComponent(componentDefinition);
				}
			}
			catch (BeanDefinitionStoreException ex) {
				String msg = ex.getMessage();
				parserContext.getReaderContext().error((msg != null ? msg : ex.toString()), element);
				return null;
			}
		}
		return definition;
	}

	/**
	 * 为给定的 {@link BeanDefinition} 解析 ID。
	 * <p>启用 {@link #shouldGenerateId 自动生成} 时直接生成名称；否则从 {@code id} 属性读取，
	 * 必要时可 {@link #shouldGenerateIdAsFallback() 回退}到生成 ID。
	 * @param element 构建 Bean 定义的源元素
	 * @param definition 待注册的 Bean 定义
	 * @param parserContext 封装当前解析状态，可访问 {@link org.springframework.beans.factory.support.BeanDefinitionRegistry}
	 * @return 解析得到的 id
	 * @throws BeanDefinitionStoreException 无法为给定 Bean 定义生成唯一名称时
	 */
	protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext)
			throws BeanDefinitionStoreException {

		if (shouldGenerateId()) {
			return parserContext.getReaderContext().generateBeanName(definition);
		}
		else {
			String id = element.getAttribute(ID_ATTRIBUTE);
			if (!StringUtils.hasText(id) && shouldGenerateIdAsFallback()) {
				id = parserContext.getReaderContext().generateBeanName(definition);
			}
			return id;
		}
	}

	/**
	 * 将给定 {@link BeanDefinitionHolder} 注册到 {@link BeanDefinitionRegistry}。
	 * <p>子类可覆盖以控制是否注册、或注册更多 Bean。
	 * <p>默认实现仅在非嵌套场景下注册，因内部 Bean 通常不应作为顶层 Bean 注册。
	 * @param definition 待注册的 Bean 定义
	 * @param registry 目标注册表
	 * @see BeanDefinitionReaderUtils#registerBeanDefinition(BeanDefinitionHolder, BeanDefinitionRegistry)
	 */
	protected void registerBeanDefinition(BeanDefinitionHolder definition, BeanDefinitionRegistry registry) {
		BeanDefinitionReaderUtils.registerBeanDefinition(definition, registry);
	}


	/**
	 * 核心模板方法：将给定 {@link Element} 解析为一个或多个 {@link BeanDefinition}。
	 * @param element 待解析的 XML 元素
	 * @param parserContext 封装当前解析状态，可访问 {@link org.springframework.beans.factory.support.BeanDefinitionRegistry}
	 * @return 解析得到的主 {@link BeanDefinition}
	 * @see #parse(org.w3c.dom.Element, ParserContext)
	 * @see #postProcessComponentDefinition(org.springframework.beans.factory.parsing.BeanComponentDefinition)
	 */
	protected abstract @Nullable AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext);

	/**
	 * 是否始终生成 ID，而不从传入的 {@link Element} 读取？
	 * <p>默认关闭。启用后解析器不会检查 {@code id} 属性。
	 * @return 是否始终生成 id
	 */
	protected boolean shouldGenerateId() {
		return false;
	}

	/**
	 * 当传入的 {@link Element} 未显式指定 {@code id} 时，是否生成 ID 作为回退？
	 * <p>默认关闭。启用后先读 {@code id}，无值时才生成。
	 * @return 未指定 id 时是否生成
	 */
	protected boolean shouldGenerateIdAsFallback() {
		return false;
	}

	/**
	 * 是否将元素的 {@code name} 属性解析为 Bean 定义别名。
	 * <p>默认返回 {@code true}。
	 * @return 是否将 {@code name} 当作别名解析
	 * @since 4.1.5
	 */
	protected boolean shouldParseNameAsAliases() {
		return true;
	}

	/**
	 * 解析 Bean 定义后是否触发
	 * {@link org.springframework.beans.factory.parsing.BeanComponentDefinition} 事件。
	 * <p>默认 {@code true}。覆盖为 {@code false} 可抑制事件。
	 * @return 解析完成后是否触发组件注册事件
	 * @see #postProcessComponentDefinition
	 * @see org.springframework.beans.factory.parsing.ReaderContext#fireComponentRegistered
	 */
	protected boolean shouldFireEvents() {
		return true;
	}

	/**
	 * 主解析完成、{@link BeanComponentDefinition} 注册前的钩子方法。
	 * <p>子类可覆盖以在解析结束后执行自定义逻辑。默认无操作。
	 * @param componentDefinition 待处理的 {@link BeanComponentDefinition}
	 */
	protected void postProcessComponentDefinition(BeanComponentDefinition componentDefinition) {
	}

}
