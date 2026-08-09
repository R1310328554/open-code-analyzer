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

package org.springframework.jdbc.core.namedparam;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * 从给定 JavaBean 的 bean 属性、record 类的组件访问器
 * 或原始字段访问获取参数值的 {@link SqlParameterSource} 实现。
 *
 * <p>这是 {@link BeanPropertySqlParameterSource} 更灵活的变体，
 * 限制在于无法枚举其 {@link #getParameterNames() 参数名称}。
 *
 * <p>在回退属性发现算法方面，本类与
 * {@link org.springframework.validation.SimpleErrors} 类似，
 * 同样仅用于属性检索（而非绑定）。
 *
 * @author Juergen Hoeller
 * @since 6.1
 * @see NamedParameterJdbcTemplate
 * @see BeanPropertySqlParameterSource
 * @see org.springframework.jdbc.core.simple.JdbcClient.StatementSpec#paramSource(Object)
 * @see org.springframework.jdbc.core.SimplePropertyRowMapper
 */
public class SimplePropertySqlParameterSource extends AbstractSqlParameterSource {

	private static final Object NO_DESCRIPTOR = new Object();

	private final Object paramObject;

	private final Map<String, Object> propertyDescriptors = new ConcurrentHashMap<>();


	/**
	 * 为给定 bean、record 或字段持有者创建新的 SqlParameterSource。
	 * @param paramObject 要包装的 bean、record 或字段持有者实例
	 */
	public SimplePropertySqlParameterSource(Object paramObject) {
		Assert.notNull(paramObject, "Parameter object must not be null");
		this.paramObject = paramObject;
	}


	@Override
	public boolean hasValue(String paramName) {
		return (getDescriptor(paramName) != NO_DESCRIPTOR);
	}

	@Override
	public @Nullable Object getValue(String paramName) throws IllegalArgumentException {
		Object desc = getDescriptor(paramName);
		if (desc instanceof PropertyDescriptor pd) {
			ReflectionUtils.makeAccessible(pd.getReadMethod());
			return ReflectionUtils.invokeMethod(pd.getReadMethod(), this.paramObject);
		}
		else if (desc instanceof Field field) {
			ReflectionUtils.makeAccessible(field);
			return ReflectionUtils.getField(field, this.paramObject);
		}
		throw new IllegalArgumentException("Cannot retrieve value for parameter '" + paramName +
				"' - neither a getter method nor a raw field found");
	}

	/**
	 * 从对应属性类型推导默认 SQL 类型。
	 * @see StatementCreatorUtils#javaTypeToSqlParameterType
	 */
	@Override
	public int getSqlType(String paramName) {
		int sqlType = super.getSqlType(paramName);
		if (sqlType != TYPE_UNKNOWN) {
			return sqlType;
		}
		Object desc = getDescriptor(paramName);
		if (desc instanceof PropertyDescriptor pd) {
			return StatementCreatorUtils.javaTypeToSqlParameterType(pd.getPropertyType());
		}
		else if (desc instanceof Field field) {
			return StatementCreatorUtils.javaTypeToSqlParameterType(field.getType());
		}
		return TYPE_UNKNOWN;
	}

	private Object getDescriptor(String paramName) {
		return this.propertyDescriptors.computeIfAbsent(paramName, name -> {
			PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(this.paramObject.getClass(), name);
			if (pd != null && pd.getReadMethod() != null) {
				return pd;
			}
			Field field = ReflectionUtils.findField(this.paramObject.getClass(), name);
			if (field != null) {
				return field;
			}
			return NO_DESCRIPTOR;
		});
	}

}
