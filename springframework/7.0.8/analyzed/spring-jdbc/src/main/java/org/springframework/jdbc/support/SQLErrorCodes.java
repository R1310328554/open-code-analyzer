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

import org.jspecify.annotations.Nullable;

import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * 用于保存特定数据库的 JDBC 错误代码的 JavaBean。此类的实例通常通过 bean 工厂加载。
 * <p> 由 Spring 的 {@link SQLErrorCodeSQLExceptionTranslator}
 * 使用。此包中的文件“sql-error-codes.xml”包含各种数据库的默认 {@code SQLErrorCodes} 实例。
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @see SQLErrorCodesFactory
 * @see SQLErrorCodeSQLExceptionTranslator
 */
public class SQLErrorCodes {

	/** 名称相关状态（`databaseProductNames`）。 */
	private String @Nullable [] databaseProductNames;

	/** `false`：该类的成员状态。 */
	private boolean useSqlStateForTranslation = false;

	private String[] badSqlGrammarCodes = new String[0];

	private String[] invalidResultSetAccessCodes = new String[0];

	private String[] duplicateKeyCodes = new String[0];

	private String[] dataIntegrityViolationCodes = new String[0];

	private String[] permissionDeniedCodes = new String[0];

	private String[] dataAccessResourceFailureCodes = new String[0];

	private String[] transientDataAccessResourceCodes = new String[0];

	private String[] cannotAcquireLockCodes = new String[0];

	private String[] deadlockLoserCodes = new String[0];

	private String[] cannotSerializeTransactionCodes = new String[0];

	/** `customTranslations`：该类的成员状态。 */
	private CustomSQLErrorCodesTranslation @Nullable [] customTranslations;

	/** 异常相关状态（`customSqlExceptionTranslator`）。 */
	private @Nullable SQLExceptionTranslator customSqlExceptionTranslator;


	/**
	 * 如果数据库名称包含空格，则设置此属性，在这种情况下我们无法使用bean名称进行查找。
	 */
	public void setDatabaseProductName(@Nullable String databaseProductName) {
		this.databaseProductNames = new String[] {databaseProductName};
	}

	/**
	 * 获取 Database Product Name（`DatabaseProductName`）。
	 */
	public @Nullable String getDatabaseProductName() {
		return (this.databaseProductNames != null && this.databaseProductNames.length > 0 ?
				this.databaseProductNames[0] : null);
	}

	/**
	 * 设置此属性可以指定多个包含空格的数据库名称，在这种情况下我们不能使用bean名称进行查找。
	 */
	public void setDatabaseProductNames(String @Nullable ... databaseProductNames) {
		this.databaseProductNames = databaseProductNames;
	}

	/**
	 * 获取 Database Product Names（`DatabaseProductNames`）。
	 */
	public String @Nullable [] getDatabaseProductNames() {
		return this.databaseProductNames;
	}

	/**
	 * 对于不提供错误代码但提供 SQL 状态的数据库（包括 PostgreSQL），将此属性设置为 true。
	 */
	public void setUseSqlStateForTranslation(boolean useStateCodeForTranslation) {
		this.useSqlStateForTranslation = useStateCodeForTranslation;
	}

	/**
	 * 判断是否 Use Sql State For Translation。
	 */
	public boolean isUseSqlStateForTranslation() {
		return this.useSqlStateForTranslation;
	}

	/**
	 * 设置 Bad Sql Grammar Codes（`BadSqlGrammarCodes`）。
	 */
	public void setBadSqlGrammarCodes(String... badSqlGrammarCodes) {
		this.badSqlGrammarCodes = StringUtils.sortStringArray(badSqlGrammarCodes);
	}

	/**
	 * 获取 Bad Sql Grammar Codes（`BadSqlGrammarCodes`）。
	 */
	public String[] getBadSqlGrammarCodes() {
		return this.badSqlGrammarCodes;
	}

	/**
	 * 设置 Invalid Result Set Access Codes（`InvalidResultSetAccessCodes`）。
	 */
	public void setInvalidResultSetAccessCodes(String... invalidResultSetAccessCodes) {
		this.invalidResultSetAccessCodes = StringUtils.sortStringArray(invalidResultSetAccessCodes);
	}

	/**
	 * 获取 Invalid Result Set Access Codes（`InvalidResultSetAccessCodes`）。
	 */
	public String[] getInvalidResultSetAccessCodes() {
		return this.invalidResultSetAccessCodes;
	}

	/**
	 * 获取 Duplicate Key Codes（`DuplicateKeyCodes`）。
	 */
	public String[] getDuplicateKeyCodes() {
		return this.duplicateKeyCodes;
	}

	/**
	 * 设置 Duplicate Key Codes（`DuplicateKeyCodes`）。
	 */
	public void setDuplicateKeyCodes(String... duplicateKeyCodes) {
		this.duplicateKeyCodes = duplicateKeyCodes;
	}

	/**
	 * 设置 Data Integrity Violation Codes（`DataIntegrityViolationCodes`）。
	 */
	public void setDataIntegrityViolationCodes(String... dataIntegrityViolationCodes) {
		this.dataIntegrityViolationCodes = StringUtils.sortStringArray(dataIntegrityViolationCodes);
	}

	/**
	 * 获取 Data Integrity Violation Codes（`DataIntegrityViolationCodes`）。
	 */
	public String[] getDataIntegrityViolationCodes() {
		return this.dataIntegrityViolationCodes;
	}

	/**
	 * 设置 Permission Denied Codes（`PermissionDeniedCodes`）。
	 */
	public void setPermissionDeniedCodes(String... permissionDeniedCodes) {
		this.permissionDeniedCodes = StringUtils.sortStringArray(permissionDeniedCodes);
	}

	/**
	 * 获取 Permission Denied Codes（`PermissionDeniedCodes`）。
	 */
	public String[] getPermissionDeniedCodes() {
		return this.permissionDeniedCodes;
	}

	/**
	 * 设置 Data Access Resource Failure Codes（`DataAccessResourceFailureCodes`）。
	 */
	public void setDataAccessResourceFailureCodes(String... dataAccessResourceFailureCodes) {
		this.dataAccessResourceFailureCodes = StringUtils.sortStringArray(dataAccessResourceFailureCodes);
	}

	/**
	 * 获取 Data Access Resource Failure Codes（`DataAccessResourceFailureCodes`）。
	 */
	public String[] getDataAccessResourceFailureCodes() {
		return this.dataAccessResourceFailureCodes;
	}

	/**
	 * 设置 Transient Data Access Resource Codes（`TransientDataAccessResourceCodes`）。
	 */
	public void setTransientDataAccessResourceCodes(String... transientDataAccessResourceCodes) {
		this.transientDataAccessResourceCodes = StringUtils.sortStringArray(transientDataAccessResourceCodes);
	}

	/**
	 * 获取 Transient Data Access Resource Codes（`TransientDataAccessResourceCodes`）。
	 */
	public String[] getTransientDataAccessResourceCodes() {
		return this.transientDataAccessResourceCodes;
	}

	/**
	 * 设置 Cannot Acquire Lock Codes（`CannotAcquireLockCodes`）。
	 */
	public void setCannotAcquireLockCodes(String... cannotAcquireLockCodes) {
		this.cannotAcquireLockCodes = StringUtils.sortStringArray(cannotAcquireLockCodes);
	}

	/**
	 * 获取 Cannot Acquire Lock Codes（`CannotAcquireLockCodes`）。
	 */
	public String[] getCannotAcquireLockCodes() {
		return this.cannotAcquireLockCodes;
	}

	/**
	 * 设置 Deadlock Loser Codes（`DeadlockLoserCodes`）。
	 */
	public void setDeadlockLoserCodes(String... deadlockLoserCodes) {
		this.deadlockLoserCodes = StringUtils.sortStringArray(deadlockLoserCodes);
	}

	/**
	 * 获取 Deadlock Loser Codes（`DeadlockLoserCodes`）。
	 */
	public String[] getDeadlockLoserCodes() {
		return this.deadlockLoserCodes;
	}

	/**
	 * 设置 Cannot Serialize Transaction Codes（`CannotSerializeTransactionCodes`）。
	 */
	public void setCannotSerializeTransactionCodes(String... cannotSerializeTransactionCodes) {
		this.cannotSerializeTransactionCodes = StringUtils.sortStringArray(cannotSerializeTransactionCodes);
	}

	/**
	 * 获取 Cannot Serialize Transaction Codes（`CannotSerializeTransactionCodes`）。
	 */
	public String[] getCannotSerializeTransactionCodes() {
		return this.cannotSerializeTransactionCodes;
	}

	/**
	 * 设置 Custom Translations（`CustomTranslations`）。
	 */
	public void setCustomTranslations(CustomSQLErrorCodesTranslation... customTranslations) {
		this.customTranslations = customTranslations;
	}

	/**
	 * 获取 Custom Translations（`CustomTranslations`）。
	 */
	public CustomSQLErrorCodesTranslation @Nullable [] getCustomTranslations() {
		return this.customTranslations;
	}

	/**
	 * 设置 Custom Sql Exception Translator Class（`CustomSqlExceptionTranslatorClass`）。
	 */
	public void setCustomSqlExceptionTranslatorClass(@Nullable Class<? extends SQLExceptionTranslator> customTranslatorClass) {
		if (customTranslatorClass != null) {
			try {
				this.customSqlExceptionTranslator =
						ReflectionUtils.accessibleConstructor(customTranslatorClass).newInstance();
			}
			catch (Throwable ex) {
				throw new IllegalStateException("Unable to instantiate custom translator", ex);
			}
		}
		else {
			this.customSqlExceptionTranslator = null;
		}
	}

	/**
	 * 设置 Custom Sql Exception Translator（`CustomSqlExceptionTranslator`）。
	 */
	public void setCustomSqlExceptionTranslator(@Nullable SQLExceptionTranslator customSqlExceptionTranslator) {
		this.customSqlExceptionTranslator = customSqlExceptionTranslator;
	}

	/**
	 * 获取 Custom Sql Exception Translator（`CustomSqlExceptionTranslator`）。
	 */
	public @Nullable SQLExceptionTranslator getCustomSqlExceptionTranslator() {
		return this.customSqlExceptionTranslator;
	}

}
