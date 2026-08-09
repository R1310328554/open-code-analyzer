
package com.taobao.arthas.core.env.convert;

/** 字符串到 {@link Long} 的转换器，使用 {@link Long#parseLong(String)}。 */
final class StringToLongConverter implements Converter<String, Long> {
    /** 解析十进制长整数字符串 */
    @Override
    public Long convert(String source, Class<Long> targetType) {
        return Long.parseLong(source);
    }
}
