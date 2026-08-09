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

package org.springframework.ejb.config;

import org.w3c.dom.Element;

import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;

import static org.springframework.beans.factory.xml.BeanDefinitionParserDelegate.DEFAULT_VALUE;
import static org.springframework.beans.factory.xml.BeanDefinitionParserDelegate.LAZY_INIT_ATTRIBUTE;
import static org.springframework.beans.factory.xml.BeanDefinitionParserDelegate.TRUE_VALUE;

/**
 * 构建 JNDI 定位 Bean 的 BeanDefinitionParser 抽象基类，
 * 支持可选的 {@code jndiEnvironment} 属性，可从 {@code environment} XML 子元素填充。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Oliver Gierke
 * @since 2.0
 */
abstract class AbstractJndiLocatingBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

	/** {@code environment} 子元素标签名。 */
	public static final String ENVIRONMENT = "environment";

	/** {@code environment-ref} 属性名。 */
	public static final String ENVIRONMENT_REF = "environment-ref";

	/** {@code jndiEnvironment} Bean 属性名。 */
	public static final String JNDI_ENVIRONMENT = "jndiEnvironment";


	/** 排除 {@code environment-ref} 与 {@code lazy-init}，由后处理单独处理。 */
	@Override
	protected boolean isEligibleAttribute(String attributeName) {
		return (super.isEligibleAttribute(attributeName) &&
				!ENVIRONMENT_REF.equals(attributeName) &&
				!LAZY_INIT_ATTRIBUTE.equals(attributeName));
	}

	/** 解析 JNDI 环境配置与 lazy-init 属性。 */
	@Override
	protected void postProcess(BeanDefinitionBuilder definitionBuilder, Element element) {
		Object envValue = DomUtils.getChildElementValueByTagName(element, ENVIRONMENT);
		if (envValue != null) {
			// 内联 environment 子元素优先，覆盖共享属性引用
			definitionBuilder.addPropertyValue(JNDI_ENVIRONMENT, envValue);
		}
		else {
			// 否则检查是否引用了共享环境属性 Bean
			String envRef = element.getAttribute(ENVIRONMENT_REF);
			if (StringUtils.hasLength(envRef)) {
				definitionBuilder.addPropertyValue(JNDI_ENVIRONMENT, new RuntimeBeanReference(envRef));
			}
		}

		String lazyInit = element.getAttribute(LAZY_INIT_ATTRIBUTE);
		if (StringUtils.hasText(lazyInit) && !DEFAULT_VALUE.equals(lazyInit)) {
			definitionBuilder.setLazyInit(TRUE_VALUE.equals(lazyInit));
		}
	}
}
