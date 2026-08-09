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

package org.springframework.beans.factory.config;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.core.env.AbstractPropertyResolver;
import org.springframework.util.StringValueResolver;
import org.springframework.util.SystemPropertyUtils;

/**
 * 用于解析 Bean 定义属性值中占位符的属性资源配置器抽象基类。
 * 实现类从属性文件或其他 {@linkplain org.springframework.core.env.PropertySource
 * 属性源} <em>拉取</em>值到 Bean 定义中。
 *
 * <p>默认占位符语法遵循 Ant / Log4J / JSP EL 风格：
 *
 * <pre class="code">${...}</pre>
 *
 * XML Bean 定义示例：
 *
 * <pre class="code">
 * &lt;bean id="dataSource" class="org.springframework.jdbc.datasource.DriverManagerDataSource"&gt;
 *   &lt;property name="driverClassName" value="${jdbc.driver}" /&gt;
 *   &lt;property name="url" value="jdbc:${jdbc.dbname}" /&gt;
 * &lt;/bean&gt;
 * </pre>
 *
 * 属性文件示例：
 *
 * <pre class="code">
 * jdbc.driver=com.mysql.jdbc.Driver
 * jdbc.dbname=mysql:mydb</pre>
 *
 * 带注解的 Bean 定义可通过
 * {@link org.springframework.beans.factory.annotation.Value @Value} 注解利用属性替换：
 *
 * <pre class="code">@Value("${person.age}")</pre>
 *
 * 实现类会检查简单属性值、列表、映射、props 以及 Bean 引用中的 Bean 名称。
 * 此外，占位符值也可交叉引用其他占位符，例如：
 *
 * <pre class="code">
 * rootPath=myrootdir
 * subPath=${rootPath}/subdir</pre>
 *
 * 与 {@link PropertyOverrideConfigurer} 不同，本类型的子类允许填充 Bean 定义中的显式占位符。
 *
 * <p>若配置器无法解析占位符，将抛出 {@link BeanDefinitionStoreException}。
 * 若要对照多个属性文件检查，请通过 {@link #setLocations locations} 属性指定多个资源。
 * 也可定义多个配置器，每个使用<i>各自</i>的占位符语法。
 * 使用 {@link #ignoreUnresolvablePlaceholders} 可在占位符无法解析时故意抑制异常。
 *
 * <p>可通过 {@link #setProperties properties} 属性为每个配置器实例全局定义默认属性值，
 * 或使用值分隔符（默认为 {@code ":"}，可通过 {@link #setValueSeparator(String)} 自定义）
 * 按属性逐项定义。
 *
 * <p>带默认值的 XML 属性示例：
 *
 * <pre class="code">
 *   &lt;property name="url" value="jdbc:${jdbc.dbname:defaultdb}" /&gt;
 * </pre>
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 3.1
 * @see PropertyPlaceholderConfigurer
 * @see org.springframework.context.support.PropertySourcesPlaceholderConfigurer
 */
public abstract class PlaceholderConfigurerSupport extends PropertyResourceConfigurer
		implements BeanNameAware, BeanFactoryAware {

	/** 默认占位符前缀：{@value}。 */
	public static final String DEFAULT_PLACEHOLDER_PREFIX = SystemPropertyUtils.PLACEHOLDER_PREFIX;

	/** 默认占位符后缀：{@value}。 */
	public static final String DEFAULT_PLACEHOLDER_SUFFIX = SystemPropertyUtils.PLACEHOLDER_SUFFIX;

	/** 默认值分隔符：{@value}。 */
	public static final String DEFAULT_VALUE_SEPARATOR = SystemPropertyUtils.VALUE_SEPARATOR;

	/**
	 * 默认转义字符：{@code '\'}。
	 * @since 6.2
	 * @see AbstractPropertyResolver#getDefaultEscapeCharacter()
	 */
	public static final Character DEFAULT_ESCAPE_CHARACTER = SystemPropertyUtils.ESCAPE_CHARACTER;


	/** 默认为 {@value #DEFAULT_PLACEHOLDER_PREFIX}。 */
	protected String placeholderPrefix = DEFAULT_PLACEHOLDER_PREFIX;

	/** 默认为 {@value #DEFAULT_PLACEHOLDER_SUFFIX}。 */
	protected String placeholderSuffix = DEFAULT_PLACEHOLDER_SUFFIX;

	/** 默认为 {@value #DEFAULT_VALUE_SEPARATOR}。 */
	protected @Nullable String valueSeparator = DEFAULT_VALUE_SEPARATOR;

	/**
	 * 默认值由 {@link AbstractPropertyResolver#getDefaultEscapeCharacter()} 决定。
	 */
	protected @Nullable Character escapeCharacter = AbstractPropertyResolver.getDefaultEscapeCharacter();

	/** 是否修剪解析后的值。 */
	protected boolean trimValues = false;

	/** 解析为占位符值时应视为 {@code null} 的值。 */
	protected @Nullable String nullValue;

	/** 是否忽略无法解析的占位符。 */
	protected boolean ignoreUnresolvablePlaceholders = false;

	/** 本配置器自身的 Bean 名称。 */
	private @Nullable String beanName;

	/** 本配置器所属的 Bean 工厂。 */
	private @Nullable BeanFactory beanFactory;


	/**
	 * 设置占位符字符串的前缀。
	 * <p>默认为 {@value #DEFAULT_PLACEHOLDER_PREFIX}。
	 */
	public void setPlaceholderPrefix(String placeholderPrefix) {
		this.placeholderPrefix = placeholderPrefix;
	}

	/**
	 * 设置占位符字符串的后缀。
	 * <p>默认为 {@value #DEFAULT_PLACEHOLDER_SUFFIX}。
	 */
	public void setPlaceholderSuffix(String placeholderSuffix) {
		this.placeholderSuffix = placeholderSuffix;
	}

	/**
	 * 指定占位符变量与关联默认值之间的分隔字符，或 {@code null} 表示
	 * 不将任何特殊字符作为值分隔符处理。
	 * <p>默认为 {@value #DEFAULT_VALUE_SEPARATOR}。
	 */
	public void setValueSeparator(@Nullable String valueSeparator) {
		this.valueSeparator = valueSeparator;
	}

	/**
	 * 设置用于忽略 {@linkplain #setPlaceholderPrefix(String) 占位符前缀} 与
	 * {@linkplain #setValueSeparator(String) 值分隔符} 的转义字符，或 {@code null}
	 * 表示不进行转义。
	 * <p>默认值由 {@link AbstractPropertyResolver#getDefaultEscapeCharacter()} 决定。
	 * @since 6.2
	 */
	public void setEscapeCharacter(@Nullable Character escapeCharacter) {
		this.escapeCharacter = escapeCharacter;
	}

	/**
	 * 指定是否在应用前修剪解析后的值，去除首尾多余空白。
	 * <p>默认为 {@code false}。
	 * @since 4.3
	 */
	public void setTrimValues(boolean trimValues) {
		this.trimValues = trimValues;
	}

	/**
	 * 设置解析为占位符值时应视为 {@code null} 的值，例如 ""（空字符串）或 "null"。
	 * <p>注意，这仅适用于完整属性值，不适用于拼接值的部分。
	 * <p>默认未定义此类 null 值。这意味着除非在此显式映射对应值，
	 * 否则无法将 {@code null} 表达为属性值。
	 */
	public void setNullValue(String nullValue) {
		this.nullValue = nullValue;
	}

	/**
	 * 设置是否忽略无法解析的占位符。
	 * <p>默认为 "false"：占位符解析失败时将抛出异常。
	 * 将此标志设为 "true" 可在该情况下原样保留占位符字符串，
	 * 交由其他占位符配置器解析。
	 */
	public void setIgnoreUnresolvablePlaceholders(boolean ignoreUnresolvablePlaceholders) {
		this.ignoreUnresolvablePlaceholders = ignoreUnresolvablePlaceholders;
	}

	/**
	 * 仅需用于检查是否正在解析自身的 Bean 定义，
	 * 以避免属性文件位置中无法解析的占位符导致失败。
	 * 后者可能发生在资源位置中使用系统属性占位符时。
	 * @see #setLocations
	 * @see org.springframework.core.io.ResourceEditor
	 */
	@Override
	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	/**
	 * 仅需用于检查是否正在解析自身的 Bean 定义，
	 * 以避免属性文件位置中无法解析的占位符导致失败。
	 * 后者可能发生在资源位置中使用系统属性占位符时。
	 * @see #setLocations
	 * @see org.springframework.core.io.ResourceEditor
	 */
	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	protected void doProcessProperties(ConfigurableListableBeanFactory beanFactoryToProcess,
			StringValueResolver valueResolver) {

		BeanDefinitionVisitor visitor = new BeanDefinitionVisitor(valueResolver);

		String[] beanNames = beanFactoryToProcess.getBeanDefinitionNames();
		for (String curName : beanNames) {
			// 检查是否正在解析自身的 Bean 定义，
			// 以避免属性文件位置中无法解析的占位符导致失败
			if (!(curName.equals(this.beanName) && beanFactoryToProcess.equals(this.beanFactory))) {
				BeanDefinition bd = beanFactoryToProcess.getBeanDefinition(curName);
				try {
					visitor.visitBeanDefinition(bd);
				}
				catch (Exception ex) {
					throw new BeanDefinitionStoreException(bd.getResourceDescription(), curName, ex.getMessage(), ex);
				}
			}
		}

		// 同时解析别名目标名称与别名中的占位符
		beanFactoryToProcess.resolveAliases(valueResolver);

		// 解析嵌入式值（如注解属性）中的占位符
		beanFactoryToProcess.addEmbeddedValueResolver(valueResolver);
	}

}
