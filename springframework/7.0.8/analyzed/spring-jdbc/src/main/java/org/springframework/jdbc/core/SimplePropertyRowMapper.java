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

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeanUtils;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * {@link RowMapper} 实现将行转换为指定映射目标类的新实例。映射的目标类必须是顶级类或 {@code static} 嵌套类，并且它可以公开具有与列名称相对应的命
 * 名参数的 <em> 数据类 </em> 构造函数，或者具有与列名称相对应的属性名称或具有相应字段名称的字段的经典 bean 属性设置方法。
 * <p>将数据类构造函数与 setter 方法组合时，任何通过构造函数参数成功映射的属性都不会通过相应的 setter 方法或字段映射进行额外映射。这意味着构造函数参数优先于属性
 * 设置方法，而属性设置方法又优先于直接字段映射。
 * <p>为了促进没有匹配名称的列和属性之间的映射，请尝试在 SQL 语句中使用下划线分隔的列别名，例如 {@code "select fname as first_name fr
 * om customer"}，其中 {@code first_name} 可以映射到目标类中的 {@code setFirstName(String)} 方法。
 * <p>这是 {@link DataClassRowMapper} 和 {@link BeanPropertyRowMapper}
 * 的灵活替代方案，适用于不需要特定定制和预定义属性映射的场景。
 * <p>就其后备属性发现算法而言，该类与{@link org.springframework.jdbc.core.namedparam.SimplePropertySqlPara
 * meterSource}类似，并且类似地用于{@link org.springframework.jdbc.core.simple.JdbcClient}。
 * @author Juergen Hoeller
 * @since 6.1
 * @param <T> 结果类型
 * @see DataClassRowMapper
 * @see BeanPropertyRowMapper
 * @see org.springframework.jdbc.core.simple.JdbcClient.StatementSpec#query(Class)
 * @see org.springframework.jdbc.core.namedparam.SimplePropertySqlParameterSource
 */
public class SimplePropertyRowMapper<T> implements RowMapper<T> {

	/**
	 * 方法 `Object`：完成本类中与「Object」相关的职责。
	 */
	private static final Object NO_DESCRIPTOR = new Object();

	/** 类相关状态（`mappedClass`）。 */
	private final Class<T> mappedClass;

	/** `conversionService`：该类的成员状态。 */
	private final ConversionService conversionService;

	/** 构造器相关状态（`mappedConstructor`）。 */
	private final Constructor<T> mappedConstructor;

	/** 构造器相关状态（`constructorParameterNames`）。 */
	private final @Nullable String[] constructorParameterNames;

	/** 构造器相关状态（`constructorParameterTypes`）。 */
	private final TypeDescriptor[] constructorParameterTypes;

	private final Map<String, Object> propertyDescriptors = new ConcurrentHashMap<>();


	/**
	 * 创建一个新的 {@code SimplePropertyRowMapper}。
	 * @param mappedClass 每行应映射到的类
	 */
	public SimplePropertyRowMapper(Class<T> mappedClass) {
		this(mappedClass, DefaultConversionService.getSharedInstance());
	}

	/**
	 * 创建一个新的 {@code SimplePropertyRowMapper}。
	 * @param mappedClass 每行应映射到的类
	 * @param conversionService 用于将 JDBC 值绑定到 bean 属性的 {@link ConversionService}
	 */
	public SimplePropertyRowMapper(Class<T> mappedClass, ConversionService conversionService) {
		Assert.notNull(mappedClass, "Mapped Class must not be null");
		Assert.notNull(conversionService, "ConversionService must not be null");
		this.mappedClass = mappedClass;
		this.conversionService = conversionService;

		this.mappedConstructor = BeanUtils.getResolvableConstructor(mappedClass);
		int paramCount = this.mappedConstructor.getParameterCount();
		this.constructorParameterNames = (paramCount > 0 ?
				BeanUtils.getParameterNames(this.mappedConstructor) : new String[0]);
		this.constructorParameterTypes = new TypeDescriptor[paramCount];
		for (int i = 0; i < paramCount; i++) {
			this.constructorParameterTypes[i] = new TypeDescriptor(new MethodParameter(this.mappedConstructor, i));
		}
	}


	/**
	 * 映射：Row（方法 `mapRow`）。
	 */
	@Override
	public T mapRow(ResultSet rs, int rowNumber) throws SQLException {
		@Nullable Object[] args = new Object[this.constructorParameterNames.length];
		Set<Integer> usedIndex = new HashSet<>();
		for (int i = 0; i < args.length; i++) {
			String name = this.constructorParameterNames[i];
			int index;
			try {
				// 首先尝试直接名称匹配
				index = rs.findColumn(name);
			}
			catch (SQLException ex) {
				// 尝试使用下划线名称匹配
				index = rs.findColumn(JdbcUtils.convertPropertyNameToUnderscoreName(name));
			}
			TypeDescriptor td = this.constructorParameterTypes[i];
			Object value = JdbcUtils.getResultSetValue(rs, index, td.getType());
			usedIndex.add(index);
			args[i] = this.conversionService.convert(value, td);
		}
		T mappedObject = BeanUtils.instantiateClass(this.mappedConstructor, args);

		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		for (int index = 1; index <= columnCount; index++) {
			if (!usedIndex.contains(index)) {
				Object desc = getDescriptor(JdbcUtils.lookupColumnName(rsmd, index));
				if (desc instanceof MethodParameter mp) {
					Method method = mp.getMethod();
					if (method != null) {
						Object value = JdbcUtils.getResultSetValue(rs, index, mp.getParameterType());
						value = this.conversionService.convert(value, new TypeDescriptor(mp));
						ReflectionUtils.makeAccessible(method);
						ReflectionUtils.invokeMethod(method, mappedObject, value);
					}
				}
				else if (desc instanceof Field field) {
					Object value = JdbcUtils.getResultSetValue(rs, index, field.getType());
					value = this.conversionService.convert(value, new TypeDescriptor(field));
					ReflectionUtils.makeAccessible(field);
					ReflectionUtils.setField(field, mappedObject, value);
				}
			}
		}

		return mappedObject;
	}

	/**
	 * 获取 Descriptor（`Descriptor`）。
	 */
	private Object getDescriptor(String column) {
		return this.propertyDescriptors.computeIfAbsent(column, name -> {

			// 首先尝试直接匹配
			PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(this.mappedClass, name);
			if (pd != null && pd.getWriteMethod() != null) {
				return BeanUtils.getWriteMethodParameter(pd);
			}
			Field field = ReflectionUtils.findField(this.mappedClass, name);
			if (field != null) {
				return field;
			}

			// 尝试改为取消下划线匹配
			String adaptedName = JdbcUtils.convertUnderscoreNameToPropertyName(name);
			if (!adaptedName.equals(name)) {
				pd = BeanUtils.getPropertyDescriptor(this.mappedClass, adaptedName);
				if (pd != null && pd.getWriteMethod() != null) {
					return BeanUtils.getWriteMethodParameter(pd);
				}
				field = ReflectionUtils.findField(this.mappedClass, adaptedName);
				if (field != null) {
					return field;
				}
			}

			// Fallback：不区分大小写的匹配
			PropertyDescriptor[] pds = BeanUtils.getPropertyDescriptors(this.mappedClass);
			for (PropertyDescriptor candidate : pds) {
				if (name.equalsIgnoreCase(candidate.getName())) {
					return BeanUtils.getWriteMethodParameter(candidate);
				}
			}
			field = ReflectionUtils.findFieldIgnoreCase(this.mappedClass, name);
			if (field != null) {
				return field;
			}

			return NO_DESCRIPTOR;
		});
	}

}
