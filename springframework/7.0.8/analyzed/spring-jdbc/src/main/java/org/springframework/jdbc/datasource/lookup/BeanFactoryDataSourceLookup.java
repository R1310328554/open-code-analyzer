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

package org.springframework.jdbc.datasource.lookup;

import javax.sql.DataSource;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.util.Assert;

/**
 * 基于 Spring {@link BeanFactory} 的 {@link DataSourceLookup} 实现。
 *
 * <p>按 Bean 名称查找 Spring 管理的 Bean，
 * 期望其类型为 {@code javax.sql.DataSource}。
 *
 * @author Costin Leau
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.beans.factory.BeanFactory
 */
public class BeanFactoryDataSourceLookup implements DataSourceLookup, BeanFactoryAware {

	private @Nullable BeanFactory beanFactory;


	/**
	 * 创建 {@link BeanFactoryDataSourceLookup} 的新实例。
	 * <p>须通过 {@code setBeanFactory} 设置要访问的 BeanFactory。
	 * @see #setBeanFactory
	 */
	public BeanFactoryDataSourceLookup() {
	}

	/**
	 * 创建 {@link BeanFactoryDataSourceLookup} 的新实例。
	 * <p>若由 Spring IoC 容器创建此对象，使用此构造函数是多余的，
	 * 所供 {@link BeanFactory} 将被创建它的 {@link BeanFactory} 替换
	 * （参见 {@link BeanFactoryAware} 契约）。
	 * 仅在本类用于 Spring IoC 容器外部时使用此构造函数。
	 * @param beanFactory 用于查找 {@link DataSource DataSources} 的 Bean 工厂
	 */
	public BeanFactoryDataSourceLookup(BeanFactory beanFactory) {
		Assert.notNull(beanFactory, "BeanFactory is required");
		this.beanFactory = beanFactory;
	}


	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}


	@Override
	public DataSource getDataSource(String dataSourceName) throws DataSourceLookupFailureException {
		Assert.state(this.beanFactory != null, "BeanFactory is required");
		try {
			return this.beanFactory.getBean(dataSourceName, DataSource.class);
		}
		catch (BeansException ex) {
			throw new DataSourceLookupFailureException(
					"Failed to look up DataSource bean with name '" + dataSourceName + "'", ex);
		}
	}

}
