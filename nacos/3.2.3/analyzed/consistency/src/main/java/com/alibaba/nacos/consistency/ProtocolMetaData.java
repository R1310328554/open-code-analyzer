/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.consistency;

import com.alibaba.nacos.common.utils.Observable;
import com.alibaba.nacos.common.utils.Observer;
import com.alibaba.nacos.common.utils.Pair;
import com.alibaba.nacos.common.utils.StringUtils;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 一致性协议元数据容器：双层 Map 结构（group → key → value），支持订阅值变更。
 * Consistent protocol metadata information, &lt;Key, &lt;Key, Value &gt;&gt; structure Listeners that can register to
 * listen to changes in value.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public final class ProtocolMetaData {
    
    /** group 名称 → 该组下的 MetaData 实例 */
    private final Map<String, MetaData> metaDataMap = new ConcurrentHashMap<>(4);
    
    /**
     * 供 Jackson 序列化使用的扁平 Map 视图。
     * used for jackson serialization.
     *
     * @return metaMap
     */
    public Map<String, Map<Object, Object>> getMetaDataMap() {
        return metaDataMap.entrySet().stream().map(entry -> Pair.with(entry.getKey(),
            entry.getValue().getItemMap().entrySet().stream()
                .collect(TreeMap::new, (m, e) -> m.put(e.getKey(), e.getValue().getData()),
                    TreeMap::putAll)))
            .collect(TreeMap::new, (m, e) -> m.put(e.getFirst(), e.getSecond()), TreeMap::putAll);
    }
    // 不保证严格时序：可能出现 time-1 的更新覆盖 time-2（time-1 < time-2）
    // Does not guarantee thread safety, there may be two updates of
    // time-1 and time-2 (time-1 <time-2), but time-1 data overwrites time-2
    
    /**
     * 批量加载协议元数据到内存。
     * save target consistency protocol metadata.
     *
     * @param mapMap {@link Map}
     */
    public void load(final Map<String, Map<String, Object>> mapMap) {
        mapMap.forEach((s, map) -> {
            metaDataMap.computeIfAbsent(s, MetaData::new);
            final MetaData data = metaDataMap.get(s);
            map.forEach(data::put);
        });
    }
    
    /**
     * 按 group 与 subKey 读取元数据值；subKey 为空时返回整个 MetaData。
     * get protocol metadata by group and key.
     *
     * @param group  group name
     * @param subKey key
     * @return target value
     */
    public Object get(String group, String subKey) {
        if (StringUtils.isBlank(subKey)) {
            return metaDataMap.get(group);
        } else {
            if (metaDataMap.containsKey(group)) {
                return metaDataMap.get(group).get(subKey);
            }
            return null;
        }
    }
    
    /**
     * 订阅指定 group/key 的值变更；MetaData 不存在时自动创建。
     * If MetaData does not exist, actively create a MetaData.
     */
    public void subscribe(final String group, final String key, final Observer observer) {
        metaDataMap.computeIfAbsent(group, s -> new MetaData(group)).subscribe(key, observer);
    }
    
    /** 取消对指定 group/key 的变更订阅 */
    public void unSubscribe(final String group, final String key, final Observer observer) {
        metaDataMap.computeIfAbsent(group, s -> new MetaData(group)).unSubscribe(key, observer);
    }
    
    /** 单个 group 下的键值元数据及订阅管理 */
    public static final class MetaData {
        
        /** subKey → 可观察的值项 */
        private final Map<String, ValueItem> itemMap = new ConcurrentHashMap<>(8);
        
        /** 所属 group 名称 */
        private final transient String group;
        
        public MetaData(String group) {
            this.group = group;
        }
        
        public Map<String, ValueItem> getItemMap() {
            return itemMap;
        }
        
        /** 写入或更新指定 key 的值并通知订阅者 */
        void put(String key, Object value) {
            ValueItem item = itemMap.computeIfAbsent(key, s -> new ValueItem(group + "/" + key));
            item.setData(value);
        }
        
        public ValueItem get(String key) {
            return itemMap.get(key);
        }
        
        // ValueItem 不存在时主动创建并注册 Observer
        // If ValueItem does not exist, actively create a ValueItem
        
        void subscribe(final String key, final Observer observer) {
            final ValueItem item =
                itemMap.computeIfAbsent(key, s -> new ValueItem(group + "/" + key));
            item.addObserver(observer);
        }
        
        void unSubscribe(final String key, final Observer observer) {
            final ValueItem item = itemMap.get(key);
            if (item == null) {
                return;
            }
            item.deleteObserver(observer);
        }
        
    }
    
    /** 带读写锁的可观察值项，变更时触发 Observer 回调 */
    public static final class ValueItem extends Observable {
        
        /** 元数据路径标识：group/key */
        private final transient String path;
        
        private final transient ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        
        private final transient ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
        
        private final transient ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
        
        /** 当前存储的值 */
        private volatile Object data;
        
        public ValueItem(String path) {
            this.path = path;
        }
        
        /** 读锁保护下返回当前值 */
        public Object getData() {
            readLock.lock();
            try {
                return data;
            } finally {
                readLock.unlock();
            }
        }
        
        /** 写锁保护下更新值并通知所有 Observer */
        void setData(Object data) {
            writeLock.lock();
            try {
                this.data = data;
                setChanged();
                notifyObservers();
            } finally {
                writeLock.unlock();
            }
        }
        
        public String getPath() {
            return path;
        }
    }
}
