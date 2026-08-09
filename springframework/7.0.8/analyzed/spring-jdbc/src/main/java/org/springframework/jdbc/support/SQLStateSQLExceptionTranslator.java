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
 * 基于 {@link SQLException} 中 SQL state 前两位（SQL state "class"）进行分析的 {@link SQLExceptionTranslator} 实现。
 * 识别标准 SQL state 值及常见厂商特定 SQL state。
 *
 * <p>无法诊断所有问题，但在数据库间可移植，无需特殊初始化（如厂商检测）。
 * 需要更精确翻译时可考虑 {@link SQLErrorCodeSQLExceptionTranslator}。
 *
 * <p>通常作为主翻译器（如 {@link SQLErrorCodeSQLExceptionTranslator} 或 {@link SQLExceptionSubclassTranslator}）
 * 背后的 {@link #setFallbackTranslator 回退翻译器}。
 * 自 6.2.12 起专门内省 {@link java.sql.BatchUpdateException} 查看底层异常
 * （与在 {@link SQLExceptionSubclassTranslator} 后使用时对齐）。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Thomas Risberg
 * @see java.sql.SQLException#getSQLState()
 * @see SQLErrorCodeSQLExceptionTranslator
 * @see SQLExceptionSubclassTranslator
 */
public class SQLStateSQLExceptionTranslator extends AbstractFallbackSQLExceptionTranslator {

	private static final Set<String> BAD_SQL_GRAMMAR_CODES = Set.of(
			"07",  // 动态 SQL 错误
			"21",  // 基数违反
			"2A",  // 直接 SQL 语法错误
			"37",  // 动态 SQL 语法错误
			"42",  // 通用 SQL 语法错误
			"65"   // Oracle：未知标识符
		);

	private static final Set<String> DATA_INTEGRITY_VIOLATION_CODES = Set.of(
			"01",  // 数据截断
			"02",  // 未找到数据
			"22",  // 值超出范围
			"23",  // 完整性约束违反
			"27",  // 触发器数据变更违反
			"44"   // WITH CHECK 违反
		);

	private static final Set<String> PESSIMISTIC_LOCKING_FAILURE_CODES = Set.of(
			"40",  // 事务回滚
			"61"   // Oracle：死锁
	);

	private static final Set<String> DATA_ACCESS_RESOURCE_FAILURE_CODES = Set.of(
			"08",  // 连接异常
			"53",  // PostgreSQL：资源不足（如磁盘满）
			"54",  // PostgreSQL：程序限制超出（如语句过于复杂）
			"57",  // DB2：内存不足 / 数据库未启动
			"58"   // DB2：意外系统错误
		);

	private static final Set<String> TRANSIENT_DATA_ACCESS_RESOURCE_CODES = Set.of(
			"JW",  // Sybase：内部 I/O 错误
			"JZ",  // Sybase：意外 I/O 错误
			"S1"   // DB2：通信失败
		);

	private static final Set<Integer> DUPLICATE_KEY_ERROR_CODES = Set.of(
			1,     // Oracle
			301,   // SAP HANA
			1062,  // MySQL/MariaDB
			2601,  // MS SQL Server
			2627,  // MS SQL Server
			-239,  // Informix
			-268   // Informix
		);


	@Override
	protected @Nullable DataAccessException doTranslate(String task, @Nullable String sql, SQLException ex) {
		SQLException sqlEx = ex;
		String sqlState;
		if (sqlEx instanceof BatchUpdateException) {
			// 解包 BatchUpdateException 以暴露可能具有更具体 SQL state 的内部异常。
			if (sqlEx.getNextException() != null) {
				SQLException nestedSqlEx = sqlEx.getNextException();
				if (nestedSqlEx.getSQLState() != null) {
					sqlEx = nestedSqlEx;
				}
			}
			sqlState = sqlEx.getSQLState();
		}
		else {
			// 暴露顶层异常，但可能使用嵌套 SQL state。
			sqlState = getSqlState(sqlEx);
		}

		// 实际 SQL state 检查...
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

		// MySQL：异常类名是否表示超时？
		// （MySQL 不抛出 JDBC 4 SQLTimeoutException）
		if (sqlEx.getClass().getName().contains("Timeout")) {
			return new QueryTimeoutException(buildMessage(task, sql, sqlEx), ex);
		}

		// 无法解析——回退到 UncategorizedSQLException。
		return null;
	}

	/**
	 * 从给定 {@link SQLException exception} 获取 SQL state 码。
	 * <p>部分 JDBC 驱动将批更新中的实际异常嵌套，可能需要深入嵌套异常。
	 * @param ex 要提取 {@link SQLException#getSQLState() SQL state} 的异常
	 * @return SQL state 码
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
	 * 检查给定 SQL state 及关联错误码（通用 SQL state 时）是否表示 {@link DuplicateKeyException}：
	 * 特定指示 SQL state 23505，或通用 SQL state 23000 加已知厂商码。
	 * @param sqlState SQL state 值
	 * @param errorCode 错误码
	 */
	static boolean indicatesDuplicateKey(@Nullable String sqlState, int errorCode) {
		return ("23505".equals(sqlState) ||
				("23000".equals(sqlState) && DUPLICATE_KEY_ERROR_CODES.contains(errorCode)));
	}

	/**
	 * 检查给定 SQL state 是否表示 {@link CannotAcquireLockException}，
	 * 特定指示为 SQL state 40001。
	 * @param sqlState SQL state 值
	 */
	static boolean indicatesCannotAcquireLock(@Nullable String sqlState) {
		return "40001".equals(sqlState);
	}

	/**
	 * 检查给定 SQL state 是否表示 {@link QueryTimeoutException}，
	 * 特定指示为 SQL state 57014。
	 * @param sqlState SQL state 值
	 */
	static boolean indicatesQueryTimeout(@Nullable String sqlState) {
		return "57014".equals(sqlState);
	}

}
