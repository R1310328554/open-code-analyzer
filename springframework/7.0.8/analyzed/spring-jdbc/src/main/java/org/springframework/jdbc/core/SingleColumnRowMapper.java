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

package org.springframework.jdbc.core;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.jspecify.annotations.Nullable;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.dao.TypeMismatchDataAccessException;
import org.springframework.jdbc.IncorrectResultSetColumnCountException;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.NumberUtils;

/**
 * {@link RowMapper} 实现将单个列转换为每行单个结果值。期望在仅包含单个列的 {@code java.sql.ResultSet} 上进行操作。
 * <p>可以指定每行的结果值的类型。单列的值将从 {@code ResultSet} 中提取并转换为指定的目标类型。
 * @author Juergen Hoeller
 * @author Kazuki Shimizu
 * @since 1.2
 * @param <T> 结果类型
 * @see JdbcTemplate#queryForList(String, Class)
 * @see JdbcTemplate#queryForObject(String, Class)
 */
public class SingleColumnRowMapper<T> implements RowMapper<@Nullable T> {

	/** 类型相关状态（`requiredType`）。 */
	private @Nullable Class<?> requiredType;

	/**
	 * 获取 Shared Instance（`SharedInstance`）。
	 */
	private @Nullable ConversionService conversionService = DefaultConversionService.getSharedInstance();


	/**
	 * 创建一个新的 {@code SingleColumnRowMapper} 用于 bean 样式配置。
	 * @see #setRequiredType
	 */
	public SingleColumnRowMapper() {
	}

	/**
	 * 创建一个新的 {@code SingleColumnRowMapper}。
	 * @param requiredType 每个结果对象期望匹配的类型
	 */
	public SingleColumnRowMapper(Class<T> requiredType) {
		if (requiredType != Object.class) {
			setRequiredType(requiredType);
		}
	}

	/**
	 * 创建一个新的 {@code SingleColumnRowMapper}。
	 * @param requiredType 每个结果对象期望匹配的类型
	 * @param conversionService 用于转换获取值的 {@link ConversionService}
	 * @since 7.0
	 */
	public SingleColumnRowMapper(Class<T> requiredType, @Nullable ConversionService conversionService) {
		if (requiredType != Object.class) {
			setRequiredType(requiredType);
		}
		setConversionService(conversionService);
	}


	/**
	 * 设置每个结果对象期望匹配的类型。 <p>如果未指定，则列值将公开为 JDBC 驱动程序返回的值。
	 */
	public void setRequiredType(Class<T> requiredType) {
		this.requiredType = ClassUtils.resolvePrimitiveIfNecessary(requiredType);
	}

	/**
	 * 设置 {@link ConversionService} 用于转换获取的值。 <p>默认是{@link DefaultConversionService}。
	 * @since 5.0.4
	 * @see DefaultConversionService#getSharedInstance()
	 */
	public void setConversionService(@Nullable ConversionService conversionService) {
		this.conversionService = conversionService;
	}


	/**
	 * 提取当前行中单列的值。 <p> 验证仅选择一列，然后委托给 {@code getColumnValue()} 以及 {@code
	 * convertValueToRequiredType}（如有必要）。
	 * @see java.sql.ResultSetMetaData#getColumnCount()
	 * @see #getColumnValue(java.sql.ResultSet, int, Class)
	 * @see #convertValueToRequiredType(Object, Class)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public @Nullable T mapRow(ResultSet rs, int rowNum) throws SQLException {
		// 验证列数。
		ResultSetMetaData rsmd = rs.getMetaData();
		int nrOfColumns = rsmd.getColumnCount();
		if (nrOfColumns != 1) {
			throw new IncorrectResultSetColumnCountException(1, nrOfColumns);
		}

		// 从 JDBC 结果集中提取列值。
		Object result = getColumnValue(rs, 1, this.requiredType);
		if (result != null && this.requiredType != null && !this.requiredType.isInstance(result)) {
			// 提取的值已不匹配：尝试转换它。
			try {
				return (T) convertValueToRequiredType(result, this.requiredType);
			}
			catch (IllegalArgumentException ex) {
				throw new TypeMismatchDataAccessException(
						"Type mismatch affecting row number " + rowNum + " and column type '" +
						rsmd.getColumnTypeName(1) + "': " + ex.getMessage());
			}
		}
		return (T) result;
	}

	/**
	 * 检索指定列的 JDBC 对象值。 <p>默认实现调用{@link JdbcUtils#getResultSetValue(java.sql.ResultSet, int,
	 * Class)}。如果未指定所需类型，则此方法委托给 {@code getColumnValue(rs, index)}，它基本上调用 {@code
	 * ResultSet.getObject(index)}，但将一些额外的默认转换应用于适当的值类型。
	 * @param rs 是保存数据的 ResultSet
	 * @param index 是列索引
	 * @param requiredType 每个结果对象期望匹配的类型（如果未指定，则为 {@code null}）
	 * @return 对象价值
	 * @throws SQLException 如果提取失败
	 * @see org.springframework.jdbc.support.JdbcUtils#getResultSetValue(java.sql.ResultSet, int, Class)
	 * @see #getColumnValue(java.sql.ResultSet, int)
	 */
	protected @Nullable Object getColumnValue(ResultSet rs, int index, @Nullable Class<?> requiredType) throws SQLException {
		if (requiredType != null) {
			return JdbcUtils.getResultSetValue(rs, index, requiredType);
		}
		else {
			// 未指定所需类型 -> 执行默认提取。
			return getColumnValue(rs, index);
		}
	}

	/**
	 * 使用最合适的值类型检索指定列的 JDBC 对象值。如果未指定所需类型，则调用。 <p>默认实现委托给{@code
	 * JdbcUtils.getResultSetValue()}，它使用{@code
	 * ResultSet.getObject(index)}方法。此外，它还包括一个“hack”，可以绕过 Oracle 返回其 TIMESTAMP
	 * 数据类型的非标准对象。有关详细信息，请参阅 {@code JdbcUtils#getResultSetValue()} javadoc。
	 * @param rs 是保存数据的 ResultSet
	 * @param index 是列索引
	 * @return 对象价值
	 * @throws SQLException 如果提取失败
	 * @see org.springframework.jdbc.support.JdbcUtils#getResultSetValue(java.sql.ResultSet, int)
	 */
	protected @Nullable Object getColumnValue(ResultSet rs, int index) throws SQLException {
		return JdbcUtils.getResultSetValue(rs, index);
	}

	/**
	 * 将给定的列值转换为指定的所需类型。仅当提取的列值不匹配时才调用。 <p>如果所需的类型是字符串，则该值将通过 {@code toString()} 简单地进行字符串化。如果是数
	 * 字，则该值将通过数字转换或字符串解析（取决于值类型）转换为数字。否则，该值将使用 {@link ConversionService} 转换为所需的类型。
	 * @param value 从 {@code getColumnValue()} 中提取的列值（绝不是 {@code null}）
	 * @param requiredType 每个结果对象期望匹配的类型（绝不是 {@code null}）
	 * @return 换算值
	 * @see #getColumnValue(java.sql.ResultSet, int, Class)
	 */
	@SuppressWarnings("unchecked")
	protected @Nullable Object convertValueToRequiredType(Object value, Class<?> requiredType) {
		if (String.class == requiredType) {
			return value.toString();
		}
		else if (Number.class.isAssignableFrom(requiredType)) {
			if (value instanceof Number number) {
				// 将原始 Number 转换为目标 Number 类。
				return NumberUtils.convertNumberToTargetClass(number, (Class<Number>) requiredType);
			}
			else {
				// 将字符串化值转换为目标 Number 类。
				return NumberUtils.parseNumber(value.toString(),(Class<Number>) requiredType);
			}
		}
		else if (this.conversionService != null && this.conversionService.canConvert(value.getClass(), requiredType)) {
			return this.conversionService.convert(value, requiredType);
		}
		else {
			throw new IllegalArgumentException(
					"Value [" + value + "] is of type [" + value.getClass().getName() +
					"] and cannot be converted to required type [" + requiredType.getName() + "]");
		}
	}


	/**
	 * 创建新的 {@code SingleColumnRowMapper} 的静态工厂方法。
	 * @param requiredType 每个结果对象期望匹配的类型
	 * @since 4.1
	 * @see #newInstance(Class, ConversionService)
	 */
	public static <T> SingleColumnRowMapper<T> newInstance(Class<T> requiredType) {
		return new SingleColumnRowMapper<>(requiredType);
	}

	/**
	 * 创建新的 {@code SingleColumnRowMapper} 的静态工厂方法。
	 * @param requiredType 每个结果对象期望匹配的类型
	 * @param conversionService {@link ConversionService} 用于转换获取的值，或 {@code null} 用于无转换
	 * @since 5.0.4
	 * @see #newInstance(Class)
	 * @see #setConversionService
	 */
	public static <T> SingleColumnRowMapper<T> newInstance(
			Class<T> requiredType, @Nullable ConversionService conversionService) {

		SingleColumnRowMapper<T> rowMapper = newInstance(requiredType);
		rowMapper.setConversionService(conversionService);
		return rowMapper;
	}

}
