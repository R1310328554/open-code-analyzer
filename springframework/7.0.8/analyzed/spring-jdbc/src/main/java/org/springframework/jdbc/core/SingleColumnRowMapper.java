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
 * {@link RowMapper} 实现，将单列转换为每行一个结果值。
 * 期望操作的 {@code java.sql.ResultSet} 仅包含单列。
 *
 * <p>可指定每行结果值的类型。单列值将从 {@code ResultSet} 提取
 * 并转换为指定的目标类型。
 *
 * @author Juergen Hoeller
 * @author Kazuki Shimizu
 * @since 1.2
 * @param <T> 结果类型
 * @see JdbcTemplate#queryForList(String, Class)
 * @see JdbcTemplate#queryForObject(String, Class)
 */
public class SingleColumnRowMapper<T> implements RowMapper<@Nullable T> {

	private @Nullable Class<?> requiredType;

	private @Nullable ConversionService conversionService = DefaultConversionService.getSharedInstance();


	/**
	 * 创建新的 {@code SingleColumnRowMapper}，用于 Bean 风格配置。
	 * @see #setRequiredType
	 */
	public SingleColumnRowMapper() {
	}

	/**
	 * 创建新的 {@code SingleColumnRowMapper}。
	 * @param requiredType 每个结果对象期望匹配的类型
	 */
	public SingleColumnRowMapper(Class<T> requiredType) {
		if (requiredType != Object.class) {
			setRequiredType(requiredType);
		}
	}

	/**
	 * 创建新的 {@code SingleColumnRowMapper}。
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
	 * 设置每个结果对象期望匹配的类型。
	 * <p>若未指定，列值将按 JDBC 驱动返回的形式暴露。
	 */
	public void setRequiredType(Class<T> requiredType) {
		this.requiredType = ClassUtils.resolvePrimitiveIfNecessary(requiredType);
	}

	/**
	 * 设置用于转换获取值的 {@link ConversionService}。
	 * <p>默认为 {@link DefaultConversionService}。
	 * @since 5.0.4
	 * @see DefaultConversionService#getSharedInstance()
	 */
	public void setConversionService(@Nullable ConversionService conversionService) {
		this.conversionService = conversionService;
	}


	/**
	 * 提取当前行单列的值。
	 * <p>验证仅选择了一列，然后委托 {@code getColumnValue()}，
	 * 必要时还调用 {@code convertValueToRequiredType}。
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

		// 从 JDBC ResultSet 提取列值。
		Object result = getColumnValue(rs, 1, this.requiredType);
		if (result != null && this.requiredType != null && !this.requiredType.isInstance(result)) {
			// 提取的值类型不匹配：尝试转换。
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
	 * 获取指定列的 JDBC 对象值。
	 * <p>默认实现调用
	 * {@link JdbcUtils#getResultSetValue(java.sql.ResultSet, int, Class)}。
	 * 若未指定 requiredType，则委托 {@code getColumnValue(rs, index)}，
	 * 基本调用 {@code ResultSet.getObject(index)}，
	 * 但会应用额外的默认转换到合适的值类型。
	 * @param rs 持有数据的 ResultSet
	 * @param index 列索引
	 * @param requiredType 每个结果对象期望匹配的类型
	 * （未指定则为 {@code null}）
	 * @return 对象值
	 * @throws SQLException 提取失败时
	 * @see org.springframework.jdbc.support.JdbcUtils#getResultSetValue(java.sql.ResultSet, int, Class)
	 * @see #getColumnValue(java.sql.ResultSet, int)
	 */
	protected @Nullable Object getColumnValue(ResultSet rs, int index, @Nullable Class<?> requiredType) throws SQLException {
		if (requiredType != null) {
			return JdbcUtils.getResultSetValue(rs, index, requiredType);
		}
		else {
			// 未指定 requiredType -> 执行默认提取。
			return getColumnValue(rs, index);
		}
	}

	/**
	 * 获取指定列的 JDBC 对象值，使用最合适的值类型。
	 * 未指定 requiredType 时调用。
	 * <p>默认实现委托 {@code JdbcUtils.getResultSetValue()}，
	 * 使用 {@code ResultSet.getObject(index)} 方法。
	 * 此外还包含处理 Oracle TIMESTAMP 返回非标准对象的变通方案。
	 * 详情参见 {@code JdbcUtils#getResultSetValue()} 的 javadoc。
	 * @param rs 持有数据的 ResultSet
	 * @param index 列索引
	 * @return 对象值
	 * @throws SQLException 提取失败时
	 * @see org.springframework.jdbc.support.JdbcUtils#getResultSetValue(java.sql.ResultSet, int)
	 */
	protected @Nullable Object getColumnValue(ResultSet rs, int index) throws SQLException {
		return JdbcUtils.getResultSetValue(rs, index);
	}

	/**
	 * 将给定的列值转换为指定的 requiredType。
	 * 仅在提取的列值类型不匹配时调用。
	 * <p>若 requiredType 为 String，通过 {@code toString()} 字符串化。
	 * 若为 Number，通过数值转换或字符串解析（取决于值类型）转换为 Number。
	 * 否则通过 {@link ConversionService} 转换为 requiredType。
	 * @param value 从 {@code getColumnValue()} 提取的列值（永不为 {@code null}）
	 * @param requiredType 每个结果对象期望匹配的类型（永不为 {@code null}）
	 * @return 转换后的值
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
				// 将字符串化的值转换为目标 Number 类。
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
	 * 创建新 {@code SingleColumnRowMapper} 的静态工厂方法。
	 * @param requiredType 每个结果对象期望匹配的类型
	 * @since 4.1
	 * @see #newInstance(Class, ConversionService)
	 */
	public static <T> SingleColumnRowMapper<T> newInstance(Class<T> requiredType) {
		return new SingleColumnRowMapper<>(requiredType);
	}

	/**
	 * 创建新 {@code SingleColumnRowMapper} 的静态工厂方法。
	 * @param requiredType 每个结果对象期望匹配的类型
	 * @param conversionService 用于转换获取值的 {@link ConversionService}，
	 * 或 {@code null} 表示不使用
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
