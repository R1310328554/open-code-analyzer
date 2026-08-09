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

import org.redisson.api.RType;

/**
 * Redis {@code TYPE} 命令回复转换器，将类型字符串映射为 {@link RType}。
 * <p>
 * 支持 string、list、set、zset、hash、stream、gcra、ReJSON-RL 等类型；
 * {@code none} 表示键不存在，返回 {@code null}。
 *
 * @author Nikita Koksharov
 *
 */
public class TypeConvertor implements Convertor<RType> {

    /** 按 Redis 类型名查找对应 {@link RType}，无法识别时抛出异常。 */
    @Override
    public RType convert(Object obj) {
        String val = obj.toString();
        if ("string".equals(val)) {
            return RType.OBJECT;
        }
        if ("list".equals(val)) {
            return RType.LIST;
        }
        if ("set".equals(val)) {
            return RType.SET;
        }
        if ("zset".equals(val)) {
            return RType.ZSET;
        }
        if ("hash".equals(val)) {
            return RType.MAP;
        }
        if ("stream".equals(val)) {
            return RType.STREAM;
        }
        if ("gcra".equals(val)) {
            return RType.GCRA;
        }
        if ("ReJSON-RL".equals(val)) {
            return RType.JSON;
        }
        if ("none".equals(val)) {
            return null;
        }

        throw new IllegalStateException("Can't recognize redis type: " + obj);
    }

}
