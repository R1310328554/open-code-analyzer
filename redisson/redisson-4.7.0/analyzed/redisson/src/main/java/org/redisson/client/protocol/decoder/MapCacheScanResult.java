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
import java.util.Map;

/**
 * RMapCache 条目扫描结果，在 {@link MapScanResult} 基础上附加空闲键列表。
 * <p>
 * 用于带 TTL/最大空闲时间的 Map 缓存 SCAN 命令，同时返回
 * 键值映射与需淘汰的空闲键。
 *
 * @author Nikita Koksharov
 *
 * @param <K> key type
 * @param <V> value type
 */
public class MapCacheScanResult<K, V> extends MapScanResult<K, V> {

    /** 达到空闲阈值的键。 */
    private final List<K> idleKeys;

    /** 构造带空闲键信息的 Map 扫描页。 */
    public MapCacheScanResult(String pos, Map<K, V> values, List<K> idleKeys) {
        super(pos, values);
        this.idleKeys = idleKeys;
    };

    /** 返回空闲时间超限的键列表。 */
    public List<K> getIdleKeys() {
        return idleKeys;
    }
    
}
