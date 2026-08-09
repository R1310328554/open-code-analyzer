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
package org.redisson;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Write-Behind 队列中的可序列化写入任务。
 * <p>{@link Remove} 表示批量删除键；{@link Add} 表示批量 put。
 *
 * @author Nikita Koksharov
 */
public class MapWriterTask implements Serializable {

    /** 删除键任务：{@link #getKeys()} 为待删键集合。 */
    public static class Remove extends MapWriterTask {

        public Remove() {
        }

        public Remove(Collection<?> keys) {
            super(keys);
        }

        public Remove(Object key) {
            super(key);
        }
        
    }

    /** 新增/更新任务：{@link #getMap()} 为待写入键值对。 */
    public static class Add extends MapWriterTask {

        public Add() {
        }
        
        public Add(Map<?, ?> map) {
            super(map);
        }

        public Add(Object key, Object value) {
            super(key, value);
        }
        
    }
    
    private Collection<?> keys = Collections.emptyList();
    private Map<?, ?> map = Collections.emptyMap();
    
    public MapWriterTask() {
    }
    
    /** 单键删除任务。 */
    public MapWriterTask(Object key) {
        this.keys = Collections.singletonList(key);
    }
    
    /** 单键 put 任务。 */
    public MapWriterTask(Object key, Object value) {
        this.map = Collections.singletonMap(key, value);
    }
    
    /** 批量 put 任务。 */
    public MapWriterTask(Map<?, ?> map) {
        this.map = map;
    }
    
    /** 批量删除任务。 */
    public MapWriterTask(Collection<?> keys) {
        this.keys = keys;
    }

    /** 返回待删除键集合（Remove 任务）。 */
    public <V> Collection<V> getKeys() {
        return (Collection<V>) keys;
    }
    
    /** 返回待写入键值映射（Add 任务）。 */
    public <K, V> Map<K, V> getMap() {
        return (Map<K, V>) map;
    }
    
}
