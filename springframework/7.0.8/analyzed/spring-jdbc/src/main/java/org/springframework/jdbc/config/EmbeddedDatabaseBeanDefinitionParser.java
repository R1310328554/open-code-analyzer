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

package org.springframework.jdbc.config;

import org.w3c.dom.Element;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseFactoryBean;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.util.StringUtils;

/**
 * {@link org.springframework.beans.factory.xml.BeanDefinitionParser} 解析 {@code
 * embedded-database} 元素并为 {@link EmbeddedDatabaseFactoryBean} 创建 {@link BeanDefinition}。
 * <p>拾取嵌套的 {@code script} 元素并为每个元素配置一个 {@link ResourceDatabasePopulator}。
 * @author Oliver Gierke
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 3.0
 * @see DatabasePopulatorConfigUtils
 */
class EmbeddedDatabaseBeanDefinitionParser extends AbstractBeanDefinitionParser {

	/**
	 * “数据库名称”属性的常量。
	 */
	static final String DB_NAME_ATTRIBUTE = "database-name";

	/**
	 * “generate-name”属性的常量。
	 */
	static final String GENERATE_NAME_ATTRIBUTE = "generate-name";


	/**
	 * 解析：Internal（方法 `parseInternal`）。
	 */
	@Override
	protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(EmbeddedDatabaseFactoryBean.class);
		setGenerateUniqueDatabaseNameFlag(element, builder);
		setDatabaseName(element, builder);
		setDatabaseType(element, builder);
		DatabasePopulatorConfigUtils.setDatabasePopulator(element, builder);
		builder.getRawBeanDefinition().setSource(parserContext.extractSource(element));
		return builder.getBeanDefinition();
	}

	/**
	 * 方法 `shouldGenerateIdAsFallback`：完成本类中与「should Generate Id As Fallback」相关的职责。
	 */
	@Override
	protected boolean shouldGenerateIdAsFallback() {
		return true;
	}

	/**
	 * 设置 Generate Unique Database Name Flag（`GenerateUniqueDatabaseNameFlag`）。
	 */
	private void setGenerateUniqueDatabaseNameFlag(Element element, BeanDefinitionBuilder builder) {
		String generateName = element.getAttribute(GENERATE_NAME_ATTRIBUTE);
		if (StringUtils.hasText(generateName)) {
			builder.addPropertyValue("generateUniqueDatabaseName", generateName);
		}
	}

	/**
	 * 设置 Database Name（`DatabaseName`）。
	 */
	private void setDatabaseName(Element element, BeanDefinitionBuilder builder) {
		// 1) 检查显式数据库名称
		String name = element.getAttribute(DB_NAME_ATTRIBUTE);

		// 2) 根据 ID 回退到隐式数据库名称
		if (!StringUtils.hasText(name)) {
			name = element.getAttribute(ID_ATTRIBUTE);
		}

		if (StringUtils.hasText(name)) {
			builder.addPropertyValue("databaseName", name);
		}
		// 否则，让 EmbeddedDatabaseFactory 使用默认的“testdb”名称
	}

	/**
	 * 设置 Database Type（`DatabaseType`）。
	 */
	private void setDatabaseType(Element element, BeanDefinitionBuilder builder) {
		String type = element.getAttribute("type");
		if (StringUtils.hasText(type)) {
			builder.addPropertyValue("databaseType", type);
		}
	}

}
