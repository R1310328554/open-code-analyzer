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

/* ===== [OCA 中文解析] =====
class CallMetaDataProviderFactory — 意图说明

工厂：封装复杂创建逻辑；源文件: `spring-jdbc/src/main/java/org/springframework/jdbc/core/metadata/CallMetaDataProviderFactory.java`

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
===== [OCA 中文解析结束] ===== */
/**
 * Factory used to create a {@link CallMetaDataProvider} implementation
 * based on the type of database being used.
 *
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 2.5
 */
public final class CallMetaDataProviderFactory {

	// [OCA] 字段 `DB2`：类成员状态。
	private static final String DB2 = "DB2";
	// [OCA] 字段 `DERBY`：类成员状态。
	private static final String DERBY = "Apache Derby";
	// [OCA] 字段 `HANA`：类成员状态。
	private static final String HANA = "HDB";
	// [OCA] 字段 `INFORMIX`：类成员状态。
	private static final String INFORMIX = "Informix Dynamic Server";
	// [OCA] 字段 `MARIA`：类成员状态。
	private static final String MARIA = "MariaDB";
	// [OCA] 字段 `MS_SQL_SERVER`：类成员状态。
	private static final String MS_SQL_SERVER = "Microsoft SQL Server";
	// [OCA] 字段 `MYSQL`：类成员状态。
	private static final String MYSQL = "MySQL";
	// [OCA] 字段 `ORACLE`：类成员状态。
	private static final String ORACLE = "Oracle";
	// [OCA] 字段 `POSTGRES`：类成员状态。
	private static final String POSTGRES = "PostgreSQL";
	// [OCA] 字段 `SYBASE`：类成员状态。
	private static final String SYBASE = "Sybase";

	// [OCA] 字段 `supportedDatabaseProductsForProcedures`：类成员状态。
	/** List of supported database products for procedure calls. */
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

	// [OCA] 字段 `supportedDatabaseProductsForFunctions`：类成员状态。
	/** List of supported database products for function calls. */
	public static final List<String> supportedDatabaseProductsForFunctions = List.of(
			MARIA,
			MS_SQL_SERVER,
			MYSQL,
			ORACLE,
			POSTGRES
		);

	// [OCA] 字段 `logger`：类成员状态。
	private static final Log logger = LogFactory.getLog(CallMetaDataProviderFactory.class);


	private CallMetaDataProviderFactory() {
	}


	/* ===== [OCA 中文解析] =====
方法 createMetaDataProvider — 意图与阅读要点

方法 `createMetaDataProvider` 复杂度较高（CCN≈19, NLOC≈57）。阅读时建议先抓住主路径，再看分支/异常/缓存等旁路逻辑；关注它在调用链中上下游的契约（入参约束、返回值语义、抛出的异常）。
	===== [OCA 中文解析结束] ===== */
	/**
	 * Create a {@link CallMetaDataProvider} based on the database meta-data.
	 * @param dataSource the JDBC DataSource to use for retrieving meta-data
	 * @param context the class that holds configuration and meta-data
	 * @return instance of the CallMetaDataProvider implementation to be used
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
