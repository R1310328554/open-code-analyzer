
package com.taobao.arthas.core.env.convert;

import java.lang.reflect.Array;

import com.taobao.arthas.core.env.ConversionService;
import com.taobao.arthas.core.util.StringUtils;

/**
 * 逗号分隔字符串到数组的转换器。
 * <p>
 * 先用 {@link StringUtils#tokenizeToStringArray} 按逗号拆分，再借助
 * {@link ConversionService} 将各元素转换为目标组件类型。
 */
final class StringToArrayConverter<T> implements Converter<String, T[]> {

    /** 用于转换数组元素的嵌套转换服务 */
    private ConversionService conversionService;

    /** @param conversionService 元素类型转换服务（通常为 {@link DefaultConversionService}） */
    public StringToArrayConverter(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    /** 将逗号分隔字符串解析为目标组件类型的数组 */
    @Override
    public T[] convert(String source, Class<T[]> targetType) {
        String[] strings = StringUtils.tokenizeToStringArray(source, ",");

        @SuppressWarnings("unchecked")
        T[] values = (T[]) Array.newInstance(targetType.getComponentType(), strings.length);
        for (int i = 0; i < strings.length; ++i) {
            @SuppressWarnings("unchecked")
            T value = (T) conversionService.convert(strings[i], targetType.getComponentType());

            values[i] = value;
        }

        return values;
    }

}
