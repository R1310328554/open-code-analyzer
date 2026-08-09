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
 * {@link RowMapper} 实现：把结果集的一行转换成指定目标类型的新实例。
 * 目标类型必须是顶级类或 {@code static} 嵌套类，并且可以暴露：
 * <em>数据类</em>风格、参数名与列名对应的构造器；或经典 JavaBean 的 setter
 * （属性名与列名对应）；也可以两者同时存在。
 *
 * <p>这里的「数据类」包括 Java <em>record</em>、Kotlin <em>data class</em>，
 * 以及任何「带命名参数构造器、并意图映射到同名列」的类。
 *
 * <p>当构造器参数与 setter 同时存在时：已经通过构造器参数成功映射的属性，
 * 不会再通过对应 setter 映射一次。也就是说，<strong>构造器参数优先于 setter</strong>。
 *
 * <p>本类继承 {@link BeanPropertyRowMapper}，因此可作为大多数映射目标类型的通用选择，
 * 能在「构造器风格」与「setter 风格」之间灵活适配。
 *
 * <p>请注意：本类更侧重使用便利，而非极致性能。若对性能要求很高，
 * 建议编写自定义 {@code RowMapper} 实现。
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 5.3
 * @param <T> 映射结果类型
 * @see SimplePropertyRowMapper
 */
public class DataClassRowMapper<T> extends BeanPropertyRowMapper<T> {

	/** 用于实例化映射目标的可解析构造器（数据类/主构造器）。 */
	private @Nullable Constructor<T> mappedConstructor;

	/** 构造器参数名列表，对应要绑定的列名（或可转换后的列名）。 */
	private @Nullable String @Nullable [] constructorParameterNames;

	/** 每个构造器参数的类型描述，用于 JDBC 值到参数类型的转换。 */
	private TypeDescriptor @Nullable [] constructorParameterTypes;


	/**
	 * 创建一个用于 Bean 风格配置的 {@code DataClassRowMapper}。
	 * @see #setMappedClass
	 * @see #setConversionService
	 */
	public DataClassRowMapper() {
	}

	/**
	 * 创建一个 {@code DataClassRowMapper}。
	 * @param mappedClass 每一行要映射成的目标类型
	 */
	public DataClassRowMapper(Class<T> mappedClass) {
		super(mappedClass);
	}


	/**
	 * 初始化映射元数据：解析目标类构造器、参数名与参数类型，
	 * 并对已由构造器接管的属性调用 {@link #suppressProperty(String)}，避免再走 setter。
	 */
	@Override
	protected void initialize(Class<T> mappedClass) {
		super.initialize(mappedClass);

		this.mappedConstructor = BeanUtils.getResolvableConstructor(mappedClass);
		int paramCount = this.mappedConstructor.getParameterCount();
		if (paramCount > 0) {
			this.constructorParameterNames = BeanUtils.getParameterNames(this.mappedConstructor);
			for (String name : this.constructorParameterNames) {
				// 构造器已映射的属性，抑制后续 setter 再映射
				suppressProperty(name);
			}
			this.constructorParameterTypes = new TypeDescriptor[paramCount];
			for (int i = 0; i < paramCount; i++) {
				this.constructorParameterTypes[i] = new TypeDescriptor(new MethodParameter(this.mappedConstructor, i));
			}
		}
	}

	/**
	 * 通过已解析的构造器创建映射实例：按参数名从 {@link ResultSet} 取值并做类型转换。
	 * <p>列名匹配策略：先直接按小写参数名找列；失败再尝试下划线形式（如 {@code userName} → {@code user_name}）。
	 */
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
					// 先尝试直接按名称匹配列
					index = rs.findColumn(lowerCaseName(name));
				}
				catch (SQLException ex) {
					// 再尝试下划线命名风格匹配
					index = rs.findColumn(underscoreName(name));
				}
				TypeDescriptor td = this.constructorParameterTypes[i];
				Object value = getColumnValue(rs, index, td.getType());
				// 将 JDBC 原始值转换为构造器参数所需类型
				args[i] = tc.convertIfNecessary(value, td.getType(), td);
			}
		}
		else {
			args = new Object[0];
		}

		return BeanUtils.instantiateClass(this.mappedConstructor, args);
	}


	/**
	 * 静态工厂：创建新的 {@code DataClassRowMapper}。
	 * @param mappedClass 每一行要映射成的目标类型
	 * @see #newInstance(Class, ConversionService)
	 */
	public static <T> DataClassRowMapper<T> newInstance(Class<T> mappedClass) {
		return new DataClassRowMapper<>(mappedClass);
	}

	/**
	 * 静态工厂：创建新的 {@code DataClassRowMapper}。
	 * @param mappedClass 每一行要映射成的目标类型
	 * @param conversionService 用于把 JDBC 值绑定到 Bean 属性的 {@link ConversionService}；
	 * 可为 {@code null} 表示不使用
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
