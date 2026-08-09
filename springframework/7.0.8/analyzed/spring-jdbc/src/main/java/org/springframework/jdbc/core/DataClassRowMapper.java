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

import java.lang.reflect.Constructor;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.TypeConverter;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.util.Assert;

/**
 * 将行映射为指定目标类新实例的 {@link RowMapper} 实现。
 * 目标类须为顶层类或 {@code static} 嵌套类，可提供与列名对应的
 * <em>数据类</em> 命名参数构造器，或属性名对应列名的经典 setter（或两者兼有）。
 *
 * <p>「数据类」指 Java <em>record</em>、Kotlin <em>data class</em>，
 * 以及具命名参数构造器且参数拟映射到列名的任意类。
 *
 * <p>构造器与 setter 并用时，已通过构造器参数映射的属性
 * 不再经对应 setter 映射；构造器参数优先于 setter。
 *
 * <p>本类继承 {@link BeanPropertyRowMapper}，可作为通用映射选择，
 * 灵活适配构造器风格或 setter 方法。
 *
 * <p>本类侧重便利而非高性能；追求性能请考虑自定义 {@code RowMapper}。
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 5.3
 * @param <T> 结果类型
 * @see SimplePropertyRowMapper
 */
public class DataClassRowMapper<T> extends BeanPropertyRowMapper<T> {

	private @Nullable Constructor<T> mappedConstructor;

	private @Nullable String @Nullable [] constructorParameterNames;

	private TypeDescriptor @Nullable [] constructorParameterTypes;


	/**
	 * 创建用于 bean 风格配置的 {@code DataClassRowMapper}。
	 * @see #setMappedClass
	 * @see #setConversionService
	 */
	public DataClassRowMapper() {
	}

	/**
	 * 创建 {@code DataClassRowMapper}。
	 * @param mappedClass 每行映射到的类
	 */
	public DataClassRowMapper(Class<T> mappedClass) {
		super(mappedClass);
	}


	@Override
	protected void initialize(Class<T> mappedClass) {
		super.initialize(mappedClass);

		this.mappedConstructor = BeanUtils.getResolvableConstructor(mappedClass);
		int paramCount = this.mappedConstructor.getParameterCount();
		if (paramCount > 0) {
			this.constructorParameterNames = BeanUtils.getParameterNames(this.mappedConstructor);
			for (String name : this.constructorParameterNames) {
				suppressProperty(name);
			}
			this.constructorParameterTypes = new TypeDescriptor[paramCount];
			for (int i = 0; i < paramCount; i++) {
				this.constructorParameterTypes[i] = new TypeDescriptor(new MethodParameter(this.mappedConstructor, i));
			}
		}
	}

	@Override
	protected T constructMappedInstance(ResultSet rs, TypeConverter tc) throws SQLException {
		Assert.state(this.mappedConstructor != null, "Mapped constructor was not initialized");

		@Nullable Object[] args;
		if (this.constructorParameterNames != null && this.constructorParameterTypes != null) {
			args = new Object[this.constructorParameterNames.length];
			for (int i = 0; i < args.length; i++) {
				String name = this.constructorParameterNames[i];
				int index;
				try {
					// Try direct name match first
					index = rs.findColumn(lowerCaseName(name));
				}
				catch (SQLException ex) {
					// Try underscored name match instead
					index = rs.findColumn(underscoreName(name));
				}
				TypeDescriptor td = this.constructorParameterTypes[i];
				Object value = getColumnValue(rs, index, td.getType());
				args[i] = tc.convertIfNecessary(value, td.getType(), td);
			}
		}
		else {
			args = new Object[0];
		}

		return BeanUtils.instantiateClass(this.mappedConstructor, args);
	}


	/**
	 * 创建 {@code DataClassRowMapper} 的静态工厂方法。
	 * @param mappedClass 每行映射到的类
	 * @see #newInstance(Class, ConversionService)
	 */
	public static <T> DataClassRowMapper<T> newInstance(Class<T> mappedClass) {
		return new DataClassRowMapper<>(mappedClass);
	}

	/**
	 * 创建 {@code DataClassRowMapper} 的静态工厂方法。
	 * @param mappedClass 每行映射到的类
	 * @param conversionService 将 JDBC 值绑定到 bean 属性的 {@link ConversionService}，无则 {@code null}
	 * @see #newInstance(Class)
	 * @see #setConversionService
	 */
	public static <T> DataClassRowMapper<T> newInstance(
			Class<T> mappedClass, @Nullable ConversionService conversionService) {

		DataClassRowMapper<T> rowMapper = newInstance(mappedClass);
		rowMapper.setConversionService(conversionService);
		return rowMapper;
	}

}
