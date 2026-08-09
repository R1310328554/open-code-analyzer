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
import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Currency;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;
import org.xml.sax.InputSource;

import org.springframework.beans.propertyeditors.ByteArrayPropertyEditor;
import org.springframework.beans.propertyeditors.CharArrayPropertyEditor;
import org.springframework.beans.propertyeditors.CharacterEditor;
import org.springframework.beans.propertyeditors.CharsetEditor;
import org.springframework.beans.propertyeditors.ClassArrayEditor;
import org.springframework.beans.propertyeditors.ClassEditor;
import org.springframework.beans.propertyeditors.CurrencyEditor;
import org.springframework.beans.propertyeditors.CustomBooleanEditor;
import org.springframework.beans.propertyeditors.CustomCollectionEditor;
import org.springframework.beans.propertyeditors.CustomMapEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.beans.propertyeditors.FileEditor;
import org.springframework.beans.propertyeditors.InputSourceEditor;
import org.springframework.beans.propertyeditors.InputStreamEditor;
import org.springframework.beans.propertyeditors.LocaleEditor;
import org.springframework.beans.propertyeditors.PathEditor;
import org.springframework.beans.propertyeditors.PatternEditor;
import org.springframework.beans.propertyeditors.PropertiesEditor;
import org.springframework.beans.propertyeditors.ReaderEditor;
import org.springframework.beans.propertyeditors.StringArrayPropertyEditor;
import org.springframework.beans.propertyeditors.TimeZoneEditor;
import org.springframework.beans.propertyeditors.URIEditor;
import org.springframework.beans.propertyeditors.URLEditor;
import org.springframework.beans.propertyeditors.UUIDEditor;
import org.springframework.beans.propertyeditors.ZoneIdEditor;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceArrayPropertyEditor;
import org.springframework.util.ClassUtils;

/**
 * {@link PropertyEditorRegistry} 接口的基础实现。
 * 提供默认 Editor 与自定义 Editor 的管理，主要作为 {@link BeanWrapperImpl} 的基类。
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Sebastien Deleuze
 * @since 1.2.6
 * @see java.beans.PropertyEditorManager
 * @see java.beans.PropertyEditorSupport#setAsText
 * @see java.beans.PropertyEditorSupport#setValue
 */
public class PropertyEditorRegistrySupport implements PropertyEditorRegistry {

	private @Nullable ConversionService conversionService;

	/** 是否已激活默认 Editor 注册。 */
	private boolean defaultEditorsActive = false;

	/** 是否激活仅用于配置值的 Editor（如 String 数组 Editor）。 */
	private boolean configValueEditorsActive = false;

	/** 惰性覆盖默认 Editor 的注册器。 */
	private @Nullable PropertyEditorRegistrar defaultEditorRegistrar;

	@SuppressWarnings("NullAway.Init")
	/** 内置默认 PropertyEditor 映射。 */
	private Map<Class<?>, PropertyEditor> defaultEditors;

	/** 被覆盖的默认 Editor。 */
	private @Nullable Map<Class<?>, PropertyEditor> overriddenDefaultEditors;

	/** 按类型注册的自定义 Editor。 */
	private @Nullable Map<Class<?>, PropertyEditor> customEditors;

	/** 按属性路径注册的自定义 Editor。 */
	private @Nullable Map<String, CustomEditorHolder> customEditorsForPath;

	/** 自定义 Editor 的缓存（超类/接口匹配结果）。 */
	private @Nullable Map<Class<?>, PropertyEditor> customEditorCache;


	/**
	 * 指定用于属性值转换的 {@link ConversionService}，作为 JavaBeans PropertyEditor 的替代。
	 */
	public void setConversionService(@Nullable ConversionService conversionService) {
		this.conversionService = conversionService;
	}

	/**
	 * 返回关联的 ConversionService（若有）。
	 */
	public @Nullable ConversionService getConversionService() {
		return this.conversionService;
	}


	//---------------------------------------------------------------------
	// 默认 Editor 管理
	//---------------------------------------------------------------------

	/**
	 * 激活本注册器实例的默认 Editor，支持在需要时惰性注册。
	 */
	protected void registerDefaultEditors() {
		this.defaultEditorsActive = true;
	}

	/**
	 * 激活仅用于配置目的的 config value Editor，
	 * 例如 {@link org.springframework.beans.propertyeditors.StringArrayPropertyEditor}。
	 * <p>这些 Editor 默认不注册，因为通常不适合数据绑定。
	 * 当然仍可通过 {@link #registerCustomEditor} 单独注册。
	 */
	public void useConfigValueEditors() {
		this.configValueEditorsActive = true;
	}

	/**
	 * 设置默认 Editor 注册器，作为惰性覆盖默认 Editor 的方式。
	 * <p>预期与 {@link PropertyEditorRegistrySupport} 协作，
	 * 将给定 {@link PropertyEditorRegistry} 向下转型并调用 {@link #overrideDefaultEditor}
	 * 注册额外默认 Editor。
	 * @param registrar 在实际需要默认 Editor 时调用的注册器
	 * @since 6.2.3
	 * @see #overrideDefaultEditor
	 */
	public void setDefaultEditorRegistrar(PropertyEditorRegistrar registrar) {
		this.defaultEditorRegistrar = registrar;
	}

	/**
	 * 用给定 PropertyEditor 覆盖指定类型的默认 Editor。
	 * <p>注意这与注册自定义 Editor 不同：语义上仍是默认 Editor。
	 * ConversionService 会覆盖此类默认 Editor，而自定义 Editor 通常覆盖 ConversionService。
	 * @param requiredType 属性类型
	 * @param propertyEditor 要注册的 Editor
	 * @see #registerCustomEditor(Class, PropertyEditor)
	 */
	public void overrideDefaultEditor(Class<?> requiredType, PropertyEditor propertyEditor) {
		if (this.overriddenDefaultEditors == null) {
			this.overriddenDefaultEditors = new HashMap<>();
		}
		this.overriddenDefaultEditors.put(requiredType, propertyEditor);
	}

	/**
	 * 获取给定属性类型的默认 Editor（若有）。
	 * <p>若默认 Editor 已激活，则惰性注册。
	 * @param requiredType 属性类型
	 * @return 默认 Editor，未找到则返回 {@code null}
	 * @see #registerDefaultEditors
	 */
	public @Nullable PropertyEditor getDefaultEditor(Class<?> requiredType) {
		if (!this.defaultEditorsActive) {
			return null;
		}
		if (this.overriddenDefaultEditors == null && this.defaultEditorRegistrar != null) {
			this.defaultEditorRegistrar.registerCustomEditors(this);
		}
		if (this.overriddenDefaultEditors != null) {
			PropertyEditor editor = this.overriddenDefaultEditors.get(requiredType);
			if (editor != null) {
				return editor;
			}
		}
		if (this.defaultEditors == null) {
			createDefaultEditors();
		}
		return this.defaultEditors.get(requiredType);
	}

	/** 为本注册器实例实际注册默认 Editor。 */
	private void createDefaultEditors() {
		this.defaultEditors = new HashMap<>(64);

		// 简单 Editor，无参数化能力；JDK 不包含这些目标类型的默认 Editor
		this.defaultEditors.put(Charset.class, new CharsetEditor());
		this.defaultEditors.put(Class.class, new ClassEditor());
		this.defaultEditors.put(Class[].class, new ClassArrayEditor());
		this.defaultEditors.put(Currency.class, new CurrencyEditor());
		this.defaultEditors.put(File.class, new FileEditor());
		this.defaultEditors.put(InputStream.class, new InputStreamEditor());
		this.defaultEditors.put(InputSource.class, new InputSourceEditor());
		this.defaultEditors.put(Locale.class, new LocaleEditor());
		this.defaultEditors.put(Path.class, new PathEditor());
		this.defaultEditors.put(Pattern.class, new PatternEditor());
		this.defaultEditors.put(Properties.class, new PropertiesEditor());
		this.defaultEditors.put(Reader.class, new ReaderEditor());
		this.defaultEditors.put(Resource[].class, new ResourceArrayPropertyEditor());
		this.defaultEditors.put(TimeZone.class, new TimeZoneEditor());
		this.defaultEditors.put(URI.class, new URIEditor());
		this.defaultEditors.put(URL.class, new URLEditor());
		this.defaultEditors.put(UUID.class, new UUIDEditor());
		this.defaultEditors.put(ZoneId.class, new ZoneIdEditor());

		// 集合 Editor 的默认实例；可通过注册自定义实例覆盖
		this.defaultEditors.put(Collection.class, new CustomCollectionEditor(Collection.class));
		this.defaultEditors.put(Set.class, new CustomCollectionEditor(Set.class));
		this.defaultEditors.put(SortedSet.class, new CustomCollectionEditor(SortedSet.class));
		this.defaultEditors.put(List.class, new CustomCollectionEditor(List.class));
		this.defaultEditors.put(SortedMap.class, new CustomMapEditor(SortedMap.class));

		// 基本类型数组的默认 Editor
		this.defaultEditors.put(byte[].class, new ByteArrayPropertyEditor());
		this.defaultEditors.put(char[].class, new CharArrayPropertyEditor());

		// JDK 不包含 char 的默认 Editor！
		this.defaultEditors.put(char.class, new CharacterEditor(false));
		this.defaultEditors.put(Character.class, new CharacterEditor(true));

		// Spring CustomBooleanEditor 接受的标志值比 JDK 默认 Editor 更多
		this.defaultEditors.put(boolean.class, new CustomBooleanEditor(false));
		this.defaultEditors.put(Boolean.class, new CustomBooleanEditor(true));

		// JDK 不包含数值包装类型的默认 Editor！
		// 用 CustomNumberEditor 覆盖 JDK 基本类型数值 Editor
		this.defaultEditors.put(byte.class, new CustomNumberEditor(Byte.class, false));
		this.defaultEditors.put(Byte.class, new CustomNumberEditor(Byte.class, true));
		this.defaultEditors.put(short.class, new CustomNumberEditor(Short.class, false));
		this.defaultEditors.put(Short.class, new CustomNumberEditor(Short.class, true));
		this.defaultEditors.put(int.class, new CustomNumberEditor(Integer.class, false));
		this.defaultEditors.put(Integer.class, new CustomNumberEditor(Integer.class, true));
		this.defaultEditors.put(long.class, new CustomNumberEditor(Long.class, false));
		this.defaultEditors.put(Long.class, new CustomNumberEditor(Long.class, true));
		this.defaultEditors.put(float.class, new CustomNumberEditor(Float.class, false));
		this.defaultEditors.put(Float.class, new CustomNumberEditor(Float.class, true));
		this.defaultEditors.put(double.class, new CustomNumberEditor(Double.class, false));
		this.defaultEditors.put(Double.class, new CustomNumberEditor(Double.class, true));
		this.defaultEditors.put(BigDecimal.class, new CustomNumberEditor(BigDecimal.class, true));
		this.defaultEditors.put(BigInteger.class, new CustomNumberEditor(BigInteger.class, true));

		// 仅在显式请求时注册 config value Editor
		if (this.configValueEditorsActive) {
			StringArrayPropertyEditor sae = new StringArrayPropertyEditor();
			this.defaultEditors.put(String[].class, sae);
			this.defaultEditors.put(short[].class, sae);
			this.defaultEditors.put(int[].class, sae);
			this.defaultEditors.put(long[].class, sae);
		}
	}

	/**
	 * 将本实例中注册的默认 Editor 复制到目标注册器。
	 * @param target 目标注册器
	 */
	protected void copyDefaultEditorsTo(PropertyEditorRegistrySupport target) {
		target.defaultEditorsActive = this.defaultEditorsActive;
		target.configValueEditorsActive = this.configValueEditorsActive;
		target.defaultEditors = this.defaultEditors;
		target.overriddenDefaultEditors = this.overriddenDefaultEditors;
	}


	//---------------------------------------------------------------------
	// 自定义 Editor 管理
	//---------------------------------------------------------------------

	@Override
	public void registerCustomEditor(Class<?> requiredType, PropertyEditor propertyEditor) {
		registerCustomEditor(requiredType, null, propertyEditor);
	}

	@Override
	public void registerCustomEditor(@Nullable Class<?> requiredType, @Nullable String propertyPath, PropertyEditor propertyEditor) {
		if (requiredType == null && propertyPath == null) {
			throw new IllegalArgumentException("Either requiredType or propertyPath is required");
		}
		if (propertyPath != null) {
			if (this.customEditorsForPath == null) {
				this.customEditorsForPath = new LinkedHashMap<>(16);
			}
			this.customEditorsForPath.put(propertyPath, new CustomEditorHolder(propertyEditor, requiredType));
		}
		else {
			if (this.customEditors == null) {
				this.customEditors = new LinkedHashMap<>(16);
			}
			this.customEditors.put(requiredType, propertyEditor);
			this.customEditorCache = null;
		}
	}

	@Override
	public @Nullable PropertyEditor findCustomEditor(@Nullable Class<?> requiredType, @Nullable String propertyPath) {
		Class<?> requiredTypeToUse = requiredType;
		if (propertyPath != null) {
			if (this.customEditorsForPath != null) {
				// 优先检查属性特定的 Editor
				PropertyEditor editor = getCustomEditor(propertyPath, requiredType);
				if (editor == null) {
					List<String> strippedPaths = new ArrayList<>();
					addStrippedPropertyPaths(strippedPaths, "", propertyPath);
					for (Iterator<String> it = strippedPaths.iterator(); it.hasNext() && editor == null;) {
						String strippedPath = it.next();
						editor = getCustomEditor(strippedPath, requiredType);
					}
				}
				if (editor != null) {
					return editor;
				}
			}
			if (requiredType == null) {
				requiredTypeToUse = getPropertyType(propertyPath);
			}
		}
		// 无属性特定 Editor → 检查类型特定 Editor
		return getCustomEditor(requiredTypeToUse);
	}

	/**
	 * 判断本注册器是否包含指定数组/集合元素的自定义 Editor。
	 * @param elementType 元素目标类型（未知时可传 {@code null}）
	 * @param propertyPath 属性路径（通常为数组/集合路径；未知时可传 {@code null}）
	 * @return 是否找到匹配的自定义 Editor
	 */
	public boolean hasCustomEditorForElement(@Nullable Class<?> elementType, @Nullable String propertyPath) {
		if (propertyPath != null && this.customEditorsForPath != null) {
			for (Map.Entry<String, CustomEditorHolder> entry : this.customEditorsForPath.entrySet()) {
				if (PropertyAccessorUtils.matchesProperty(entry.getKey(), propertyPath) &&
						entry.getValue().getPropertyEditor(elementType) != null) {
					return true;
				}
			}
		}
		// 无属性特定 Editor → 检查类型特定 Editor
		return (elementType != null && this.customEditors != null && this.customEditors.containsKey(elementType));
	}

	/**
	 * 确定给定属性路径的属性类型。
	 * <p>当 {@link #findCustomEditor} 未指定必需类型时调用，
	 * 以便仅凭属性路径也能找到类型特定的 Editor。
	 * <p>默认实现始终返回 {@code null}。
	 * BeanWrapperImpl 用 BeanWrapper 接口的标准 {@code getPropertyType} 覆盖。
	 * @param propertyPath 要确定类型的属性路径
	 * @return 属性类型，无法确定则返回 {@code null}
	 * @see BeanWrapper#getPropertyType(String)
	 */
	protected @Nullable Class<?> getPropertyType(String propertyPath) {
		return null;
	}

	/**
	 * 获取已为给定属性注册的自定义 Editor。
	 * @param propertyName 要查找的属性路径
	 * @param requiredType 要查找的类型
	 * @return 自定义 Editor，若无针对此属性的则返回 {@code null}
	 */
	private @Nullable PropertyEditor getCustomEditor(String propertyName, @Nullable Class<?> requiredType) {
		CustomEditorHolder holder =
				(this.customEditorsForPath != null ? this.customEditorsForPath.get(propertyName) : null);
		return (holder != null ? holder.getPropertyEditor(requiredType) : null);
	}

	/**
	 * 获取给定类型的自定义 Editor。若无直接匹配，则尝试超类的自定义 Editor
	 * （超类 Editor 仍可通过 {@code getAsText} 将值渲染为 String）。
	 * @param requiredType 要查找的类型
	 * @return 自定义 Editor，未找到则返回 {@code null}
	 * @see java.beans.PropertyEditor#getAsText()
	 */
	private @Nullable PropertyEditor getCustomEditor(@Nullable Class<?> requiredType) {
		if (requiredType == null || this.customEditors == null) {
			return null;
		}
		// 检查直接为类型注册的 Editor
		PropertyEditor editor = this.customEditors.get(requiredType);
		if (editor == null) {
			// 检查缓存中为超类或接口注册的 Editor
			if (this.customEditorCache != null) {
				editor = this.customEditorCache.get(requiredType);
			}
			if (editor == null) {
				// 查找超类或接口的 Editor
				for (Map.Entry<Class<?>, PropertyEditor> entry : this.customEditors.entrySet()) {
					Class<?> key = entry.getKey();
					if (key.isAssignableFrom(requiredType)) {
						editor = entry.getValue();
						// 缓存搜索类型，避免重复的 assignable-from 检查开销
						if (this.customEditorCache == null) {
							this.customEditorCache = new HashMap<>();
						}
						this.customEditorCache.put(requiredType, editor);
						if (editor != null) {
							break;
						}
					}
				}
			}
		}
		return editor;
	}

	/**
	 * 根据已注册的自定义 Editor（若针对特定类型注册）猜测指定属性的属性类型。
	 * @param propertyName 属性名
	 * @return 属性类型，无法确定则返回 {@code null}
	 */
	protected @Nullable Class<?> guessPropertyTypeFromEditors(String propertyName) {
		if (this.customEditorsForPath != null) {
			CustomEditorHolder editorHolder = this.customEditorsForPath.get(propertyName);
			if (editorHolder == null) {
				List<String> strippedPaths = new ArrayList<>();
				addStrippedPropertyPaths(strippedPaths, "", propertyName);
				for (Iterator<String> it = strippedPaths.iterator(); it.hasNext() && editorHolder == null;) {
					String strippedName = it.next();
					editorHolder = this.customEditorsForPath.get(strippedName);
				}
			}
			if (editorHolder != null) {
				return editorHolder.getRegisteredType();
			}
		}
		return null;
	}

	/**
	 * 将本实例中注册的自定义 Editor 复制到目标注册器。
	 * @param target 目标注册器
	 * @param nestedProperty 目标注册器的嵌套属性路径（若有）。
	 * 若非 null，仅复制注册在该嵌套属性之下的 Editor；若为 null，复制全部 Editor。
	 */
	protected void copyCustomEditorsTo(PropertyEditorRegistry target, @Nullable String nestedProperty) {
		String actualPropertyName =
				(nestedProperty != null ? PropertyAccessorUtils.getPropertyName(nestedProperty) : null);
		if (this.customEditors != null) {
			this.customEditors.forEach(target::registerCustomEditor);
		}
		if (this.customEditorsForPath != null) {
			this.customEditorsForPath.forEach((editorPath, editorHolder) -> {
				if (nestedProperty != null) {
					int pos = PropertyAccessorUtils.getFirstNestedPropertySeparatorIndex(editorPath);
					if (pos != -1) {
						String editorNestedProperty = editorPath.substring(0, pos);
						String editorNestedPath = editorPath.substring(pos + 1);
						if (editorNestedProperty.equals(nestedProperty) || editorNestedProperty.equals(actualPropertyName)) {
							target.registerCustomEditor(
									editorHolder.getRegisteredType(), editorNestedPath, editorHolder.getPropertyEditor());
						}
					}
				}
				else {
					target.registerCustomEditor(
							editorHolder.getRegisteredType(), editorPath, editorHolder.getPropertyEditor());
				}
			});
		}
	}


	/**
	 * 添加剥离键/索引变体后的属性路径，递归处理嵌套路径。
	 * @param strippedPaths 结果列表
	 * @param nestedPath 当前嵌套路径
	 * @param propertyPath 待检查是否剥离键/索引的属性路径
	 */
	private void addStrippedPropertyPaths(List<String> strippedPaths, String nestedPath, String propertyPath) {
		int startIndex = propertyPath.indexOf(PropertyAccessor.PROPERTY_KEY_PREFIX_CHAR);
		if (startIndex != -1) {
			int endIndex = propertyPath.indexOf(PropertyAccessor.PROPERTY_KEY_SUFFIX_CHAR);
			if (endIndex != -1) {
				String prefix = propertyPath.substring(0, startIndex);
				String key = propertyPath.substring(startIndex, endIndex + 1);
				String suffix = propertyPath.substring(endIndex + 1);
				// 剥离第一个键
				strippedPaths.add(nestedPath + prefix + suffix);
				// 在已剥离第一个键的情况下继续查找更多键
				addStrippedPropertyPaths(strippedPaths, nestedPath + prefix, suffix);
				// 在未剥离第一个键的情况下继续查找更多键
				addStrippedPropertyPaths(strippedPaths, nestedPath + prefix + key, suffix);
			}
		}
	}


	/**
	 * 带属性名的已注册自定义 Editor 持有者。
	 * 保存 PropertyEditor 本身及其注册类型。
	 */
	private static final class CustomEditorHolder {

		private final PropertyEditor propertyEditor;

		private final @Nullable Class<?> registeredType;

		private CustomEditorHolder(PropertyEditor propertyEditor, @Nullable Class<?> registeredType) {
			this.propertyEditor = propertyEditor;
			this.registeredType = registeredType;
		}

		private PropertyEditor getPropertyEditor() {
			return this.propertyEditor;
		}

		private @Nullable Class<?> getRegisteredType() {
			return this.registeredType;
		}

		private @Nullable PropertyEditor getPropertyEditor(@Nullable Class<?> requiredType) {
			// 特殊情况：未指定必需类型（通常仅发生在集合元素），
			// 或必需类型不可赋值给注册类型（通常发生在 Object 泛型属性）——
			// 若未针对 Collection 或数组类型注册，则返回 PropertyEditor
			// （未针对 Collection/数组注册时，假定用于元素）
			if (this.registeredType == null ||
					(requiredType != null &&
					(ClassUtils.isAssignable(this.registeredType, requiredType) ||
					ClassUtils.isAssignable(requiredType, this.registeredType))) ||
					(requiredType == null &&
					(!Collection.class.isAssignableFrom(this.registeredType) && !this.registeredType.isArray()))) {
				return this.propertyEditor;
			}
			else {
				return null;
			}
		}
	}

}
