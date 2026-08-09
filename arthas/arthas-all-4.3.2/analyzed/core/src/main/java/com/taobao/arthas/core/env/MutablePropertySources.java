/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.taobao.arthas.core.env;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * {@link PropertySources} 的可变默认实现。
 * <p>
 * 使用 {@link LinkedList} 维护属性源顺序，支持增删改及相对位置插入；
 * {@link #addFirst}/{@link #addLast} 等方法的<em>优先级</em>指
 * {@link PropertyResolver} 解析属性时的搜索顺序（靠前者优先）。
 *
 * @author Chris Beams
 * @since 3.1
 * @see PropertySourcesPropertyResolver
 */
public class MutablePropertySources implements PropertySources {

    /** 相对插入时目标 PropertySource 不存在的错误消息模板 */
    static final String NON_EXISTENT_PROPERTY_SOURCE_MESSAGE = "PropertySource named [%s] does not exist";
    static final String ILLEGAL_RELATIVE_ADDITION_MESSAGE = "PropertySource named [%s] cannot be added relative to itself";

    /** 按搜索优先级排序的属性源链表（addFirst 为最高优先级） */
    private final LinkedList<PropertySource<?>> propertySourceList = new LinkedList<PropertySource<?>>();

    /**
     * Create a new {@link MutablePropertySources} object.
     */
    public MutablePropertySources() {
    }

    /**
     * Create a new {@code MutablePropertySources} from the given propertySources
     * object, preserving the original order of contained {@code PropertySource}
     * objects.
     */
    public MutablePropertySources(PropertySources propertySources) {
        this();
        for (PropertySource<?> propertySource : propertySources) {
            this.addLast(propertySource);
        }
    }

    public boolean contains(String name) {
        return this.propertySourceList.contains(PropertySource.named(name));
    }

    public PropertySource<?> get(String name) {
        int index = this.propertySourceList.indexOf(PropertySource.named(name));
        return index == -1 ? null : this.propertySourceList.get(index);
    }

    public Iterator<PropertySource<?>> iterator() {
        return this.propertySourceList.iterator();
    }

    /** 将属性源插入链表头部，获得最高搜索优先级 */

    public void addFirst(PropertySource<?> propertySource) {
//		if (logger.isDebugEnabled()) {
//			logger.debug(String.format("Adding [%s] PropertySource with highest search precedence",
//					propertySource.getName()));
//		}
        // 同名/同实例先移除再插入，避免重复
        removeIfPresent(propertySource);
        this.propertySourceList.addFirst(propertySource);
    }

    /** 将属性源追加到链表尾部，优先级最低 */

    public void addLast(PropertySource<?> propertySource) {
//		if (logger.isDebugEnabled()) {
//			logger.debug(String.format("Adding [%s] PropertySource with lowest search precedence",
//					propertySource.getName()));
//		}
        removeIfPresent(propertySource);
        this.propertySourceList.addLast(propertySource);
    }

    /** 在指定相对属性源<em>之前</em>插入，优先级略高于该源 */

    public void addBefore(String relativePropertySourceName, PropertySource<?> propertySource) {
//		if (logger.isDebugEnabled()) {
//			logger.debug(String.format("Adding [%s] PropertySource with search precedence immediately higher than [%s]",
//					propertySource.getName(), relativePropertySourceName));
//		}
        assertLegalRelativeAddition(relativePropertySourceName, propertySource);
        removeIfPresent(propertySource);
        int index = assertPresentAndGetIndex(relativePropertySourceName);
        addAtIndex(index, propertySource);
    }

    /** 在指定相对属性源<em>之后</em>插入，优先级略低于该源 */

    public void addAfter(String relativePropertySourceName, PropertySource<?> propertySource) {
//		if (logger.isDebugEnabled()) {
//			logger.debug(String.format("Adding [%s] PropertySource with search precedence immediately lower than [%s]",
//					propertySource.getName(), relativePropertySourceName));
//		}
        assertLegalRelativeAddition(relativePropertySourceName, propertySource);
        removeIfPresent(propertySource);
        int index = assertPresentAndGetIndex(relativePropertySourceName);
        addAtIndex(index + 1, propertySource);
    }

    /** 返回属性源在链表中的索引作为优先级，未找到返回 {@code -1} */

    public int precedenceOf(PropertySource<?> propertySource) {
        return this.propertySourceList.indexOf(propertySource);
    }

    /**
     * 按名称移除并返回属性源，未找到返回 {@code null}。
     * 
     * @param name 待移除的属性源名称
     */
    public PropertySource<?> remove(String name) {
//		if (logger.isDebugEnabled()) {
//			logger.debug(String.format("Removing [%s] PropertySource", name));
//		}
        int index = this.propertySourceList.indexOf(PropertySource.named(name));
        return index == -1 ? null : this.propertySourceList.remove(index);
    }

    /**
     * 用新属性源替换同名条目。
     * 
     * @param name 待替换的属性源名称
     * @param propertySource 新的属性源实例
     * @throws IllegalArgumentException 若指定名称不存在
     * @see #contains
     */
    public void replace(String name, PropertySource<?> propertySource) {
//		if (logger.isDebugEnabled()) {
//			logger.debug(String.format("Replacing [%s] PropertySource with [%s]",
//					name, propertySource.getName()));
//		}
        int index = assertPresentAndGetIndex(name);
        this.propertySourceList.set(index, propertySource);
    }

    /** 返回当前管理的 {@link PropertySource} 数量 */

    public int size() {
        return this.propertySourceList.size();
    }

    @Override
    public String toString() {
        String[] names = new String[this.size()];
        for (int i = 0; i < size(); i++) {
            names[i] = this.propertySourceList.get(i).getName();
        }
        return String.format("[%s]", arrayToCommaDelimitedString(names));
    }

    /**
     * Ensure that the given property source is not being added relative to itself.
     */
    protected void assertLegalRelativeAddition(String relativePropertySourceName, PropertySource<?> propertySource) {
//		String newPropertySourceName = propertySource.getName();
//		Assert.isTrue(!relativePropertySourceName.equals(newPropertySourceName),
//				String.format(ILLEGAL_RELATIVE_ADDITION_MESSAGE, newPropertySourceName));
    }

    /**
     * Remove the given property source if it is present.
     */
    /** 若链表中已存在同实例则先移除，避免重复注册 */
    protected void removeIfPresent(PropertySource<?> propertySource) {
		this.propertySourceList.remove(propertySource);
    }

    /**
     * Add the given property source at a particular index in the list.
     */
    /** 在指定索引处插入属性源（addBefore/addAfter 内部使用） */
    private void addAtIndex(int index, PropertySource<?> propertySource) {
        removeIfPresent(propertySource);
        this.propertySourceList.add(index, propertySource);
    }

    /**
     * Assert that the named property source is present and return its index.
     * 
     * @param name the {@linkplain PropertySource#getName() name of the property
     *             source} to find
     * @throws IllegalArgumentException if the named property source is not present
     */
    private int assertPresentAndGetIndex(String name) {
        int index = this.propertySourceList.indexOf(PropertySource.named(name));
//		Assert.isTrue(index >= 0, String.format(NON_EXISTENT_PROPERTY_SOURCE_MESSAGE, name));
        return index;
    }

    /**
     * Convenience method to return a String array as a delimited (e.g. CSV) String.
     * E.g. useful for {@code toString()} implementations.
     * 
     * @param arr   the array to display
     * @param delim the delimiter to use (probably a ",")
     * @return the delimited String
     */
    private static String arrayToDelimitedString(Object[] arr, String delim) {
        if (arr == null || arr.length == 0) {
            return "";
        }
        if (arr.length == 1) {
            return nullSafeToString(arr[0]);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) {
                sb.append(delim);
            }
            sb.append(arr[i]);
        }
        return sb.toString();
    }

    /**
     * Return a String representation of the specified Object.
     * <p>
     * Builds a String representation of the contents in case of an array. Returns
     * {@code "null"} if {@code obj} is {@code null}.
     * 
     * @param obj the object to build a String representation for
     * @return a String representation of {@code obj}
     */
    private static String nullSafeToString(Object obj) {
        if (obj == null) {
            return "null";
        }
        if (obj instanceof String) {
            return (String) obj;
        }
        if (obj instanceof Object[]) {
            return nullSafeToString((Object[]) obj);
        }
        if (obj instanceof boolean[]) {
            return nullSafeToString((boolean[]) obj);
        }
        if (obj instanceof byte[]) {
            return nullSafeToString((byte[]) obj);
        }
        if (obj instanceof char[]) {
            return nullSafeToString((char[]) obj);
        }
        if (obj instanceof double[]) {
            return nullSafeToString((double[]) obj);
        }
        if (obj instanceof float[]) {
            return nullSafeToString((float[]) obj);
        }
        if (obj instanceof int[]) {
            return nullSafeToString((int[]) obj);
        }
        if (obj instanceof long[]) {
            return nullSafeToString((long[]) obj);
        }
        if (obj instanceof short[]) {
            return nullSafeToString((short[]) obj);
        }
        String str = obj.toString();
        return (str != null ? str : "");
    }

    /**
     * Convenience method to return a String array as a CSV String. E.g. useful for
     * {@code toString()} implementations.
     * 
     * @param arr the array to display
     * @return the delimited String
     */
    private static String arrayToCommaDelimitedString(Object[] arr) {
        return arrayToDelimitedString(arr, ",");
    }
}
