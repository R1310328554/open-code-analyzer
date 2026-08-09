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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Redis MGET/批量读取响应解码器，将扁平 value 列表与预设 key 列表配对为 Map。
 * <p>
 * 实现 {@link MultiDecoder}，按索引一一对应 {@code keys[i] -> parts[i]}。
 *
 * @author Nikita Koksharov
 *
 */
public class BucketsDecoder implements MultiDecoder<Map<Object, Object>> {

    /** 与响应值按序配对的 key 列表。 */
    private final List<Object> keys;
    
    /** @param keys 请求时使用的 key 顺序列表 */
    public BucketsDecoder(List<Object> keys) {
        this.keys = keys;
    }

    /**
     * 将 Redis 返回的 value 列表按索引与 keys 组装为 Map。
     *
     * @param parts 协议层解析出的 value 列表
     * @param state 解码状态（本实现未使用）
     * @return key 到 value 的映射
     */
    @Override
    public Map<Object, Object> decode(List<Object> parts, State state) {
        Map<Object, Object> result = new HashMap<>();
        for (int i = 0; i < parts.size(); i++) {
            result.put(keys.get(i), parts.get(i));
        }
        return result;
    }

}
