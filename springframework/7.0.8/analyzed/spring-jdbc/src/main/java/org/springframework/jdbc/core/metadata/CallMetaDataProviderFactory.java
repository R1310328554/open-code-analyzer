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

package org.springframework.jdbc.core.metadata;

import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;

/**
 * 根据所用数据库类型创建 {@link CallMetaDataProvider} 实现的工厂。
 *
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 2.5
 */
public final class CallMetaDataProviderFactory {

	private static final String DB2 = "DB2";
	private static final String DERBY = "Apache Derby";
	private static final String HANA = "HDB";
	private static final String INFORMIX = "Informix Dynamic Server";
	private static final String MARIA = "MariaDB";
	private static final String MS_SQL_SERVER = "Microsoft SQL Server";
	private static final String MYSQL = "MySQL";
	private static final String ORACLE = "Oracle";
	private static final String POSTGRES = "PostgreSQL";
	private static final String SYBASE = "Sybase";

	/** 支持存储过程调用的数据库产品列表。 */
	public static final List<String> supportedDatabaseProductsForProcedures = List.of(
			DERBY,
			DB2,
			INFORMIX,
			MARIA,
			MS_SQL_SERVER,
			MYSQL,
			ORACLE,
			POSTGRES,
			SYBASE
		);

	/** 支持函数调用的数据库产品列表。 */
	public static final List<String> supportedDatabaseProductsForFunctions = List.of(
			MARIA,
			MS_SQL_SERVER,
			MYSQL,
			ORACLE,
			POSTGRES
		);

	private static final Log logger = LogFactory.getLog(CallMetaDataProviderFactory.class);


	private CallMetaDataProviderFactory() {
	}


	/**
	 * 根据数据库元数据创建 {@link CallMetaDataProvider}。
	 * @param dataSource 用于获取元数据的 JDBC DataSource
	 * @param context 持有配置与元数据的上下文类
	 * @return 要使用的 CallMetaDataProvider 实现实例
	 */
	public static CallMetaDataProvider createMetaDataProvider(DataSource dataSource, final CallMetaDataContext context) {
		try {
			return JdbcUtils.extractDatabaseMetaData(dataSource, databaseMetaData -> {
				String databaseProductName = JdbcUtils.commonDatabaseName(databaseMetaData.getDatabaseProductName());
				if (databaseProductName == null) {
					databaseProductName = "";
				}

				boolean accessProcedureColumnMetaData = context.isAccessCallParameterMetaData();
				if (context.isFunction()) {
					if (!supportedDatabaseProductsForFunctions.contains(databaseProductName)) {
						if (logger.isInfoEnabled()) {
							logger.info(databaseProductName + " is not one of the databases fully supported for function calls " +
									"-- supported are: " + supportedDatabaseProductsForFunctions);
						}
						if (accessProcedureColumnMetaData) {
							logger.info("Metadata processing disabled - you must specify all parameters explicitly");
							accessProcedureColumnMetaData = false;
						}
					}
				}
				else {
					if (!supportedDatabaseProductsForProcedures.contains(databaseProductName)) {
						if (logger.isInfoEnabled()) {
							logger.info(databaseProductName + " is not one of the databases fully supported for procedure calls " +
									"-- supported are: " + supportedDatabaseProductsForProcedures);
						}
						if (accessProcedureColumnMetaData) {
							logger.info("Metadata processing disabled - you must specify all parameters explicitly");
							accessProcedureColumnMetaData = false;
						}
					}
				}

				CallMetaDataProvider provider = switch (databaseProductName) {
					case ORACLE -> new OracleCallMetaDataProvider(databaseMetaData);
					case POSTGRES -> new PostgresCallMetaDataProvider(databaseMetaData);
					case DERBY -> new DerbyCallMetaDataProvider(databaseMetaData);
					case DB2 -> new Db2CallMetaDataProvider(databaseMetaData);
					case HANA -> new HanaCallMetaDataProvider(databaseMetaData);
					case MS_SQL_SERVER -> new SqlServerCallMetaDataProvider(databaseMetaData);
					case SYBASE -> new SybaseCallMetaDataProvider(databaseMetaData);
					default -> new GenericCallMetaDataProvider(databaseMetaData);
				};

				if (logger.isDebugEnabled()) {
					logger.debug("Using " + provider.getClass().getName());
				}
				provider.initializeWithMetaData(databaseMetaData);
				if (accessProcedureColumnMetaData) {
					provider.initializeWithProcedureColumnMetaData(databaseMetaData,
							context.getCatalogName(), context.getSchemaName(), context.getProcedureName());
				}
				return provider;
			});
		}
		catch (MetaDataAccessException ex) {
			throw new DataAccessResourceFailureException("Error retrieving database meta-data", ex);
		}
	}

}
