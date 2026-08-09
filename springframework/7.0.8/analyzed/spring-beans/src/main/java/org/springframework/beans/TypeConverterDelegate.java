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

import java.beans.PropertyEditor;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.core.CollectionFactory;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.NumberUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * 将属性值转换为目标类型的内部辅助类。
 *
 * <p>基于给定的 {@link PropertyEditorRegistrySupport} 实例工作，
 * 由 {@link BeanWrapperImpl} 与 {@link SimpleTypeConverter} 作为委托使用。
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Dave Syer
 * @author Yanming Zhou
 * @since 2.0
 * @see BeanWrapperImpl
 * @see SimpleTypeConverter
 */
class TypeConverterDelegate {

	private static final Log logger = LogFactory.getLog(TypeConverterDelegate.class);

	/** 属性 Editor 注册表，提供自定义/默认 Editor 与 {@link ConversionService}。 */
	private final PropertyEditorRegistrySupport propertyEditorRegistry;

	/** 当前操作的目标 Bean 实例，可作为 Editor 上下文（如枚举解析）。 */
	private final @Nullable Object targetObject;


	/**
	 * 为给定 Editor 注册表创建 {@code TypeConverterDelegate}。
	 * @param propertyEditorRegistry 要使用的 Editor 注册表
	 */
	public TypeConverterDelegate(PropertyEditorRegistrySupport propertyEditorRegistry) {
		this(propertyEditorRegistry, null);
	}

	/**
	 * 为给定 Editor 注册表与 Bean 实例创建 {@code TypeConverterDelegate}。
	 * @param propertyEditorRegistry 要使用的 Editor 注册表
	 * @param targetObject 操作目标对象（可作为上下文传给 Editor）
	 */
	public TypeConverterDelegate(PropertyEditorRegistrySupport propertyEditorRegistry, @Nullable Object targetObject) {
		this.propertyEditorRegistry = propertyEditorRegistry;
		this.targetObject = targetObject;
	}


	/**
	 * 将值转换为指定属性所需的目标类型。
	 * @param propertyName 属性名
	 * @param oldValue 旧值（若有，可为 {@code null}）
	 * @param newValue 拟设置的新值
	 * @param requiredType 必须转换到的类型
	 * （若未知可为 {@code null}，例如集合元素场景）
	 * @return 新值，可能经类型转换得到
	 * @throws IllegalArgumentException 类型转换失败时
	 */
	public <T> @Nullable T convertIfNecessary(@Nullable String propertyName, @Nullable Object oldValue,
			Object newValue, @Nullable Class<T> requiredType) throws IllegalArgumentException {

		return convertIfNecessary(propertyName, oldValue, newValue, requiredType, TypeDescriptor.valueOf(requiredType));
	}

	/**
	 * 将值（必要时从 {@link String}）转换为指定属性所需类型。
	 * <p>核心转换入口：依次尝试自定义 Editor、{@link ConversionService}、
	 * 默认 Editor，再应用数组/集合/Map/枚举等标准转换规则。
	 * @param propertyName 属性名
	 * @param oldValue 旧值（若有，可为 {@code null}）
	 * @param newValue 拟设置的新值
	 * @param requiredType 必须转换到的类型
	 * （若未知可为 {@code null}，例如集合元素场景）
	 * @param typeDescriptor 目标属性或字段的类型描述符
	 * @return 新值，可能经类型转换得到
	 * @throws IllegalArgumentException 类型转换失败时
	 */
	@SuppressWarnings("unchecked")
	public <T> @Nullable T convertIfNecessary(@Nullable String propertyName, @Nullable Object oldValue, @Nullable Object newValue,
			@Nullable Class<T> requiredType, @Nullable TypeDescriptor typeDescriptor) throws IllegalArgumentException {

		// 该类型/属性是否注册了自定义 Editor？
		PropertyEditor editor = this.propertyEditorRegistry.findCustomEditor(requiredType, propertyName);

		ConversionFailedException conversionAttemptEx = null;

		// 无自定义 Editor，但配置了 ConversionService？
		ConversionService conversionService = this.propertyEditorRegistry.getConversionService();
		if (editor == null && conversionService != null && newValue != null && typeDescriptor != null) {
			TypeDescriptor sourceTypeDesc = TypeDescriptor.forObject(newValue);
			if (conversionService.canConvert(sourceTypeDesc, typeDescriptor)) {
				try {
					return (T) conversionService.convert(newValue, sourceTypeDesc, typeDescriptor);
				}
				catch (ConversionFailedException ex) {
					// 转换失败则回退到下方默认转换逻辑
					conversionAttemptEx = ex;
				}
			}
		}

		Object convertedValue = newValue;

		// 值类型与目标类型不匹配？
		if (editor != null || (requiredType != null && !ClassUtils.isAssignableValue(requiredType, convertedValue))) {
			if (typeDescriptor != null && requiredType != null && Collection.class.isAssignableFrom(requiredType)) {
				TypeDescriptor elementTypeDesc = typeDescriptor.getElementTypeDescriptor();
				if (elementTypeDesc != null) {
					Class<?> elementType = elementTypeDesc.getType();
					if (convertedValue instanceof String text) {
						if (Class.class == elementType || Enum.class.isAssignableFrom(elementType)) {
							convertedValue = StringUtils.commaDelimitedListToStringArray(text);
						}
						if (editor == null && String.class != elementType) {
							editor = findDefaultEditor(elementType.arrayType());
						}
					}
				}
			}
			if (editor == null) {
				editor = findDefaultEditor(requiredType);
			}
			convertedValue = doConvertValue(oldValue, convertedValue, requiredType, editor);
		}

		boolean standardConversion = false;

		if (requiredType != null) {
			// 在合适时尝试标准类型转换规则。

			if (convertedValue != null) {
				if (Object.class == requiredType) {
					return (T) convertedValue;
				}
				else if (requiredType.isArray()) {
					// 需要数组：逐元素转换为目标组件类型。
					if (convertedValue instanceof String text &&
							Enum.class.isAssignableFrom(requiredType.componentType())) {
						convertedValue = StringUtils.commaDelimitedListToStringArray(text);
					}
					return (T) convertToTypedArray(convertedValue, propertyName, requiredType.componentType());
				}
				else if (convertedValue.getClass().isArray()) {
					if (Collection.class.isAssignableFrom(requiredType)) {
						convertedValue = convertToTypedCollection(CollectionUtils.arrayToList(convertedValue),
								propertyName, requiredType, typeDescriptor);
						standardConversion = true;
					}
					else if (Array.getLength(convertedValue) == 1) {
						// 单元素数组可解包为标量
						convertedValue = Array.get(convertedValue, 0);
						standardConversion = true;
					}
				}
				else if (convertedValue instanceof Collection<?> coll) {
					// 若已确定元素类型，则逐元素转换。
					convertedValue = convertToTypedCollection(coll, propertyName, requiredType, typeDescriptor);
					standardConversion = true;
				}
				else if (convertedValue instanceof Map<?, ?> map) {
					// 若已确定键/值类型，则分别转换。
					convertedValue = convertToTypedMap(map, propertyName, requiredType, typeDescriptor);
					standardConversion = true;
				}
				if (String.class == requiredType && ClassUtils.isPrimitiveOrWrapper(convertedValue.getClass())) {
					// 任意基本类型/包装类型都可转为字符串
					return (T) convertedValue.toString();
				}
				else if (convertedValue instanceof String text && !requiredType.isInstance(convertedValue)) {
					if (conversionAttemptEx == null && !requiredType.isInterface() && !requiredType.isEnum()) {
						try {
							Constructor<T> strCtor = requiredType.getConstructor(String.class);
							return BeanUtils.instantiateClass(strCtor, convertedValue);
						}
						catch (NoSuchMethodException ex) {
							// 无 String 构造器，继续尝试字段查找
							if (logger.isTraceEnabled()) {
								logger.trace("No String constructor found on type [" + requiredType.getName() + "]", ex);
							}
						}
						catch (Exception ex) {
							if (logger.isDebugEnabled()) {
								logger.debug("Construction via String failed for type [" + requiredType.getName() + "]", ex);
							}
						}
					}
					String trimmedValue = text.trim();
					if (requiredType.isEnum() && trimmedValue.isEmpty()) {
						// 空枚举标识符：将枚举值重置为 null
						return null;
					}
					convertedValue = attemptToConvertStringToEnum(requiredType, trimmedValue, convertedValue);
					standardConversion = true;
				}
				else if (convertedValue instanceof Number num && Number.class.isAssignableFrom(requiredType)) {
					convertedValue = NumberUtils.convertNumberToTargetClass(num, (Class<Number>) requiredType);
					standardConversion = true;
				}
			}
			else {
				// convertedValue == null
				if (requiredType == Optional.class) {
					convertedValue = Optional.empty();
				}
			}

			if (!ClassUtils.isAssignableValue(requiredType, convertedValue)) {
				if (conversionAttemptEx != null) {
					// 上方 ConversionService 调用抛出的原始异常
					throw conversionAttemptEx;
				}
				else if (conversionService != null && typeDescriptor != null) {
					// 此前未尝试 ConversionService（可能找到了自定义 Editor 但无法产出目标类型）
					TypeDescriptor sourceTypeDesc = TypeDescriptor.forObject(newValue);
					if (conversionService.canConvert(sourceTypeDesc, typeDescriptor)) {
						return (T) conversionService.convert(newValue, sourceTypeDesc, typeDescriptor);
					}
				}

				// 确定无法匹配：抛出 IllegalArgumentException / IllegalStateException
				StringBuilder msg = new StringBuilder();
				msg.append("Cannot convert value of type '").append(ClassUtils.getDescriptiveType(newValue));
				msg.append("' to required type '").append(ClassUtils.getQualifiedName(requiredType)).append('\'');
				if (propertyName != null) {
					msg.append(" for property '").append(propertyName).append('\'');
				}
				if (editor != null) {
					msg.append(": PropertyEditor [").append(editor.getClass().getName()).append(
							"] returned inappropriate value of type '").append(
							ClassUtils.getDescriptiveType(convertedValue)).append('\'');
					throw new IllegalArgumentException(msg.toString());
				}
				else {
					msg.append(": no matching editors or conversion strategy found");
					throw new IllegalStateException(msg.toString());
				}
			}
		}

		if (conversionAttemptEx != null) {
			if (editor == null && !standardConversion && requiredType != null && Object.class != requiredType) {
				throw conversionAttemptEx;
			}
			logger.debug("Original ConversionService attempt failed - ignored since " +
					"PropertyEditor based conversion eventually succeeded", conversionAttemptEx);
		}

		return (T) convertedValue;
	}

	private Object attemptToConvertStringToEnum(Class<?> requiredType, String trimmedValue, Object currentConvertedValue) {
		Object convertedValue = currentConvertedValue;

		if (Enum.class == requiredType && this.targetObject != null) {
			// 目标类型声明为原始 Enum：将 trimmed 值视为 <enum.fqn>.FIELD_NAME
			int index = trimmedValue.lastIndexOf('.');
			if (index > - 1) {
				String enumType = trimmedValue.substring(0, index);
				String fieldName = trimmedValue.substring(index + 1);
				ClassLoader cl = this.targetObject.getClass().getClassLoader();
				try {
					Class<?> enumValueType = ClassUtils.forName(enumType, cl);
					Field enumField = enumValueType.getField(fieldName);
					convertedValue = enumField.get(null);
				}
				catch (ClassNotFoundException ex) {
					if (logger.isTraceEnabled()) {
						logger.trace("Enum class [" + enumType + "] cannot be loaded", ex);
					}
				}
				catch (Throwable ex) {
					if (logger.isTraceEnabled()) {
						logger.trace("Field [" + fieldName + "] isn't an enum value for type [" + enumType + "]", ex);
					}
				}
			}
		}

		if (convertedValue == currentConvertedValue) {
			// 回退：按字段名查找（Java 枚举或自定义静态字段枚举）
			// 结果仍需后续类型检查，故此处不直接返回
			try {
				Field enumField = requiredType.getField(trimmedValue);
				ReflectionUtils.makeAccessible(enumField);
				convertedValue = enumField.get(null);
			}
			catch (Throwable ex) {
				if (logger.isTraceEnabled()) {
					logger.trace("Field [" + convertedValue + "] isn't an enum value", ex);
				}
			}
		}

		return convertedValue;
	}
	/**
	 * 为给定类型查找默认 PropertyEditor。
	 * @param requiredType 要查找 Editor 的类型
	 * @return 对应 Editor，若无则 {@code null}
	 */
	private @Nullable PropertyEditor findDefaultEditor(@Nullable Class<?> requiredType) {
		PropertyEditor editor = null;
		if (requiredType != null) {
			// 无自定义 Editor → 查 BeanWrapperImpl 的默认 Editor
			editor = this.propertyEditorRegistry.getDefaultEditor(requiredType);
			if (editor == null && String.class != requiredType) {
				// 仍无 → 查标准 JavaBean 约定 Editor
				editor = BeanUtils.findEditorByConvention(requiredType);
			}
		}
		return editor;
	}

	/**
	 * 使用给定 PropertyEditor 将值（必要时从 String）转换为目标类型。
	 * @param oldValue 旧值（若有，可为 {@code null}）
	 * @param newValue 拟设置的新值
	 * @param requiredType 必须转换到的类型
	 * （若未知可为 {@code null}，例如集合元素场景）
	 * @param editor 要使用的 PropertyEditor
	 * @return 新值，可能经类型转换得到
	 * @throws IllegalArgumentException 类型转换失败时
	 */
	private @Nullable Object doConvertValue(@Nullable Object oldValue, @Nullable Object newValue,
			@Nullable Class<?> requiredType, @Nullable PropertyEditor editor) {

		Object convertedValue = newValue;

		if (editor != null && !(convertedValue instanceof String)) {
			// 非 String → 使用 PropertyEditor.setValue。
			// 标准 PropertyEditor 通常返回同一对象；
			// 此处允许特殊 Editor 覆盖 setValue，以支持非 String → 目标类型。
			try {
				editor.setValue(convertedValue);
				Object newConvertedValue = editor.getValue();
				if (newConvertedValue != convertedValue) {
					convertedValue = newConvertedValue;
					// Editor 已完成转换，不再用于后续 setAsText
					editor = null;
				}
			}
			catch (Exception ex) {
				if (logger.isDebugEnabled()) {
					logger.debug("PropertyEditor [" + editor.getClass().getName() + "] does not support setValue call", ex);
				}
				// 吞掉异常继续后续流程
			}
		}

		Object returnValue = convertedValue;

		if (requiredType != null && !requiredType.isArray() && convertedValue instanceof String[] array) {
			// String 数组 → 逗号分隔的单个 String（若 Editor 尚未处理）
			// 随后可能进入 PropertyEditor.setAsText
			if (logger.isTraceEnabled()) {
				logger.trace("Converting String array to comma-delimited String [" + convertedValue + "]");
			}
			convertedValue = StringUtils.arrayToCommaDelimitedString(array);
		}

		if (convertedValue instanceof String newTextValue) {
			if (editor != null) {
				// String 值 → 使用 PropertyEditor.setAsText
				if (logger.isTraceEnabled()) {
					logger.trace("Converting String to [" + requiredType + "] using property editor [" + editor + "]");
				}
				return doConvertTextValue(oldValue, newTextValue, editor);
			}
			else if (String.class == requiredType) {
				returnValue = convertedValue;
			}
		}

		return returnValue;
	}

	/**
	 * 使用给定 PropertyEditor 将文本值转换为目标类型。
	 * @param oldValue 旧值（若有，可为 {@code null}）
	 * @param newTextValue 拟设置的文本值
	 * @param editor 要使用的 PropertyEditor
	 * @return 转换后的值
	 */
	private Object doConvertTextValue(@Nullable Object oldValue, String newTextValue, PropertyEditor editor) {
		try {
			editor.setValue(oldValue);
		}
		catch (Exception ex) {
			if (logger.isDebugEnabled()) {
				logger.debug("PropertyEditor [" + editor.getClass().getName() + "] does not support setValue call", ex);
			}
			// 吞掉异常继续 setAsText
		}
		editor.setAsText(newTextValue);
		return editor.getValue();
	}

	private Object convertToTypedArray(Object input, @Nullable String propertyName, Class<?> componentType) {
		if (input instanceof Collection<?> coll) {
			// 集合元素 → 数组元素
			Object result = Array.newInstance(componentType, coll.size());
			int i = 0;
			for (Iterator<?> it = coll.iterator(); it.hasNext(); i++) {
				Object value = convertIfNecessary(
						buildIndexedPropertyName(propertyName, i), null, it.next(), componentType);
				Array.set(result, i, value);
			}
			return result;
		}
		else if (input.getClass().isArray()) {
			// 数组元素按需转换
			if (componentType.equals(input.getClass().componentType()) &&
					!this.propertyEditorRegistry.hasCustomEditorForElement(componentType, propertyName)) {
				return input;
			}
			int arrayLength = Array.getLength(input);
			Object result = Array.newInstance(componentType, arrayLength);
			for (int i = 0; i < arrayLength; i++) {
				Object value = convertIfNecessary(
						buildIndexedPropertyName(propertyName, i), null, Array.get(input, i), componentType);
				Array.set(result, i, value);
			}
			return result;
		}
		else {
			// 标量值：包装为单元素数组
			Object result = Array.newInstance(componentType, 1);
			Object value = convertIfNecessary(
					buildIndexedPropertyName(propertyName, 0), null, input, componentType);
			Array.set(result, 0, value);
			return result;
		}
	}

	@SuppressWarnings("unchecked")
	/** 将集合元素转换为目标元素类型，必要时创建目标集合类型的副本。 */
	private Collection<?> convertToTypedCollection(Collection<?> original, @Nullable String propertyName,
			Class<?> requiredType, @Nullable TypeDescriptor typeDescriptor) {

		if (!Collection.class.isAssignableFrom(requiredType)) {
			return original;
		}

		boolean approximable = CollectionFactory.isApproximableCollectionType(requiredType);
		if (!approximable && !canCreateCopy(requiredType)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Custom Collection type [" + original.getClass().getName() +
						"] does not allow for creating a copy - injecting original Collection as-is");
			}
			return original;
		}

		boolean originalAllowed = requiredType.isInstance(original);
		TypeDescriptor elementType = (typeDescriptor != null ? typeDescriptor.getElementTypeDescriptor() : null);
		if (elementType == null && originalAllowed &&
				!this.propertyEditorRegistry.hasCustomEditorForElement(null, propertyName)) {
			return original;
		}

		Iterator<?> it;
		try {
			it = original.iterator();
		}
		catch (Throwable ex) {
			if (logger.isDebugEnabled()) {
				logger.debug("Cannot access Collection of type [" + original.getClass().getName() +
						"] - injecting original Collection as-is: " + ex);
			}
			return original;
		}

		Collection<Object> convertedCopy;
		try {
			if (approximable && requiredType.isInstance(original)) {
				convertedCopy = CollectionFactory.createApproximateCollection(original, original.size());
			}
			else {
				convertedCopy = CollectionFactory.createCollection(requiredType, original.size());
			}
		}
		catch (Throwable ex) {
			if (logger.isDebugEnabled()) {
				logger.debug("Cannot create copy of Collection type [" + original.getClass().getName() +
						"] - injecting original Collection as-is: " + ex);
			}
			return original;
		}

		for (int i = 0; it.hasNext(); i++) {
			Object element = it.next();
			String indexedPropertyName = buildIndexedPropertyName(propertyName, i);
			Object convertedElement = convertIfNecessary(indexedPropertyName, null, element,
					(elementType != null ? elementType.getType() : null) , elementType);
			try {
				convertedCopy.add(convertedElement);
			}
			catch (Throwable ex) {
				if (logger.isDebugEnabled()) {
					logger.debug("Collection type [" + original.getClass().getName() +
							"] seems to be read-only - injecting original Collection as-is: " + ex);
				}
				return original;
			}
			originalAllowed = originalAllowed && (element == convertedElement);
		}
		return (originalAllowed ? original : convertedCopy);
	}

	@SuppressWarnings("unchecked")
	/** 将 Map 的键与值分别转换为目标类型，必要时创建目标 Map 类型的副本。 */
	private Map<?, ?> convertToTypedMap(Map<?, ?> original, @Nullable String propertyName,
			Class<?> requiredType, @Nullable TypeDescriptor typeDescriptor) {

		if (!Map.class.isAssignableFrom(requiredType)) {
			return original;
		}

		boolean approximable = CollectionFactory.isApproximableMapType(requiredType);
		if (!approximable && !canCreateCopy(requiredType)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Custom Map type [" + original.getClass().getName() +
						"] does not allow for creating a copy - injecting original Map as-is");
			}
			return original;
		}

		boolean originalAllowed = requiredType.isInstance(original);
		TypeDescriptor keyType = (typeDescriptor != null ? typeDescriptor.getMapKeyTypeDescriptor() : null);
		TypeDescriptor valueType = (typeDescriptor != null ? typeDescriptor.getMapValueTypeDescriptor() : null);
		if (keyType == null && valueType == null && originalAllowed &&
				!this.propertyEditorRegistry.hasCustomEditorForElement(null, propertyName)) {
			return original;
		}

		Iterator<?> it;
		try {
			it = original.entrySet().iterator();
		}
		catch (Throwable ex) {
			if (logger.isDebugEnabled()) {
				logger.debug("Cannot access Map of type [" + original.getClass().getName() +
						"] - injecting original Map as-is: " + ex);
			}
			return original;
		}

		Map<Object, Object> convertedCopy;
		try {
			if (approximable && requiredType.isInstance(original)) {
				convertedCopy = CollectionFactory.createApproximateMap(original, original.size());
			}
			else {
				convertedCopy = CollectionFactory.createMap(requiredType, original.size());
			}
		}
		catch (Throwable ex) {
			if (logger.isDebugEnabled()) {
				logger.debug("Cannot create copy of Map type [" + original.getClass().getName() +
						"] - injecting original Map as-is: " + ex);
			}
			return original;
		}

		while (it.hasNext()) {
			Map.Entry<?, ?> entry = (Map.Entry<?, ?>) it.next();
			Object key = entry.getKey();
			Object value = entry.getValue();
			String keyedPropertyName = buildKeyedPropertyName(propertyName, key);
			Object convertedKey = convertIfNecessary(keyedPropertyName, null, key,
					(keyType != null ? keyType.getType() : null), keyType);
			Object convertedValue = convertIfNecessary(keyedPropertyName, null, value,
					(valueType!= null ? valueType.getType() : null), valueType);
			try {
				convertedCopy.put(convertedKey, convertedValue);
			}
			catch (Throwable ex) {
				if (logger.isDebugEnabled()) {
					logger.debug("Map type [" + original.getClass().getName() +
							"] seems to be read-only - injecting original Map as-is: " + ex);
				}
				return original;
			}
			originalAllowed = originalAllowed && (key == convertedKey) && (value == convertedValue);
		}
		return (originalAllowed ? original : convertedCopy);
	}

	/** 构建带下标后缀的属性名，如 {@code items[0]}。 */
	private @Nullable String buildIndexedPropertyName(@Nullable String propertyName, int index) {
		return (propertyName != null ?
				propertyName + PropertyAccessor.PROPERTY_KEY_PREFIX + index + PropertyAccessor.PROPERTY_KEY_SUFFIX :
				null);
	}

	/** 构建带键后缀的属性名，如 {@code map[key]}。 */
	private @Nullable String buildKeyedPropertyName(@Nullable String propertyName, Object key) {
		return (propertyName != null ?
				propertyName + PropertyAccessor.PROPERTY_KEY_PREFIX + key + PropertyAccessor.PROPERTY_KEY_SUFFIX :
				null);
	}

	/** 目标集合/Map 类型是否可通过公共无参构造实例化以创建副本。 */
	private boolean canCreateCopy(Class<?> requiredType) {
		return (!requiredType.isInterface() && !Modifier.isAbstract(requiredType.getModifiers()) &&
				Modifier.isPublic(requiredType.getModifiers()) && ClassUtils.hasConstructor(requiredType));
	}

}
