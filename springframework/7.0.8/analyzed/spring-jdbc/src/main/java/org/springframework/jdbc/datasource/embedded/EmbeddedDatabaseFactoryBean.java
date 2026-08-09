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
 * {@link EmbeddedDatabaseFactory} 的子类，实现 {@link FactoryBean} 以注册为 Spring bean。返回实际的
 * {@link DataSource}，该 {@link DataSource} 提供与 Spring 的嵌入式数据库的连接。
 * <p> 返回目标 {@link DataSource} 而不是 {@link EmbeddedDatabase} 代理，因为 {@link FactoryBean}
 * 将管理嵌入式数据库实例的初始化和销毁​​生命周期。
 * <p>I 实现 {@link DisposableBean} 以在关闭管理 Spring 容器时关闭嵌入式数据库。
 * @author Keith Donald
 * @author Juergen Hoeller
 * @since 3.0
 */
public class EmbeddedDatabaseFactoryBean extends EmbeddedDatabaseFactory
		implements FactoryBean<DataSource>, InitializingBean, DisposableBean {

	/** `databaseCleaner`：该类的成员状态。 */
	private @Nullable DatabasePopulator databaseCleaner;


	/**
	 * 设置要在 bean 销毁回调中运行的脚本执行，清理数据库并使其处于其他人已知的状态。
	 * @param databaseCleaner 在销毁时运行的数据库脚本执行器
	 * @see #setDatabasePopulator
	 * @see org.springframework.jdbc.datasource.init.DataSourceInitializer#setDatabaseCleaner
	 */
	public void setDatabaseCleaner(DatabasePopulator databaseCleaner) {
		this.databaseCleaner = databaseCleaner;
	}

	/**
	 * 在…之后回调：Properties Set（方法 `afterPropertiesSet`）。
	 */
	@Override
	public void afterPropertiesSet() {
		initDatabase();
	}


	/**
	 * 获取 Object（`Object`）。
	 */
	@Override
	public @Nullable DataSource getObject() {
		return getDataSource();
	}

	/**
	 * 获取 Object Type（`ObjectType`）。
	 */
	@Override
	public Class<? extends DataSource> getObjectType() {
		return DataSource.class;
	}

	/**
	 * 判断是否 Singleton。
	 */
	@Override
	public boolean isSingleton() {
		return true;
	}


	/**
	 * 销毁（方法 `destroy`）。
	 */
	@Override
	public void destroy() {
		DatabasePopulator cleaner = this.databaseCleaner;
		if (cleaner != null && getDataSource() != null) {
			DatabasePopulatorUtils.execute(cleaner, getDataSource());
		}
		shutdownDatabase();
	}

}
