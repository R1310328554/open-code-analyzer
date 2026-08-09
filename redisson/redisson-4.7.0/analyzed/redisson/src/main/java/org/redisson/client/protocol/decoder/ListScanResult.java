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

import java.util.List;

import org.redisson.ScanResult;
import org.redisson.client.RedisClient;

/**
 * 列表型 SCAN 迭代结果，实现 {@link ScanResult} 接口。
 * <p>
 * 包含游标位置 {@code pos}、本次扫描到的值列表，以及可选的
 * 执行扫描的 {@link RedisClient} 引用（用于集群路由）。
 *
 * @author Nikita Koksharov
 *
 * @param <V> value type
 */
public class ListScanResult<V> implements ScanResult<V> {

    /** SCAN 游标，{@code "0"} 表示迭代结束。 */
    private final String pos;
    /** 本批次扫描到的元素列表。 */
    private final List<V> values;
    /** 执行该 SCAN 的 Redis 客户端（集群场景下回填）。 */
    private RedisClient client;

    /** 构造一次 SCAN 页结果。 */
    public ListScanResult(String pos, List<V> values) {
        this.pos = pos;
        this.values = values;
    }

    @Override
    public String getPos() {
        return pos;
    }

    @Override
    public List<V> getValues() {
        return values;
    }

    @Override
    public void setRedisClient(RedisClient client) {
        this.client = client;
    }

    @Override
    public RedisClient getRedisClient() {
        return client;
    }

    @Override
    public String toString() {
        return "ListScanResult{" +
                "pos=" + pos +
                ", values=" + values +
                ", client=" + client +
                '}';
    }
}
