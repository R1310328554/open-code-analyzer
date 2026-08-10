/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.client.registration.cli;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.keycloak.client.cli.common.AttributeKey;
import org.keycloak.client.cli.common.AttributeOperation;
import org.keycloak.client.cli.util.AttributeException;
import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.core.JsonParseException;

/**
 * 注册 CLI 客户端表示的属性反射工具。
 * <p>
 * 通过字段（非 getter/setter）索引支持 {@code --set} 点分路径赋值、
 * 列表/Map 嵌套操作及 {@code attrs} 命令的类型描述输出。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class ReflectionUtil {

    /** 类型到字段名索引的缓存。 */
    static Map<Class, Map<String, Field>> index = new HashMap<>();

    /** 扫描类型声明字段并填充 {@link #index}。 */
    static void populateAttributesIndex(Class type) {
        // 使用字段而非 getter/setter，因 JSON 映射有时也直接写字段
        // 未来可能因 Field.setAccessible() 依赖而需调整
        Map<String, Field> map = new HashMap<>();
        Field [] fields  = type.getDeclaredFields();
        for (Field f: fields) {
            // 确保非 public 字段也可访问
            f.setAccessible(true);
            map.put(f.getName(), f);
        }
        index.put(type, map);
    }

    /**
     * 返回类型的可写属性字段映射（列表/Map 类型返回空映射）。
     *
     * @param gtype 类或参数化类型
     * @return 字段名到 {@link Field} 的映射
     */
    public static Map<String, Field> getAttrFieldsForType(Type gtype) {
        Class type;
        if (gtype instanceof Class) {
            type = (Class) gtype;
        } else if (gtype instanceof ParameterizedType) {
            type = (Class) ((ParameterizedType) gtype).getRawType();
        } else {
            throw new RuntimeException("Unexpected type: " + gtype);
        }

        if (isListType(type) || isMapType(type)) {
            return Collections.emptyMap();
        }
        Map<String, Field> map = index.get(type);
        if (map == null) {
            populateAttributesIndex(type);
            map = index.get(type);
        }
        return map;
    }

    /** 判断类型是否为 List 或数组。 */
    public static boolean isListType(Class type) {
        return List.class.isAssignableFrom(type) || type.isArray();
    }

    /** 判断是否为 CLI 支持的基本类型（String、数值、布尔）。 */
    public static boolean isBasicType(Type type) {
        return type == String.class || type == Boolean.class || type == boolean.class
                || type == Integer.class || type == int.class || type == Long.class || type == long.class
                || type == Float.class || type == float.class || type == Double.class || type == double.class;
    }

    /** 判断类型是否实现 {@link Map}。 */
    public static boolean isMapType(Class type) {
        return Map.class.isAssignableFrom(type);
    }

    /**
     * 将字符串或数值转换为目标字段类型。
     *
     * @param value 源值
     * @param type 目标类型
     * @return 转换后的值
     * @throws IOException JSON 反序列化失败时
     */
    public static Object convertValueToType(Object value, Class<?> type) throws IOException {

        if (value == null) {
            return null;

        } else if (value instanceof String) {
            if (type == String.class) {
                return value;
            } else if (type == Boolean.class) {
                return Boolean.valueOf((String) value);
            } else if (type == Integer.class) {
                return Integer.valueOf((String) value);
            } else if (type == Long.class) {
                return Long.valueOf((String) value);
            } else {
                return JsonSerialization.readValue((String) value, type);
            }
        } else if (value instanceof Number) {
            if (type == Integer.class) {
                return ((Number) value).intValue();
            } else if (type == Long.class) {
                return ((Long) value).longValue();
            } else if (type == String.class) {
                return String.valueOf(value);
            }
        } else if (value instanceof Boolean) {
            if (type == Boolean.class) {
                return value;
            } else if (type == String.class) {
                return String.valueOf(value);
            }
        }

        throw new RuntimeException("Unable to handle type [" + type + "]");
    }

    /**
     * 按 {@link AttributeOperation} 列表在客户端对象上设置/追加/删除属性。
     *
     * @param client 目标客户端表示对象
     * @param attrs 属性操作序列
     * @throws AttributeException 未知属性或类型不匹配时
     */
    public static void setAttributes(Object client, List<AttributeOperation> attrs) {

        for (AttributeOperation item: attrs) {

            AttributeKey attr = item.getKey();
            Object nested = client;

            List<AttributeKey.Component> cs = attr.getComponents();
            for (int i = 0; i < cs.size(); i++) {
                AttributeKey.Component c = cs.get(i);

                Class type = nested.getClass();
                Field field = null;

                if (!isMapType(type)) {
                    Map<String, Field> fields = getAttrFieldsForType(type);
                    if (fields == null) {
                        throw new AttributeException(attr.toString(), "Unexpected condition - unknown type: " + type);
                    }

                    field = fields.get(c.getName());
                    Class parent = type;
                    while (field == null) {
                        parent = parent.getSuperclass();
                        if (parent == Object.class) {
                            throw new AttributeException(attr.toString(), "Unknown attribute '" + c.getName() + "' on " + client.getClass());
                        }

                        fields = getAttrFieldsForType(parent);
                        field = fields.get(c.getName());
                    }
                }
                // 基本类型：直接 field.set
                type = field == null ? type : field.getType();
                if (isBasicType(type)) {
                    if (i < cs.size() - 1) {
                        throw new AttributeException(attr.toString(), "Attribute is of primitive type, and can't be nested further: " + c);
                    }

                    try {
                        Object val = convertValueToType(item.getValue(), type);
                        field.set(nested, val);
                    } catch (Exception e) {
                        throw new AttributeException(attr.toString(), "Failed to set attribute " + attr, e);
                    }
                } else if (isListType(type)) {
                    if (i < cs.size() -1) {
                        // not the target component
                        try {
                            nested = field.get(nested);
                        } catch (Exception e) {
                            throw new AttributeException(attr.toString(), "Failed to get attribute \"" + c + "\" in " + attr, e);
                        }
                        if (c.getIndex() >= 0) {
                            // list item
                            // get idx-th item
                            List l = (List) nested;
                            if (c.getIndex() >= l.size()) {
                                throw new AttributeException(attr.toString(), "Array index out of bounds for \"" + c + "\" in " + attr);
                            }
                            nested = l.get(c.getIndex());
                        }
                    } else {
                        // target component
                        Class itype = type;
                        Type gtype = field.getGenericType();
                        if (gtype instanceof ParameterizedType) {
                            Type[] typeArgs = ((ParameterizedType) gtype).getActualTypeArguments();
                            if (typeArgs.length >= 1 && typeArgs[0] instanceof Class) {
                                itype = (Class) typeArgs[0];
                            } else {
                                itype = String.class;
                            }
                        }
                        if (c.getIndex() >= 0 || attr.isAppend()) {
                            // some list item
                            // get the list first
                            List target;
                            try {
                                target = (List) field.get(nested);
                            } catch (Exception e) {
                                throw new AttributeException(attr.toString(), "Failed to get list attribute: " + attr, e);
                            }

                            // now replace or add idx-th item
                            if (target == null) {
                                target = createNewList(type);
                                try {
                                    field.set(nested, target);
                                } catch (Exception e) {
                                    throw new AttributeException(attr.toString(), "Failed to set list attribute " + attr, e);
                                }
                            }
                            if (c.getIndex() >= target.size()) {
                                throw new AttributeException(attr.toString(), "Array index out of bounds for \"" + c + "\" in " + attr);
                            }

                            if (attr.isAppend()) {
                                try {
                                    Object value = convertValueToType(item.getValue(), itype);
                                    if (c.getIndex() >= 0) {
                                        target.add(c.getIndex(), value);
                                    } else {
                                        target.add(value);
                                    }
                                } catch (Exception e) {
                                    throw new AttributeException(attr.toString(), "Failed to set list attribute " + attr, e);
                                }

                            } else {
                                if (item.getType() == AttributeOperation.Type.SET) {
                                    try {
                                        Object value = convertValueToType(item.getValue(), itype);
                                        target.set(c.getIndex(), value);
                                    } catch (Exception e) {
                                        throw new AttributeException(attr.toString(), "Failed to set list attribute " + attr, e);
                                    }
                                } else {
                                    try {
                                        target.remove(c.getIndex());
                                    } catch (Exception e) {
                                        throw new AttributeException(attr.toString(), "Failed to remove list attribute " + attr, e);
                                    }
                                }
                            }

                        } else {
                            // set the whole list field itself
                            List value = createNewList(type);;
                            if (item.getType() == AttributeOperation.Type.SET) {
                                List converted = convertValueToList(item.getValue(), itype);
                                value.addAll(converted);
                            }
                            try {
                                field.set(nested, value);
                            } catch (Exception e) {
                                throw new AttributeException(attr.toString(), "Failed to set list attribute " + attr, e);
                            }
                        }
                    }
                } else {
                    // object type
                    if (i < cs.size() -1) {
                        // not the target component
                        Object value;
                        if (field == null) {
                            if (isMapType(nested.getClass())) {
                                value = ((Map) nested).get(c.getName());
                            } else {
                                throw new RuntimeException("Unexpected condition while processing: " + attr);
                            }
                        } else {
                            try {
                                value = field.get(nested);
                            } catch (Exception e) {
                                throw new AttributeException(attr.toString(), "Failed to get attribute \"" + c + "\" in " + attr, e);
                            }
                        }
                        if (value == null) {
                            // create the target attribute
                            if (isMapType(nested.getClass())) {
                                throw new RuntimeException("Creating nested object trees not supported");
                            } else {
                                try {
                                    value = createNewObject(type);
                                    field.set(nested, value);
                                } catch (Exception e) {
                                    throw new AttributeException(attr.toString(), "Failed to set attribute " + attr, e);
                                }
                            }
                        }
                        nested = value;
                    } else {
                        // target component
                        // todo implement map put
                        if (isMapType(nested.getClass())) {
                            try {
                                ((Map) nested).put(c.getName(), item.getValue());
                            } catch (Exception e) {
                                throw new AttributeException(attr.toString(), "Failed to set map key " + attr, e);
                            }
                        } else {
                            try {
                                Object value = convertValueToType(item.getValue(), type);
                                field.set(nested, value);
                            } catch (Exception e) {
                                throw new AttributeException(attr.toString(), "Failed to set attribute " + attr, e);
                            }
                        }
                    }
                }
            }
        }
    }

    /** 通过无参构造实例化对象类型。 */
    private static Object createNewObject(Class type) throws Exception {
        return type.newInstance();
    }

    /** 创建指定 List 实现类型的空列表实例。 */
    public static List createNewList(Class type) {

        if (type == List.class) {
            return new ArrayList();
        } else if (type.isInterface()) {
            throw new RuntimeException("Can't instantiate a list type: " + type);
        }

        try {
            return (List) type.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate a list type: " + type, e);
        }
    }

    /**
     * 将 JSON 数组字符串解析为指定元素类型的 List。
     *
     * @param value 以 {@code [} 开头的 JSON 数组字面量
     * @param itemType 列表元素类型
     * @return 转换后的列表
     */
    public static List convertValueToList(String value, Class itemType) {
        try {
            List result = new LinkedList();
            if (!value.startsWith("[")) {
                throw new RuntimeException("List attribute value has to start with '[' - '" + value + "'");
            }
            List parsed = JsonSerialization.readValue(value, List.class);
            for (Object item: parsed) {
                if (itemType.isAssignableFrom(item.getClass())) {
                    result.add(item);
                } else {
                    result.add(convertValueToType(item, itemType));
                }
            }
            return result;

        } catch (JsonParseException e) {
            throw new RuntimeException("Failed to parse list value: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse list value: " + value, e);
        }
    }

    /**
     * 将 {@code source} 中非 null 字段值复制到 {@code dest}（同类型）。
     *
     * @param source 源对象
     * @param dest 目标对象
     * @param <T> 对象类型
     */
    public static <T> void merge(T source, T dest) {
        // 复用字段索引，逐字段 getter/setter 式复制
        Map<String, Field> fieldMap = getAttrFieldsForType(source.getClass());
        try {
            for (Field field : fieldMap.values()) {
                Object localValue = field.get(source);
                if (localValue != null) {
                    field.set(dest, localValue);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to merge changes", e);
        }
    }


    /**
     * 列出指定嵌套路径下子属性的 JSON 类型描述（供 {@code attrs} 命令使用）。
     *
     * @param type 根类型
     * @param attr 属性路径键，{@code null} 表示根级
     * @return 属性名到类型字符串的有序映射
     */
    public static LinkedHashMap<String, String> getAttributeListWithJSonTypes(Class type, AttributeKey attr) {

        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        attr = attr != null ? attr : new AttributeKey();

        Map<String, Field> fields = getAttrFieldsForType(type);
        for (AttributeKey.Component c: attr.getComponents()) {
            Field f = fields.get(c.getName());
            if (f == null) {
                throw new AttributeException(attr.toString(), "No such attribute: " + attr);
            }

            type = f.getType();
            if (isBasicType(type) || isListType(type) || isMapType(type)) {
                return result;
            } else {
                fields = getAttrFieldsForType(type);
            }
        }

        for (Map.Entry<String, Field> item : fields.entrySet()) {
            String key = item.getKey();
            Class clazz = item.getValue().getType();
            String t = getTypeString(clazz, item.getValue());

            result.put(key, t);
        }
        return result;
    }

    /**
     * 按点分路径解析目标 {@link Field}。
     *
     * @param type 根类型
     * @param attr 属性路径
     * @return 路径末端字段
     * @throws AttributeException 路径不存在时
     */
    public static Field resolveField(Class type, AttributeKey attr) {
        Field f = null;
        Type gtype = type;

        for (AttributeKey.Component c: attr.getComponents()) {
            if (f != null) {
                gtype = f.getGenericType();
                if (gtype instanceof ParameterizedType) {
                    Type[] typeargs = ((ParameterizedType) gtype).getActualTypeArguments();
                    if (typeargs.length > 0) {
                        gtype = typeargs[typeargs.length-1];
                    }
                }
            }
            Map<String, Field> fields = getAttrFieldsForType(gtype);
            f = fields.get(c.getName());
            if (f == null) {
                throw new AttributeException(attr.toString(), "No such attribute: " + attr);
            }
        }
        return f;
    }

    /**
     * 将 Java 类型转换为 {@code attrs} 命令显示的类型字符串（如 {@code string}、{@code array (object)}）。
     *
     * @param type 类型，可为 {@code null} 时从 {@code field} 推断
     * @param field 可选字段（提供泛型信息）
     * @return 人类可读的类型描述
     */
    public static String getTypeString(Type type, Field field) {
        if (type == null) {
            if (field == null) {
                throw new IllegalArgumentException("type == null and field == null");
            }
            type = field.getGenericType();
        }
        if (type instanceof ParameterizedType) {
            StringBuilder sb = new StringBuilder();
            String rtype = getTypeString(((ParameterizedType) type).getRawType(), null);

            sb.append(rtype);
            sb.append(" ").append("(");
            Type[] typeArgs = ((ParameterizedType) type).getActualTypeArguments();

            for (int i = 0; i < typeArgs.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(getTypeString(typeArgs[i], null));
            }
            sb.append(")");
            return sb.toString();
        }

        if (!(type instanceof Class)) {
            throw new IllegalArgumentException("unsupported type " + type.getClass().getName());
        }

        Class clazz = (Class) type;

        if (CharSequence.class.isAssignableFrom(clazz)) {
            return "string";
        } else if (Integer.class.isAssignableFrom(clazz) || int.class.isAssignableFrom(clazz)) {
            return "int";
        } else if (Long.class.isAssignableFrom(clazz) || long.class.isAssignableFrom(clazz)) {
            return "long";
        } else if (Float.class.isAssignableFrom(clazz) || float.class.isAssignableFrom(clazz)) {
            return "float";
        } else if (Double.class.isAssignableFrom(clazz) || double.class.isAssignableFrom(clazz)) {
            return "double";
        } else if (Number.class.isAssignableFrom(clazz)) {
            return "number";
        } else if (Boolean.class.isAssignableFrom(clazz) || boolean.class.isAssignableFrom(clazz)) {
            return "boolean";
        } else if (isListType(clazz)) {
            if (field != null) {
                Type gtype = field.getGenericType();
                if (gtype == clazz && clazz.isArray()) {
                    return "array (" + getTypeString(clazz.getComponentType(), null) + ")";
                }
                return getTypeString(gtype, null);
            }
            return "array";
        } else if (isMapType(clazz)) {
            if (field != null) {
                Type gtype = field.getGenericType();
                return getTypeString(gtype, null);
            }
            return "object";
        } else {
            return "object";
        }
    }
}
