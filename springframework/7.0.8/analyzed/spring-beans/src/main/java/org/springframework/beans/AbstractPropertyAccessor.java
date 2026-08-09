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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

/**
 * {@link PropertyAccessor} 接口的抽象实现。
 * <p>为本接口上的便捷方法提供默认实现；真正的属性读写留给子类完成。
 *
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @since 2.0
 * @see #getPropertyValue
 * @see #setPropertyValue
 */
public abstract class AbstractPropertyAccessor extends TypeConverterSupport implements ConfigurablePropertyAccessor {

	/** 是否在应用 PropertyEditor 前先取出属性的旧值（默认 false）。 */
	private boolean extractOldValueForEditor = false;

	/** 嵌套路径上遇到 null 中间节点时，是否自动创建默认实例以继续访问（默认 false）。 */
	private boolean autoGrowNestedPaths = false;

	/**
	 * 批量设置且 {@code ignoreUnknown=true} 时，临时抑制“属性不可写”异常的抛出。
	 * <p>包可见，供同包访问器在真正抛出 {@link NotWritablePropertyException} 前检查。
	 */
	boolean suppressNotWritablePropertyException = false;


	/**
	 * 设置是否在应用编辑器前提取属性旧值。
	 */
	@Override
	public void setExtractOldValueForEditor(boolean extractOldValueForEditor) {
		this.extractOldValueForEditor = extractOldValueForEditor;
	}

	/**
	 * 返回是否在应用编辑器前提取属性旧值。
	 */
	@Override
	public boolean isExtractOldValueForEditor() {
		return this.extractOldValueForEditor;
	}

	/**
	 * 设置是否在嵌套路径上自动增长（为 null 中间节点创建默认实例）。
	 */
	@Override
	public void setAutoGrowNestedPaths(boolean autoGrowNestedPaths) {
		this.autoGrowNestedPaths = autoGrowNestedPaths;
	}

	/**
	 * 返回是否在嵌套路径上自动增长。
	 */
	@Override
	public boolean isAutoGrowNestedPaths() {
		return this.autoGrowNestedPaths;
	}


	/**
	 * 按 {@link PropertyValue} 写入单个属性（委托到名称/值重载）。
	 */
	@Override
	public void setPropertyValue(PropertyValue pv) throws BeansException {
		setPropertyValue(pv.getName(), pv.getValue());
	}

	/**
	 * 将 Map 中的条目作为属性批量写入。
	 */
	@Override
	public void setPropertyValues(Map<?, ?> map) throws BeansException {
		setPropertyValues(new MutablePropertyValues(map));
	}

	/**
	 * 批量写入属性；未知属性与无效嵌套路径均不忽略。
	 */
	@Override
	public void setPropertyValues(PropertyValues pvs) throws BeansException {
		setPropertyValues(pvs, false, false);
	}

	/**
	 * 批量写入属性；可选择忽略未知（不可写）属性。
	 * @param ignoreUnknown 为 true 时跳过 {@link NotWritablePropertyException}
	 */
	@Override
	public void setPropertyValues(PropertyValues pvs, boolean ignoreUnknown) throws BeansException {
		setPropertyValues(pvs, ignoreUnknown, false);
	}

	/**
	 * 批量写入属性的核心实现。
	 * <p>逐个调用 {@link #setPropertyValue(PropertyValue)}：严重失败（如匹配字段缺失）直接抛出；
	 * 较轻的访问异常可按策略忽略或汇总后一次性抛出。
	 * @param pvs 待写入的属性集合
	 * @param ignoreUnknown 为 true 时忽略不可写/未知属性
	 * @param ignoreInvalid 为 true 时忽略嵌套路径上的 null 值异常
	 */
	@Override
	public void setPropertyValues(PropertyValues pvs, boolean ignoreUnknown, boolean ignoreInvalid)
			throws BeansException {

		// 收集单条属性访问失败，最后统一抛出批量异常
		List<PropertyAccessException> propertyAccessExceptions = null;
		// MutablePropertyValues 可直接取内部列表，避免再拷贝数组
		List<PropertyValue> propertyValues = (pvs instanceof MutablePropertyValues mpvs ?
				mpvs.getPropertyValueList() : Arrays.asList(pvs.getPropertyValues()));

		if (ignoreUnknown) {
			// 打开抑制开关，供子类在抛出 NotWritablePropertyException 前查询
			this.suppressNotWritablePropertyException = true;
		}
		try {
			for (PropertyValue pv : propertyValues) {
				// setPropertyValue 可能抛出任意 BeansException；若属于严重失败（例如没有匹配字段），
				// 此处不会捕获，直接向外抛出。这里只处理相对较轻的异常。
				try {
					setPropertyValue(pv);
				}
				catch (NotWritablePropertyException ex) {
					if (!ignoreUnknown) {
						throw ex;
					}
					// 否则忽略该条，继续后续属性
				}
				catch (NullValueInNestedPathException ex) {
					if (!ignoreInvalid) {
						throw ex;
					}
					// 否则忽略该条，继续后续属性
				}
				catch (PropertyAccessException ex) {
					// 个别属性访问失败：先攒起来，循环结束后再抛复合异常
					if (propertyAccessExceptions == null) {
						propertyAccessExceptions = new ArrayList<>();
					}
					propertyAccessExceptions.add(ex);
				}
			}
		}
		finally {
			if (ignoreUnknown) {
				// 无论成功失败，都复位抑制标志，避免影响后续单次写入
				this.suppressNotWritablePropertyException = false;
			}
		}

		// 若存在单条访问异常，包装为批量更新异常一并抛出
		if (propertyAccessExceptions != null) {
			PropertyAccessException[] paeArray = propertyAccessExceptions.toArray(new PropertyAccessException[0]);
			throw new PropertyBatchUpdateException(paeArray);
		}
	}


	/**
	 * 重新声明为 public；默认实现返回 {@code null}，由子类按需覆盖。
	 */
	@Override
	public @Nullable Class<?> getPropertyType(String propertyPath) {
		return null;
	}

	/**
	 * 真正读取属性值（由子类实现）。
	 * @param propertyName 要读取的属性名（可为嵌套路径）
	 * @return 属性当前值
	 * @throws InvalidPropertyException 属性不存在或不可读
	 * @throws PropertyAccessException 属性合法但访问器方法执行失败
	 */
	@Override
	public abstract @Nullable Object getPropertyValue(String propertyName) throws BeansException;

	/**
	 * 真正写入属性值（由子类实现）。
	 * @param propertyName 要写入的属性名（可为嵌套路径）
	 * @param value 新值
	 * @throws InvalidPropertyException 属性不存在或不可写
	 * @throws PropertyAccessException 属性合法但访问器失败，或发生类型不匹配
	 */
	@Override
	public abstract void setPropertyValue(String propertyName, @Nullable Object value) throws BeansException;

}
