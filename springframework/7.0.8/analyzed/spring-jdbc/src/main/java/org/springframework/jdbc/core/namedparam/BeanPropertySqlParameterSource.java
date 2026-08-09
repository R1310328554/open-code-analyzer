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
import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.NotReadablePropertyException;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.util.StringUtils;

/**
 * {@link SqlParameterSource} 实现，从给定 JavaBean 对象的 bean 属性获取参数值。 bean 属性的名称必须与参数名称匹配。还支持记录类的
 * 组件，访问器方法与参数名称匹配。
 * <p> 使用 Spring {@link BeanWrapper} 进行底层 bean 属性访问。
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @since 2.0
 * @see NamedParameterJdbcTemplate
 * @see SimplePropertySqlParameterSource
 */
public class BeanPropertySqlParameterSource extends AbstractSqlParameterSource {

	/** Bean相关状态（`beanWrapper`）。 */
	private final BeanWrapper beanWrapper;

	/** 名称相关状态（`propertyNames`）。 */
	private String @Nullable [] propertyNames;


	/**
	 * 为给定的 bean 创建一个新的 BeanPropertySqlParameterSource。
	 * @param object 要包装的 bean 实例
	 */
	public BeanPropertySqlParameterSource(Object object) {
		this.beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(object);
	}


	/**
	 * 判断是否包含/具备 Value。
	 */
	@Override
	public boolean hasValue(String paramName) {
		return this.beanWrapper.isReadableProperty(paramName);
	}

	/**
	 * 获取 Value（`Value`）。
	 */
	@Override
	public @Nullable Object getValue(String paramName) throws IllegalArgumentException {
		try {
			return this.beanWrapper.getPropertyValue(paramName);
		}
		catch (NotReadablePropertyException ex) {
			throw new IllegalArgumentException(ex.getMessage());
		}
	}

	/**
	 * 从相应的属性类型派生默认 SQL 类型。
	 * @see org.springframework.jdbc.core.StatementCreatorUtils#javaTypeToSqlParameterType
	 */
	@Override
	public int getSqlType(String paramName) {
		int sqlType = super.getSqlType(paramName);
		if (sqlType != TYPE_UNKNOWN) {
			return sqlType;
		}
		Class<?> propType = this.beanWrapper.getPropertyType(paramName);
		return StatementCreatorUtils.javaTypeToSqlParameterType(propType);
	}

	/**
	 * 获取 Parameter Names（`ParameterNames`）。
	 */
	@Override
	public String[] getParameterNames() {
		return getReadablePropertyNames();
	}

	/**
	 * 提供对包装 bean 的属性名称的访问。使用 {@link PropertyAccessor} 接口中提供的支持。
	 * @return 包含所有已知属性名称的数组
	 */
	public String[] getReadablePropertyNames() {
		if (this.propertyNames == null) {
			List<String> names = new ArrayList<>();
			PropertyDescriptor[] props = this.beanWrapper.getPropertyDescriptors();
			for (PropertyDescriptor pd : props) {
				if (this.beanWrapper.isReadableProperty(pd.getName())) {
					names.add(pd.getName());
				}
			}
			this.propertyNames = StringUtils.toStringArray(names);
		}
		return this.propertyNames;
	}

}
