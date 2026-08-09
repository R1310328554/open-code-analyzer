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
 * {@link SqlParameterSource} 实现，从给定 JavaBean 对象的 bean 属性、记录类的组件访问器或原始字段访问获取参数值。
 * <p>这是 {@link BeanPropertySqlParameterSource} 的更灵活的变体，其限制是它无法枚举其 {@link
 * #getParameterNames() parameter names}。
 * <p>就其后备属性发现算法而言，该类与 {@link org.springframework.validation.SimpleErrors}
 * 类似，后者也仅用于属性检索目的（而不是绑定）。
 * @author Juergen Hoeller
 * @since 6.1
 * @see NamedParameterJdbcTemplate
 * @see BeanPropertySqlParameterSource
 * @see org.springframework.jdbc.core.simple.JdbcClient.StatementSpec#paramSource(Object)
 * @see org.springframework.jdbc.core.SimplePropertyRowMapper
 */
public class SimplePropertySqlParameterSource extends AbstractSqlParameterSource {

	/**
	 * 方法 `Object`：完成本类中与「Object」相关的职责。
	 */
	private static final Object NO_DESCRIPTOR = new Object();

	/** `paramObject`：该类的成员状态。 */
	private final Object paramObject;

	private final Map<String, Object> propertyDescriptors = new ConcurrentHashMap<>();


	/**
	 * 为给定的 bean、记录或字段持有者创建一个新的 SqlParameterSource。
	 * @param paramObject 要包装的 bean、记录或字段持有者实例
	 */
	public SimplePropertySqlParameterSource(Object paramObject) {
		Assert.notNull(paramObject, "Parameter object must not be null");
		this.paramObject = paramObject;
	}


	/**
	 * 判断是否包含/具备 Value。
	 */
	@Override
	public boolean hasValue(String paramName) {
		return (getDescriptor(paramName) != NO_DESCRIPTOR);
	}

	/**
	 * 获取 Value（`Value`）。
	 */
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
	 * 从相应的属性类型派生默认 SQL 类型。
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

	/**
	 * 获取 Descriptor（`Descriptor`）。
	 */
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
