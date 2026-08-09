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

package org.springframework.scripting.config;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * 支持装配 Groovy、JRuby、BeanShell 等动态语言
 * 所支持对象的 {@code NamespaceHandler}。以下为参考文档中
 * 装配 Groovy Bean 的示例：
 *
 * <pre class="code">
 * &lt;lang:groovy id="messenger"
 *     refresh-check-delay="5000"
 *     script-source="classpath:Messenger.groovy"&gt;
 * &lt;lang:property name="message" value="I Can Do The Frug"/&gt;
 * &lt;/lang:groovy&gt;
 * </pre>
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @since 2.0
 * @deprecated 无替代方案，已不再积极维护
 */
@Deprecated(since = "7.0")
public class LangNamespaceHandler extends NamespaceHandlerSupport {

	@Override
	public void init() {
		registerScriptBeanDefinitionParser("groovy", "org.springframework.scripting.groovy.GroovyScriptFactory");
		registerScriptBeanDefinitionParser("bsh", "org.springframework.scripting.bsh.BshScriptFactory");
		registerScriptBeanDefinitionParser("std", "org.springframework.scripting.support.StandardScriptFactory");
		registerBeanDefinitionParser("defaults", new ScriptingDefaultsParser());
	}

	private void registerScriptBeanDefinitionParser(String key, String scriptFactoryClassName) {
		registerBeanDefinitionParser(key, new ScriptBeanDefinitionParser(scriptFactoryClassName));
	}

}
