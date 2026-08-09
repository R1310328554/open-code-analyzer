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
package org.redisson.connection.decoder;

import org.redisson.client.handler.State;
import org.redisson.client.protocol.decoder.MultiDecoder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * RMapCache {@code getAll} 响应解码器，将每 4 个字段一组展开为扁平五元组列表。
 * <p>
 * 每组对应：key、value、idle、ttl、remainIdleToLive。
 *
 * @author Nikita Koksharov
 *
 */
public class MapCacheGetAllDecoder implements MultiDecoder<List<Object>> {

    /** key 在 args 列表中的索引偏移量。 */
    private final int shiftIndex;
    /** 请求时的 key 参数列表，用于还原 key。 */
    private final List<Object> args;
    /** 是否保留 value 为 null 的条目。 */
    private final boolean allowNulls;

    /** 默认不允许 null value。 */
    public MapCacheGetAllDecoder(List<Object> args, int shiftIndex) {
        this(args, shiftIndex, false);
    }
    
    /** @param allowNulls 为 true 时保留 null value 条目 */
    public MapCacheGetAllDecoder(List<Object> args, int shiftIndex, boolean allowNulls) {
        this.args = args;
        this.shiftIndex = shiftIndex;
        this.allowNulls = allowNulls;
    }

    /**
     * 每 4 个响应字段（value、idle、ttl、remain）对应一个 key，展开为 [key,value,idle,ttl,remain,...]。
     */
    @Override
    public List<Object> decode(List<Object> parts, State state) {
        if (parts.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<Object> result = new ArrayList<Object>(parts.size()*5);
        for (int index = 0; index < parts.size(); index += 4) {
            Object value = parts.get(index);
            if (!allowNulls && value == null) {
                continue;
            }
            
            Object key = args.get(index/4+shiftIndex);
            result.add(key);
            result.add(value);
            result.add(parts.get(index+1));
            result.add(parts.get(index+2));
            result.add(parts.get(index+3));
        }
        return result;
    }

}
