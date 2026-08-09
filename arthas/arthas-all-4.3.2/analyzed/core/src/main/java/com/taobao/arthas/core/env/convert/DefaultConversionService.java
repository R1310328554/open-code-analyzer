package com.taobao.arthas.core.env.convert;

import java.lang.reflect.Array;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link ConfigurableConversionService} 的默认实现。
 * <p>
 * 启动时注册常用 {@link String} → 标量/枚举/数组/InetAddress 转换器，
 * 通过 {@link ConvertiblePair} 键在 {@link ConcurrentHashMap} 中查找。
 */
public class DefaultConversionService implements ConfigurableConversionService {

    /** 源-目标类型对到转换器的全局映射表 */
    private static ConcurrentHashMap<ConvertiblePair, Converter> converters = new ConcurrentHashMap<ConvertiblePair, Converter>();

    /** 构造时注册内置转换器 */
    public DefaultConversionService() {
        addDefaultConverter();

    }

    /** 注册 String 到各目标类型的默认转换器 */
    private void addDefaultConverter() {
        converters.put(new ConvertiblePair(String.class, Integer.class), new StringToIntegerConverter());
        converters.put(new ConvertiblePair(String.class, Long.class), new StringToLongConverter());

        converters.put(new ConvertiblePair(String.class, Boolean.class), new StringToBooleanConverter());

        converters.put(new ConvertiblePair(String.class, InetAddress.class), new StringToInetAddressConverter());

        converters.put(new ConvertiblePair(String.class, Enum.class), new StringToEnumConverter());

        converters.put(new ConvertiblePair(String.class, Arrays.class), new StringToArrayConverter(this));

    }

    /** 判断 sourceType 能否转换为 targetType（含枚举与数组的泛化匹配） */
    @Override
    public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
        if (sourceType == targetType) {
            return true;
        }

        // 基本类型先映射为包装类再查表
        if (targetType.isPrimitive()) {
            targetType = objectiveClass(targetType);
        }

        if (converters.containsKey(new ConvertiblePair(sourceType, targetType))) {
            return true;
        }
        if (targetType.isEnum()) {
            if (converters.containsKey(new ConvertiblePair(sourceType, Enum.class))) {
                return true;
            }
        }

        if (targetType.isArray()) {
            return true;
        }
        return false;
    }

    /** 查找转换器并执行转换；无匹配时原样返回 source */
    @Override
    public <T> T convert(Object source, Class<T> targetType) {

        if (targetType.isPrimitive()) {
            targetType = (Class<T>) objectiveClass(targetType);
        }

        Converter converter = converters.get(new ConvertiblePair(source.getClass(), targetType));

        if (converter == null && targetType.isArray()) {
            converter = converters.get(new ConvertiblePair(source.getClass(), Arrays.class));
        }

        if (converter == null && targetType.isEnum()) {
            converter = converters.get(new ConvertiblePair(source.getClass(), Enum.class));
        }
        if (converter != null) {
            return (T) converter.convert(source, targetType);
        }

        return (T) source;
    }

    /**
     * 获取给定组件类型的数组 Class 对象。
     *
     * @param klass 组件类型
     * @param <C>   组件类型泛型
     * @return {@code C[]} 对应的 Class
     */
    public static <C> Class<C[]> arrayClass(Class<C> klass) {
        return (Class<C[]>) Array.newInstance(klass, 0).getClass();
    }

    /**
     * 将基本类型或基本类型数组映射为对应的包装类/对象数组 Class。
     *
     * @param klass 基本类型、数组或普通 Class
     * @return 继承 {@link Object} 的等价 Class
     */
    public static Class<?> objectiveClass(Class<?> klass) {
        Class<?> component = klass.getComponentType();
        if (component != null) {
            if (component.isPrimitive() || component.isArray())
                return arrayClass(objectiveClass(component));
        } else if (klass.isPrimitive()) {
            if (klass == char.class)
                return Character.class;
            if (klass == int.class)
                return Integer.class;
            if (klass == boolean.class)
                return Boolean.class;
            if (klass == byte.class)
                return Byte.class;
            if (klass == double.class)
                return Double.class;
            if (klass == float.class)
                return Float.class;
            if (klass == long.class)
                return Long.class;
            if (klass == short.class)
                return Short.class;
        }

        return klass;
    }

}
