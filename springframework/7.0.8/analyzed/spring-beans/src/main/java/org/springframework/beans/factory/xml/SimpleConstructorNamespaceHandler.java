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

import java.util.Collection;

import org.jspecify.annotations.Nullable;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.core.Conventions;
import org.springframework.util.StringUtils;

/**
 * 简单 {@code NamespaceHandler} 实现，将自定义属性直接映射到 bean 构造参数。
 * 需注意的重要一点是，此 {@code NamespaceHandler} 没有对应的 schema，
 * 因为无法预先知道所有可能的属性名。
 *
 * <p>此 {@code NamespaceHandler} 的用法示例如下：
 *
 * <pre class="code">
 * &lt;bean id=&quot;author&quot; class=&quot;..TestBean&quot; c:name=&quot;Enescu&quot; c:work-ref=&quot;compositions&quot;/&gt;
 * </pre>
 *
 * 此处 '{@code c:name}' 直接对应类 '{@code TestBean}' 构造函数上声明的
 * '{@code name}' 参数。'{@code c:work-ref}' 属性对应 '{@code work}' 参数，
 * 其值不是具体值，而是将作为参数考虑的 bean 名称。
 *
 * <b>注意</b>：此实现仅支持命名参数——不支持索引或类型。
 * 此外，名称作为容器的提示，容器默认进行类型自省。
 *
 * @author Costin Leau
 * @since 3.1
 * @see SimplePropertyNamespaceHandler
 */
public class SimpleConstructorNamespaceHandler implements NamespaceHandler {

	private static final String REF_SUFFIX = "-ref";

	private static final String DELIMITER_PREFIX = "_";


	@Override
	public void init() {
	}

	@Override
	public @Nullable BeanDefinition parse(Element element, ParserContext parserContext) {
		parserContext.getReaderContext().error(
				"Class [" + getClass().getName() + "] does not support custom elements.", element);
		return null;
	}

	@Override
	public BeanDefinitionHolder decorate(Node node, BeanDefinitionHolder definition, ParserContext parserContext) {
		if (node instanceof Attr attr) {
			String argName = parserContext.getDelegate().getLocalName(attr).strip();
			String argValue = attr.getValue().strip();

			ConstructorArgumentValues cvs = definition.getBeanDefinition().getConstructorArgumentValues();
			boolean ref = false;

			// 处理 -ref 参数
			if (argName.endsWith(REF_SUFFIX)) {
				ref = true;
				argName = argName.substring(0, argName.length() - REF_SUFFIX.length());
			}

			ValueHolder valueHolder = new ValueHolder(ref ? new RuntimeBeanReference(argValue) : argValue);
			valueHolder.setSource(parserContext.getReaderContext().extractSource(attr));

			// 处理 "转义"/"_" 参数
			if (argName.startsWith(DELIMITER_PREFIX)) {
				String arg = argName.substring(1).trim();

				// 快速默认检查
				if (!StringUtils.hasText(arg)) {
					cvs.addGenericArgumentValue(valueHolder);
				}
				// 否则假定为索引
				else {
					int index = -1;
					try {
						index = Integer.parseInt(arg);
					}
					catch (NumberFormatException ex) {
						parserContext.getReaderContext().error(
								"Constructor argument '" + argName + "' specifies an invalid integer", attr);
					}
					if (index < 0) {
						parserContext.getReaderContext().error(
								"Constructor argument '" + argName + "' specifies a negative index", attr);
					}

					if (cvs.hasIndexedArgumentValue(index)) {
						parserContext.getReaderContext().error(
								"Constructor argument '" + argName + "' with index "+ index+" already defined using <constructor-arg>." +
								" Only one approach may be used per argument.", attr);
					}

					cvs.addIndexedArgumentValue(index, valueHolder);
				}
			}
			// 无转义 → 构造参数名
			else {
				String name = Conventions.attributeNameToPropertyName(argName);
				if (containsArgWithName(name, cvs)) {
					parserContext.getReaderContext().error(
							"Constructor argument '" + argName + "' already defined using <constructor-arg>." +
							" Only one approach may be used per argument.", attr);
				}
				valueHolder.setName(Conventions.attributeNameToPropertyName(argName));
				cvs.addGenericArgumentValue(valueHolder);
			}
		}
		return definition;
	}

	private boolean containsArgWithName(String name, ConstructorArgumentValues cvs) {
		return (checkName(name, cvs.getGenericArgumentValues()) ||
				checkName(name, cvs.getIndexedArgumentValues().values()));
	}

	private boolean checkName(String name, Collection<ValueHolder> values) {
		for (ValueHolder holder : values) {
			if (name.equals(holder.getName())) {
				return true;
			}
		}
		return false;
	}

}
