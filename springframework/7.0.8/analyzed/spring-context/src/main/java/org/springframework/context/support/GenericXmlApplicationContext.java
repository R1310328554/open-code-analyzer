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

import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * 内置 XML 支持的便捷应用上下文。
 * 这是 {@link ClassPathXmlApplicationContext} 与
 * {@link FileSystemXmlApplicationContext} 的灵活替代方案，
 * 通过 setter 配置，最终调用 {@link #refresh()} 激活上下文。
 *
 * <p>存在多个配置文件时，后加载文件中的 Bean 定义会覆盖先加载文件中的定义。
 * 可借此通过追加到列表末尾的额外配置文件有意覆盖某些 Bean 定义。
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 3.0
 * @see #load
 * @see XmlBeanDefinitionReader
 * @see org.springframework.context.annotation.AnnotationConfigApplicationContext
 */
public class GenericXmlApplicationContext extends GenericApplicationContext {

	/** 底层 XML Bean 定义读取器。 */
	private final XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(this);


	/**
	 * 创建新的 GenericXmlApplicationContext，需先 {@link #load 加载} 再手动 {@link #refresh 刷新}。
	 */
	public GenericXmlApplicationContext() {
	}

	/**
	 * 创建新的 GenericXmlApplicationContext，从给定资源加载 Bean 定义并自动刷新上下文。
	 * @param resources 要加载的资源
	 */
	public GenericXmlApplicationContext(Resource... resources) {
		load(resources);
		refresh();
	}

	/**
	 * 创建新的 GenericXmlApplicationContext，从给定资源位置加载 Bean 定义并自动刷新上下文。
	 * @param resourceLocations 要加载的资源位置
	 */
	public GenericXmlApplicationContext(String... resourceLocations) {
		load(resourceLocations);
		refresh();
	}

	/**
	 * 创建新的 GenericXmlApplicationContext，从给定资源位置加载 Bean 定义并自动刷新上下文。
	 * @param relativeClass 加载各资源名时用作包前缀的类
	 * @param resourceNames 相对限定资源名
	 */
	public GenericXmlApplicationContext(Class<?> relativeClass, String... resourceNames) {
		load(relativeClass, resourceNames);
		refresh();
	}


	/**
	 * 暴露底层 {@link XmlBeanDefinitionReader}，便于额外配置及多种 {@code loadBeanDefinition} 变体。
	 */
	public final XmlBeanDefinitionReader getReader() {
		return this.reader;
	}

	/**
	 * 设置是否使用 XML 校验。默认为 {@code true}。
	 */
	public void setValidating(boolean validating) {
		this.reader.setValidating(validating);
	}

	/**
	 * 将给定环境委托给底层 {@link XmlBeanDefinitionReader}。
	 * 应在任何 {@code #load} 调用之前执行。
	 */
	@Override
	public void setEnvironment(ConfigurableEnvironment environment) {
		super.setEnvironment(environment);
		this.reader.setEnvironment(getEnvironment());
	}


	//---------------------------------------------------------------------
	// 加载 XML Bean 定义文件的便捷方法
	//---------------------------------------------------------------------

	/**
	 * 从给定 XML 资源加载 Bean 定义。
	 * @param resources 要加载的一个或多个资源
	 */
	public void load(Resource... resources) {
		this.reader.loadBeanDefinitions(resources);
	}

	/**
	 * 从给定 XML 资源位置加载 Bean 定义。
	 * @param resourceLocations 要加载的一个或多个资源位置
	 */
	public void load(String... resourceLocations) {
		this.reader.loadBeanDefinitions(resourceLocations);
	}

	/**
	 * 从给定 XML 资源位置加载 Bean 定义。
	 * @param relativeClass 加载各资源名时用作包前缀的类
	 * @param resourceNames 相对限定资源名
	 */
	public void load(Class<?> relativeClass, String... resourceNames) {
		Resource[] resources = new Resource[resourceNames.length];
		for (int i = 0; i < resourceNames.length; i++) {
			resources[i] = new ClassPathResource(resourceNames[i], relativeClass);
		}
		this.load(resources);
	}

}
