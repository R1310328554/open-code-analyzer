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

import java.util.Collection;
import java.util.Map;

import org.redisson.ScanResult;
import org.redisson.client.RedisClient;

/**
 * Hash 结构 {@code HSCAN} 命令的扫描结果封装。
 * <p>
 * 包含下次迭代游标、当前批次的键值映射，
 * 并实现 {@link ScanResult} 以支持集群环境下的客户端绑定。
 *
 * @author Nikita Koksharov
 *
 * @param <K> key type
 * @param <V> value type
 */
public class MapScanResult<K, V> implements ScanResult<Map.Entry<K, V>> {

    /** 下次扫描起始游标，{@code "0"} 表示迭代结束。 */
    private final String pos;
    /** 本批次扫描得到的键值映射。 */
    private final Map<K, V> values;
    /** 执行扫描的 Redis 节点客户端（集群场景下由框架注入）。 */
    private RedisClient client;

    /** 构造包含游标与键值映射的扫描结果。 */
    public MapScanResult(String pos, Map<K, V> values) {
        super();
        this.pos = pos;
        this.values = values;
    }

    /** 以 Map 条目集合形式返回扫描值，供 {@link ScanResult} 接口使用。 */
    @Override
    public Collection<Map.Entry<K, V>> getValues() {
        return values.entrySet();
    }
    
    /** 直接返回底层键值 Map。 */
    public Map<K, V> getMap() {
        return values;
    }
    
    /** 返回下次扫描应使用的游标字符串。 */
    @Override
    public String getPos() {
        return pos;
    }

    /** 绑定响应该扫描命令的 Redis 客户端。 */
    @Override
    public void setRedisClient(RedisClient client) {
        this.client = client;
    }

    /** 获取绑定的 Redis 客户端，集群路由续扫时使用。 */
    @Override
    public RedisClient getRedisClient() {
        return client;
    }

}
