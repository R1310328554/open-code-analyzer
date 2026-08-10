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

package com.alibaba.nacos.client.config.impl;

import com.alibaba.nacos.api.config.ConfigChangeItem;
import com.alibaba.nacos.api.config.PropertyChangeType;
import com.alibaba.nacos.api.config.listener.ConfigChangeParser;

import java.util.HashMap;
import java.util.Map;

/**
 * 配置变更解析器抽象基类，按配置类型过滤并对比键值差异。
 *
 * <p>子类实现具体格式（如 properties、yaml）的解析，{@link #filterChangeData} 产出 {@link ConfigChangeItem} 集合。</p>
 *
 * @author rushsky518
 */
public abstract class AbstractConfigChangeParser implements ConfigChangeParser {
    
    /** 本解析器负责的配置类型标识。 */
    private final String configType;
    
    /** 指定负责的配置类型。 */
    public AbstractConfigChangeParser(String configType) {
        this.configType = configType;
    }
    
    @Override
    /** 判断是否与给定配置类型匹配（忽略大小写）。 */
    public boolean isResponsibleFor(String type) {
        return this.configType.equalsIgnoreCase(type);
    }
    
    /**
     * 对比新旧 Map，生成新增、修改、删除的 {@link ConfigChangeItem}。
     *
     * @param oldMap 变更前键值
     * @param newMap 变更后键值
     * @return 变更项映射
     */
    protected Map<String, ConfigChangeItem> filterChangeData(Map oldMap, Map newMap) {
        Map<String, ConfigChangeItem> result = new HashMap<>(16);
        for (Map.Entry<String, Object> e : (Iterable<Map.Entry<String, Object>>) oldMap
            .entrySet()) {
            ConfigChangeItem cci;
            if (newMap.containsKey(e.getKey())) {
                if (e.getValue().equals(newMap.get(e.getKey()))) {
                    continue;
                }
                cci = new ConfigChangeItem(e.getKey(), e.getValue().toString(),
                    newMap.get(e.getKey()).toString());
                cci.setType(PropertyChangeType.MODIFIED);
            } else {
                cci = new ConfigChangeItem(e.getKey(), e.getValue().toString(), null);
                cci.setType(PropertyChangeType.DELETED);
            }
            
            result.put(e.getKey(), cci);
        }
        
        for (Map.Entry<String, Object> e : (Iterable<Map.Entry<String, Object>>) newMap
            .entrySet()) {
            if (!oldMap.containsKey(e.getKey())) {
                ConfigChangeItem cci =
                    new ConfigChangeItem(e.getKey(), null, e.getValue().toString());
                cci.setType(PropertyChangeType.ADDED);
                result.put(e.getKey(), cci);
            }
        }
        
        return result;
    }
    
}
