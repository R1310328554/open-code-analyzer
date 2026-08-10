package org.keycloak.testframework.injection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * 测试框架注入相关的反射工具类。
 * <p>
 * 提供字段/方法枚举、字段赋值及从 {@link Supplier} 泛型签名解析值类型与注解类型。
 */
public class ReflectionUtils {

    /**
     * 列出类及其父类（至 {@link Object} 之前）的所有声明字段。
     *
     * @param clazz 目标类
     * @return 字段列表
     */
    public static List<Field> listFields(Class<?> clazz) {
        List<Field> fields = new LinkedList<>(Arrays.asList(clazz.getDeclaredFields()));

        Class<?> superclass = clazz.getSuperclass();
        while (superclass != null && !superclass.equals(Object.class)) {
            fields.addAll(Arrays.asList(superclass.getDeclaredFields()));
            superclass = superclass.getSuperclass();
        }

        return fields;
    }

    /**
     * 列出类层次中带指定注解的方法，子类方法优先于父类同名方法。
     *
     * @param clazz 起始类
     * @param annotationClass 注解类型
     * @return 匹配方法列表
     */
    public static List<Method> listMethods(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        List<Method> methods = new LinkedList<>();
        List<Class<?>> hierarchy = new LinkedList<>();

        Class<?> current = clazz;
        while (current != null && !current.equals(Object.class)) {
            hierarchy.add(current);
            current = current.getSuperclass();
        }

        for (Class<?> c : hierarchy) {
            for (Method m : c.getDeclaredMethods()) {
                if (m.getAnnotation(annotationClass) == null) {
                    continue;
                }

                if (methods.stream().noneMatch(e -> e.getName().equals(m.getName()) && Arrays.equals(e.getParameterTypes(), m.getParameterTypes()))) {
                    methods.add(0, m);
                }
            }
        }

        return methods;
    }

    /**
     * 强制可访问并设置对象字段值。
     *
     * @param field 目标字段
     * @param object 实例对象
     * @param value 新值
     */
    public static void setField(Field field, Object object, Object value) {
        try {
            field.setAccessible(true);
            field.set(object, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /** 从供应器 {@code getValue} 方法泛型参数解析值类型。 */
    public static Class<?> getValueType(Supplier<?, ?> supplier) {
        return getActualTypeArgument(supplier, 0);
    }

    /** 从供应器 {@code getValue} 方法泛型参数解析注解类型。 */
    public static Class<?> getAnnotationType(Supplier<?, ?> supplier) {
        return getActualTypeArgument(supplier, 1);
    }

    /** 读取 {@code getValue(InstanceContext)} 第 {@code argument} 个泛型实参。 */
    private static Class<?> getActualTypeArgument(Supplier<?, ?> supplier, int argument) {
        try {
            ParameterizedType parameterizedType = (ParameterizedType) supplier.getClass().getMethod("getValue", InstanceContext.class).getGenericParameterTypes()[0];
            return (Class<?>) parameterizedType.getActualTypeArguments()[argument];
        } catch (Throwable e) {
            throw new RuntimeException("Failed to discover generic types for supplier " + supplier.getClass().getName() + "; supplier must implement getValue method directly", e);
        }

    }

}
