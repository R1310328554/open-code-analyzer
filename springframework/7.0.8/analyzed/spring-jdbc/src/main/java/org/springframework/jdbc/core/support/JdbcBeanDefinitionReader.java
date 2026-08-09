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

package org.springframework.jdbc.core.support;

import java.util.Properties;

import javax.sql.DataSource;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;

/**
 * 基于给定 SQL 语句从数据库表读取值的 Bean 定义读取器。
 *
 * <p>期望三列分别为 bean 名称、属性名和字符串形式的属性值。
 * 各列格式与 PropertiesBeanDefinitionReader 所识别的 properties 格式相同。
 *
 * <p><b>注意：</b> 此类主要作为自定义 JDBC Bean 定义读取器的示例，
 * 并不提供完整功能。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #loadBeanDefinitions
 * @deprecated 建议使用 Spring 通用 Bean 定义格式和/或自定义 BeanDefinitionReader 实现
 */
@Deprecated(since = "5.3")
public class JdbcBeanDefinitionReader {

	private final org.springframework.beans.factory.support.PropertiesBeanDefinitionReader propReader;

	private @Nullable JdbcTemplate jdbcTemplate;


	/**
	 * 为给定 Bean 工厂创建新的 JdbcBeanDefinitionReader，
	 * 底层使用默认 PropertiesBeanDefinitionReader。
	 * <p>仍需设置 DataSource 或 JdbcTemplate。
	 * @see #setDataSource
	 * @see #setJdbcTemplate
	 */
	public JdbcBeanDefinitionReader(BeanDefinitionRegistry beanFactory) {
		this.propReader = new org.springframework.beans.factory.support.PropertiesBeanDefinitionReader(beanFactory);
	}

	/**
	 * 创建新的 JdbcBeanDefinitionReader，委托给给定 PropertiesBeanDefinitionReader。
	 * <p>仍需设置 DataSource 或 JdbcTemplate。
	 * @see #setDataSource
	 * @see #setJdbcTemplate
	 */
	public JdbcBeanDefinitionReader(org.springframework.beans.factory.support.PropertiesBeanDefinitionReader reader) {
		Assert.notNull(reader, "Bean definition reader must not be null");
		this.propReader = reader;
	}


	/**
	 * 设置用于获取数据库连接的 DataSource。
	 * 将隐式使用给定 DataSource 创建新的 JdbcTemplate。
	 */
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	/**
	 * 设置本 Bean 工厂使用的 JdbcTemplate。
	 * 其中包含 DataSource、SQLExceptionTranslator 等配置。
	 */
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		Assert.notNull(jdbcTemplate, "JdbcTemplate must not be null");
		this.jdbcTemplate = jdbcTemplate;
	}


	/**
	 * 通过给定 SQL 从数据库加载 Bean 定义。
	 * @param sql 用于加载 Bean 定义的 SQL 查询。
	 * 前三列必须为 bean 名称、属性名和属性值。
	 * 允许任意 join 及其他列，例如
	 * {@code SELECT BEAN_NAME, PROPERTY, VALUE FROM CONFIG WHERE CONFIG.APP_ID = 1}。
	 * 也可执行 join。列名不重要，仅前三列顺序有意义。
	 */
	public void loadBeanDefinitions(String sql) {
		Assert.notNull(this.jdbcTemplate, "Not fully configured - specify DataSource or JdbcTemplate");
		final Properties props = new Properties();
		this.jdbcTemplate.query(sql, rs -> {
			String beanName = rs.getString(1);
			String property = rs.getString(2);
			String value = rs.getString(3);
			// Make a properties entry by combining bean name and property.
			props.setProperty(beanName + '.' + property, value);
		});
		this.propReader.registerBeanDefinitions(props);
	}

}
