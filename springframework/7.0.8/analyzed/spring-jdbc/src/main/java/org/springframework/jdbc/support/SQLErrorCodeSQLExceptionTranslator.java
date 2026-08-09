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

import java.lang.reflect.Constructor;
import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.util.Arrays;

import javax.sql.DataSource;

import org.jspecify.annotations.Nullable;

import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.PermissionDeniedDataAccessException;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.InvalidResultSetAccessException;
import org.springframework.util.function.SingletonSupplier;
import org.springframework.util.function.SupplierUtils;

/**
 * 实现 {@link SQLExceptionTranslator}，用于分析特定于供应商的错误代码。比基于 SQL 状态的实现更精确，但很大程度上取决于供应商。
 * <p> 该类应用以下匹配规则： <ul> <li> 尝试由任何子类实现的自定义翻译。请注意，此类是具体的，通常会自行使用，在这种情况下，此规则不适用。 <li>应用错误代码匹配
 * 。默认情况下，错误代码是从 SQLErrorCodesFactory 获取的。该工厂从类路径加载“sql-error-codes.xml”文件，定义数据库元数据中数据库名称的错
 * 误代码映射。 <li>回退到后备翻译器。 {@link SQLStateSQLExceptionTranslator} 是默认的后备转换器，仅分析异常的 SQL 状态。由于Ja
 * va 6引入了自己的{@code SQLException}子类层次结构，因此我们默认使用{@link SQLExceptionSubclassTranslator}，当没有遇
 * 到特定子类时，它又会回退到Spring自己的SQL状态转换。 </ul>
 * <p>
 * 名为“sql-error-codes.xml”的配置文件默认从此包中读取。可以通过类路径根目录（例如“/WEB-INF/classes”目录）中的同名文件来覆盖它，只要
 * Spring JDBC 包是从同一个 ClassLoader 加载的。
 * <p> 如果在类路径的根目录中找到用户提供的 `sql-error-codes.xml` 文件，则默认情况下通常会使用此转换器作为使用此策略的信号。否则，从 6.0 开始，{@
 * link SQLExceptionSubclassTranslator} 将作为默认转换器。
 * @author Rod Johnson
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @see SQLErrorCodesFactory
 * @see SQLStateSQLExceptionTranslator
 */
public class SQLErrorCodeSQLExceptionTranslator extends AbstractFallbackSQLExceptionTranslator {

	private static final int MESSAGE_ONLY_CONSTRUCTOR = 1;
	private static final int MESSAGE_THROWABLE_CONSTRUCTOR = 2;
	private static final int MESSAGE_SQLEX_CONSTRUCTOR = 3;
	private static final int MESSAGE_SQL_THROWABLE_CONSTRUCTOR = 4;
	private static final int MESSAGE_SQL_SQLEX_CONSTRUCTOR = 5;

	/** 是否已提供用户自定义错误码文件。 */
	private static final boolean userProvidedErrorCodesFilePresent =
			new ClassPathResource(SQLErrorCodesFactory.SQL_ERROR_CODE_OVERRIDE_PATH,
					SQLErrorCodesFactory.class.getClassLoader()).exists();

	/** 本转换器使用的 {@link SQLErrorCodes}。 */
	private @Nullable SingletonSupplier<SQLErrorCodes> sqlErrorCodes;


	/**
	 * 用作 JavaBean 的构造函数。必须设置 SqlErrorCodes 或 DataSource 属性。
	 */
	public SQLErrorCodeSQLExceptionTranslator() {
		setFallbackTranslator(new SQLExceptionSubclassTranslator());
	}

	/**
	 * 为给定的数据源创建 SQL 错误代码转换器。调用此构造函数将导致从 DataSource 获取 Connection 以获取元数据。
	 * @param dataSource 用于查找元数据并确定哪些错误代码可用的数据源
	 * @see SQLErrorCodesFactory
	 */
	public SQLErrorCodeSQLExceptionTranslator(DataSource dataSource) {
		this();
		setDataSource(dataSource);
	}

	/**
	 * 为给定的数据库产品名称创建 SQL 错误代码转换器。调用此构造函数将避免从 DataSource 获取 Connection 来获取元数据。
	 * @param dbName 标识错误代码条目的数据库产品名称
	 * @see SQLErrorCodesFactory
	 * @see java.sql.DatabaseMetaData#getDatabaseProductName()
	 */
	public SQLErrorCodeSQLExceptionTranslator(String dbName) {
		this();
		setDatabaseProductName(dbName);
	}

	/**
	 * 给定这些错误代码创建一个 SQLErrorCode 转换器。不需要使用连接执行数据库元数据查找。
	 * @param sec 错误代码
	 */
	public SQLErrorCodeSQLExceptionTranslator(SQLErrorCodes sec) {
		this();
		this.sqlErrorCodes = SingletonSupplier.of(sec);
	}


	/**
	 * 设置此转换器的数据源。 <p>S设置此属性将导致从数据源获取连接以获取元数据。
	 * @param dataSource 用于查找元数据并确定哪些错误代码可用的数据源
	 * @see SQLErrorCodesFactory#getErrorCodes(javax.sql.DataSource)
	 * @see java.sql.DatabaseMetaData#getDatabaseProductName()
	 */
	public void setDataSource(DataSource dataSource) {
		this.sqlErrorCodes =
				SingletonSupplier.of(() -> SQLErrorCodesFactory.getInstance().resolveErrorCodes(dataSource));
		this.sqlErrorCodes.get();  // try early initialization - otherwise the supplier will retry later
	}

	/**
	 * 设置该转换器的数据库产品名称。 <p>S设置此属性将避免从数据源获取连接来获取元数据。
	 * @param dbName 标识错误代码条目的数据库产品名称
	 * @see SQLErrorCodesFactory#getErrorCodes(String)
	 * @see java.sql.DatabaseMetaData#getDatabaseProductName()
	 */
	public void setDatabaseProductName(String dbName) {
		this.sqlErrorCodes = SingletonSupplier.of(SQLErrorCodesFactory.getInstance().getErrorCodes(dbName));
	}

	/**
	 * 设置用于翻译的自定义错误代码。
	 * @param sec 要使用的自定义错误代码
	 */
	public void setSqlErrorCodes(@Nullable SQLErrorCodes sec) {
		this.sqlErrorCodes = SingletonSupplier.ofNullable(sec);
	}

	/**
	 * 返回该转换器使用的错误代码。通常通过数据源确定。
	 * @see #setDataSource
	 */
	public @Nullable SQLErrorCodes getSqlErrorCodes() {
		return SupplierUtils.resolve(this.sqlErrorCodes);
	}


	/**
	 * 基于 SQL 错误码将 {@link SQLException} 翻译为 Spring {@link DataAccessException}。
	 * @param task 发生异常的任务描述
	 * @param sql 导致异常的 SQL（若有）
	 * @param ex 原始 SQLException
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected @Nullable DataAccessException doTranslate(String task, @Nullable String sql, SQLException ex) {
		SQLException sqlEx = ex;
		if (sqlEx instanceof BatchUpdateException && sqlEx.getNextException() != null) {
			SQLException nestedSqlEx = sqlEx.getNextException();
			if (nestedSqlEx.getErrorCode() > 0 || nestedSqlEx.getSQLState() != null) {
				sqlEx = nestedSqlEx;
			}
		}

		// 首先，尝试从重写方法进行自定义翻译。
		DataAccessException dae = customTranslate(task, sql, sqlEx);
		if (dae != null) {
			return dae;
		}

		// 接下来，尝试自定义 SQLException 转换器（如果可用）。
		SQLErrorCodes sqlErrorCodes = getSqlErrorCodes();
		if (sqlErrorCodes != null) {
			SQLExceptionTranslator customTranslator = sqlErrorCodes.getCustomSqlExceptionTranslator();
			if (customTranslator != null) {
				dae = customTranslator.translate(task, sql, sqlEx);
				if (dae != null) {
					return dae;
				}
			}
		}

		// 检查 SQLErrorCodes 以及相应的错误代码（如果有）。
		if (sqlErrorCodes != null) {
			String errorCode;
			if (sqlErrorCodes.isUseSqlStateForTranslation()) {
				errorCode = sqlEx.getSQLState();
			}
			else {
				// 尝试查找具有实际错误代码的 SQLException，循环查找原因。
				// 例如，适用于 JDK 1.6 起的 java.sql.DataTruncation。
				SQLException current = sqlEx;
				while (current.getErrorCode() == 0 && current.getCause() instanceof SQLException sqlException) {
					current = sqlException;
				}
				errorCode = Integer.toString(current.getErrorCode());
			}

			if (errorCode != null) {
				// 首先查找定义的自定义翻译。
				CustomSQLErrorCodesTranslation[] customTranslations = sqlErrorCodes.getCustomTranslations();
				if (customTranslations != null) {
					for (CustomSQLErrorCodesTranslation customTranslation : customTranslations) {
						if (Arrays.binarySearch(customTranslation.getErrorCodes(), errorCode) >= 0 &&
								customTranslation.getExceptionClass() != null) {
							dae = createCustomException(task, sql, sqlEx, customTranslation.getExceptionClass());
							if (dae != null) {
								logTranslation(task, sql, sqlEx, true);
								return dae;
							}
						}
					}
				}
				// 接下来，查找分组的错误代码。
				if (Arrays.binarySearch(sqlErrorCodes.getBadSqlGrammarCodes(), errorCode) >= 0) {
					logTranslation(task, sql, sqlEx, false);
					return new BadSqlGrammarException(task, (sql != null ? sql : ""), sqlEx);
				}
				else if (Arrays.binarySearch(sqlErrorCodes.getInvalidResultSetAccessCodes(), errorCode) >= 0) {
					logTranslation(task, sql, sqlEx, false);
					return new InvalidResultSetAccessException(task, (sql != null ? sql : ""), sqlEx);
				}
				else if (Arrays.binarySearch(sqlErrorCodes.getDuplicateKeyCodes(), errorCode) >= 0) {
					logTranslation(task, sql, sqlEx, false);
					return new DuplicateKeyException(buildMessage(task, sql, sqlEx), sqlEx);
				}
				else if (Arrays.binarySearch(sqlErrorCodes.getDataIntegrityViolationCodes(), errorCode) >= 0) {
					logTranslation(task, sql, sqlEx, false);
					return new DataIntegrityViolationException(buildMessage(task, sql, sqlEx), sqlEx);
				}
				else if (Arrays.binarySearch(sqlErrorCodes.getPermissionDeniedCodes(), errorCode) >= 0) {
					logTranslation(task, sql, sqlEx, false);
					return new PermissionDeniedDataAccessException(buildMessage(task, sql, sqlEx), sqlEx);
				}
				else if (Arrays.binarySearch(sqlErrorCodes.getDataAccessResourceFailureCodes(), errorCode) >= 0) {
					logTranslation(task, sql, sqlEx, false);
					return new DataAccessResourceFailureException(buildMessage(task, sql, sqlEx), sqlEx);
				}
				else if (Arrays.binarySearch(sqlErrorCodes.getTransientDataAccessResourceCodes(), errorCode) >= 0) {
					logTranslation(task, sql, sqlEx, false);
					return new TransientDataAccessResourceException(buildMessage(task, sql, sqlEx), sqlEx);
				}
				else if (Arrays.binarySearch(sqlErrorCodes.getCannotAcquireLockCodes(), errorCode) >= 0) {
					logTranslation(task, sql, sqlEx, false);
					return new CannotAcquireLockException(buildMessage(task, sql, sqlEx), sqlEx);
				}
				else if (Arrays.binarySearch(sqlErrorCodes.getDeadlockLoserCodes(), errorCode) >= 0) {
					logTranslation(task, sql, sqlEx, false);
					return new org.springframework.dao.DeadlockLoserDataAccessException(buildMessage(task, sql, sqlEx), sqlEx);
				}
				else if (Arrays.binarySearch(sqlErrorCodes.getCannotSerializeTransactionCodes(), errorCode) >= 0) {
					logTranslation(task, sql, sqlEx, false);
					return new org.springframework.dao.CannotSerializeTransactionException(buildMessage(task, sql, sqlEx), sqlEx);
				}
			}
		}

		// 我们无法更准确地识别它 - 让我们将其交给 SQLState 后备翻译器。
		if (logger.isDebugEnabled()) {
			String codes;
			if (sqlErrorCodes != null && sqlErrorCodes.isUseSqlStateForTranslation()) {
				codes = "SQL state '" + sqlEx.getSQLState() + "', error code '" + sqlEx.getErrorCode();
			}
			else {
				codes = "Error code '" + sqlEx.getErrorCode() + "'";
			}
			logger.debug("Unable to translate SQLException with " + codes + ", will now try the fallback translator");
		}

		return null;
	}

	/**
	 * 子类可以重写此方法以尝试从 {@link SQLException} 到 {@link DataAccessException} 的自定义映射。
	 * @param task 描述正在尝试的任务的可读文本
	 * @param sql 导致问题的 SQL 查询或更新（可能是 {@code null}）
	 * @param sqlEx 有问题的 SQLException
	 * @return null} 如果没有自定义翻译适用，否则由自定义翻译生成 {@link DataAccessException}。此异常应包含 {@code sqlEx} 参数作为嵌套根本原因。此实现始终返回 {@code null}，这意味着翻译器始终回退到默认错误代码。
	 * @deprecated 6.1，支持 {@link #setCustomTranslator}
	 */
	@Deprecated(since = "6.1")
	protected @Nullable DataAccessException customTranslate(String task, @Nullable String sql, SQLException sqlEx) {
		return null;
	}

	/**
	 * 根据 {@link CustomSQLErrorCodesTranslation} 定义中的给定异常类创建自定义 {@link DataAccessException}。
	 * @param task 描述正在尝试的任务的可读文本
	 * @param sql 导致问题的 SQL 查询或更新（可能是 {@code null}）
	 * @param sqlEx 有问题的 SQLException
	 * @param exceptionClass 要使用的异常类，如 {@link CustomSQLErrorCodesTranslation} 定义中所定义
	 * @return null} 如果无法创建自定义异常，否则生成 {@link DataAccessException}。此异常应包含 {@code sqlEx} 参数作为嵌套根本原因。
	 * @see CustomSQLErrorCodesTranslation#setExceptionClass
	 */
	protected @Nullable DataAccessException createCustomException(
			String task, @Nullable String sql, SQLException sqlEx, Class<?> exceptionClass) {

		// 为给定的异常类找到合适的构造函数
		try {
			int constructorType = 0;
			Constructor<?>[] constructors = exceptionClass.getConstructors();
			for (Constructor<?> constructor : constructors) {
				Class<?>[] parameterTypes = constructor.getParameterTypes();
				if (parameterTypes.length == 1 && String.class == parameterTypes[0] &&
						constructorType < MESSAGE_ONLY_CONSTRUCTOR) {
					constructorType = MESSAGE_ONLY_CONSTRUCTOR;
				}
				if (parameterTypes.length == 2 && String.class == parameterTypes[0] &&
						Throwable.class == parameterTypes[1] &&
						constructorType < MESSAGE_THROWABLE_CONSTRUCTOR) {
					constructorType = MESSAGE_THROWABLE_CONSTRUCTOR;
				}
				if (parameterTypes.length == 2 && String.class == parameterTypes[0] &&
						SQLException.class == parameterTypes[1] &&
						constructorType < MESSAGE_SQLEX_CONSTRUCTOR) {
					constructorType = MESSAGE_SQLEX_CONSTRUCTOR;
				}
				if (parameterTypes.length == 3 && String.class == parameterTypes[0] &&
						String.class == parameterTypes[1] && Throwable.class == parameterTypes[2] &&
						constructorType < MESSAGE_SQL_THROWABLE_CONSTRUCTOR) {
					constructorType = MESSAGE_SQL_THROWABLE_CONSTRUCTOR;
				}
				if (parameterTypes.length == 3 && String.class == parameterTypes[0] &&
						String.class == parameterTypes[1] && SQLException.class == parameterTypes[2] &&
						constructorType < MESSAGE_SQL_SQLEX_CONSTRUCTOR) {
					constructorType = MESSAGE_SQL_SQLEX_CONSTRUCTOR;
				}
			}

			// 调用构造函数
			Constructor<?> exceptionConstructor;
			return switch (constructorType) {
				case MESSAGE_SQL_SQLEX_CONSTRUCTOR -> {
					Class<?>[] messageAndSqlAndSqlExArgsClass = new Class<?>[] {String.class, String.class, SQLException.class};
					Object[] messageAndSqlAndSqlExArgs = new Object[] {task, sql, sqlEx};
					exceptionConstructor = exceptionClass.getConstructor(messageAndSqlAndSqlExArgsClass);
					yield (DataAccessException) exceptionConstructor.newInstance(messageAndSqlAndSqlExArgs);
				}
				case MESSAGE_SQL_THROWABLE_CONSTRUCTOR -> {
					Class<?>[] messageAndSqlAndThrowableArgsClass = new Class<?>[] {String.class, String.class, Throwable.class};
					Object[] messageAndSqlAndThrowableArgs = new Object[] {task, sql, sqlEx};
					exceptionConstructor = exceptionClass.getConstructor(messageAndSqlAndThrowableArgsClass);
					yield (DataAccessException) exceptionConstructor.newInstance(messageAndSqlAndThrowableArgs);
				}
				case MESSAGE_SQLEX_CONSTRUCTOR -> {
					Class<?>[] messageAndSqlExArgsClass = new Class<?>[] {String.class, SQLException.class};
					Object[] messageAndSqlExArgs = new Object[] {task + ": " + sqlEx.getMessage(), sqlEx};
					exceptionConstructor = exceptionClass.getConstructor(messageAndSqlExArgsClass);
					yield (DataAccessException) exceptionConstructor.newInstance(messageAndSqlExArgs);
				}
				case MESSAGE_THROWABLE_CONSTRUCTOR -> {
					Class<?>[] messageAndThrowableArgsClass = new Class<?>[] {String.class, Throwable.class};
					Object[] messageAndThrowableArgs = new Object[] {task + ": " + sqlEx.getMessage(), sqlEx};
					exceptionConstructor = exceptionClass.getConstructor(messageAndThrowableArgsClass);
					yield (DataAccessException)exceptionConstructor.newInstance(messageAndThrowableArgs);
				}
				case MESSAGE_ONLY_CONSTRUCTOR -> {
					Class<?>[] messageOnlyArgsClass = new Class<?>[] {String.class};
					Object[] messageOnlyArgs = new Object[] {task + ": " + sqlEx.getMessage()};
					exceptionConstructor = exceptionClass.getConstructor(messageOnlyArgsClass);
					yield (DataAccessException) exceptionConstructor.newInstance(messageOnlyArgs);
				}
				default -> {
					if (logger.isWarnEnabled()) {
						logger.warn("Unable to find appropriate constructor of custom exception class [" +
								exceptionClass.getName() + "]");
					}
					yield null;
				}
			};
		}
		catch (Throwable ex) {
			if (logger.isWarnEnabled()) {
				logger.warn("Unable to instantiate custom exception class [" + exceptionClass.getName() + "]", ex);
			}
			return null;
		}
	}

	/**
	 * 在 debug 级别记录 SQLException 翻译信息。
	 * @param custom 是否为自定义翻译
	 */
	private void logTranslation(String task, @Nullable String sql, SQLException sqlEx, boolean custom) {
		if (logger.isDebugEnabled()) {
			String intro = custom ? "Custom translation of" : "Translating";
			logger.debug(intro + " SQLException with SQL state '" + sqlEx.getSQLState() +
					"', error code '" + sqlEx.getErrorCode() + "', message [" + sqlEx.getMessage() + "]" +
					(sql != null ? "; SQL was [" + sql + "]": "") + " for task [" + task + "]");
		}
	}


	/**
	 * 检查类路径根目录中是否存在用户提供的 `sql-error-codes.xml` 文件。
	 */
	static boolean hasUserProvidedErrorCodesFile() {
		return userProvidedErrorCodesFilePresent;
	}

}
