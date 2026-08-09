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

package org.springframework.beans.factory.groovy;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import groovy.lang.Closure;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.Writable;
import groovy.xml.StreamingMarkupBuilder;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;

/**
 * 供 {@link GroovyBeanDefinitionReader} 在 Groovy DSL 中读取 Spring XML 命名空间表达式。
 *
 * @author Jeff Brown
 * @author Juergen Hoeller
 * @author Dave Syer
 * @since 4.0
 */
class GroovyDynamicElementReader extends GroovyObjectSupport {

	/** 根命名空间名称。 */
	private final String rootNamespace;

	/** XML 命名空间映射。 */
	private final Map<String, String> xmlNamespaces;

	/** Bean 定义解析委托。 */
	private final BeanDefinitionParserDelegate delegate;

	/** 当前 Bean 定义包装器。 */
	private final GroovyBeanDefinitionWrapper beanDefinition;

	/** 是否为装饰模式（decorate 而非 parse）。 */
	protected final boolean decorating;

	/** 是否在调用结束后执行 afterInvocation 钩子。 */
	private boolean callAfterInvocation = true;


	/**
	 * 创建动态元素读取器。
	 * @param namespace 根命名空间
	 * @param namespaceMap XML 命名空间映射
	 * @param delegate Bean 定义解析委托
	 * @param beanDefinition Bean 定义包装器
	 * @param decorating 是否为装饰模式
	 */
	public GroovyDynamicElementReader(String namespace, Map<String, String> namespaceMap,
			BeanDefinitionParserDelegate delegate, GroovyBeanDefinitionWrapper beanDefinition, boolean decorating) {

		this.rootNamespace = namespace;
		this.xmlNamespaces = namespaceMap;
		this.delegate = delegate;
		this.beanDefinition = beanDefinition;
		this.decorating = decorating;
	}


	@Override
	public @Nullable Object invokeMethod(String name, Object obj) {
		Object[] args = (Object[]) obj;
		if (name.equals("doCall")) {
			// 处理闭包调用：设置委托并执行
			@SuppressWarnings("unchecked")
			Closure<Object> callable = (Closure<Object>) args[0];
			callable.setResolveStrategy(Closure.DELEGATE_FIRST);
			callable.setDelegate(this);
			Object result = callable.call();

			if (this.callAfterInvocation) {
				afterInvocation();
				this.callAfterInvocation = false;
			}
			return result;
		}
		else {
			// 将 Groovy DSL 调用转换为 XML 标记，再交由委托解析
			StreamingMarkupBuilder builder = new StreamingMarkupBuilder();
			String myNamespace = this.rootNamespace;
			Map<String, String> myNamespaces = this.xmlNamespaces;

			@SuppressWarnings("serial")
			Closure<Object> callable = new Closure<>(this) {
				@Override
				public Object call(Object... arguments) {
					((GroovyObject) getProperty("mkp")).invokeMethod("declareNamespace", new Object[] {myNamespaces});
					int len = args.length;
					if (len > 0 && args[len-1] instanceof Closure<?> callable) {
						callable.setResolveStrategy(Closure.DELEGATE_FIRST);
						callable.setDelegate(builder);
					}
					return ((GroovyObject) ((GroovyObject) getDelegate()).getProperty(myNamespace)).invokeMethod(name, args);
				}
			};

			callable.setResolveStrategy(Closure.DELEGATE_FIRST);
			callable.setDelegate(builder);
			Writable writable = (Writable) builder.bind(callable);
			StringWriter sw = new StringWriter();
			try {
				writable.writeTo(sw);
			}
			catch (IOException ex) {
				throw new IllegalStateException(ex);
			}

			// 从生成的 XML 字符串解析 DOM 元素
			Element element = this.delegate.getReaderContext().readDocumentFromString(sw.toString()).getDocumentElement();
			this.delegate.initDefaults(element);
			if (this.decorating) {
				// 装饰模式：对已有 Bean 定义进行装饰
				BeanDefinitionHolder holder = this.beanDefinition.getBeanDefinitionHolder();
				holder = this.delegate.decorateIfRequired(element, holder, null);
				this.beanDefinition.setBeanDefinitionHolder(holder);
			}
			else {
				// 解析模式：解析自定义元素并设置 Bean 定义
				BeanDefinition beanDefinition = this.delegate.parseCustomElement(element);
				if (beanDefinition != null) {
					this.beanDefinition.setBeanDefinition((AbstractBeanDefinition) beanDefinition);
				}
			}
			if (this.callAfterInvocation) {
				afterInvocation();
				this.callAfterInvocation = false;
			}
			return element;
		}
	}

	/**
	 * 调用完成后的钩子，子类或匿名类可覆盖以实现自定义行为。
	 */
	protected void afterInvocation() {
		// NOOP
	}

}
