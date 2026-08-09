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

package org.springframework.jdbc.support;

import java.sql.DatabaseMetaData;
import java.util.Collections;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.PatternMatchUtils;
import org.springframework.util.StringUtils;

/**
 * 用于基于取自 {@link java.sql.DatabaseMetaData} 的“databaseProductName”创建 {@link SQLErrorCodes}
 * 的工厂。
 * <p>返回 {@code SQLErrorCodes}，其中填充了名为“sql-error-codes.xml”的配置文件中定义的供应商代码。如果没有被类路径根目录中的文件覆盖
 * （例如在“/WEB-INF/classes”目录中），则读取此包中的默认文件。
 * @author Thomas Risberg
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see java.sql.DatabaseMetaData#getDatabaseProductName()
 */
public class SQLErrorCodesFactory {

	/**
	 * 自定义 SQL 错误代码文件的名称，从类路径的根目录加载（例如，从“/WEB-INF/classes”目录）。
	 */
	public static final String SQL_ERROR_CODE_OVERRIDE_PATH = "sql-error-codes.xml";

	/**
	 * 默认 SQL 错误代码文件的名称，从类路径加载。
	 */
	public static final String SQL_ERROR_CODE_DEFAULT_PATH = "org/springframework/jdbc/support/sql-error-codes.xml";


	/**
	 * 获取 Log（`Log`）。
	 */
	private static final Log logger = LogFactory.getLog(SQLErrorCodesFactory.class);

	/**
	 * 跟踪单个实例，以便我们可以将其返回给请求它的类。延迟初始化以避免在不需要时使 {@code SQLErrorCodesFactory} 构造函数在本机映像上可访问。
	 */
	private static @Nullable SQLErrorCodesFactory instance;


	/**
	 * 返回单例实例。
	 */
	public static SQLErrorCodesFactory getInstance() {
		if (instance == null) {
			instance = new SQLErrorCodesFactory();
		}
		return instance;
	}


	/**
	 * 映射以保存配置文件中定义的所有数据库的错误代码。键是数据库产品名称，值是 SQLErrorCodes 实例。
	 */
	private final Map<String, SQLErrorCodes> errorCodesMap;

	/**
	 * 映射以缓存每个数据源的 SQLErrorCodes 实例。
	 */
	private final Map<DataSource, SQLErrorCodes> dataSourceCache = new ConcurrentReferenceHashMap<>(16);


	/**
	 * 创建 {@link SQLErrorCodesFactory} 类的新实例。 <p>不公开以强制实施单例设计模式。除了允许通过覆盖 {@link #loadResource(S
	 * tring)} 方法进行测试之外，将是私有的。 <p><b>不要在应用程序代码中子类化。</b>
	 * @see #loadResource(String)
	 */
	protected SQLErrorCodesFactory() {

		Map<String, SQLErrorCodes> errorCodes;

		try {
			DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
			lbf.setBeanClassLoader(getClass().getClassLoader());
			XmlBeanDefinitionReader bdr = new XmlBeanDefinitionReader(lbf);

			// 加载默认的 SQL 错误代码。
			Resource resource = loadResource(SQL_ERROR_CODE_DEFAULT_PATH);
			if (resource != null && resource.exists()) {
				bdr.loadBeanDefinitions(resource);
			}
			else {
				logger.info("Default sql-error-codes.xml not found (should be included in spring-jdbc jar)");
			}

			// 加载自定义 SQL 错误代码，覆盖默认值。
			resource = loadResource(SQL_ERROR_CODE_OVERRIDE_PATH);
			if (resource != null && resource.exists()) {
				bdr.loadBeanDefinitions(resource);
				logger.debug("Found custom sql-error-codes.xml file at the root of the classpath");
			}

			// 检查 SQLErrorCodes 类型的所有 bean。
			errorCodes = lbf.getBeansOfType(SQLErrorCodes.class, true, false);
			if (logger.isTraceEnabled()) {
				logger.trace("SQLErrorCodes loaded: " + errorCodes.keySet());
			}
		}
		catch (BeansException ex) {
			logger.warn("Error loading SQL error codes from config file", ex);
			errorCodes = Collections.emptyMap();
		}

		this.errorCodesMap = errorCodes;
	}

	/**
	 * 从类路径加载给定的资源。 <p><b> 不应被应用程序开发人员覆盖，应用程序开发人员应从静态 {@link #getInstance()} 方法获取此类的实例。 </b> <p
	 * > 受保护以实现可测试性。
	 * @param path 资源路径；自定义路径或者 {@link #SQL_ERROR_CODE_DEFAULT_PATH} 或 {@link #SQL_ERROR_CODE_OVERRIDE_PATH} 之一。
	 * @return 资源，如果未找到资源则为 {@code null}
	 * @see #getInstance
	 */
	protected @Nullable Resource loadResource(String path) {
		return new ClassPathResource(path, getClass().getClassLoader());
	}


	/**
	 * 返回给定数据库的 {@link SQLErrorCodes} 实例。 <p>不需要数据库元数据查找。
	 * @param databaseName 数据库名称（不得为 {@code null}）
	 * @return 给定数据库的 {@code SQLErrorCodes} 实例（绝不是 {@code null}；可能为空）
	 * @throws IllegalArgumentException 如果提供的数据库名称是 {@code null}
	 */
	public SQLErrorCodes getErrorCodes(String databaseName) {
		Assert.notNull(databaseName, "Database product name must not be null");

		SQLErrorCodes sec = this.errorCodesMap.get(databaseName);
		if (sec == null) {
			for (SQLErrorCodes candidate : this.errorCodesMap.values()) {
				if (PatternMatchUtils.simpleMatch(candidate.getDatabaseProductNames(), databaseName)) {
					sec = candidate;
					break;
				}
			}
		}
		if (sec != null) {
			checkCustomTranslatorRegistry(databaseName, sec);
			if (logger.isDebugEnabled()) {
				logger.debug("SQL error codes for '" + databaseName + "' found");
			}
			return sec;
		}

		// 无法在定义的数据库中找到该数据库。
		if (logger.isDebugEnabled()) {
			logger.debug("SQL error codes for '" + databaseName + "' not found");
		}
		return new SQLErrorCodes();
	}

	/**
	 * 返回给定 {@link DataSource} 的 {@link SQLErrorCodes}，评估 {@link java.sql.DatabaseMetaData}
	 * 中的“databaseProductName”，如果未找到 {@code SQLErrorCodes}，则返回空错误代码实例。
	 * @param dataSource 标识数据库的 {@code DataSource}
	 * @return 对应的 {@code SQLErrorCodes} 对象（绝不是 {@code null}；可能为空）
	 * @see java.sql.DatabaseMetaData#getDatabaseProductName()
	 */
	public SQLErrorCodes getErrorCodes(DataSource dataSource) {
		SQLErrorCodes sec = resolveErrorCodes(dataSource);
		return (sec != null ? sec : new SQLErrorCodes());
	}

	/**
	 * 返回给定 {@link DataSource} 的 {@link SQLErrorCodes}，评估 {@link java.sql.DatabaseMetaData}
	 * 中的“databaseProductName”，如果出现 JDBC 元数据访问问题，则返回 {@code null}。
	 * @param dataSource 标识数据库的 {@code DataSource}
	 * @return 对应的 {@code SQLErrorCodes} 对象，或 {@code null}（如果出现 JDBC 元数据访问问题）
	 * @since 5.2.9
	 * @see java.sql.DatabaseMetaData#getDatabaseProductName()
	 */
	public @Nullable SQLErrorCodes resolveErrorCodes(DataSource dataSource) {
		Assert.notNull(dataSource, "DataSource must not be null");
		if (logger.isDebugEnabled()) {
			logger.debug("Looking up default SQLErrorCodes for DataSource [" + identify(dataSource) + "]");
		}

		// 尝试对现有缓存条目进行高效的无锁访问
		SQLErrorCodes sec = this.dataSourceCache.get(dataSource);
		if (sec == null) {
			synchronized (this.dataSourceCache) {
				// 在完整的 dataSourceCache 锁内进行双重检查
				sec = this.dataSourceCache.get(dataSource);
				if (sec == null) {
					// 我们找不到它 - 必须查找它。
					try {
						String name = JdbcUtils.extractDatabaseMetaData(dataSource,
								DatabaseMetaData::getDatabaseProductName);
						if (StringUtils.hasLength(name)) {
							return registerDatabase(dataSource, name);
						}
					}
					catch (MetaDataAccessException ex) {
						logger.warn("Error while extracting database name", ex);
					}
					return null;
				}
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("SQLErrorCodes found in cache for DataSource [" + identify(dataSource) + "]");
		}

		return sec;
	}

	/**
	 * 将指定的数据库名称与给定的 {@link DataSource} 关联。
	 * @param dataSource 标识数据库的 {@code DataSource}
	 * @param databaseName 错误代码定义文件中指定的相应数据库名称（不得为 {@code null}）
	 * @return 对应的 {@code SQLErrorCodes} 对象（绝不是 {@code null}）
	 * @see #unregisterDatabase(DataSource)
	 */
	public SQLErrorCodes registerDatabase(DataSource dataSource, String databaseName) {
		SQLErrorCodes sec = getErrorCodes(databaseName);
		if (logger.isDebugEnabled()) {
			logger.debug("Caching SQL error codes for DataSource [" + identify(dataSource) +
					"]: database product name is '" + databaseName + "'");
		}
		this.dataSourceCache.put(dataSource, sec);
		return sec;
	}

	/**
	 * 清除指定 {@link DataSource} 的缓存（如果已注册）。
	 * @param dataSource 标识数据库的 {@code DataSource}
	 * @return 已删除的相应 {@code SQLErrorCodes} 对象，或 {@code null}（如果未注册）
	 * @since 4.3.5
	 * @see #registerDatabase(DataSource, String)
	 */
	public @Nullable SQLErrorCodes unregisterDatabase(DataSource dataSource) {
		return this.dataSourceCache.remove(dataSource);
	}

	/**
	 * 为给定的 {@link DataSource} 构建标识字符串，主要用于记录目的。
	 * @param dataSource {@code DataSource} 进行反思
	 * @return 识别字符串
	 */
	private String identify(DataSource dataSource) {
		return dataSource.getClass().getName() + '@' + Integer.toHexString(dataSource.hashCode());
	}

	/**
	 * 检查 {@link CustomSQLExceptionTranslatorRegistry} 中是否有任何条目。
	 */
	private void checkCustomTranslatorRegistry(String databaseName, SQLErrorCodes errorCodes) {
		SQLExceptionTranslator customTranslator =
				CustomSQLExceptionTranslatorRegistry.getInstance().findTranslatorForDatabase(databaseName);
		if (customTranslator != null) {
			if (errorCodes.getCustomSqlExceptionTranslator() != null && logger.isDebugEnabled()) {
				logger.debug("Overriding already defined custom translator '" +
						errorCodes.getCustomSqlExceptionTranslator().getClass().getSimpleName() +
						" with '" + customTranslator.getClass().getSimpleName() +
						"' found in the CustomSQLExceptionTranslatorRegistry for database '" + databaseName + "'");
			}
			else if (logger.isTraceEnabled()) {
				logger.trace("Using custom translator '" + customTranslator.getClass().getSimpleName() +
						"' found in the CustomSQLExceptionTranslatorRegistry for database '" + databaseName + "'");
			}
			errorCodes.setCustomSqlExceptionTranslator(customTranslator);
		}
	}

}
