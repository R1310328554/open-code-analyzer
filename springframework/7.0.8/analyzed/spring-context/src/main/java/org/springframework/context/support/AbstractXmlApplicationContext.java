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

package org.springframework.context.support;

import java.io.IOException;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.BeanDefinitionDocumentReader;
import org.springframework.beans.factory.xml.ResourceEntityResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

/**
 * 便于使用的 {@link org.springframework.context.ApplicationContext} 实现基类，
 * 从包含可由 {@link org.springframework.beans.factory.xml.XmlBeanDefinitionReader}
 * 解析的 Bean 定义的 XML 文档中加载配置。
 *
 * <p>子类只需实现 {@link #getConfigResources} 和/或 {@link #getConfigLocations} 方法。
 * 此外，它们可以覆盖 {@link #getResourceByPath} 钩子，以特定于环境的方式解释相对路径，
 * 和/或覆盖 {@link #getResourcePatternResolver} 以支持扩展的模式解析。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #getConfigResources
 * @see #getConfigLocations
 * @see org.springframework.beans.factory.xml.XmlBeanDefinitionReader
 */
public abstract class AbstractXmlApplicationContext extends AbstractRefreshableConfigApplicationContext {

	private boolean validating = true;


	/**
	 * 创建一个没有父上下文的 AbstractXmlApplicationContext。
	 */
	public AbstractXmlApplicationContext() {
	}

	/**
	 * 使用给定的父上下文创建新的 AbstractXmlApplicationContext。
	 * @param parent 父上下文
	 */
	public AbstractXmlApplicationContext(@Nullable ApplicationContext parent) {
		super(parent);
	}


	/**
	 * 设置是否启用 XML 校验。默认为 {@code true}。
	 */
	public void setValidating(boolean validating) {
		this.validating = validating;
	}


	/**
	 * 通过 XmlBeanDefinitionReader 加载 Bean 定义。
	 * @see org.springframework.beans.factory.xml.XmlBeanDefinitionReader
	 * @see #initBeanDefinitionReader
	 * @see #loadBeanDefinitions
	 */
	@Override
	protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws BeansException, IOException {
		// Create a new XmlBeanDefinitionReader for the given BeanFactory.
		XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);

		// Configure the bean definition reader with this context's
		// resource loading environment.
		beanDefinitionReader.setEnvironment(getEnvironment());
		beanDefinitionReader.setResourceLoader(this);
		beanDefinitionReader.setEntityResolver(new ResourceEntityResolver(this));

		// Allow a subclass to provide custom initialization of the reader,
		// then proceed with actually loading the bean definitions.
		initBeanDefinitionReader(beanDefinitionReader);
		loadBeanDefinitions(beanDefinitionReader);
	}

	/**
	 * 初始化用于加载本上下文 Bean 定义的 Bean 定义读取器。默认实现会设置校验标志。
	 * <p>子类可覆盖此方法，例如关闭 XML 校验或使用不同的
	 * {@link BeanDefinitionDocumentReader} 实现。
	 * @param reader 本上下文使用的 Bean 定义读取器
	 * @see XmlBeanDefinitionReader#setValidating
	 * @see XmlBeanDefinitionReader#setDocumentReaderClass
	 */
	protected void initBeanDefinitionReader(XmlBeanDefinitionReader reader) {
		reader.setValidating(this.validating);
	}

	/**
	 * 使用给定的 XmlBeanDefinitionReader 加载 Bean 定义。
	 * <p>Bean 工厂的生命周期由 {@link #refreshBeanFactory} 方法管理；
	 * 因此本方法仅负责加载和/或注册 Bean 定义。
	 * @param reader 要使用的 XmlBeanDefinitionReader
	 * @throws BeansException 若 Bean 注册出错
	 * @throws IOException 若找不到所需的 XML 文档
	 * @see #refreshBeanFactory
	 * @see #getConfigLocations
	 * @see #getResources
	 * @see #getResourcePatternResolver
	 */
	protected void loadBeanDefinitions(XmlBeanDefinitionReader reader) throws BeansException, IOException {
		Resource[] configResources = getConfigResources();
		if (configResources != null) {
			reader.loadBeanDefinitions(configResources);
		}
		String[] configLocations = getConfigLocations();
		if (configLocations != null) {
			reader.loadBeanDefinitions(configLocations);
		}
	}

	/**
	 * 返回用于构建本上下文的 XML Bean 定义文件所对应的 Resource 对象数组。
	 * <p>默认实现返回 {@code null}。子类可覆盖此方法，
	 * 提供预先构建的 Resource 对象，而非位置字符串。
	 * @return Resource 对象数组；若无则返回 {@code null}
	 * @see #getConfigLocations()
	 */
	protected Resource @Nullable [] getConfigResources() {
		return null;
	}

}
