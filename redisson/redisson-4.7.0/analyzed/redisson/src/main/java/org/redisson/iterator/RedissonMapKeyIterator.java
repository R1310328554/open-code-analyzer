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

/**
 * {@link org.redisson.RedissonMap} 的 HSCAN 键迭代器。
 * <p>
 * 仅迭代 field 名（key），不加载 value；remove 时 fastRemove 对应 field。
 *
 * @author Nikita Koksharov
 *
 * @param <M> loaded value type
 */
public class RedissonMapKeyIterator<M> extends BaseIterator<M, M> {

    /** 被迭代的 Redisson Map。 */
    private final RedissonMap map;
    /** HSCAN 键名匹配模式。 */
    private final String pattern;
    /** 每批 SCAN 数量 hint。 */
    private final int count;

    /** 构造 Map 键迭代器。 */
    public RedissonMapKeyIterator(RedissonMap map, String pattern, int count) {
        this.map = map;
        this.pattern = pattern;
        this.count = count;
    }

    /** 在指定节点执行 scanKeyIterator 拉取下一批 key。 */
    @Override
    protected ScanResult<M> iterator(RedisClient client, String nextIterPos) {
        return map.scanKeyIterator(map.getRawName(), client, nextIterPos, pattern, count);
    }

    /** SCAN 结果即为 key，直接强转返回。 */
    @Override
    protected M getValue(Object entry) {
        return (M) entry;
    }

    /** 删除当前迭代的 field key。 */
    @Override
    protected void remove(Object value) {
        map.fastRemove(value);
    }

}
