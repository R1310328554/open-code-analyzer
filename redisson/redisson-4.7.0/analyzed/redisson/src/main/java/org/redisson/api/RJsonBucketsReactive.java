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
package org.redisson.api;

import org.redisson.codec.JsonCodec;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface RJsonBucketsReactive {
    
    /**
     * 按 Redis 键批量读取 JSON 对象（默认 JSON 路径）。
     *
     * @param keys Redis 键名
     * @param <V>  type of object with specific json-path
     * @return 键名到 JSON 值的映射
     */
    <V> Mono<Map<String, V>> get(String... keys);
    
    /**
     * 按 Redis 键批量读取指定 JSON 路径下的值。
     *
     * @param codec JSON 编解码器
     * @param path JSON 路径
     * @param keys Redis 键名
     * @param <V>   type of value at specific json-path
     * @return 键名到 JSON 值的映射
     */
    <V> Mono<Map<String, V>> get(JsonCodec codec, String path, String... keys);
    
    /**
     * 按 Redis 键批量写入 JSON 对象（默认 JSON 路径）。
     *
     * @param buckets JSON 桶映射
     */
    Mono<Void> set(Map<String, ?> buckets);
    
    /**
     * 按 Redis 键批量写入指定 JSON 路径下的值。
     *
     * @param codec JSON 编解码器
     * @param path JSON 路径
     * @param buckets JSON 桶映射
     */
    Mono<Void> set(JsonCodec codec, String path, Map<String, ?> buckets);
}
