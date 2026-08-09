
package com.taobao.arthas.core.env.convert;

/** 字符串到枚举常量的转换器，通过 {@link Enum#valueOf(Class, String)} 解析。 */
@SuppressWarnings("rawtypes")
final class StringToEnumConverter<T extends Enum> implements Converter<String, T> {
    /** 按枚举名称（区分大小写）解析目标枚举值 */
    @SuppressWarnings("unchecked")
    @Override
    public T convert(String source, Class<T> targetType) {
        return (T) Enum.valueOf(targetType, source);
    }

}
