/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.client.codec;

import org.redisson.client.protocol.Decoder;

/**
 * t-digest 相关 {@link Double} 编解码器，继承 {@link StringCodec} 并处理特殊浮点字面量。
 *
 * @author Nikita Koksharov
 *
 */
public class TDigestDoubleCodec extends StringCodec {

    /** 单例实例。 */
    public static final TDigestDoubleCodec INSTANCE = new TDigestDoubleCodec();

    private final Decoder<Object> decoder = (buf, state) -> {
        String str = (String) TDigestDoubleCodec.super.getValueDecoder().decode(buf, state);
        return parse(str);
    };

    /**
     * 解析 t-digest 浮点回复，映射特殊字面量
     * {@code nan}、{@code inf} 与 {@code -inf}。
     *
     * @param str 原始回复字符串
     * @return 解析后的 double，值为空时返回 {@code null}
     */
    public static Double parse(String str) {
        if (str == null) {
            return null;
        }
        switch (str) {
            case "nan":
            case "NaN":
                return Double.NaN;
            case "inf":
            case "+inf":
                return Double.POSITIVE_INFINITY;
            case "-inf":
                return Double.NEGATIVE_INFINITY;
            default:
                return Double.valueOf(str);
        }
    }

    /** 先按字符串解码，再调用 {@link #parse(String)} 转换。 */
    @Override
    public Decoder<Object> getValueDecoder() {
        return decoder;
    }

}
