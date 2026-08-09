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
package org.redisson.client.protocol.decoder;

import org.redisson.ScanResult;
import org.redisson.client.RedisClient;

import java.util.Collection;
import java.util.List;

/**
 * RMapCache 键扫描结果，扩展 {@link ScanResult} 以携带空闲键列表。
 * <p>
 * 除常规 SCAN 的游标与普通键外，还包含 {@code idleKeys}——
 * 满足最大空闲时间条件的键，用于缓存淘汰相关命令。
 *
 * @author Nikita Koksharov
 *
 * @param <K> key type
 */
public class MapCacheKeyScanResult<K> implements ScanResult<K> {

    /** 达到空闲阈值的键列表。 */
    private final List<K> idleKeys;
    /** SCAN 游标。 */
    private final String pos;
    /** 本批次扫描到的键。 */
    private final List<K> keys;
    /** 执行扫描的 Redis 客户端。 */
    private RedisClient client;

    /** 构造带空闲键信息的键扫描页。 */
    public MapCacheKeyScanResult(String pos, List<K> keys, List<K> idleKeys) {
        super();
        this.pos = pos;
        this.keys = keys;
        this.idleKeys = idleKeys;
    }

    @Override
    public Collection<K> getValues() {
        return keys;
    }

    @Override
    public String getPos() {
        return pos;
    }

    @Override
    public void setRedisClient(RedisClient client) {
        this.client = client;
    }

    @Override
    public RedisClient getRedisClient() {
        return client;
    }

    /** 返回空闲时间超限的键集合。 */
    public List<K> getIdleKeys() {
        return idleKeys;
    }
}
