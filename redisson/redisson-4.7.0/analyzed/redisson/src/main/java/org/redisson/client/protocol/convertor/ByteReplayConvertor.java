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
 * 将 Redis 回复转换为 {@link Byte}。
 * <p>
 * {@code null} 输入返回 {@code 0}，其余通过 {@code toString()} 解析为字节值。
 *
 * @author Nikita Koksharov
 *
 */
public class ByteReplayConvertor implements Convertor<Byte> {

    /** {@code null} 返回 0，否则解析字符串为字节。 */
    @Override
    public Byte convert(Object obj) {
        if (obj == null) {
            return 0;
        }
        return Byte.valueOf(obj.toString());
    }


}
