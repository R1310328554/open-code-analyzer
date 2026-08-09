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
 * Bean 定义读取器，根据给定的 SQL 语句从数据库表中读取值。
 * <p> 期望 bean 名称、属性名称和值的列为字符串。每个格式都与 PropertiesBeanDefinitionReader 识别的属性格式相同。
 * <p><b>NOTE:</b> 这主要用作基于 JDBC 的自定义 bean 定义阅读器的示例。它并不旨在提供全面的功能。
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #loadBeanDefinitions
 * @deprecated 支持 Spring 的通用 bean 定义格式和/或自定义 BeanDefinitionReader 实现
 */
@Deprecated(since = "5.3")
public class JdbcBeanDefinitionReader {

	/** `propReader`：该类的成员状态。 */
	private final org.springframework.beans.factory.support.PropertiesBeanDefinitionReader propReader;

	/** 模板相关状态（`jdbcTemplate`）。 */
	private @Nullable JdbcTemplate jdbcTemplate;


	/**
	 * 使用下面的默认 PropertiesBeanDefinitionReader 为给定的 bean 工厂创建一个新的 JdbcBeanDefinitionReader。
	 * <p>DataSource 或 JdbcTemplate 仍需要设置。
	 * @see #setDataSource
	 * @see #setJdbcTemplate
	 */
	public JdbcBeanDefinitionReader(BeanDefinitionRegistry beanFactory) {
		this.propReader = new org.springframework.beans.factory.support.PropertiesBeanDefinitionReader(beanFactory);
	}

	/**
	 * 创建一个新的 JdbcBeanDefinitionReader，委托给下面给定的 PropertiesBeanDefinitionReader。 <p>DataSource
	 * 或 JdbcTemplate 仍需要设置。
	 * @see #setDataSource
	 * @see #setJdbcTemplate
	 */
	public JdbcBeanDefinitionReader(org.springframework.beans.factory.support.PropertiesBeanDefinitionReader reader) {
		Assert.notNull(reader, "Bean definition reader must not be null");
		this.propReader = reader;
	}


	/**
	 * 设置用于获取数据库连接的数据源。将使用给定的 DataSource 隐式创建一个新的 JdbcTemplate。
	 */
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	/**
	 * 设置该 bean 工厂要使用的 JdbcTemplate。包含数据源、SQLExceptionTranslator 等的设置。
	 */
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		Assert.notNull(jdbcTemplate, "JdbcTemplate must not be null");
		this.jdbcTemplate = jdbcTemplate;
	}


	/**
	 * 通过给定的 SQL 字符串从数据库加载 Bean 定义。
	 * @param sql 用于加载 bean 定义的 SQL 查询。前三列必须是 bean 名称、属性名称和值。允许任何联接和任何其他列：例如 {@code SELECT BEAN_NAME, PROPERTY, VALUE FROM CONFIG WHERE CONFIG.APP_ID = 1} 也可以执行联接。列名称并不重要——只有前三列的顺序。
	 */
	public void loadBeanDefinitions(String sql) {
		Assert.notNull(this.jdbcTemplate, "Not fully configured - specify DataSource or JdbcTemplate");
		final Properties props = new Properties();
		this.jdbcTemplate.query(sql, rs -> {
			String beanName = rs.getString(1);
			String property = rs.getString(2);
			String value = rs.getString(3);
			// 通过组合 bean 名称和属性来创建属性条目。
			props.setProperty(beanName + '.' + property, value);
		});
		this.propReader.registerBeanDefinitions(props);
	}

}
