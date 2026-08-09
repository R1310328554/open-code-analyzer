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

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * {@code jee} 命名空间的 {@link org.springframework.beans.factory.xml.NamespaceHandler}。
 *
 * @author Rob Harrop
 * @since 2.0
 */
public class JeeNamespaceHandler extends NamespaceHandlerSupport {

	/** 注册 JNDI 查找、本地/远程无状态会话 Bean 三类 XML 元素解析器。 */
	@Override
	public void init() {
		registerBeanDefinitionParser("jndi-lookup", new JndiLookupBeanDefinitionParser());
		registerBeanDefinitionParser("local-slsb", new LocalStatelessSessionBeanDefinitionParser());
		registerBeanDefinitionParser("remote-slsb", new RemoteStatelessSessionBeanDefinitionParser());
	}

}
