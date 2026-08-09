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

import java.sql.BatchUpdateException;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLInvalidAuthorizationSpecException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLNonTransientException;
import java.sql.SQLRecoverableException;
import java.sql.SQLSyntaxErrorException;
import java.sql.SQLTimeoutException;
import java.sql.SQLTransactionRollbackException;
import java.sql.SQLTransientConnectionException;
import java.sql.SQLTransientException;

import org.jspecify.annotations.Nullable;

import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.PermissionDeniedDataAccessException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.jdbc.BadSqlGrammarException;

/**
 * {@link SQLExceptionTranslator} 实现，用于分析 JDBC 驱动程序抛出的特定 {@link java.sql.SQLException} 子类。
 * 如果 JDBC 驱动程序实际上未公开 JDBC 4 兼容的 {@code SQLException} 子类，则 <p> 会返回到标准 {@link
 * SQLStateSQLExceptionTranslator}。
 * <p> 从 6.0 开始，此转换器充当默认的 JDBC 异常转换器。从 6.2.12 开始，它专门内省 {@link
 * java.sql.BatchUpdateException} 以查看底层异常，类似于以前的默认 {@link
 * SQLErrorCodeSQLExceptionTranslator}。
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @since 2.5
 * @see java.sql.SQLTransientException
 * @see java.sql.SQLNonTransientException
 * @see java.sql.SQLRecoverableException
 */
public class SQLExceptionSubclassTranslator extends AbstractFallbackSQLExceptionTranslator {

	/**
	 * 创建 `SQLExceptionSubclassTranslator` 的新实例。
	 */
	public SQLExceptionSubclassTranslator() {
		setFallbackTranslator(new SQLStateSQLExceptionTranslator());
	}

	/**
	 * 执行核心逻辑：Translate（方法 `doTranslate`）。
	 */
	@Override
	protected @Nullable DataAccessException doTranslate(String task, @Nullable String sql, SQLException ex) {
		SQLException sqlEx = ex;
		if (sqlEx instanceof BatchUpdateException && sqlEx.getNextException() != null) {
			sqlEx = sqlEx.getNextException();
		}

		if (sqlEx instanceof SQLTransientException) {
			if (sqlEx instanceof SQLTransientConnectionException) {
				return new TransientDataAccessResourceException(buildMessage(task, sql, sqlEx), ex);
			}
			if (sqlEx instanceof SQLTransactionRollbackException) {
				if (SQLStateSQLExceptionTranslator.indicatesCannotAcquireLock(sqlEx.getSQLState())) {
					return new CannotAcquireLockException(buildMessage(task, sql, sqlEx), ex);
				}
				return new PessimisticLockingFailureException(buildMessage(task, sql, sqlEx), ex);
			}
			if (sqlEx instanceof SQLTimeoutException) {
				return new QueryTimeoutException(buildMessage(task, sql, sqlEx), ex);
			}
		}
		else if (sqlEx instanceof SQLNonTransientException) {
			if (sqlEx instanceof SQLNonTransientConnectionException) {
				return new DataAccessResourceFailureException(buildMessage(task, sql, sqlEx), ex);
			}
			if (sqlEx instanceof SQLDataException) {
				return new DataIntegrityViolationException(buildMessage(task, sql, sqlEx), ex);
			}
			if (sqlEx instanceof SQLIntegrityConstraintViolationException) {
				if (SQLStateSQLExceptionTranslator.indicatesDuplicateKey(sqlEx.getSQLState(), sqlEx.getErrorCode())) {
					return new DuplicateKeyException(buildMessage(task, sql, sqlEx), ex);
				}
				return new DataIntegrityViolationException(buildMessage(task, sql, sqlEx), ex);
			}
			if (sqlEx instanceof SQLInvalidAuthorizationSpecException) {
				return new PermissionDeniedDataAccessException(buildMessage(task, sql, sqlEx), ex);
			}
			if (sqlEx instanceof SQLSyntaxErrorException) {
				return new BadSqlGrammarException(task, (sql != null ? sql : ""), ex);
			}
			if (sqlEx instanceof SQLFeatureNotSupportedException) {
				return new InvalidDataAccessApiUsageException(buildMessage(task, sql, sqlEx), ex);
			}
		}
		else if (sqlEx instanceof SQLRecoverableException) {
			return new RecoverableDataAccessException(buildMessage(task, sql, sqlEx), ex);
		}

		// 回退到 Spring 自己的 SQL 状态转换...
		return null;
	}

}
