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
package org.redisson.client.protocol.convertor;

/**
 * 长整型回复转换器，保留 {@code null} 而不替换为默认值。
 * <p>
 * 非空回复通过 {@link String} 形式解析为 {@link Long}，兼容多种协议表示。
 *
 * @author Su Ko
 *
 */
public class LongNullReplayConvertor implements Convertor<Long> {

    /** {@code null} 原样返回，否则解析为 {@link Long}。 */
    @Override
    public Long convert(Object obj) {
        if (obj == null) {
            return null;
        }
        return Long.valueOf(obj.toString());
    }
}
