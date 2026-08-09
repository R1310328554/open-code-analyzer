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

/* ===== [OCA 中文解析] =====
文件意图总览

基于 JavaBean 属性名的 RowMapper：按列名（含下划线转驼峰）匹配 setter，将 ResultSet 行映射为目标类型实例；便利优先，非高性能场景首选。
===== [OCA 中文解析结束] ===== */
package org.springframework.jdbc.core;

import java.beans.PropertyDescriptor;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.NotWritablePropertyException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/* ===== [OCA 中文解析] =====
class BeanPropertyRowMapper — 意图说明

按列名与 JavaBean setter 匹配的行映射器：支持下划线列名转驼峰属性、ConversionService 类型转换及 NULL 基元处理策略。

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
===== [OCA 中文解析结束] ===== */
/**
 * {@link RowMapper} 实现，将每行映射为指定目标类的新实例。目标类须为顶级或 {@code static} 嵌套类，且有无参构造器。
 * <p>列值按 ResultSet 元数据列名与目标类 public setter 匹配；支持直接匹配或下划线列名转驼峰属性名。
 * <p>内置 String、数值、Boolean、BigDecimal、{@code java.util.Date} 等常见类型转换。
 * <p>列名与属性不一致时可在 SQL 中使用下划线别名，如 {@code first_name} 映射 {@code setFirstName}。
 * <p>数据库 NULL 默认调用 setter(null)；基元属性会触发 {@link TypeMismatchException}，可设 {@link #setPrimitivesDefaultedForNullValue(boolean)} 忽略。
 * <p>映射 record/data class 请用 {@link DataClassRowMapper}。
 * <p>本类侧重便利而非性能，高性能场景请用定制 {@code RowMapper}。
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 2.5
 * @param <T> 结果类型
 * @see DataClassRowMapper
 * @see SimplePropertyRowMapper
 */
public class BeanPropertyRowMapper<T> implements RowMapper<T> {

	/**
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 */
	private @Nullable Class<T> mappedClass;

	/**
	 */
	private boolean checkFullyPopulated = false;

	/**
	 * 对于目标类中的原始属性，是否应忽略 {@code NULL} 数据库值。
	 * @see #setPrimitivesDefaultedForNullValue(boolean)
	 */
	private boolean primitivesDefaultedForNullValue = false;

	/**
	 */
	private @Nullable ConversionService conversionService = DefaultConversionService.getSharedInstance();

	/**
	 */
	private @Nullable Map<String, PropertyDescriptor> mappedProperties;

	/**
	 */
	private @Nullable Set<String> mappedPropertyNames;


	/**
	 * 创建 {@code BeanPropertyRowMapper}，供后续通过 setter 配置 mappedClass 等属性。
	 * @see #setMappedClass
	 * @see #setCheckFullyPopulated
	 */
	public BeanPropertyRowMapper() {
	}

	/**
	 * 创建一个新的 {@code BeanPropertyRowMapper}，接受目标 bean 中未填充的属性。
	 * @param mappedClass 每行应映射到的类
	 */
	public BeanPropertyRowMapper(Class<T> mappedClass) {
		initialize(mappedClass);
	}

	/**
	 * 创建一个新的 {@code BeanPropertyRowMapper}。
	 * @param mappedClass 每行应映射到的类
	 * @param checkFullyPopulated 我们是否严格验证所有 bean 属性是否已从相应的数据库列映射
	 */
	public BeanPropertyRowMapper(Class<T> mappedClass, boolean checkFullyPopulated) {
		initialize(mappedClass);
		this.checkFullyPopulated = checkFullyPopulated;
	}


	/**
	 * 设置每行应映射到的类。
	 */
	public void setMappedClass(Class<T> mappedClass) {
		if (this.mappedClass == null) {
			initialize(mappedClass);
		}
		else {
			if (this.mappedClass != mappedClass) {
				throw new InvalidDataAccessApiUsageException("The mapped class can not be reassigned to map to " +
						mappedClass + " since it is already providing mapping for " + this.mappedClass);
			}
		}
	}

	/**
	 * 获取我们要映射到的类。
	 */
	public final @Nullable Class<T> getMappedClass() {
		return this.mappedClass;
	}

	/**
	 * 设置我们是否严格验证所有 bean 属性是否已从相应的数据库列映射。 <p>Default 是 {@code false}，接受目标 bean 中未填充的属性。
	 */
	public void setCheckFullyPopulated(boolean checkFullyPopulated) {
		this.checkFullyPopulated = checkFullyPopulated;
	}

	/**
	 * 返回我们是否严格验证所有 bean 属性是否已从相应的数据库列映射。
	 */
	public boolean isCheckFullyPopulated() {
		return this.checkFullyPopulated;
	}

	/**
	 * 设置在映射到目标类中相应的原始属性时是否应忽略 {@code NULL} 数据库列值。 <p>Default 是 {@code false}，当 null 映射到 Java 原
	 * 语时抛出异常。 <p>如果此标志设置为 {@code true} 并且您使用映射 bean 中的 <em>ignored</em> 原始属性值来更新数据库，则数据库中的值将从 
	 * {@code NULL} 更改为该原始属性的当前值。该值可能是属性的初始值（可能是 Java 各自基元类型的默认值），也可能是在默认构造函数（或初始化块）中为该属性设置的其他值
	 * ，或者是在映射 bean 中设置其他属性的副作用。
	 */
	public void setPrimitivesDefaultedForNullValue(boolean primitivesDefaultedForNullValue) {
		this.primitivesDefaultedForNullValue = primitivesDefaultedForNullValue;
	}

	/**
	 * 获取 {@code primitivesDefaultedForNullValue} 标志的值。
	 * @see #setPrimitivesDefaultedForNullValue(boolean)
	 */
	public boolean isPrimitivesDefaultedForNullValue() {
		return this.primitivesDefaultedForNullValue;
	}

	/**
	 * 设置 {@link ConversionService} 将 JDBC 值绑定到 bean 属性，或设置 {@code null} 不绑定。 <p>Default 是
	 * {@link DefaultConversionService}。这提供了对 {@code java.time} 转换和其他特殊类型的支持。
	 * @since 4.3
	 * @see #initBeanWrapper(BeanWrapper)
	 */
	public void setConversionService(@Nullable ConversionService conversionService) {
		this.conversionService = conversionService;
	}

	/**
	 * 返回 {@link ConversionService} 以将 JDBC 值绑定到 bean 属性，如果没有，则返回 {@code null}。
	 * @since 4.3
	 */
	public @Nullable ConversionService getConversionService() {
		return this.conversionService;
	}


	/**
	 * 初始化给定类的映射元数据。
	 * @param mappedClass 映射的类
	 * @see #setMappedClass
	 * @see BeanUtils#getPropertyDescriptors
	 * @see #mappedNames(PropertyDescriptor)
	 */
	protected void initialize(Class<T> mappedClass) {
		this.mappedClass = mappedClass;
		this.mappedProperties = new HashMap<>();
		this.mappedPropertyNames = new HashSet<>();

		for (PropertyDescriptor pd : BeanUtils.getPropertyDescriptors(mappedClass)) {
			if (pd.getWriteMethod() != null) {
				Set<String> mappedNames = mappedNames(pd);
				for (String mappedName : mappedNames) {
					this.mappedProperties.put(mappedName, pd);
				}
				this.mappedPropertyNames.add(pd.getName());
			}
		}
	}

	/**
	 * 从映射的属性中删除指定的属性。
	 * @param propertyName 属性名称（由属性描述符使用）
	 * @since 5.3.9
	 */
	protected void suppressProperty(@Nullable String propertyName) {
		if (this.mappedProperties != null) {
			this.mappedProperties.remove(lowerCaseName(propertyName));
			this.mappedProperties.remove(underscoreName(propertyName));
		}
	}

	/**
	 * 确定给定属性的映射名称。 <p>子类可以重写此方法来自定义映射名称，添加或删除由此基本方法确定的集合（它以小写和基于下划线的形式返回属性名称），或完全替换集合。
	 * @param pd 初始化时发现的属性描述符
	 * @return 映射名称集
	 * @since 6.1.4
	 * @see #initialize
	 * @see #lowerCaseName
	 * @see #underscoreName
	 */
	protected Set<String> mappedNames(PropertyDescriptor pd) {
		Set<String> mappedNames = new HashSet<>(4);
		mappedNames.add(lowerCaseName(pd.getName()));
		mappedNames.add(underscoreName(pd.getName()));
		return mappedNames;
	}

	/**
	 * 将给定名称转换为小写。 <p> 默认情况下，转换将在美国区域设置内进行。
	 * @param name 原名
	 * @return 转换后的名字
	 * @since 4.2
	 * @see #underscoreName
	 */
	protected String lowerCaseName(@Nullable String name) {
		if (!StringUtils.hasLength(name)) {
			return "";
		}
		return name.toLowerCase(Locale.US);
	}

	/**
	 * 将驼峰命名法的名称转换为带下划线的小写名称。 <p>任何大写字母都会转换为小写字母，并在前面加上下划线。
	 * @param name 原名
	 * @return 转换后的名字
	 * @since 4.2
	 * @see JdbcUtils#convertPropertyNameToUnderscoreName
	 */
	protected String underscoreName(@Nullable String name) {
		return JdbcUtils.convertPropertyNameToUnderscoreName(name);
	}


	/**
	 * 提取当前行中所有列的值。 <p>U利用公共设置器和结果集元数据。
	 * @see java.sql.ResultSetMetaData
	 */
	@Override
	public T mapRow(ResultSet rs, int rowNumber) throws SQLException {
		BeanWrapperImpl bw = new BeanWrapperImpl();
		initBeanWrapper(bw);

		T mappedObject = constructMappedInstance(rs, bw);
		bw.setBeanInstance(mappedObject);

		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		Set<String> populatedProperties = (isCheckFullyPopulated() ? new HashSet<>() : null);

		for (int index = 1; index <= columnCount; index++) {
			String column = JdbcUtils.lookupColumnName(rsmd, index);
			String property = lowerCaseName(StringUtils.delete(column, " "));
			PropertyDescriptor pd = (this.mappedProperties != null ? this.mappedProperties.get(property) : null);
			if (pd != null) {
				try {
					Object value = getColumnValue(rs, index, pd);
					if (rowNumber == 0 && logger.isDebugEnabled()) {
						logger.debug("Mapping column '" + column + "' to property '" + pd.getName() +
								"' of type '" + ClassUtils.getQualifiedName(pd.getPropertyType()) + "'");
					}
					try {
						bw.setPropertyValue(pd.getName(), value);
					}
					catch (TypeMismatchException ex) {
						if (value == null && isPrimitivesDefaultedForNullValue()) {
							if (logger.isDebugEnabled()) {
								String propertyType = ClassUtils.getQualifiedName(pd.getPropertyType());
								logger.debug("""
										Ignoring intercepted TypeMismatchException for row %d and column '%s' \
										with null value when setting property '%s' of type '%s' on object: %s"
										""".formatted(rowNumber, column, pd.getName(), propertyType, mappedObject), ex);
							}
						}
						else {
							throw ex;
						}
					}
					if (populatedProperties != null) {
						populatedProperties.add(pd.getName());
					}
				}
				catch (NotWritablePropertyException ex) {
					throw new DataRetrievalFailureException(
							"Unable to map column '" + column + "' to property '" + pd.getName() + "'", ex);
				}
			}
		}

		if (populatedProperties != null && !populatedProperties.equals(this.mappedPropertyNames)) {
			throw new InvalidDataAccessApiUsageException("Given ResultSet does not contain all properties " +
					"necessary to populate object of " + this.mappedClass + ": " + this.mappedPropertyNames);
		}

		return mappedObject;
	}

	/**
	 * 为当前行构造映射类的实例。
	 * @param rs 要映射的 ResultSet（针对当前行预先初始化）
	 * @param tc 具有此 RowMapper 转换服务的 TypeConverter
	 * @return 映射类的对应实例
	 * @throws SQLException 如果遇到 SQLException
	 * @since 5.3
	 */
	protected T constructMappedInstance(ResultSet rs, TypeConverter tc) throws SQLException {
		Assert.state(this.mappedClass != null, "Mapped class was not specified");
		return BeanUtils.instantiateClass(this.mappedClass);
	}

	/**
	 * 初始化给定的 BeanWrapper 以用于行映射。 <p>要为每一行调用。 <p>默认实现应用配置的 {@link ConversionService}（如果有）。可以在子类
	 * 中重写。
	 * @param bw 要初始化的 BeanWrapper
	 * @see #getConversionService()
	 * @see BeanWrapper#setConversionService
	 */
	protected void initBeanWrapper(BeanWrapper bw) {
		ConversionService cs = getConversionService();
		if (cs != null) {
			bw.setConversionService(cs);
		}
	}

	/**
	 * 检索指定列的 JDBC 对象值。 <p>默认实现使用指定{@link PropertyDescriptor}的类型调用{@link
	 * JdbcUtils#getResultSetValue(java.sql.ResultSet, int, Class)}。 <p>Subclasses
	 * 可以覆盖它以预先检查特定值类型，或后处理从 {@code getResultSetValue} 返回的值。
	 * @param rs 是保存数据的 ResultSet
	 * @param index 是列索引
	 * @param pd 每个结果对象期望匹配的 bean 属性
	 * @return 对象价值
	 * @throws SQLException 如果提取失败
	 * @see #getColumnValue(ResultSet, int, Class)
	 */
	protected @Nullable Object getColumnValue(ResultSet rs, int index, PropertyDescriptor pd) throws SQLException {
		return JdbcUtils.getResultSetValue(rs, index, pd.getPropertyType());
	}

	/**
	 * 检索指定列的 JDBC 对象值。 <p>默认实现调用{@link JdbcUtils#getResultSetValue(java.sql.ResultSet, int,
	 * Class)}。 <p>Subclasses 可以重写此设置以预先检查特定值类型，或对从 {@code getResultSetValue} 返回的值进行后处理。
	 * @param rs 是保存数据的 ResultSet
	 * @param index 是列索引
	 * @param paramType 目标参数类型
	 * @return 对象价值
	 * @throws SQLException 如果提取失败
	 * @since 5.3
	 * @see org.springframework.jdbc.support.JdbcUtils#getResultSetValue(java.sql.ResultSet, int, Class)
	 */
	protected @Nullable Object getColumnValue(ResultSet rs, int index, Class<?> paramType) throws SQLException {
		return JdbcUtils.getResultSetValue(rs, index, paramType);
	}


	/**
	 * 创建新的 {@code BeanPropertyRowMapper} 的静态工厂方法。
	 * @param mappedClass 每行应映射到的类
	 * @see #newInstance(Class, ConversionService)
	 */
	public static <T> BeanPropertyRowMapper<T> newInstance(Class<T> mappedClass) {
		return new BeanPropertyRowMapper<>(mappedClass);
	}

	/**
	 * 创建新的 {@code BeanPropertyRowMapper} 的静态工厂方法。
	 * @param mappedClass 每行应映射到的类
	 * @param conversionService {@link ConversionService} 用于将 JDBC 值绑定到 bean 属性，或 {@code null} 用于无
	 * @since 5.2.3
	 * @see #newInstance(Class)
	 * @see #setConversionService
	 */
	public static <T> BeanPropertyRowMapper<T> newInstance(
			Class<T> mappedClass, @Nullable ConversionService conversionService) {

		BeanPropertyRowMapper<T> rowMapper = newInstance(mappedClass);
		rowMapper.setConversionService(conversionService);
		return rowMapper;
	}

}
