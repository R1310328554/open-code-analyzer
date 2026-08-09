
package com.taobao.arthas.core.env.convert;

/** 字符串到 {@link Integer} 的转换器，使用 {@link Integer#parseInt(String)}。 */
final class StringToIntegerConverter implements Converter<String, Integer> {
    /** 解析十进制整数字符串 */
    @Override
    public Integer convert(String source, Class<Integer> targetType) {
        return Integer.parseInt(source);
    }
}
