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

package org.springframework.jdbc.datasource.init;

import javax.sql.DataSource;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * 在初始化阶段 {@linkplain #setDatabasePopulator 设置} 数据库，
 * 在销毁阶段 {@link #setDatabaseCleaner 清理} 数据库。
 *
 * @author Dave Syer
 * @author Sam Brannen
 * @since 3.0
 * @see DatabasePopulator
 */
public class DataSourceInitializer implements InitializingBean, DisposableBean {

	private @Nullable DataSource dataSource;

	private @Nullable DatabasePopulator databasePopulator;

	private @Nullable DatabasePopulator databaseCleaner;

	private boolean enabled = true;


	/**
	 * 本组件初始化时要填充、关闭时要清理的数据库 {@link DataSource}。
	 * <p>此属性为必填，无默认值。
	 * @param dataSource DataSource
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * 设置 Bean 初始化阶段要执行的 {@link DatabasePopulator}（若有）。
	 * @param databasePopulator 初始化时使用的 {@code DatabasePopulator}
	 * @see #setDatabaseCleaner
	 */
	public void setDatabasePopulator(@Nullable DatabasePopulator databasePopulator) {
		this.databasePopulator = databasePopulator;
	}

	/**
	 * 设置 Bean 销毁阶段要执行的 {@link DatabasePopulator}（若有），
	 * 用于清理数据库并使其处于已知状态。
	 * @param databaseCleaner 销毁时使用的 {@code DatabasePopulator}
	 * @see #setDatabasePopulator
	 */
	public void setDatabaseCleaner(@Nullable DatabasePopulator databaseCleaner) {
		this.databaseCleaner = databaseCleaner;
	}

	/**
	 * 显式启用或禁用 {@linkplain #setDatabasePopulator 数据库填充器}
	 * 与 {@linkplain #setDatabaseCleaner 数据库清理器}。
	 * @param enabled 若为 {@code true}，启动时调用填充器、关闭时调用清理器
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}


	/**
	 * 使用 {@linkplain #setDatabasePopulator 数据库填充器} 设置数据库。
	 */
	@Override
	public void afterPropertiesSet() {
		execute(this.databasePopulator);
	}

	/**
	 * 使用 {@linkplain #setDatabaseCleaner 数据库清理器} 清理数据库。
	 */
	@Override
	public void destroy() {
		execute(this.databaseCleaner);
	}

	private void execute(@Nullable DatabasePopulator populator) {
		Assert.state(this.dataSource != null, "DataSource must be set");
		if (this.enabled && populator != null) {
			DatabasePopulatorUtils.execute(populator, this.dataSource);
		}
	}

}
