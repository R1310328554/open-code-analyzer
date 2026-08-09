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
package org.redisson.iterator;

import org.redisson.RedissonMap;
import org.redisson.ScanResult;
import org.redisson.client.RedisClient;

import java.util.Map.Entry;

/**
 * {@link org.redisson.RedissonMap} 的 HSCAN 条目迭代器。
 * <p>
 * 支持 pattern/count 分批扫描；迭代 Entry 可 setValue 写回 Map。
 *
 * @author Nikita Koksharov
 *
 * @param <M> loaded value type
 */
public class RedissonMapIterator<M> extends RedissonBaseMapIterator<M> {

    /** 被迭代的 Redisson Map 实例。 */
    private final RedissonMap map;
    /** HSCAN 键名匹配模式，可为 null 表示全部。 */
    private final String pattern;
    /** 每批 SCAN 建议返回的 field 数量 hint。 */
    private final int count;

    /** 构造 Map 条目迭代器。 */
    public RedissonMapIterator(RedissonMap map, String pattern, int count) {
        this.map = map;
        this.pattern = pattern;
        this.count = count;
    }

    /** 通过 Map.put 更新 field 并返回旧值。 */
    @Override
    protected Object put(Entry<Object, Object> entry, Object value) {
        return map.put(entry.getKey(), value);
    }

    /** 在指定 Redis 节点上执行 HSCAN 获取下一批 Entry。 */
    @Override
    protected ScanResult<Entry<Object, Object>> iterator(RedisClient client, String nextIterPos) {
        return map.scanIterator(map.getRawName(), client, nextIterPos, pattern, count);
    }

    /** 迭代过程中删除当前 field（fastRemove）。 */
    @Override
    protected void remove(Entry<Object, Object> value) {
        map.fastRemove(value.getKey());
    }

}
