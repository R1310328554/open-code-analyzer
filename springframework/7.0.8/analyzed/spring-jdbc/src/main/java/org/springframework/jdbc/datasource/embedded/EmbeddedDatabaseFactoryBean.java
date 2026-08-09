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

package org.springframework.jdbc.datasource.embedded;

import javax.sql.DataSource;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;

/**
 * {@link EmbeddedDatabaseFactory} 的子类，实现 {@link FactoryBean}
 * 以便注册为 Spring Bean。向 Spring 返回提供嵌入式数据库连接的
 * 实际 {@link DataSource}。
 *
 * <p>返回目标 {@link DataSource} 而非 {@link EmbeddedDatabase} 代理，
 * 因为 {@link FactoryBean} 将管理嵌入式数据库实例的初始化与销毁生命周期。
 *
 * <p>实现 {@link DisposableBean}，在 Spring 容器关闭时关闭嵌入式数据库。
 *
 * @author Keith Donald
 * @author Juergen Hoeller
 * @since 3.0
 */
public class EmbeddedDatabaseFactoryBean extends EmbeddedDatabaseFactory
		implements FactoryBean<DataSource>, InitializingBean, DisposableBean {

	private @Nullable DatabasePopulator databaseCleaner;


	/**
	 * 设置在 Bean 销毁回调中执行的脚本，
	 * 清理数据库并使其处于已知状态供后续使用。
	 * @param databaseCleaner 销毁时执行的数据库脚本执行器
	 * @see #setDatabasePopulator
	 * @see org.springframework.jdbc.datasource.init.DataSourceInitializer#setDatabaseCleaner
	 */
	public void setDatabaseCleaner(DatabasePopulator databaseCleaner) {
		this.databaseCleaner = databaseCleaner;
	}

	@Override
	public void afterPropertiesSet() {
		initDatabase();
	}


	@Override
	public @Nullable DataSource getObject() {
		return getDataSource();
	}

	@Override
	public Class<? extends DataSource> getObjectType() {
		return DataSource.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}


	@Override
	public void destroy() {
		DatabasePopulator cleaner = this.databaseCleaner;
		if (cleaner != null && getDataSource() != null) {
			DatabasePopulatorUtils.execute(cleaner, getDataSource());
		}
		shutdownDatabase();
	}

}
