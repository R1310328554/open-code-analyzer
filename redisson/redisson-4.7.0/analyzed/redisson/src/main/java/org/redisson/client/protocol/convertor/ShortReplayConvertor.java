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
 * 短整型回复转换器：{@code null} 映射为 {@code 0}。
 * <p>
 * 非空值通过 {@link String} 解析为 {@link Short}。
 *
 * @author Nikita Koksharov
 *
 */
public class ShortReplayConvertor implements Convertor<Short> {

    /** {@code null} 返回 {@code 0}，否则解析为 {@link Short}。 */
    @Override
    public Short convert(Object obj) {
        if (obj == null) {
            return 0;
        }
        return Short.valueOf(obj.toString());
    }


}
