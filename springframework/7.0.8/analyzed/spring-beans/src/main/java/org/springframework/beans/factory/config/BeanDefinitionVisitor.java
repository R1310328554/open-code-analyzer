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

package org.springframework.beans.factory.config;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringValueResolver;

/**
 * 遍历 {@link BeanDefinition} 对象的访问者类，特别是其中包含的
 * 属性值和构造器参数值，并解析 bean 元数据值。
 *
 * <p>由 {@link PlaceholderConfigurerSupport} 使用，以解析 BeanDefinition 中
 * 包含的所有字符串值，处理发现的任何占位符。
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 1.2
 * @see BeanDefinition
 * @see BeanDefinition#getPropertyValues
 * @see BeanDefinition#getConstructorArgumentValues
 * @see PlaceholderConfigurerSupport
 */
public class BeanDefinitionVisitor {

	/** 用于解析字符串值的解析器。 */
	private @Nullable StringValueResolver valueResolver;


	/**
	 * 创建新的 BeanDefinitionVisitor，将指定的值解析器应用于所有 bean 元数据值。
	 * @param valueResolver 要应用的 StringValueResolver
	 */
	public BeanDefinitionVisitor(StringValueResolver valueResolver) {
		Assert.notNull(valueResolver, "StringValueResolver must not be null");
		this.valueResolver = valueResolver;
	}

	/**
	 * 创建用于子类化的 BeanDefinitionVisitor。
	 * 子类需要覆盖 {@link #resolveStringValue} 方法。
	 */
	protected BeanDefinitionVisitor() {
	}


	/**
	 * 遍历给定的 BeanDefinition 对象及其包含的 MutablePropertyValues
	 * 和 ConstructorArgumentValues。
	 * @param beanDefinition 要遍历的 BeanDefinition 对象
	 * @see #resolveStringValue(String)
	 */
	public void visitBeanDefinition(BeanDefinition beanDefinition) {
		visitParentName(beanDefinition);
		visitBeanClassName(beanDefinition);
		visitFactoryBeanName(beanDefinition);
		visitFactoryMethodName(beanDefinition);
		visitScope(beanDefinition);
		if (beanDefinition.hasPropertyValues()) {
			visitPropertyValues(beanDefinition.getPropertyValues());
		}
		if (beanDefinition.hasConstructorArgumentValues()) {
			ConstructorArgumentValues cas = beanDefinition.getConstructorArgumentValues();
			visitIndexedArgumentValues(cas.getIndexedArgumentValues());
			visitGenericArgumentValues(cas.getGenericArgumentValues());
		}
	}

	/** 访问并解析父 bean 定义名称。 */
	protected void visitParentName(BeanDefinition beanDefinition) {
		String parentName = beanDefinition.getParentName();
		if (parentName != null) {
			String resolvedName = resolveStringValue(parentName);
			if (!parentName.equals(resolvedName)) {
				beanDefinition.setParentName(resolvedName);
			}
		}
	}

	/** 访问并解析 bean 类名。 */
	protected void visitBeanClassName(BeanDefinition beanDefinition) {
		String beanClassName = beanDefinition.getBeanClassName();
		if (beanClassName != null) {
			String resolvedName = resolveStringValue(beanClassName);
			if (!beanClassName.equals(resolvedName)) {
				beanDefinition.setBeanClassName(resolvedName);
			}
		}
	}

	/** 访问并解析工厂 bean 名称。 */
	protected void visitFactoryBeanName(BeanDefinition beanDefinition) {
		String factoryBeanName = beanDefinition.getFactoryBeanName();
		if (factoryBeanName != null) {
			String resolvedName = resolveStringValue(factoryBeanName);
			if (!factoryBeanName.equals(resolvedName)) {
				beanDefinition.setFactoryBeanName(resolvedName);
			}
		}
	}

	/** 访问并解析工厂方法名称。 */
	protected void visitFactoryMethodName(BeanDefinition beanDefinition) {
		String factoryMethodName = beanDefinition.getFactoryMethodName();
		if (factoryMethodName != null) {
			String resolvedName = resolveStringValue(factoryMethodName);
			if (!factoryMethodName.equals(resolvedName)) {
				beanDefinition.setFactoryMethodName(resolvedName);
			}
		}
	}

	/** 访问并解析作用域名称。 */
	protected void visitScope(BeanDefinition beanDefinition) {
		String scope = beanDefinition.getScope();
		if (scope != null) {
			String resolvedScope = resolveStringValue(scope);
			if (!scope.equals(resolvedScope)) {
				beanDefinition.setScope(resolvedScope);
			}
		}
	}

	/** 遍历并解析所有属性值。 */
	protected void visitPropertyValues(MutablePropertyValues pvs) {
		PropertyValue[] pvArray = pvs.getPropertyValues();
		for (PropertyValue pv : pvArray) {
			Object newVal = resolveValue(pv.getValue());
			if (!ObjectUtils.nullSafeEquals(newVal, pv.getValue())) {
				pvs.add(pv.getName(), newVal);
			}
		}
	}

	/** 遍历并解析带索引的构造器参数值。 */
	protected void visitIndexedArgumentValues(Map<Integer, ConstructorArgumentValues.ValueHolder> ias) {
		for (ConstructorArgumentValues.ValueHolder valueHolder : ias.values()) {
			Object newVal = resolveValue(valueHolder.getValue());
			if (!ObjectUtils.nullSafeEquals(newVal, valueHolder.getValue())) {
				valueHolder.setValue(newVal);
			}
		}
	}

	/** 遍历并解析通用构造器参数值。 */
	protected void visitGenericArgumentValues(List<ConstructorArgumentValues.ValueHolder> gas) {
		for (ConstructorArgumentValues.ValueHolder valueHolder : gas) {
			Object newVal = resolveValue(valueHolder.getValue());
			if (!ObjectUtils.nullSafeEquals(newVal, valueHolder.getValue())) {
				valueHolder.setValue(newVal);
			}
		}
	}

	/**
	 * 递归解析给定值：处理嵌套 BeanDefinition、引用、集合、字符串等。
	 */
	@SuppressWarnings("rawtypes")
	protected @Nullable Object resolveValue(@Nullable Object value) {
		if (value instanceof BeanDefinition beanDef) {
			visitBeanDefinition(beanDef);
		}
		else if (value instanceof BeanDefinitionHolder beanDefHolder) {
			visitBeanDefinition(beanDefHolder.getBeanDefinition());
		}
		else if (value instanceof RuntimeBeanReference ref) {
			String newBeanName = resolveStringValue(ref.getBeanName());
			if (newBeanName == null) {
				return null;
			}
			if (!newBeanName.equals(ref.getBeanName())) {
				return new RuntimeBeanReference(newBeanName);
			}
		}
		else if (value instanceof RuntimeBeanNameReference ref) {
			String newBeanName = resolveStringValue(ref.getBeanName());
			if (newBeanName == null) {
				return null;
			}
			if (!newBeanName.equals(ref.getBeanName())) {
				return new RuntimeBeanNameReference(newBeanName);
			}
		}
		else if (value instanceof Object[] array) {
			visitArray(array);
		}
		else if (value instanceof List list) {
			visitList(list);
		}
		else if (value instanceof Set set) {
			visitSet(set);
		}
		else if (value instanceof Map map) {
			visitMap(map);
		}
		else if (value instanceof TypedStringValue typedStringValue) {
			String stringValue = typedStringValue.getValue();
			if (stringValue != null) {
				String visitedString = resolveStringValue(stringValue);
				typedStringValue.setValue(visitedString);
			}
		}
		else if (value instanceof String strValue) {
			return resolveStringValue(strValue);
		}
		return value;
	}

	/** 遍历并解析数组中的每个元素。 */
	protected void visitArray(@Nullable Object[] arrayVal) {
		for (int i = 0; i < arrayVal.length; i++) {
			Object elem = arrayVal[i];
			Object newVal = resolveValue(elem);
			if (!ObjectUtils.nullSafeEquals(newVal, elem)) {
				arrayVal[i] = newVal;
			}
		}
	}

	/** 遍历并解析列表中的每个元素。 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	protected void visitList(List listVal) {
		for (int i = 0; i < listVal.size(); i++) {
			Object elem = listVal.get(i);
			Object newVal = resolveValue(elem);
			if (!ObjectUtils.nullSafeEquals(newVal, elem)) {
				listVal.set(i, newVal);
			}
		}
	}

	/** 遍历并解析集合中的每个元素（必要时重建集合内容）。 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	protected void visitSet(Set setVal) {
		Set newContent = new LinkedHashSet();
		boolean entriesModified = false;
		for (Object elem : setVal) {
			int elemHash = (elem != null ? elem.hashCode() : 0);
			Object newVal = resolveValue(elem);
			int newValHash = (newVal != null ? newVal.hashCode() : 0);
			newContent.add(newVal);
			entriesModified = entriesModified || (newVal != elem || newValHash != elemHash);
		}
		if (entriesModified) {
			setVal.clear();
			setVal.addAll(newContent);
		}
	}

	/** 遍历并解析映射中的键和值（必要时重建映射内容）。 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	protected void visitMap(Map<?, ?> mapVal) {
		Map newContent = new LinkedHashMap();
		boolean entriesModified = false;
		for (Map.Entry entry : mapVal.entrySet()) {
			Object key = entry.getKey();
			int keyHash = (key != null ? key.hashCode() : 0);
			Object newKey = resolveValue(key);
			int newKeyHash = (newKey != null ? newKey.hashCode() : 0);
			Object val = entry.getValue();
			Object newVal = resolveValue(val);
			newContent.put(newKey, newVal);
			entriesModified = entriesModified || (newVal != val || newKey != key || newKeyHash != keyHash);
		}
		if (entriesModified) {
			mapVal.clear();
			mapVal.putAll(newContent);
		}
	}

	/**
	 * 解析给定字符串值，例如解析占位符。
	 * @param strVal 原始字符串值
	 * @return 解析后的字符串值
	 */
	protected @Nullable String resolveStringValue(String strVal) {
		if (this.valueResolver == null) {
			throw new IllegalStateException("No StringValueResolver specified - pass a resolver " +
					"object into the constructor or override the 'resolveStringValue' method");
		}
		String resolvedValue = this.valueResolver.resolveStringValue(strVal);
		// 若未修改则返回原始字符串
		return (strVal.equals(resolvedValue) ? strVal : resolvedValue);
	}

}
