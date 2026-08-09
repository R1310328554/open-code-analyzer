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
 * 基于 {@link java.sql.DatabaseMetaData} 中的 "databaseProductName" 创建 {@link SQLErrorCodes} 的工厂。
 *
 * <p>返回在 "sql-error-codes.xml" 配置文件中定义的厂商错误码填充的 {@code SQLErrorCodes}。
 * 若类路径根目录（如 "/WEB-INF/classes"）未覆盖，则读取本包中的默认文件。
 *
 * @author Thomas Risberg
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see java.sql.DatabaseMetaData#getDatabaseProductName()
 */
public class SQLErrorCodesFactory {

	/**
	 * 自定义 SQL 错误码文件名，从类路径根目录加载（如 "/WEB-INF/classes"）。
	 */
	public static final String SQL_ERROR_CODE_OVERRIDE_PATH = "sql-error-codes.xml";

	/**
	 * 默认 SQL 错误码文件名，从类路径加载。
	 */
	public static final String SQL_ERROR_CODE_DEFAULT_PATH = "org/springframework/jdbc/support/sql-error-codes.xml";


	private static final Log logger = LogFactory.getLog(SQLErrorCodesFactory.class);

	/**
	 * 跟踪单例实例，以便返回给请求的类。
	 * 延迟初始化，避免在不需要时使 {@code SQLErrorCodesFactory} 构造器在 native image 中可达。
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
	 * 保存配置文件中所有数据库错误码的映射，键为数据库产品名，值为 SQLErrorCodes 实例。
	 */
	private final Map<String, SQLErrorCodes> errorCodesMap;

	/**
	 * 按 DataSource 缓存 SQLErrorCodes 实例的映射。
	 */
	private final Map<DataSource, SQLErrorCodes> dataSourceCache = new ConcurrentReferenceHashMap<>(16);


	/**
	 * 创建 {@link SQLErrorCodesFactory} 新实例。
	 * <p>非 public，以强制单例模式。若非为允许通过覆盖 {@link #loadResource(String)} 进行测试，本应为 private。
	 * <p><b>应用代码请勿子类化。</b>
	 * @see #loadResource(String)
	 */
	protected SQLErrorCodesFactory() {

		Map<String, SQLErrorCodes> errorCodes;

		try {
			DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
			lbf.setBeanClassLoader(getClass().getClassLoader());
			XmlBeanDefinitionReader bdr = new XmlBeanDefinitionReader(lbf);

			// Load default SQL error codes.
			Resource resource = loadResource(SQL_ERROR_CODE_DEFAULT_PATH);
			if (resource != null && resource.exists()) {
				bdr.loadBeanDefinitions(resource);
			}
			else {
				logger.info("Default sql-error-codes.xml not found (should be included in spring-jdbc jar)");
			}

			// Load custom SQL error codes, overriding defaults.
			resource = loadResource(SQL_ERROR_CODE_OVERRIDE_PATH);
			if (resource != null && resource.exists()) {
				bdr.loadBeanDefinitions(resource);
				logger.debug("Found custom sql-error-codes.xml file at the root of the classpath");
			}

			// Check all beans of type SQLErrorCodes.
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
	 * 从类路径加载给定资源。
	 * <p><b>应用开发者不应覆盖，应通过静态 {@link #getInstance()} 获取本类实例。</b>
	 * <p>protected 以便测试。
	 * @param path 资源路径，可为自定义路径或 {@link #SQL_ERROR_CODE_DEFAULT_PATH}、{@link #SQL_ERROR_CODE_OVERRIDE_PATH}
	 * @return 资源，未找到时 {@code null}
	 * @see #getInstance
	 */
	protected @Nullable Resource loadResource(String path) {
		return new ClassPathResource(path, getClass().getClassLoader());
	}


	/**
	 * 返回给定数据库的 {@link SQLErrorCodes} 实例，无需数据库元数据查找。
	 * @param databaseName 数据库名称（不得为 {@code null}）
	 * @return 给定数据库的 {@code SQLErrorCodes}（永不为 {@code null}，可能为空）
	 * @throws IllegalArgumentException 数据库名称为 {@code null} 时
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

		// Could not find the database among the defined ones.
		if (logger.isDebugEnabled()) {
			logger.debug("SQL error codes for '" + databaseName + "' not found");
		}
		return new SQLErrorCodes();
	}

	/**
	 * 返回给定 {@link DataSource} 的 {@link SQLErrorCodes}，
	 * 从 {@link java.sql.DatabaseMetaData} 读取 "databaseProductName"；
	 * 未找到时返回空错误码实例。
	 * @param dataSource 标识数据库的 {@code DataSource}
	 * @return 对应的 {@code SQLErrorCodes}（永不为 {@code null}，可能为空）
	 * @see java.sql.DatabaseMetaData#getDatabaseProductName()
	 */
	public SQLErrorCodes getErrorCodes(DataSource dataSource) {
		SQLErrorCodes sec = resolveErrorCodes(dataSource);
		return (sec != null ? sec : new SQLErrorCodes());
	}

	/**
	 * 返回给定 {@link DataSource} 的 {@link SQLErrorCodes}，
	 * 从 {@link java.sql.DatabaseMetaData} 读取 "databaseProductName"；
	 * JDBC 元数据访问出问题时返回 {@code null}。
	 * @param dataSource 标识数据库的 {@code DataSource}
	 * @return 对应的 {@code SQLErrorCodes}，元数据访问失败时 {@code null}
	 * @since 5.2.9
	 * @see java.sql.DatabaseMetaData#getDatabaseProductName()
	 */
	public @Nullable SQLErrorCodes resolveErrorCodes(DataSource dataSource) {
		Assert.notNull(dataSource, "DataSource must not be null");
		if (logger.isDebugEnabled()) {
			logger.debug("Looking up default SQLErrorCodes for DataSource [" + identify(dataSource) + "]");
		}

		// Try efficient lock-free access for existing cache entry
		SQLErrorCodes sec = this.dataSourceCache.get(dataSource);
		if (sec == null) {
			synchronized (this.dataSourceCache) {
				// Double-check within full dataSourceCache lock
				sec = this.dataSourceCache.get(dataSource);
				if (sec == null) {
					// We could not find it - got to look it up.
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
	 * 将指定数据库名称与给定 {@link DataSource} 关联。
	 * @param dataSource 标识数据库的 {@code DataSource}
	 * @param databaseName 错误码定义文件中的数据库名称（不得为 {@code null}）
	 * @return 对应的 {@code SQLErrorCodes}（永不为 {@code null}）
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
	 * 清除指定 {@link DataSource} 的缓存（若已注册）。
	 * @param dataSource 标识数据库的 {@code DataSource}
	 * @return 被移除的 {@code SQLErrorCodes}，未注册时 {@code null}
	 * @since 4.3.5
	 * @see #registerDatabase(DataSource, String)
	 */
	public @Nullable SQLErrorCodes unregisterDatabase(DataSource dataSource) {
		return this.dataSourceCache.remove(dataSource);
	}

	/**
	 * 为给定 {@link DataSource} 构建标识字符串，主要用于日志。
	 * @param dataSource 要内省的 {@code DataSource}
	 * @return 标识字符串
	 */
	private String identify(DataSource dataSource) {
		return dataSource.getClass().getName() + '@' + Integer.toHexString(dataSource.hashCode());
	}

	/**
	 * 检查 {@link CustomSQLExceptionTranslatorRegistry} 中是否有条目。
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
