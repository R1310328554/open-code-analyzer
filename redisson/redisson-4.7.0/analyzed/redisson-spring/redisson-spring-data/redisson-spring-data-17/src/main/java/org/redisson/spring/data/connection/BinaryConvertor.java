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
package org.redisson.spring.data.connection;

import org.redisson.client.protocol.convertor.Convertor;

import io.netty.util.CharsetUtil;

/**
 * Redis 协议层字符串到字节数组转换器。
 * <p>供 Spring Data Redis 命令参数在 UTF-8 二进制与 String 间转换。
 *
 * @author Nikita Koksharov
 *
 */
public class BinaryConvertor implements Convertor<Object> {

    /** {@link String} 转为 UTF-8 字节数组；其他类型原样返回。 */
    @Override
    public Object convert(Object obj) {
        if (obj instanceof String) {
            return ((String) obj).getBytes(CharsetUtil.UTF_8);
        }
        return obj;
    }

}
