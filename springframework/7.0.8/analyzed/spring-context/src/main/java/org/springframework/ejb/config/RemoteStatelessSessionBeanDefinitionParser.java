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

import org.springframework.beans.BeanUtils;
import org.springframework.jndi.JndiObjectFactoryBean;

/**
 * 解析 {@code remote-slsb} 标签并创建普通 {@link JndiObjectFactoryBean} 定义的
 * {@link org.springframework.beans.factory.xml.BeanDefinitionParser} 实现（自 6.0 起）。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
class RemoteStatelessSessionBeanDefinitionParser extends AbstractJndiLocatingBeanDefinitionParser {

	@Override
	protected Class<?> getBeanClass(Element element) {
		return JndiObjectFactoryBean.class;
	}

	/** 仅接受 {@link JndiObjectFactoryBean} 上实际存在的可写属性。 */
	@Override
	protected boolean isEligibleAttribute(String attributeName) {
		return (super.isEligibleAttribute(attributeName) &&
				BeanUtils.getPropertyDescriptor(JndiObjectFactoryBean.class, extractPropertyName(attributeName)) != null);
	}

}
