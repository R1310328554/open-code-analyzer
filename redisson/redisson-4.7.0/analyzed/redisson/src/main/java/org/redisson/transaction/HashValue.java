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
package org.redisson.transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 事务 commit 前禁用 LocalCachedMap 条目时的聚合状态。
 * <p>
 * {@link #keyIds} 为需禁用的缓存键哈希；{@link #allKeys} 为整表禁用；
 * {@link #counter} 用于等待各节点 ACK 完成（与订阅者数量对齐）。
 *
 * @author Nikita Koksharov
 *
 */
public class HashValue {

    /** 待收到的禁用 ACK 计数（减至 0 表示该 Map 协调完成）。 */
    private final AtomicInteger counter = new AtomicInteger();
    /** 需禁用本地缓存的具体键哈希列表。 */
    private final List<byte[]> keyIds = new ArrayList<byte[]>();
    /** true 表示禁用该 Map 的全部本地缓存条目。 */
    private boolean allKeys;

    /** @return 是否整表禁用本地缓存 */
    public boolean isAllKeys() {
        return allKeys;
    }

    /** 标记 delete/expire 等整键操作时需禁用全表缓存。 */
    public void setAllKeys(boolean allKeys) {
        this.allKeys = allKeys;
    }

    public HashValue() {
    }
    
    /** @return ACK 计数器 */
    public AtomicInteger getCounter() {
        return counter;
    }
    
    /** @return 待禁用键哈希列表 */
    public List<byte[]> getKeyIds() {
        return keyIds;
    }
    
}
