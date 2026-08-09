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

package org.springframework.beans;

import java.beans.PropertyChangeEvent;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.UndeclaredThrowableException;
import java.security.PrivilegedActionException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.core.CollectionFactory;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.ConversionException;
import org.springframework.core.convert.ConverterNotFoundException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * 基础的 {@link ConfigurablePropertyAccessor}，为常见属性访问场景提供基础设施。
 *
 * <p>必要时会把集合、数组取值转换成目标集合或数组类型。处理集合/数组的自定义
 * PropertyEditor 既可通过 {@code setValue} 写入对象，也可通过 {@code setAsText}
 * 处理逗号分隔字符串——当 String 数组本身不可直接赋值时，框架会按该格式转换。
 *
 * <p>核心能力：
 * <ul>
 * <li>嵌套路径（如 {@code address.street}）与索引/映射键（如 {@code list[0].name}、{@code map[key]}）；</li>
 * <li>写入前按目标类型做转换（委托 {@link TypeConverterDelegate}）；</li>
 * <li>可选的自动增长：嵌套路径上的 null 中间节点、数组/List 下标越界时扩容。</li>
 * </ul>
 *
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @author Rod Johnson
 * @author Rob Harrop
 * @author Sam Brannen
 * @since 4.2
 * @see #registerCustomEditor
 * @see #setPropertyValues
 * @see #setPropertyValue
 * @see #getPropertyValue
 * @see #getPropertyType
 * @see BeanWrapper
 * @see PropertyEditorRegistrySupport
 */
public abstract class AbstractNestablePropertyAccessor extends AbstractPropertyAccessor {

	/**
	 * 本类实例会大量创建，故共用一个静态 logger，避免每次 new 都新建日志器。
	 */
	private static final Log logger = LogFactory.getLog(AbstractNestablePropertyAccessor.class);

	/** 数组/集合自动扩容时允许的最大下标（默认无上限）。 */
	private int autoGrowCollectionLimit = Integer.MAX_VALUE;

	/** 当前访问器包装的目标对象（已 unwrap Optional）。 */
	@Nullable Object wrappedObject;

	/** 相对根对象的嵌套路径前缀，例如 {@code "address."}；根访问器为空串。 */
	private String nestedPath = "";

	/** 路径顶端的根对象；无嵌套时等于 {@link #wrappedObject}。 */
	@Nullable Object rootObject;

	/** 嵌套访问器缓存：规范化嵌套路径 → Accessor 实例（保留已注册的自定义编辑器）。 */
	private @Nullable Map<String, AbstractNestablePropertyAccessor> nestedPropertyAccessors;


	/**
	 * 创建空访问器；之后需再设置被包装实例。会注册默认 PropertyEditor。
	 * @see #setWrappedInstance
	 */
	protected AbstractNestablePropertyAccessor() {
		this(true);
	}

	/**
	 * 创建空访问器；之后需再设置被包装实例。
	 * @param registerDefaultEditors whether to register default editors
	 * (can be suppressed if the accessor won't need any type conversion)
	 * @see #setWrappedInstance
	 */
	protected AbstractNestablePropertyAccessor(boolean registerDefaultEditors) {
		if (registerDefaultEditors) {
			registerDefaultEditors();
		}
		this.typeConverterDelegate = new TypeConverterDelegate(this);
	}

	/**
	 * 为给定对象创建访问器。
	 * @param object the object wrapped by this accessor
	 */
	protected AbstractNestablePropertyAccessor(Object object) {
		registerDefaultEditors();
		setWrappedInstance(object);
	}

	/**
	 * 创建访问器，并包装指定类型的新实例。
	 * @param clazz class to instantiate and wrap
	 */
	protected AbstractNestablePropertyAccessor(Class<?> clazz) {
		registerDefaultEditors();
		setWrappedInstance(BeanUtils.instantiateClass(clazz));
	}

	/**
	 * 为给定对象创建访问器，并登记该对象所处的嵌套路径。
	 * @param object the object wrapped by this accessor
	 * @param nestedPath the nested path of the object
	 * @param rootObject the root object at the top of the path
	 */
	protected AbstractNestablePropertyAccessor(Object object, String nestedPath, Object rootObject) {
		registerDefaultEditors();
		setWrappedInstance(object, nestedPath, rootObject);
	}

	/**
	 * 为给定对象创建访问器，并登记嵌套路径；从父访问器继承编辑器相关配置。
	 * @param object the object wrapped by this accessor
	 * @param nestedPath the nested path of the object
	 * @param parent the containing accessor (must not be {@code null})
	 */
	protected AbstractNestablePropertyAccessor(Object object, String nestedPath, AbstractNestablePropertyAccessor parent) {
		setWrappedInstance(object, nestedPath, parent.getWrappedInstance());
		setExtractOldValueForEditor(parent.isExtractOldValueForEditor());
		setAutoGrowNestedPaths(parent.isAutoGrowNestedPaths());
		setAutoGrowCollectionLimit(parent.getAutoGrowCollectionLimit());
		setConversionService(parent.getConversionService());
	}


	/**
	 * 设置数组与集合自动扩容的上限。
	 * <p>普通访问器上默认无限制。
	 */
	public void setAutoGrowCollectionLimit(int autoGrowCollectionLimit) {
		this.autoGrowCollectionLimit = autoGrowCollectionLimit;
	}

	/**
	 * 返回数组与集合自动扩容的上限。
	 */
	public int getAutoGrowCollectionLimit() {
		return this.autoGrowCollectionLimit;
	}

	/**
	 * 切换目标对象；仅当新对象的 Class 与旧对象不同时才替换缓存的内省结果。
	 * @param object the new target object
	 */
	public void setWrappedInstance(Object object) {
		setWrappedInstance(object, "", null);
	}

	/**
	 * 切换目标对象；仅当新对象的 Class 与旧对象不同时才替换缓存的内省结果。
	 * @param object the new target object
	 * @param nestedPath the nested path of the object
	 * @param rootObject the root object at the top of the path
	 */
	public void setWrappedInstance(Object object, @Nullable String nestedPath, @Nullable Object rootObject) {
		this.wrappedObject = ObjectUtils.unwrapOptional(object);
		Assert.notNull(this.wrappedObject, "Target object must not be null");
		this.nestedPath = (nestedPath != null ? nestedPath : "");
		this.rootObject = (!this.nestedPath.isEmpty() ? rootObject : this.wrappedObject);
		// 目标变了，嵌套访问器缓存全部失效
		this.nestedPropertyAccessors = null;
		this.typeConverterDelegate = new TypeConverterDelegate(this, this.wrappedObject);
	}

	public final Object getWrappedInstance() {
		Assert.state(this.wrappedObject != null, "No wrapped object");
		return this.wrappedObject;
	}

	public final Class<?> getWrappedClass() {
		return getWrappedInstance().getClass();
	}

	/**
	 * 返回本访问器所包装对象的嵌套路径。
	 */
	public final String getNestedPath() {
		return this.nestedPath;
	}

	/**
	 * 返回路径顶端的根对象。
	 * @see #getNestedPath
	 */
	public final Object getRootInstance() {
		Assert.state(this.rootObject != null, "No root object");
		return this.rootObject;
	}

	/**
	 * 返回路径顶端根对象的 Class。
	 * @see #getNestedPath
	 */
	public final Class<?> getRootClass() {
		return getRootInstance().getClass();
	}

	@Override
	public void setPropertyValue(String propertyName, @Nullable Object value) throws BeansException {
		// 先沿嵌套路径导航到「最终属性」所在的访问器，再在其上设值
		AbstractNestablePropertyAccessor nestedPa;
		try {
			nestedPa = getPropertyAccessorForPropertyPath(propertyName);
		}
		catch (NotReadablePropertyException ex) {
			throw new NotWritablePropertyException(getRootClass(), this.nestedPath + propertyName,
					"Nested property in path '" + propertyName + "' does not exist", ex);
		}
		PropertyTokenHolder tokens = getPropertyNameTokens(getFinalPath(nestedPa, propertyName));
		nestedPa.setPropertyValue(tokens, new PropertyValue(propertyName, value));
	}

	@Override
	public void setPropertyValue(PropertyValue pv) throws BeansException {
		PropertyTokenHolder tokens = (PropertyTokenHolder) pv.resolvedTokens;
		if (tokens == null) {
			String propertyName = pv.getName();
			AbstractNestablePropertyAccessor nestedPa;
			try {
				nestedPa = getPropertyAccessorForPropertyPath(propertyName);
			}
			catch (NotReadablePropertyException ex) {
				throw new NotWritablePropertyException(getRootClass(), this.nestedPath + propertyName,
						"Nested property in path '" + propertyName + "' does not exist", ex);
			}
			tokens = getPropertyNameTokens(getFinalPath(nestedPa, propertyName));
			// 仅当最终落在当前访问器上时，把解析结果缓存进 PropertyValue，避免重复分词
			if (nestedPa == this) {
				pv.getOriginalPropertyValue().resolvedTokens = tokens;
			}
			nestedPa.setPropertyValue(tokens, pv);
		}
		else {
			setPropertyValue(tokens, pv);
		}
	}

	/**
	 * 按 token 写入：有下标/键则走索引属性，否则写本地简单属性。
	 */
	protected void setPropertyValue(PropertyTokenHolder tokens, PropertyValue pv) throws BeansException {
		if (tokens.keys != null) {
			processKeyedProperty(tokens, pv);
		}
		else {
			processLocalProperty(tokens, pv);
		}
	}

	/**
	 * 处理带索引/映射键的属性写入，例如 {@code arr[0]}、{@code list[1]}、{@code map[key]}。
	 * <p>最后一个 key 表示要写入的位置；更前面的 key 用于先取出承载容器。
	 * 数组与 List 在开启自动增长且未超限时可扩容；Map 则对 key/value 分别做类型转换。
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	private void processKeyedProperty(PropertyTokenHolder tokens, PropertyValue pv) {
		Object propValue = getPropertyHoldingValue(tokens);
		PropertyHandler ph = getLocalPropertyHandler(tokens.actualName);
		if (ph == null) {
			throw new InvalidPropertyException(
					getRootClass(), this.nestedPath + tokens.actualName, "No property handler found");
		}
		Assert.state(tokens.keys != null, "No token keys");
		String lastKey = tokens.keys[tokens.keys.length - 1];

		if (propValue.getClass().isArray()) {
			Class<?> componentType = propValue.getClass().componentType();
			int arrayIndex = Integer.parseInt(lastKey);
			Object oldValue = null;
			try {
				if (isExtractOldValueForEditor() && arrayIndex < Array.getLength(propValue)) {
					oldValue = Array.get(propValue, arrayIndex);
				}
				Object convertedValue = convertIfNecessary(tokens.canonicalName, oldValue, pv.getValue(),
						componentType, ph.nested(tokens.keys.length));
				int length = Array.getLength(propValue);
				// 下标越界但未超 autoGrow 上限：新建更大数组、回写属性后再 set
				if (arrayIndex >= length && arrayIndex < this.autoGrowCollectionLimit) {
					Object newArray = Array.newInstance(componentType, arrayIndex + 1);
					System.arraycopy(propValue, 0, newArray, 0, length);
					int lastKeyIndex = tokens.canonicalName.lastIndexOf('[');
					String propName = tokens.canonicalName.substring(0, lastKeyIndex);
					setPropertyValue(propName, newArray);
					propValue = getPropertyValue(propName);
				}
				Array.set(propValue, arrayIndex, convertedValue);
			}
			catch (IndexOutOfBoundsException ex) {
				throw new InvalidPropertyException(getRootClass(), this.nestedPath + tokens.canonicalName,
						"Invalid array index in property path '" + tokens.canonicalName + "'", ex);
			}
		}

		else if (propValue instanceof List list) {
			TypeDescriptor requiredType = ph.getCollectionType(tokens.keys.length);
			int index = Integer.parseInt(lastKey);
			Object oldValue = null;
			if (isExtractOldValueForEditor() && index < list.size()) {
				oldValue = list.get(index);
			}
			Object convertedValue = convertIfNecessary(tokens.canonicalName, oldValue, pv.getValue(),
					requiredType.getResolvableType().resolve(), requiredType);
			int size = list.size();
			if (index >= size && index < this.autoGrowCollectionLimit) {
				// 用 null 填到目标下标前一位，再 add；不支持 null 间隙的 List 会失败
				for (int i = size; i < index; i++) {
					try {
						list.add(null);
					}
					catch (NullPointerException ex) {
						throw new InvalidPropertyException(getRootClass(), this.nestedPath + tokens.canonicalName,
								"Cannot set element with index " + index + " in List of size " +
								size + ", accessed using property path '" + tokens.canonicalName +
								"': List does not support filling up gaps with null elements");
					}
				}
				list.add(convertedValue);
			}
			else {
				try {
					list.set(index, convertedValue);
				}
				catch (IndexOutOfBoundsException ex) {
					throw new InvalidPropertyException(getRootClass(), this.nestedPath + tokens.canonicalName,
							"Invalid list index in property path '" + tokens.canonicalName + "'", ex);
				}
			}
		}

		else if (propValue instanceof Map map) {
			TypeDescriptor mapKeyType = ph.getMapKeyType(tokens.keys.length);
			TypeDescriptor mapValueType = ph.getMapValueType(tokens.keys.length);
			// 重要：Map key 转换不要传入完整属性名，以免 PropertyEditor 误作用于 key
			Object convertedMapKey = convertIfNecessary(null, null, lastKey,
					mapKeyType.getResolvableType().resolve(), mapKeyType);
			Object oldValue = null;
			if (isExtractOldValueForEditor()) {
				oldValue = map.get(convertedMapKey);
			}
			// value 则传入完整属性名与旧值，以便走完整转换链路
			Object convertedMapValue = convertIfNecessary(tokens.canonicalName, oldValue, pv.getValue(),
					mapValueType.getResolvableType().resolve(), mapValueType);
			map.put(convertedMapKey, convertedMapValue);
		}

		else {
			throw new InvalidPropertyException(getRootClass(), this.nestedPath + tokens.canonicalName,
					"Property referenced in indexed property path '" + tokens.canonicalName +
					"' is neither an array nor a List nor a Map; returned value was [" + propValue + "]");
		}
	}

	/**
	 * 取出「承载最后一个下标/键」的容器值：对除末尾以外的全部 key 做 get。
	 * 若中间结果为 null 且允许自动增长，则先为该属性写入默认实例。
	 */
	private Object getPropertyHoldingValue(PropertyTokenHolder tokens) {
		// 应用除最后一个以外的全部索引/映射键
		Assert.state(tokens.keys != null, "No token keys");
		PropertyTokenHolder getterTokens = new PropertyTokenHolder(tokens.actualName);
		getterTokens.canonicalName = tokens.canonicalName;
		getterTokens.keys = new String[tokens.keys.length - 1];
		System.arraycopy(tokens.keys, 0, getterTokens.keys, 0, tokens.keys.length - 1);

		Object propValue;
		try {
			propValue = getPropertyValue(getterTokens);
		}
		catch (NotReadablePropertyException ex) {
			throw new NotWritablePropertyException(getRootClass(), this.nestedPath + tokens.canonicalName,
					"Cannot access indexed value in property referenced " +
					"in indexed property path '" + tokens.canonicalName + "'", ex);
		}

		if (propValue == null) {
			// 索引路径上出现 null（例如 map 值为 null）
			if (isAutoGrowNestedPaths()) {
				int lastKeyIndex = tokens.canonicalName.lastIndexOf('[');
				getterTokens.canonicalName = tokens.canonicalName.substring(0, lastKeyIndex);
				propValue = setDefaultValue(getterTokens);
			}
			else {
				throw new NullValueInNestedPathException(getRootClass(), this.nestedPath + tokens.canonicalName,
						"Cannot access indexed value in property referenced " +
						"in indexed property path '" + tokens.canonicalName + "': returned null");
			}
		}
		return propValue;
	}

	/**
	 * 写入当前对象上的本地（非索引）属性：可读旧值 → 类型转换 → PropertyHandler.setValue。
	 */
	private void processLocalProperty(PropertyTokenHolder tokens, PropertyValue pv) {
		PropertyHandler ph = getLocalPropertyHandler(tokens.actualName);
		if (ph == null || !ph.isWritable()) {
			if (pv.isOptional()) {
				if (logger.isDebugEnabled()) {
					logger.debug("Ignoring optional value for property '" + tokens.actualName +
							"' - property not found on bean class [" + getRootClass().getName() + "]");
				}
				return;
			}
			if (this.suppressNotWritablePropertyException) {
				// ignoreUnknown=true 的常见优化：上层反正会吞掉异常，这里直接返回
				return;
			}
			throw createNotWritablePropertyException(tokens.canonicalName);
		}

		Object oldValue = null;
		try {
			Object originalValue = pv.getValue();
			Object valueToApply = originalValue;
			if (!Boolean.FALSE.equals(pv.conversionNecessary)) {
				if (pv.isConverted()) {
					valueToApply = pv.getConvertedValue();
				}
				else {
					if (isExtractOldValueForEditor() && ph.isReadable()) {
						try {
							oldValue = ph.getValue();
						}
						catch (Exception ex) {
							if (ex instanceof PrivilegedActionException pae) {
								ex = pae.getException();
							}
							if (logger.isDebugEnabled()) {
								logger.debug("Could not read previous value of property '" +
										this.nestedPath + tokens.canonicalName + "'", ex);
							}
						}
					}
					valueToApply = convertForProperty(
							tokens.canonicalName, oldValue, originalValue, ph.toTypeDescriptor());
				}
				pv.getOriginalPropertyValue().conversionNecessary = (valueToApply != originalValue);
			}
			ph.setValue(valueToApply);
		}
		catch (TypeMismatchException ex) {
			// 子类可提供回退写入（例如字段访问），失败则原样抛出
			if (!ph.setValueFallbackIfPossible(pv.getValue())) {
				throw ex;
			}
		}
		catch (InvocationTargetException ex) {
			PropertyChangeEvent propertyChangeEvent = new PropertyChangeEvent(
					getRootInstance(), this.nestedPath + tokens.canonicalName, oldValue, pv.getValue());
			if (ex.getTargetException() instanceof ClassCastException) {
				throw new TypeMismatchException(propertyChangeEvent, ph.getPropertyType(), ex.getTargetException());
			}
			else {
				Throwable cause = ex.getTargetException();
				if (cause instanceof UndeclaredThrowableException) {
					// 例如 Groovy 生成方法可能包一层 UndeclaredThrowableException
					cause = cause.getCause();
				}
				throw new MethodInvocationException(propertyChangeEvent, cause);
			}
		}
		catch (Exception ex) {
			PropertyChangeEvent pce = new PropertyChangeEvent(
					getRootInstance(), this.nestedPath + tokens.canonicalName, oldValue, pv.getValue());
			throw new MethodInvocationException(pce, ex);
		}
	}

	@Override
	public @Nullable Class<?> getPropertyType(String propertyName) throws BeansException {
		if (this.wrappedObject == null) {
			return null;
		}
		try {
			PropertyHandler ph = getPropertyHandler(propertyName);
			if (ph != null) {
				return ph.getPropertyType();
			}
			else {
				// 可能是索引/映射属性：尝试实际取值推断类型
				Object value = getPropertyValue(propertyName);
				if (value != null) {
					return value.getClass();
				}
				// 再看自定义编辑器能否暗示目标类型
				Class<?> editorType = guessPropertyTypeFromEditors(propertyName);
				if (editorType != null) {
					return editorType;
				}
			}
		}
		catch (InvalidPropertyException ex) {
			// 无法确定类型
		}
		return null;
	}

	@Override
	public @Nullable TypeDescriptor getPropertyTypeDescriptor(String propertyName) throws BeansException {
		try {
			AbstractNestablePropertyAccessor nestedPa = getPropertyAccessorForPropertyPath(propertyName);
			String finalPath = getFinalPath(nestedPa, propertyName);
			PropertyTokenHolder tokens = getPropertyNameTokens(finalPath);
			PropertyHandler ph = nestedPa.getLocalPropertyHandler(tokens.actualName);
			if (ph != null) {
				if (tokens.keys != null) {
					if (ph.isReadable() || ph.isWritable()) {
						// 索引层数对应嵌套 TypeDescriptor
						return ph.nested(tokens.keys.length);
					}
				}
				else {
					if (ph.isReadable() || ph.isWritable()) {
						return ph.toTypeDescriptor();
					}
				}
			}
		}
		catch (InvalidPropertyException ex) {
			// 无法确定类型
		}
		return null;
	}

	@Override
	public boolean isReadableProperty(String propertyName) {
		try {
			PropertyHandler ph = getPropertyHandler(propertyName);
			if (ph != null) {
				return ph.isReadable();
			}
			else {
				// 可能是索引/映射属性：能成功 get 则视为可读
				getPropertyValue(propertyName);
				return true;
			}
		}
		catch (InvalidPropertyException ex) {
			// 无法求值，即不可读
		}
		return false;
	}

	@Override
	public boolean isWritableProperty(String propertyName) {
		try {
			PropertyHandler ph = getPropertyHandler(propertyName);
			if (ph != null) {
				return ph.isWritable();
			}
			else {
				// 可能是索引/映射属性：能成功 get 则视为可写
				getPropertyValue(propertyName);
				return true;
			}
		}
		catch (InvalidPropertyException ex) {
			// 无法求值，即不可写
		}
		return false;
	}

	/**
	 * 必要时转换取值；把底层 ConversionException 包装为 Beans 侧异常。
	 */
	private @Nullable Object convertIfNecessary(@Nullable String propertyName, @Nullable Object oldValue,
			@Nullable Object newValue, @Nullable Class<?> requiredType, @Nullable TypeDescriptor td)
			throws TypeMismatchException {

		Assert.state(this.typeConverterDelegate != null, "No TypeConverterDelegate");
		try {
			return this.typeConverterDelegate.convertIfNecessary(propertyName, oldValue, newValue, requiredType, td);
		}
		catch (ConverterNotFoundException | IllegalStateException ex) {
			PropertyChangeEvent pce =
					new PropertyChangeEvent(getRootInstance(), this.nestedPath + propertyName, oldValue, newValue);
			throw new ConversionNotSupportedException(pce, requiredType, ex);
		}
		catch (ConversionException | IllegalArgumentException ex) {
			PropertyChangeEvent pce =
					new PropertyChangeEvent(getRootInstance(), this.nestedPath + propertyName, oldValue, newValue);
			throw new TypeMismatchException(pce, requiredType, ex);
		}
	}

	/**
	 * 按属性 TypeDescriptor 转换待写入值。
	 */
	protected @Nullable Object convertForProperty(
			String propertyName, @Nullable Object oldValue, @Nullable Object newValue, TypeDescriptor td)
			throws TypeMismatchException {

		return convertIfNecessary(propertyName, oldValue, newValue, td.getType(), td);
	}

	@Override
	public @Nullable Object getPropertyValue(String propertyName) throws BeansException {
		AbstractNestablePropertyAccessor nestedPa = getPropertyAccessorForPropertyPath(propertyName);
		PropertyTokenHolder tokens = getPropertyNameTokens(getFinalPath(nestedPa, propertyName));
		return nestedPa.getPropertyValue(tokens);
	}

	/**
	 * 按 token 读取属性：先取本地属性值，再依次应用数组下标、List/Iterable 索引或 Map 键。
	 * <p>开启 {@code autoGrowNestedPaths} 时，null 中间节点、越界数组/List 可自动增长。
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	protected @Nullable Object getPropertyValue(PropertyTokenHolder tokens) throws BeansException {
		String propertyName = tokens.canonicalName;
		String actualName = tokens.actualName;
		PropertyHandler ph = getLocalPropertyHandler(actualName);
		if (ph == null || !ph.isReadable()) {
			throw new NotReadablePropertyException(getRootClass(), this.nestedPath + propertyName);
		}
		try {
			Object value = ph.getValue();
			if (tokens.keys != null) {
				if (value == null) {
					if (isAutoGrowNestedPaths()) {
						value = setDefaultValue(new PropertyTokenHolder(tokens.actualName));
					}
					else {
						throw new NullValueInNestedPathException(getRootClass(), this.nestedPath + propertyName,
								"Cannot access indexed value of property referenced in indexed " +
										"property path '" + propertyName + "': returned null");
					}
				}
				StringBuilder indexedPropertyName = new StringBuilder(tokens.actualName);
				// 逐层应用索引与 Map 键
				for (int i = 0; i < tokens.keys.length; i++) {
					String key = tokens.keys[i];
					if (value == null) {
						throw new NullValueInNestedPathException(getRootClass(), this.nestedPath + propertyName,
								"Cannot access indexed value of property referenced in indexed " +
										"property path '" + propertyName + "': returned null");
					}
					else if (value.getClass().isArray()) {
						int index = Integer.parseInt(key);
						value = growArrayIfNecessary(value, index, indexedPropertyName.toString());
						value = Array.get(value, index);
					}
					else if (value instanceof List list) {
						int index = Integer.parseInt(key);
						growCollectionIfNecessary(list, index, indexedPropertyName.toString(), ph, i + 1);
						value = list.get(index);
					}
					else if (value instanceof Map map) {
						Class<?> mapKeyType = ph.getResolvableType().getNested(i + 1).asMap().resolveGeneric(0);
						// 重要：Map key 转换不传完整属性名，避免 PropertyEditor 作用到 key
						TypeDescriptor typeDescriptor = TypeDescriptor.valueOf(mapKeyType);
						Object convertedMapKey = convertIfNecessary(null, null, key, mapKeyType, typeDescriptor);
						value = map.get(convertedMapKey);
					}
					else if (value instanceof Iterable iterable) {
						// Set/Collection/Iterable：用迭代器按索引取元素（不自动扩容）
						int index = Integer.parseInt(key);
						if (value instanceof Collection<?> coll) {
							if (index < 0 || index >= coll.size()) {
								throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName,
										"Cannot get element with index " + index + " from Collection of size " +
												coll.size() + ", accessed using property path '" + propertyName + "'");
							}
						}
						Iterator<Object> it = iterable.iterator();
						boolean found = false;
						int currIndex = 0;
						for (; it.hasNext(); currIndex++) {
							Object elem = it.next();
							if (currIndex == index) {
								value = elem;
								found = true;
								break;
							}
						}
						if (!found) {
							throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName,
									"Cannot get element with index " + index + " from Iterable of size " +
											currIndex + ", accessed using property path '" + propertyName + "'");
						}
					}
					else {
						throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName,
								"Property referenced in indexed property path '" + propertyName +
										"' is neither an array nor a List/Set/Collection/Iterable nor a Map; " +
										"returned value was [" + value + "]");
					}
					indexedPropertyName.append(PROPERTY_KEY_PREFIX).append(key).append(PROPERTY_KEY_SUFFIX);
				}
			}
			return value;
		}
		catch (InvalidPropertyException ex) {
			throw ex;
		}
		catch (IndexOutOfBoundsException ex) {
			throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName,
					"Index of out of bounds in property path '" + propertyName + "'", ex);
		}
		catch (NumberFormatException | TypeMismatchException ex) {
			throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName,
					"Invalid index in property path '" + propertyName + "'", ex);
		}
		catch (InvocationTargetException ex) {
			throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName,
					"Getter for property '" + actualName + "' threw exception", ex);
		}
		catch (Exception ex) {
			throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName,
					"Illegal attempt to get property '" + actualName + "' threw exception", ex);
		}
	}


	/**
	 * 返回指定 {@code propertyName} 的 {@link PropertyHandler}，必要时沿嵌套路径导航。
	 * 找不到时返回 {@code null}，不抛异常。
	 * @param propertyName the property to obtain the descriptor for
	 * @return the property descriptor for the specified property,
	 * or {@code null} if not found
	 * @throws BeansException in case of introspection failure
	 */
	protected @Nullable PropertyHandler getPropertyHandler(String propertyName) throws BeansException {
		Assert.notNull(propertyName, "Property name must not be null");
		AbstractNestablePropertyAccessor nestedPa = getPropertyAccessorForPropertyPath(propertyName);
		return nestedPa.getLocalPropertyHandler(getFinalPath(nestedPa, propertyName));
	}

	/**
	 * 返回当前上下文中本地属性 {@code propertyName} 的 {@link PropertyHandler}。
	 * 仅用于访问「当前包装对象」上的属性。
	 * @param propertyName the name of a local property
	 * @return the handler for that property, or {@code null} if it has not been found
	 */
	protected abstract @Nullable PropertyHandler getLocalPropertyHandler(String propertyName);

	/**
	 * 创建新的嵌套属性访问器实例。
	 * 子类可覆盖以返回特定的 PropertyAccessor 子类型。
	 * @param object the object wrapped by this PropertyAccessor
	 * @param nestedPath the nested path of the object
	 * @return the nested PropertyAccessor instance
	 */
	protected abstract AbstractNestablePropertyAccessor newNestedPropertyAccessor(Object object, String nestedPath);

	/**
	 * 为指定属性创建 {@link NotWritablePropertyException}。
	 */
	protected abstract NotWritablePropertyException createNotWritablePropertyException(String propertyName);


	/**
	 * 若开启自动增长且下标越界未超限，则扩容数组并用默认元素填充空隙后回写。
	 */
	private Object growArrayIfNecessary(Object array, int index, String name) {
		if (!isAutoGrowNestedPaths()) {
			return array;
		}
		int length = Array.getLength(array);
		if (index >= length && index < this.autoGrowCollectionLimit) {
			Class<?> componentType = array.getClass().componentType();
			Object newArray = Array.newInstance(componentType, index + 1);
			System.arraycopy(array, 0, newArray, 0, length);
			for (int i = length; i < Array.getLength(newArray); i++) {
				Array.set(newArray, i, newValue(componentType, null, name));
			}
			setPropertyValue(name, newArray);
			Object defaultValue = getPropertyValue(name);
			Assert.state(defaultValue != null, "Default value must not be null");
			return defaultValue;
		}
		else {
			return array;
		}
	}

	/**
	 * 若开启自动增长且下标越界未超限，向集合追加默认元素直到可访问该下标。
	 */
	private void growCollectionIfNecessary(Collection<Object> collection, int index, String name,
			PropertyHandler ph, int nestingLevel) {

		if (!isAutoGrowNestedPaths()) {
			return;
		}
		int size = collection.size();
		if (index >= size && index < this.autoGrowCollectionLimit) {
			Class<?> elementType = ph.getResolvableType().getNested(nestingLevel).asCollection().resolveGeneric();
			if (elementType != null) {
				for (int i = collection.size(); i < index + 1; i++) {
					collection.add(newValue(elementType, null, name));
				}
			}
		}
	}

	/**
	 * 取路径的最后一段（目标 Bean 上的属性名）；非嵌套时原样返回。
	 * @param pa property accessor to work on
	 * @param nestedPath property path we know is nested
	 * @return last component of the path (the property on the target bean)
	 */
	protected String getFinalPath(AbstractNestablePropertyAccessor pa, String nestedPath) {
		if (pa == this) {
			return nestedPath;
		}
		return nestedPath.substring(PropertyAccessorUtils.getLastNestedPropertySeparatorIndex(nestedPath) + 1);
	}

	/**
	 * 递归导航嵌套属性路径，返回对应层级的属性访问器。
	 * @param propertyPath property path, which may be nested
	 * @return a property accessor for the target bean
	 */
	protected AbstractNestablePropertyAccessor getPropertyAccessorForPropertyPath(String propertyPath) {
		int pos = PropertyAccessorUtils.getFirstNestedPropertySeparatorIndex(propertyPath);
		// 遇到第一个「.」分隔符则拆分，递归进入嵌套访问器
		if (pos > -1) {
			String nestedProperty = propertyPath.substring(0, pos);
			String nestedPath = propertyPath.substring(pos + 1);
			AbstractNestablePropertyAccessor nestedPa = getNestedPropertyAccessor(nestedProperty);
			return nestedPa.getPropertyAccessorForPropertyPath(nestedPath);
		}
		else {
			return this;
		}
	}

	/**
	 * 获取给定嵌套属性的访问器；缓存未命中则新建。
	 * <p>必须缓存嵌套 PropertyAccessor，以便保留为嵌套属性注册的自定义编辑器。
	 * @param nestedProperty property to create the PropertyAccessor for
	 * @return the PropertyAccessor instance, either cached or newly created
	 */
	private AbstractNestablePropertyAccessor getNestedPropertyAccessor(String nestedProperty) {
		Map<String, AbstractNestablePropertyAccessor> nestedAccessors = this.nestedPropertyAccessors;
		if (nestedAccessors == null) {
			nestedAccessors = new HashMap<>();
			this.nestedPropertyAccessors = nestedAccessors;
		}
		// 先读出嵌套属性当前值
		PropertyTokenHolder tokens = getPropertyNameTokens(nestedProperty);
		String canonicalName = tokens.canonicalName;
		Object value = getPropertyValue(tokens);
		if (value == null || (value instanceof Optional<?> optional && optional.isEmpty())) {
			if (isAutoGrowNestedPaths()) {
				value = setDefaultValue(tokens);
			}
			else {
				throw new NullValueInNestedPathException(getRootClass(), this.nestedPath + canonicalName);
			}
		}

		// 查缓存；包装目标已变则重建
		AbstractNestablePropertyAccessor nestedPa = nestedAccessors.get(canonicalName);
		if (nestedPa == null || nestedPa.getWrappedInstance() != ObjectUtils.unwrapOptional(value)) {
			if (logger.isTraceEnabled()) {
				logger.trace("Creating new nested " + getClass().getSimpleName() + " for property '" + canonicalName + "'");
			}
			nestedPa = newNestedPropertyAccessor(value, this.nestedPath + canonicalName + NESTED_PROPERTY_SEPARATOR);
			// 继承默认与自定义 PropertyEditor
			copyDefaultEditorsTo(nestedPa);
			copyCustomEditorsTo(nestedPa, canonicalName);
			nestedAccessors.put(canonicalName, nestedPa);
		}
		else {
			if (logger.isTraceEnabled()) {
				logger.trace("Using cached nested property accessor for property '" + canonicalName + "'");
			}
		}
		return nestedPa;
	}

	/** 为 token 对应属性创建默认值并写回，再读出返回。 */
	private Object setDefaultValue(PropertyTokenHolder tokens) {
		PropertyValue pv = createDefaultPropertyValue(tokens);
		setPropertyValue(tokens, pv);
		Object defaultValue = getPropertyValue(tokens);
		Assert.state(defaultValue != null, "Default value must not be null");
		return defaultValue;
	}

	/** 按属性类型描述符构造默认 PropertyValue（数组/集合/Map/无参构造）。 */
	private PropertyValue createDefaultPropertyValue(PropertyTokenHolder tokens) {
		TypeDescriptor desc = getPropertyTypeDescriptor(tokens.canonicalName);
		if (desc == null) {
			throw new NullValueInNestedPathException(getRootClass(), this.nestedPath + tokens.canonicalName,
					"Could not determine property type for auto-growing a default value");
		}
		Object defaultValue = newValue(desc.getType(), desc, tokens.canonicalName);
		return new PropertyValue(tokens.canonicalName, defaultValue);
	}

	/**
	 * 按类型创建「自动增长」用的默认实例：数组、Collection、Map，或非 private 无参构造。
	 */
	private Object newValue(Class<?> type, @Nullable TypeDescriptor desc, String name) {
		try {
			if (type.isArray()) {
				return createArray(type);
			}
			else if (Collection.class.isAssignableFrom(type)) {
				TypeDescriptor elementDesc = (desc != null ? desc.getElementTypeDescriptor() : null);
				return CollectionFactory.createCollection(type, (elementDesc != null ? elementDesc.getType() : null), 16);
			}
			else if (Map.class.isAssignableFrom(type)) {
				TypeDescriptor keyDesc = (desc != null ? desc.getMapKeyTypeDescriptor() : null);
				return CollectionFactory.createMap(type, (keyDesc != null ? keyDesc.getType() : null), 16);
			}
			else {
				Constructor<?> ctor = type.getDeclaredConstructor();
				if (Modifier.isPrivate(ctor.getModifiers())) {
					throw new IllegalAccessException("Auto-growing not allowed with private constructor: " + ctor);
				}
				return BeanUtils.instantiateClass(ctor);
			}
		}
		catch (Throwable ex) {
			throw new NullValueInNestedPathException(getRootClass(), this.nestedPath + name,
					"Could not instantiate property type [" + type.getName() + "] to auto-grow nested property path", ex);
		}
	}

	/**
	 * 按给定数组类型创建数组；多维数组递归创建一层子数组占位。
	 * @param arrayType the desired type of the target array
	 * @return a new array instance
	 */
	private static Object createArray(Class<?> arrayType) {
		Assert.notNull(arrayType, "Array type must not be null");
		Class<?> componentType = arrayType.componentType();
		if (componentType.isArray()) {
			Object array = Array.newInstance(componentType, 1);
			Array.set(array, 0, createArray(componentType));
			return array;
		}
		else {
			return Array.newInstance(componentType, 0);
		}
	}

	/**
	 * 将属性名解析为 token：实际属性名 + 规范化规范名 + 下标/键序列。
	 * <p>例如 {@code map['a'][0]} → actualName={@code map}，keys={@code a},{@code 0}。
	 * @param propertyName the property name to parse
	 * @return representation of the parsed property tokens
	 */
	private PropertyTokenHolder getPropertyNameTokens(String propertyName) {
		String actualName = null;
		List<String> keys = new ArrayList<>(2);
		int searchIndex = 0;
		while (searchIndex != -1) {
			int keyStart = propertyName.indexOf(PROPERTY_KEY_PREFIX, searchIndex);
			searchIndex = -1;
			if (keyStart != -1) {
				int keyEnd = getPropertyNameKeyEnd(propertyName, keyStart + PROPERTY_KEY_PREFIX.length());
				if (keyEnd != -1) {
					if (actualName == null) {
						actualName = propertyName.substring(0, keyStart);
					}
					String key = propertyName.substring(keyStart + PROPERTY_KEY_PREFIX.length(), keyEnd);
					// 去掉 map['key'] / map["key"] 外层引号
					if (key.length() > 1 && ((key.startsWith("'") && key.endsWith("'")) ||
							(key.startsWith("\"") && key.endsWith("\"")))) {
						key = key.substring(1, key.length() - 1);
					}
					keys.add(key);
					searchIndex = keyEnd + PROPERTY_KEY_SUFFIX.length();
				}
			}
		}
		PropertyTokenHolder tokens = new PropertyTokenHolder(actualName != null ? actualName : propertyName);
		if (!keys.isEmpty()) {
			tokens.canonicalName += PROPERTY_KEY_PREFIX +
					StringUtils.collectionToDelimitedString(keys, PROPERTY_KEY_SUFFIX + PROPERTY_KEY_PREFIX) +
					PROPERTY_KEY_SUFFIX;
			tokens.keys = StringUtils.toStringArray(keys);
		}
		return tokens;
	}

	/**
	 * 从 {@code startIndex} 起查找与开括号匹配的闭括号下标，支持键内嵌套 {@code []}。
	 */
	private int getPropertyNameKeyEnd(String propertyName, int startIndex) {
		int unclosedPrefixes = 0;
		int length = propertyName.length();
		for (int i = startIndex; i < length; i++) {
			switch (propertyName.charAt(i)) {
				case PropertyAccessor.PROPERTY_KEY_PREFIX_CHAR -> {
					// 属性名中还有未闭合的开括号
					unclosedPrefixes++;
				}
				case PropertyAccessor.PROPERTY_KEY_SUFFIX_CHAR -> {
					if (unclosedPrefixes == 0) {
						// 左侧无未闭合开括号 → 这就是要找的闭括号
						return i;
					}
					else {
						// 只是闭合了属性名内部某层括号
						unclosedPrefixes--;
					}
				}
			}
		}
		return -1;
	}

	@Override
	public String toString() {
		String className = getClass().getName();
		if (this.wrappedObject == null) {
			return className + ": no wrapped object set";
		}
		return className + ": wrapping object [" + ObjectUtils.identityToString(this.wrappedObject) + ']';
	}


	/**
	 * 单个属性的读写处理器抽象：屏蔽 JavaBean 与直接字段等不同访问方式。
	 * <p>子类（如 BeanWrapperImpl、DirectFieldAccessor）提供具体 get/set 与类型描述。
	 */
	protected abstract static class PropertyHandler {

		/** 属性声明类型（可能为 null）。 */
		private final @Nullable Class<?> propertyType;

		/** 是否可读。 */
		private final boolean readable;

		/** 是否可写。 */
		private final boolean writable;

		public PropertyHandler(@Nullable Class<?> propertyType, boolean readable, boolean writable) {
			this.propertyType = propertyType;
			this.readable = readable;
			this.writable = writable;
		}

		public @Nullable Class<?> getPropertyType() {
			return this.propertyType;
		}

		public boolean isReadable() {
			return this.readable;
		}

		public boolean isWritable() {
			return this.writable;
		}

		public abstract TypeDescriptor toTypeDescriptor();

		public abstract ResolvableType getResolvableType();

		/** 按嵌套层级解析 Map 的 key 类型。 */
		public TypeDescriptor getMapKeyType(int nestingLevel) {
			return TypeDescriptor.valueOf(getResolvableType().getNested(nestingLevel).asMap().resolveGeneric(0));
		}

		/** 按嵌套层级解析 Map 的 value 类型。 */
		public TypeDescriptor getMapValueType(int nestingLevel) {
			return TypeDescriptor.valueOf(getResolvableType().getNested(nestingLevel).asMap().resolveGeneric(1));
		}

		/** 按嵌套层级解析 Collection 元素类型。 */
		public TypeDescriptor getCollectionType(int nestingLevel) {
			return TypeDescriptor.valueOf(getResolvableType().getNested(nestingLevel).asCollection().resolveGeneric());
		}

		public abstract @Nullable TypeDescriptor nested(int level);

		public abstract @Nullable Object getValue() throws Exception;

		public abstract void setValue(@Nullable Object value) throws Exception;

		/**
		 * 主写入路径失败时的可选回退（默认不支持）。
		 * @return {@code true} 表示回退写入成功
		 */
		public boolean setValueFallbackIfPossible(@Nullable Object value) {
			return false;
		}
	}


	/**
	 * 属性名分词结果：实际名、规范化名、以及 {@code []} 中的键/下标序列。
	 */
	protected static class PropertyTokenHolder {

		public PropertyTokenHolder(String name) {
			this.actualName = name;
			this.canonicalName = name;
		}

		/** 去掉下标后的属性名，如 {@code address}、{@code items}。 */
		public String actualName;

		/** 规范化后的完整名（含去引号后的键），如 {@code items[0]}。 */
		public String canonicalName;

		/** 有序的索引/映射键；无下标时为 {@code null}。 */
		public String @Nullable [] keys;
	}

}
