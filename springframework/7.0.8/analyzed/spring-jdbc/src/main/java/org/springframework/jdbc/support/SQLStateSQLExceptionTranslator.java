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
import java.sql.SQLException;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.jdbc.BadSqlGrammarException;

/**
 * {@link SQLExceptionTranslator} 实现，根据前两位数字（SQL 状态“类”）分析 {@link SQLException} 中的 SQL
 * 状态。检测标准 SQL 状态值和众所周知的特定于供应商的 SQL 状态。
 * <p>无法诊断所有问题，但可以在数据库之间移植，并且不需要特殊的初始化（无需数据库供应商检测等）。要获得更精确的翻译，请考虑 {@link SQLErrorCodeSQLExc
 * eptionTranslator}。
 * <p> 此转换器通常用作主转换器（例如 {@link SQLErrorCodeSQLExceptionTranslator} 或 {@link
 * SQLExceptionSubclassTranslator}）后面的 {@link #setFallbackTranslator fallback}。从 6.2.12
 * 开始，它专门内省 {@link java.sql.BatchUpdateException} 以查看底层异常（用于在 {@link
 * SQLExceptionSubclassTranslator} 后面使用时的对齐）。
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Thomas Risberg
 * @see java.sql.SQLException#getSQLState()
 * @see SQLErrorCodeSQLExceptionTranslator
 * @see SQLExceptionSubclassTranslator
 */
public class SQLStateSQLExceptionTranslator extends AbstractFallbackSQLExceptionTranslator {

	/**
	 * 方法 `of`：完成本类中与「of」相关的职责。
	 */
	private static final Set<String> BAD_SQL_GRAMMAR_CODES = Set.of(
			"07",  // Dynamic SQL error
			"21",  // Cardinality violation
			"2A",  // Syntax error direct SQL
			"37",  // Syntax error dynamic SQL
			"42",  // General SQL syntax error
			"65"   // Oracle: unknown identifier
		);

	/**
	 * 方法 `of`：完成本类中与「of」相关的职责。
	 */
	private static final Set<String> DATA_INTEGRITY_VIOLATION_CODES = Set.of(
			"01",  // Data truncation
			"02",  // No data found
			"22",  // Value out of range
			"23",  // Integrity constraint violation
			"27",  // Triggered data change violation
			"44"   // With check violation
		);

	/**
	 * 方法 `of`：完成本类中与「of」相关的职责。
	 */
	private static final Set<String> PESSIMISTIC_LOCKING_FAILURE_CODES = Set.of(
			"40",  // Transaction rollback
			"61"   // Oracle: deadlock
	);

	/**
	 * 方法 `of`：完成本类中与「of」相关的职责。
	 */
	private static final Set<String> DATA_ACCESS_RESOURCE_FAILURE_CODES = Set.of(
			"08",  // Connection exception
			"53",  // PostgreSQL: insufficient resources (for example, disk full)
			"54",  // PostgreSQL: program limit exceeded (for example, statement too complex)
			"57",  // DB2: out-of-memory exception / database not started
			"58"   // DB2: unexpected system error
		);

	/**
	 * 方法 `of`：完成本类中与「of」相关的职责。
	 */
	private static final Set<String> TRANSIENT_DATA_ACCESS_RESOURCE_CODES = Set.of(
			"JW",  // Sybase: internal I/O error
			"JZ",  // Sybase: unexpected I/O error
			"S1"   // DB2: communication failure
		);

	/**
	 * 方法 `of`：完成本类中与「of」相关的职责。
	 */
	private static final Set<Integer> DUPLICATE_KEY_ERROR_CODES = Set.of(
			1,     // Oracle
			301,   // SAP HANA
			1062,  // MySQL/MariaDB
			2601,  // MS SQL Server
			2627,  // MS SQL Server
			-239,  // Informix
			-268   // Informix
		);


	/**
	 * 执行核心逻辑：Translate（方法 `doTranslate`）。
	 */
	@Override
	protected @Nullable DataAccessException doTranslate(String task, @Nullable String sql, SQLException ex) {
		SQLException sqlEx = ex;
		String sqlState;
		if (sqlEx instanceof BatchUpdateException) {
			// 展开 BatchUpdateException 以公开包含的异常
			// 具有可能更具体的 SQL 状态。
			if (sqlEx.getNextException() != null) {
				SQLException nestedSqlEx = sqlEx.getNextException();
				if (nestedSqlEx.getSQLState() != null) {
					sqlEx = nestedSqlEx;
				}
			}
			sqlState = sqlEx.getSQLState();
		}
		else {
			// 公开顶级异常但可能使用嵌套 SQL 状态。
			sqlState = getSqlState(sqlEx);
		}

		// 实际的 SQL 状态检查...
		if (sqlState != null && sqlState.length() >= 2) {
			String classCode = sqlState.substring(0, 2);
			if (logger.isDebugEnabled()) {
				logger.debug("Extracted SQL state class '" + classCode + "' from value '" + sqlState + "'");
			}
			if (BAD_SQL_GRAMMAR_CODES.contains(classCode)) {
				return new BadSqlGrammarException(task, (sql != null ? sql : ""), ex);
			}
			else if (DATA_INTEGRITY_VIOLATION_CODES.contains(classCode)) {
				if (indicatesDuplicateKey(sqlState, sqlEx.getErrorCode())) {
					return new DuplicateKeyException(buildMessage(task, sql, sqlEx), ex);
				}
				return new DataIntegrityViolationException(buildMessage(task, sql, sqlEx), ex);
			}
			else if (PESSIMISTIC_LOCKING_FAILURE_CODES.contains(classCode)) {
				if (indicatesCannotAcquireLock(sqlState)) {
					return new CannotAcquireLockException(buildMessage(task, sql, sqlEx), ex);
				}
				return new PessimisticLockingFailureException(buildMessage(task, sql, sqlEx), ex);
			}
			else if (DATA_ACCESS_RESOURCE_FAILURE_CODES.contains(classCode)) {
				if (indicatesQueryTimeout(sqlState)) {
					return new QueryTimeoutException(buildMessage(task, sql, sqlEx), ex);
				}
				return new DataAccessResourceFailureException(buildMessage(task, sql, sqlEx), ex);
			}
			else if (TRANSIENT_DATA_ACCESS_RESOURCE_CODES.contains(classCode)) {
				return new TransientDataAccessResourceException(buildMessage(task, sql, sqlEx), ex);
			}
		}

		// 对于 MySQL：指示超时的异常类名称？
		// （因为 MySQL 不会抛出 JDBC 4 SQLTimeoutException）
		if (sqlEx.getClass().getName().contains("Timeout")) {
			return new QueryTimeoutException(buildMessage(task, sql, sqlEx), ex);
		}

		// 无法正确解决任何问题 - 求助于 UncategorizedSQLException。
		return null;
	}

	/**
	 * 从提供的 {@link SQLException exception} 获取 SQL 状态代码。 <p>一些 JDBC 驱动程序会嵌套来自批量更新的实际异常，因此我们可能需要深
	 * 入研究嵌套异常。
	 * @param ex 要从中提取 {@link SQLException#getSQLState() SQL state} 的异常
	 * @return SQL状态代码
	 */
	private @Nullable String getSqlState(SQLException ex) {
		String sqlState = ex.getSQLState();
		if (sqlState == null) {
			SQLException nestedEx = ex.getNextException();
			if (nestedEx != null) {
				sqlState = nestedEx.getSQLState();
			}
		}
		return sqlState;
	}


	/**
	 * 检查给定的 SQL 状态和关联的错误代码（在通用 SQL 状态值的情况下）是否指示 {@link DuplicateKeyException}：作为特定指示的 SQL 状态 2
	 * 3505，或具有众所周知的供应商代码的通用 SQL 状态 23000。
	 * @param sqlState SQL 状态值
	 * @param errorCode 错误代码
	 */
	static boolean indicatesDuplicateKey(@Nullable String sqlState, int errorCode) {
		return ("23505".equals(sqlState) ||
				("23000".equals(sqlState) && DUPLICATE_KEY_ERROR_CODES.contains(errorCode)));
	}

	/**
	 * 检查给定的 SQL 状态是否指示 {@link CannotAcquireLockException}，以 SQL 状态 40001 作为特定指示。
	 * @param sqlState SQL 状态值
	 */
	static boolean indicatesCannotAcquireLock(@Nullable String sqlState) {
		return "40001".equals(sqlState);
	}

	/**
	 * 检查给定的 SQL 状态是否指示 {@link QueryTimeoutException}，其中 SQL 状态 57014 作为特定指示。
	 * @param sqlState SQL 状态值
	 */
	static boolean indicatesQueryTimeout(@Nullable String sqlState) {
		return "57014".equals(sqlState);
	}

}
