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
package org.redisson.api.map;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletionStage;

/**
 * 异步 Map 写入器，用于写穿透（write-through）模式下将变更异步同步到外部数据源。
 * 
 * @author Nikita Koksharov
 *
 * @param <K> key type
 * @param <V> value type
 */
public interface MapWriterAsync<K, V> {

    /** 异步将一批键值对写入外部数据源。 */
    CompletionStage<Void> write(Map<K, V> map);

    /** 异步从外部数据源删除指定键对应的条目。 */
    CompletionStage<Void> delete(Collection<K> keys);
    
}
