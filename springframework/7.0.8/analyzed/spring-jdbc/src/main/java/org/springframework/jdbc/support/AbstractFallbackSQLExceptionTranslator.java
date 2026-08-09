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

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.dao.DataAccessException;
import org.springframework.util.Assert;

/**
 * {@link SQLExceptionTranslator} 实现的基类，允许回退到其他一些 {@link SQLExceptionTranslator}，以及自定义覆盖。
 * @author Juergen Hoeller
 * @since 2.5.6
 * @see #doTranslate
 * @see #setFallbackTranslator
 * @see #setCustomTranslator
 */
public abstract class AbstractFallbackSQLExceptionTranslator implements SQLExceptionTranslator {

	/**
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/** `fallbackTranslator`：该类的成员状态。 */
	private @Nullable SQLExceptionTranslator fallbackTranslator;

	/** `customTranslator`：该类的成员状态。 */
	private @Nullable SQLExceptionTranslator customTranslator;


	/**
	 * 设置当此翻译器本身无法找到特定匹配项时要使用的后备翻译器。
	 */
	public void setFallbackTranslator(@Nullable SQLExceptionTranslator fallback) {
		this.fallbackTranslator = fallback;
	}

	/**
	 * 返回后备异常转换器（如果有）。
	 * @see #setFallbackTranslator
	 */
	public @Nullable SQLExceptionTranslator getFallbackTranslator() {
		return this.fallbackTranslator;
	}

	/**
	 * 设置自定义异常翻译器以覆盖该翻译器找到的任何匹配项。请注意，如果此类自定义 {@link SQLExceptionTranslator} 委托本身没有覆盖，则它会返回 {@co
	 * de null}。
	 * @since 6.1
	 */
	public void setCustomTranslator(@Nullable SQLExceptionTranslator customTranslator) {
		this.customTranslator = customTranslator;
	}

	/**
	 * 返回自定义异常翻译器（如果有）。
	 * @since 6.1
	 * @see #setCustomTranslator
	 */
	public @Nullable SQLExceptionTranslator getCustomTranslator() {
		return this.customTranslator;
	}


	/**
	 * 预检查参数，调用 {@link #doTranslate}，并在必要时调用 {@link #getFallbackTranslator() fallback
	 * translator}。
	 */
	@Override
	public @Nullable DataAccessException translate(String task, @Nullable String sql, SQLException ex) {
		Assert.notNull(ex, "Cannot translate a null SQLException");

		SQLExceptionTranslator custom = getCustomTranslator();
		if (custom != null) {
			DataAccessException dae = custom.translate(task, sql, ex);
			if (dae != null) {
				// 找到自定义异常匹配。
				return dae;
			}
		}

		DataAccessException dae = doTranslate(task, sql, ex);
		if (dae != null) {
			// 找到特定的异常匹配。
			return dae;
		}

		// 寻找后备...
		SQLExceptionTranslator fallback = getFallbackTranslator();
		if (fallback != null) {
			return fallback.translate(task, sql, ex);
		}

		return null;
	}

	/**
	 * 用于实际翻译给定异常的模板方法。 <p>传入的参数将被预先检查。此外，该方法允许返回 {@code null} 以指示未找到异常匹配并且应启动后备翻译。
	 * @param task 描述正在尝试的任务的可读文本
	 * @param sql 导致问题的 SQL 查询或更新（如果已知）
	 * @param ex 有问题的 {@code SQLException}
	 * @return DataAccessException，包装 {@code SQLException}；或 {@code null}（如果未找到异常匹配）
	 */
	protected abstract @Nullable DataAccessException doTranslate(String task, @Nullable String sql, SQLException ex);


	/**
	 * 为给定的 {@link java.sql.SQLException} 构建消息 {@code String}。 <p> 在创建通用 {@link
	 * org.springframework.dao.DataAccessException} 类的实例时由转换器子类调用。
	 * @param task 描述正在尝试的任务的可读文本
	 * @param sql 导致问题的 SQL 语句
	 * @param ex 有问题的 {@code SQLException}
	 * @return 要使用的消息 {@code String}
	 */
	protected String buildMessage(String task, @Nullable String sql, SQLException ex) {
		return task + "; " + (sql != null ? ("SQL [" + sql + "]; ") : "") + ex.getMessage();
	}

}
