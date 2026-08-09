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

/* ===== [OCA 中文解析] =====
文件意图总览

解析 XML {@code <context:annotation-config/>} 元素，注册注解配置所需的 BeanPostProcessor。
===== [OCA 中文解析结束] ===== */
package org.springframework.context.annotation;

import java.util.Set;

import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;

/* ===== [OCA 中文解析] =====
class AnnotationConfigBeanDefinitionParser — 意图说明

将 {@code <context:annotation-config/>} 解析为一系列基础设施 Bean 定义并注册到容器。

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
===== [OCA 中文解析结束] ===== */
/**
 * Parser for the &lt;context:annotation-config/&gt; element.
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @author Christian Dupuis
 * @since 2.5
 * @see AnnotationConfigUtils
 */
public class AnnotationConfigBeanDefinitionParser implements BeanDefinitionParser {

	@Override
	public @Nullable BeanDefinition parse(Element element, ParserContext parserContext) {
		Object source = parserContext.extractSource(element);

		// [OCA] 获取所有相关 BeanPostProcessor 的 Bean 定义。
		Set<BeanDefinitionHolder> processorDefinitions =
				AnnotationConfigUtils.registerAnnotationConfigProcessors(parserContext.getRegistry(), source);

		// [OCA] 为外围 {@code <context:annotation-config>} 元素注册组合组件。
		CompositeComponentDefinition compDefinition = new CompositeComponentDefinition(element.getTagName(), source);
		parserContext.pushContainingComponent(compDefinition);

		// [OCA] 将具体 Bean 嵌套到外围组合组件中。
		for (BeanDefinitionHolder processorDefinition : processorDefinitions) {
			parserContext.registerComponent(new BeanComponentDefinition(processorDefinition));
		}

		// [OCA] 最终注册组合组件。
		parserContext.popAndRegisterContainingComponent();

		return null;
	}

}
