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
package org.redisson.api.map.event;

/**
 * {@link org.redisson.api.RMapCache} 条目<b>更新</b>事件监听器。
 *
 * @author Nikita Koksharov
 *
 * @param <K> 键类型
 * @param <V> 值类型
 */
public interface EntryUpdatedListener<K, V> extends MapEntryListener {

    /**
     * 当已有条目的值被修改时回调。
     *
     * @param event 条目事件详情
     */
    void onUpdated(EntryEvent<K, V> event);
    
}
