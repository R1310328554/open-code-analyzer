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
package org.redisson.liveobject.resolver;

import java.io.IOException;

import org.redisson.client.codec.Codec;
import org.redisson.client.handler.State;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

/**
 * 默认 Live Object Redis 命名方案。
 * <p>
 * 实体 key：{@code redisson_live_object:{hex(id)}:全限定类名}；
 * 嵌套字段引用与索引 key 使用独立前缀。
 *
 * @author Rui Gu (https://github.com/jackygurui)
 * @author Nikita Koksharov
 */
public class DefaultNamingScheme extends AbstractNamingScheme implements NamingScheme {

    /** @param codec 用于 id 与 map key 编解码的 Codec */
    public DefaultNamingScheme(Codec codec) {
        super(codec);
    }

    /** 返回 SCAN/模式匹配用的通配 key 模式（id 段为 *）。 */
    @Override
    public String getNamePattern(Class<?> entityClass) {
        return "redisson_live_object:{" + "*" + "}:" + entityClass.getName();
    }

    /** 将 id 编码为 hex 并拼成实体 Live Object 的 Redis key。 */
    @Override
    public String getName(Class<?> entityClass, Object idValue) {
        try {
            String encode = bytesToHex(codec.getMapKeyEncoder().encode(idValue));
            return "redisson_live_object:{"+ encode + "}:" + entityClass.getName();
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable create name for '" + entityClass + "' with id:" + idValue, ex);
        }
    }

    /** 嵌套 {@link RObject} 字段的引用 key（含实体 id 与字段名）。 */
    @Override
    public String getFieldReferenceName(Class<?> entityClass, Object idValue, Class<?> fieldClass, String fieldName) {
        try {
            String encode = bytesToHex(codec.getMapKeyEncoder().encode(idValue));
            return "redisson_live_object_field:{" + encode + "}:" + entityClass.getName() + ":" + fieldName;
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable create name for '" + entityClass + "' and field:'" + fieldName + "' with id:" + idValue, ex);
        }
    }

    /** 从 key 的 {@code {hex}} 段解码出原始 id 对象。 */
    @Override
    public Object resolveId(String name) {
        String decode = name.substring(name.indexOf("{") + 1, name.indexOf("}"));
        
        ByteBuf b = Unpooled.wrappedBuffer(ByteBufUtil.decodeHexDump(decode)); 
        try {
            return codec.getMapKeyDecoder().decode(b, new State());
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to decode [" + decode + "] into object", ex);
        } finally {
            b.release();
        }
    }

    /** ByteBuf → 小写 hex 字符串，并在 finally 中 release。 */
    private static String bytesToHex(ByteBuf bytes) {
        try {
            return ByteBufUtil.hexDump(bytes);
        } finally {
            bytes.release();
        }
    }

    /** 二级索引 Redis key：按实体类与索引字段名命名。 */
    @Override
    public String getIndexName(Class<?> entityClass, String fieldName) {
        return "redisson_live_object_index:{" + entityClass.getName() + "}:" + fieldName;
    }

}
