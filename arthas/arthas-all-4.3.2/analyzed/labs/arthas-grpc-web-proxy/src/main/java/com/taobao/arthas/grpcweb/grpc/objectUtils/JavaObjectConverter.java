package com.taobao.arthas.grpcweb.grpc.objectUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.arthas.api.ArthasServices.ArrayElement;
import io.arthas.api.ArthasServices.ArrayValue;
import io.arthas.api.ArthasServices.BasicValue;
import io.arthas.api.ArthasServices.CollectionValue;
import io.arthas.api.ArthasServices.CollectionValue.Builder;
import io.arthas.api.ArthasServices.JavaField;
import io.arthas.api.ArthasServices.JavaFields;
import io.arthas.api.ArthasServices.JavaObject;
import io.arthas.api.ArthasServices.MapEntry;
import io.arthas.api.ArthasServices.MapValue;
import io.arthas.api.ArthasServices.NullValue;
import io.arthas.api.ArthasServices.UnexpandedObject;

/**
 * 将 JVM 堆内 Java 对象反射序列化为 gRPC {@link JavaObject}  protobuf 结构。
 * <p>
 * 支持基本类型、数组、集合、Map 与普通 POJO 字段；通过深度限制避免循环引用导致栈溢出。
 */
public class JavaObjectConverter {
    /** 最大递归深度，超过后返回 null 或 {@link UnexpandedObject} */
    private static final int MAX_DEPTH = 5;

    /**
     * 以默认最大深度将对象转为 {@link JavaObject}。
     *
     * @param obj 待序列化的堆对象，可为 null
     * @return protobuf 形式的 Java 对象描述
     */
    public static JavaObject toJavaObject(Object obj) {
        return toJavaObject(obj, 0);
    }

    /**
     * 按 Arthas expand 参数换算实际递归深度后序列化对象。
     * <p>
     * expand 越大展开越深；0 或负数时使用接近 {@link #MAX_DEPTH} 的深度。
     *
     * @param obj    待序列化对象
     * @param expand 展开层级（与 CLI expand 语义一致）
     * @return protobuf JavaObject
     */
    public static JavaObject toJavaObjectWithExpand(Object obj, int expand){
        int depth;
        if(expand <= 0){
            depth = MAX_DEPTH - 1;
        }else if(expand >= MAX_DEPTH){
            depth = 0;
        }else {
            depth = MAX_DEPTH - expand;
        }
        return toJavaObject(obj, depth);
    }

    /**
     * 以指定起始深度递归序列化对象。
     *
     * @param obj   待序列化对象
     * @param depth 当前递归深度，达到 {@link #MAX_DEPTH} 时停止展开
     * @return JavaObject；深度超限时可能为 null
     */
    public static JavaObject toJavaObject(Object obj, int depth) {
        if (depth >= MAX_DEPTH) {
            return null;
        }

        if (obj == null) {
            return JavaObject.newBuilder().setNullValue(NullValue.getDefaultInstance()).build();
        }

        JavaObject.Builder objectBuilder = JavaObject.newBuilder();
        Class<? extends Object> objClazz = obj.getClass();
        objectBuilder.setClassName(objClazz.getName());

        // 包装类与 String 等基本类型
        if (isBasicType(objClazz)) {
            return objectBuilder.setBasicValue(createBasicValue(obj)).build();
        } else if (obj instanceof Collection) {
            return objectBuilder.setCollection(createCollectionValue((Collection<?>) obj, depth)).build();
        } else if (obj instanceof Map) {
            return objectBuilder.setMap(createMapValue((Map<?, ?>) obj, depth)).build();
        } else if (objClazz.isArray()) {
            return objectBuilder.setArrayValue(toArrayValue(obj, depth)).build();
        }

        // 普通对象：遍历声明字段
        Field[] fields = objClazz.getDeclaredFields();
        List<JavaField> javaFields = new ArrayList<>();

        for (Field field : fields) {
            field.setAccessible(true);
            JavaField.Builder fieldBuilder = JavaField.newBuilder();
            fieldBuilder.setName(field.getName());

            try {
                Object fieldValue = field.get(obj);
                Class<?> fieldType = field.getType();

                if (fieldValue == null) {
                    fieldBuilder.setNullValue(NullValue.newBuilder().setClassName(fieldType.getName()).build());
                } else if (fieldType.isArray()) {
                    ArrayValue arrayValue = toArrayValue(fieldValue, depth + 1);
                    if (arrayValue != null) {
                        fieldBuilder.setArrayValue(arrayValue);
                    } else {
                        fieldBuilder.setUnexpandedObject(
                                UnexpandedObject.newBuilder().setClassName(fieldType.getName()).build());
                    }
                } else if (fieldType.isPrimitive() || isBasicType(fieldType)) {
                    BasicValue basicValue = createBasicValue(fieldValue);
                    fieldBuilder.setBasicValue(basicValue);
                } else if (fieldValue instanceof Collection) {
                    fieldBuilder.setCollection(createCollectionValue((Collection<?>) fieldValue, depth));
                } else if (fieldValue instanceof Map) {
                    fieldBuilder.setMap(createMapValue((Map<?, ?>) fieldValue, depth));
                } else {
                    JavaObject nestedObject = toJavaObject(fieldValue, depth + 1);
                    if (nestedObject != null) {
                        fieldBuilder.setObjectValue(nestedObject);
                    } else {
                        fieldBuilder.setUnexpandedObject(
                                UnexpandedObject.newBuilder().setClassName(fieldType.getName()).build());
                    }
                }
            } catch (IllegalAccessException e) {
                // 无法访问的字段跳过
            }
            javaFields.add(fieldBuilder.build());
        }

        objectBuilder.setFields(JavaFields.newBuilder().addAllFields(javaFields).build());
        return objectBuilder.build();
    }

    /**
     * 将 Java 数组转为 {@link ArrayValue}，元素按类型递归或标记为未展开。
     */
    private static ArrayValue toArrayValue(Object array, int depth) {
        if (array == null || depth >= MAX_DEPTH) {
            return null;
        }

        ArrayValue.Builder arrayBuilder = ArrayValue.newBuilder();
        Class<?> componentType = array.getClass().getComponentType();

        arrayBuilder.setClassName(componentType.getName());

        int length = Array.getLength(array);
        for (int i = 0; i < length; i++) {
            Object element = Array.get(array, i);

            if (element != null) {
                if (componentType.isArray()) {
                    ArrayValue nestedArrayValue = toArrayValue(element, depth + 1);
                    if (nestedArrayValue != null) {
                        arrayBuilder.addElements(ArrayElement.newBuilder().setArrayValue(nestedArrayValue));
                    } else {
                        arrayBuilder.addElements(ArrayElement.newBuilder().setUnexpandedObject(
                                UnexpandedObject.newBuilder().setClassName(element.getClass().getName()).build()));
                    }

                } else if (componentType.isPrimitive() || isBasicType(componentType)) {
                    BasicValue basicValue = createBasicValue(element);
                    arrayBuilder.addElements(ArrayElement.newBuilder().setBasicValue(basicValue));
                } else {
                    JavaObject nestedObject = toJavaObject(element, depth + 1);
                    if (nestedObject != null) {
                        arrayBuilder.addElements(ArrayElement.newBuilder().setObjectValue(nestedObject));
                    } else {
                        arrayBuilder.addElements(ArrayElement.newBuilder().setUnexpandedObject(
                                UnexpandedObject.newBuilder().setClassName(element.getClass().getName()).build()));
                    }

                }
            } else {
                arrayBuilder.addElements(ArrayElement.newBuilder()
                        .setNullValue(NullValue.newBuilder().setClassName(componentType.getName()).build()));
            }
        }

        return arrayBuilder.build();
    }

    /** 将 Map 的键值对逐条序列化为 {@link MapValue} */
    private static MapValue createMapValue(Map<?, ?> map, int depth) {
        MapValue.Builder builder = MapValue.newBuilder();

        for (Entry<?, ?> entry : map.entrySet()) {
            MapEntry mapEntry = MapEntry.newBuilder().setKey(toJavaObject(entry.getKey(), depth))
                    .setValue(toJavaObject(entry.getValue(), depth)).build();
            builder.addEntries(mapEntry);
        }
        return builder.build();
    }

    /** 将集合元素序列化为 {@link CollectionValue} */
    private static CollectionValue createCollectionValue(Collection<?> collection, int depth) {
        Builder builder = CollectionValue.newBuilder();
        for (Object o : collection) {
            builder.addElements(toJavaObject(o, depth));
        }
        return builder.build();
    }

    /** 根据运行时类型填充 {@link BasicValue} 的 oneof 字段 */
    private static BasicValue createBasicValue(Object value) {
        BasicValue.Builder builder = BasicValue.newBuilder();

        if (value instanceof Integer) {
            builder.setInt((int) value);
        } else if (value instanceof Long) {
            builder.setLong((long) value);
        } else if (value instanceof Float) {
            builder.setFloat((float) value);
        } else if (value instanceof Double) {
            builder.setDouble((double) value);
        } else if (value instanceof Boolean) {
            builder.setBoolean((boolean) value);
        } else if (value instanceof String) {
            builder.setString((String) value);
        }

        return builder.build();
    }

    /** 判断是否为 String 及常见包装类型 */
    private static boolean isBasicType(Class<?> clazz) {
        if (String.class.equals(clazz) || Integer.class.equals(clazz) || Long.class.equals(clazz)
                || Float.class.equals(clazz) || Double.class.equals(clazz) || Boolean.class.equals(clazz)) {
            return true;
        }
        return false;
    }
}
