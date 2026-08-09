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

import groovy.lang.GroovyObject;
import groovy.lang.GroovySystem;
import groovy.lang.MetaClass;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.groovy.GroovyBeanDefinitionReader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * 扩展 {@link GenericApplicationContext} 并实现 {@link GroovyObject} 的
 * {@link org.springframework.context.ApplicationContext} 实现，
 * 允许使用点号解引用语法获取 Bean，而无需调用 {@link #getBean}。
 *
 * <p>可视为 Groovy Bean 定义的 {@link GenericXmlApplicationContext} 等价物，
 * 甚至可视为其升级版，因为它也能无缝理解 XML Bean 定义文件。
 * 主要区别在于：在 Groovy 脚本中，上下文可与内联 Bean 定义闭包配合使用，例如：
 *
 * <pre class="code">
 * import org.hibernate.SessionFactory
 * import org.apache.commons.dbcp.BasicDataSource
 *
 * def context = new GenericGroovyApplicationContext()
 * context.reader.beans {
 *     dataSource(BasicDataSource) {                  // &lt;--- invokeMethod
 *         driverClassName = "org.hsqldb.jdbcDriver"
 *         url = "jdbc:hsqldb:mem:grailsDB"
 *         username = "sa"                            // &lt;-- setProperty
 *         password = ""
 *         settings = [mynew:"setting"]
 *     }
 *     sessionFactory(SessionFactory) {
 *         dataSource = dataSource                    // &lt;-- getProperty for retrieving references
 *     }
 *     myService(MyService) {
 *         nestedBean = { AnotherBean bean -&gt;         // &lt;-- setProperty with closure for nested bean
 *             dataSource = dataSource
 *         }
 *     }
 * }
 * context.refresh()
 * </pre>
 *
 * <p>或者，从外部资源（例如 "applicationContext.groovy" 文件）加载如下 Groovy Bean 定义脚本：
 *
 * <pre class="code">
 * import org.hibernate.SessionFactory
 * import org.apache.commons.dbcp.BasicDataSource
 *
 * beans {
 *     dataSource(BasicDataSource) {
 *         driverClassName = "org.hsqldb.jdbcDriver"
 *         url = "jdbc:hsqldb:mem:grailsDB"
 *         username = "sa"
 *         password = ""
 *         settings = [mynew:"setting"]
 *     }
 *     sessionFactory(SessionFactory) {
 *         dataSource = dataSource
 *     }
 *     myService(MyService) {
 *         nestedBean = { AnotherBean bean -&gt;
 *             dataSource = dataSource
 *         }
 *     }
 * }
 * </pre>
 *
 * <p>配合以下 Java 代码创建 {@code GenericGroovyApplicationContext}
 *（也可使用 Ant 风格 '*'/'**' 位置模式）：
 *
 * <pre class="code">
 * GenericGroovyApplicationContext context = new GenericGroovyApplicationContext();
 * context.load("org/myapp/applicationContext.groovy");
 * context.refresh();
 * </pre>
 *
 * <p>若无需额外配置，还可更简洁地：
 *
 * <pre class="code">
 * ApplicationContext context = new GenericGroovyApplicationContext("org/myapp/applicationContext.groovy");
 * </pre>
 *
 * <p><b>本应用上下文也理解 XML Bean 定义文件，可与 Groovy Bean 定义文件无缝混用。</b>
 * ".xml" 文件按 XML 内容解析；其他类型资源按 Groovy 脚本解析。
 *
 * @author Juergen Hoeller
 * @author Jeff Brown
 * @since 4.0
 * @see org.springframework.beans.factory.groovy.GroovyBeanDefinitionReader
 */
public class GenericGroovyApplicationContext extends GenericApplicationContext implements GroovyObject {

	/** Groovy Bean 定义读取器。 */
	private final GroovyBeanDefinitionReader reader = new GroovyBeanDefinitionReader(this);

	/** 用于在 Groovy 属性访问时委托到上下文 Bean 的包装器。 */
	private final BeanWrapper contextWrapper = new BeanWrapperImpl(this);

	/** Groovy 元类，支持动态方法/属性调用。 */
	private MetaClass metaClass = GroovySystem.getMetaClassRegistry().getMetaClass(getClass());


	/**
	 * 创建新的 GenericGroovyApplicationContext，需先 {@link #load 加载} 再手动 {@link #refresh 刷新}。
	 */
	public GenericGroovyApplicationContext() {
	}

	/**
	 * 创建新的 GenericGroovyApplicationContext，从给定资源加载 Bean 定义并自动刷新上下文。
	 * @param resources 要加载的资源
	 */
	public GenericGroovyApplicationContext(Resource... resources) {
		load(resources);
		refresh();
	}

	/**
	 * 创建新的 GenericGroovyApplicationContext，从给定资源位置加载 Bean 定义并自动刷新上下文。
	 * @param resourceLocations 要加载的资源位置
	 */
	public GenericGroovyApplicationContext(String... resourceLocations) {
		load(resourceLocations);
		refresh();
	}

	/**
	 * 创建新的 GenericGroovyApplicationContext，从给定资源位置加载 Bean 定义并自动刷新上下文。
	 * @param relativeClass 加载各资源名时用作包前缀的类
	 * @param resourceNames 相对限定资源名
	 */
	public GenericGroovyApplicationContext(Class<?> relativeClass, String... resourceNames) {
		load(relativeClass, resourceNames);
		refresh();
	}


	/**
	 * 暴露底层 {@link GroovyBeanDefinitionReader}，便于访问其 {@code loadBeanDefinition} 方法
	 * 以及指定内联 Groovy Bean 定义闭包。
	 * @see GroovyBeanDefinitionReader#loadBeanDefinitions(org.springframework.core.io.Resource...)
	 * @see GroovyBeanDefinitionReader#loadBeanDefinitions(String...)
	 */
	public final GroovyBeanDefinitionReader getReader() {
		return this.reader;
	}

	/**
	 * 将给定环境委托给底层 {@link GroovyBeanDefinitionReader}。
	 * 应在任何 {@code #load} 调用之前执行。
	 */
	@Override
	public void setEnvironment(ConfigurableEnvironment environment) {
		super.setEnvironment(environment);
		this.reader.setEnvironment(getEnvironment());
	}

	/**
	 * 从给定 Groovy 脚本或 XML 文件加载 Bean 定义。
	 * <p>注意：".xml" 文件按 XML 内容解析；其他类型资源按 Groovy 脚本解析。
	 * @param resources 要加载的一个或多个资源
	 */
	public void load(Resource... resources) {
		this.reader.loadBeanDefinitions(resources);
	}

	/**
	 * 从给定 Groovy 脚本或 XML 文件加载 Bean 定义。
	 * <p>注意：".xml" 文件按 XML 内容解析；其他类型资源按 Groovy 脚本解析。
	 * @param resourceLocations 要加载的一个或多个资源位置
	 */
	public void load(String... resourceLocations) {
		this.reader.loadBeanDefinitions(resourceLocations);
	}

	/**
	 * 从给定 Groovy 脚本或 XML 文件加载 Bean 定义。
	 * <p>注意：".xml" 文件按 XML 内容解析；其他类型资源按 Groovy 脚本解析。
	 * @param relativeClass 加载各资源名时用作包前缀的类
	 * @param resourceNames 相对限定资源名
	 */
	public void load(Class<?> relativeClass, String... resourceNames) {
		Resource[] resources = new Resource[resourceNames.length];
		for (int i = 0; i < resourceNames.length; i++) {
			resources[i] = new ClassPathResource(resourceNames[i], relativeClass);
		}
		load(resources);
	}


	// GroovyObject 接口实现

	@Override
	public void setMetaClass(MetaClass metaClass) {
		this.metaClass = metaClass;
	}

	@Override
	public MetaClass getMetaClass() {
		return this.metaClass;
	}

	@Override
	public Object invokeMethod(String name, Object args) {
		return this.metaClass.invokeMethod(this, name, args);
	}

	@Override
	public void setProperty(String property, Object newValue) {
		if (newValue instanceof BeanDefinition beanDefinition) {
			registerBeanDefinition(property, beanDefinition);
		}
		else {
			this.metaClass.setProperty(this, property, newValue);
		}
	}

	@Override
	public @Nullable Object getProperty(String property) {
		if (containsBean(property)) {
			return getBean(property);
		}
		else if (this.contextWrapper.isReadableProperty(property)) {
			return this.contextWrapper.getPropertyValue(property);
		}
		throw new NoSuchBeanDefinitionException(property);
	}

}
