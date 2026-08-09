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
 * {@link SQLExceptionTranslator} 实现的基类，支持回退到其他 {@link SQLExceptionTranslator}，
 * 以及自定义覆盖。
 *
 * @author Juergen Hoeller
 * @since 2.5.6
 * @see #doTranslate
 * @see #setFallbackTranslator
 * @see #setCustomTranslator
 */
public abstract class AbstractFallbackSQLExceptionTranslator implements SQLExceptionTranslator {

	/** 供子类使用的 Logger。 */
	protected final Log logger = LogFactory.getLog(getClass());

	private @Nullable SQLExceptionTranslator fallbackTranslator;

	private @Nullable SQLExceptionTranslator customTranslator;


	/**
	 * 设置本翻译器无法匹配时使用的回退翻译器。
	 */
	public void setFallbackTranslator(@Nullable SQLExceptionTranslator fallback) {
		this.fallbackTranslator = fallback;
	}

	/**
	 * 返回回退异常翻译器（若有）。
	 * @see #setFallbackTranslator
	 */
	public @Nullable SQLExceptionTranslator getFallbackTranslator() {
		return this.fallbackTranslator;
	}

	/**
	 * 设置自定义异常翻译器，覆盖本翻译器可能找到的任何匹配。
	 * 注意：自定义 {@link SQLExceptionTranslator} 委托在无自身覆盖时应返回 {@code null}。
	 * @since 6.1
	 */
	public void setCustomTranslator(@Nullable SQLExceptionTranslator customTranslator) {
		this.customTranslator = customTranslator;
	}

	/**
	 * 返回自定义异常翻译器（若有）。
	 * @since 6.1
	 * @see #setCustomTranslator
	 */
	public @Nullable SQLExceptionTranslator getCustomTranslator() {
		return this.customTranslator;
	}


	/**
	 * 预检查参数，调用 {@link #doTranslate}，必要时调用 {@link #getFallbackTranslator() 回退翻译器}。
	 */
	@Override
	public @Nullable DataAccessException translate(String task, @Nullable String sql, SQLException ex) {
		Assert.notNull(ex, "Cannot translate a null SQLException");

		SQLExceptionTranslator custom = getCustomTranslator();
		if (custom != null) {
			DataAccessException dae = custom.translate(task, sql, ex);
			if (dae != null) {
				// Custom exception match found.
				return dae;
			}
		}

		DataAccessException dae = doTranslate(task, sql, ex);
		if (dae != null) {
			// Specific exception match found.
			return dae;
		}

		// Looking for a fallback...
		SQLExceptionTranslator fallback = getFallbackTranslator();
		if (fallback != null) {
			return fallback.translate(task, sql, ex);
		}

		return null;
	}

	/**
	 * 实际翻译给定异常的模板方法。
	 * <p>传入参数已预检查；本方法可返回 {@code null} 表示未找到匹配，应启用回退翻译。
	 * @param task 描述正在尝试任务的可读文本
	 * @param sql 导致问题的 SQL 查询或更新（若已知）
	 * @param ex 触发的 {@code SQLException}
	 * @return 包装 {@code SQLException} 的 DataAccessException；未找到匹配时 {@code null}
	 */
	protected abstract @Nullable DataAccessException doTranslate(String task, @Nullable String sql, SQLException ex);


	/**
	 * 为给定 {@link java.sql.SQLException} 构建消息 {@code String}。
	 * <p>翻译器子类创建通用 {@link org.springframework.dao.DataAccessException} 实例时调用。
	 * @param task 描述正在尝试任务的可读文本
	 * @param sql 导致问题的 SQL 语句
	 * @param ex 触发的 {@code SQLException}
	 * @return 要使用的消息 {@code String}
	 */
	protected String buildMessage(String task, @Nullable String sql, SQLException ex) {
		return task + "; " + (sql != null ? ("SQL [" + sql + "]; ") : "") + ex.getMessage();
	}

}
