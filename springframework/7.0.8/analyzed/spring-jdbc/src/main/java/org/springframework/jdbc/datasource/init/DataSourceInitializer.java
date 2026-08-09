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
 * 用于初始化期间的 {@linkplain #setDatabasePopulator set up} 数据库和销毁期间的 {@link #setDatabaseCleaner
 * clean up} 数据库。
 * @author Dave Syer
 * @author Sam Brannen
 * @since 3.0
 * @see DatabasePopulator
 */
public class DataSourceInitializer implements InitializingBean, DisposableBean {

	/** 来源相关状态（`dataSource`）。 */
	private @Nullable DataSource dataSource;

	/** `databasePopulator`：该类的成员状态。 */
	private @Nullable DatabasePopulator databasePopulator;

	/** `databaseCleaner`：该类的成员状态。 */
	private @Nullable DatabasePopulator databaseCleaner;

	/** `true`：该类的成员状态。 */
	private boolean enabled = true;


	/**
	 * {@link DataSource}，用于在初始化此组件时填充数据库并在关闭此组件时清除数据库。 <p>此属性是强制性的，没有提供默认值。
	 * @param dataSource 数据源
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * 将 {@link DatabasePopulator} 设置为在 bean 初始化阶段执行（如果有）。
	 * @param databasePopulator 初始化期间使用的 {@code DatabasePopulator}
	 * @see #setDatabaseCleaner
	 */
	public void setDatabasePopulator(@Nullable DatabasePopulator databasePopulator) {
		this.databasePopulator = databasePopulator;
	}

	/**
	 * 将 {@link DatabasePopulator} 设置为在 bean 销毁阶段执行（如果有），清理数据库并使其处于其他人已知的状态。
	 * @param databaseCleaner 销毁期间使用的 {@code DatabasePopulator}
	 * @see #setDatabasePopulator
	 */
	public void setDatabaseCleaner(@Nullable DatabasePopulator databaseCleaner) {
		this.databaseCleaner = databaseCleaner;
	}

	/**
	 * 用于显式启用或禁用 {@linkplain #setDatabasePopulator database populator} 和 {@linkplain
	 * #setDatabaseCleaner database cleaner} 的标记。
	 * @param enabled {@code true} 是否应在启动和关闭时分别调用数据库填充器和数据库清理器
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}


	/**
	 * 使用 {@linkplain #setDatabasePopulator database populator} 设置数据库。
	 */
	@Override
	public void afterPropertiesSet() {
		execute(this.databasePopulator);
	}

	/**
	 * 使用 {@linkplain #setDatabaseCleaner database cleaner} 清理数据库。
	 */
	@Override
	public void destroy() {
		execute(this.databaseCleaner);
	}

	/**
	 * 执行（方法 `execute`）。
	 */
	private void execute(@Nullable DatabasePopulator populator) {
		Assert.state(this.dataSource != null, "DataSource must be set");
		if (this.enabled && populator != null) {
			DatabasePopulatorUtils.execute(populator, this.dataSource);
		}
	}

}
