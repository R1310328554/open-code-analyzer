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

package com.alibaba.nacos.api.config;

import java.util.Collection;
import java.util.Map;

/**
 * 配置变更事件，封装一次监听回调中的多项变更。
 *
 * <p>内部以键映射 {@link ConfigChangeItem}，可通过 {@link #getChangeItem(String)} 或
 * {@link #getChangeItems()} 遍历变更详情。</p>
 *
 * @author rushsky518
 */
public class ConfigChangeEvent {
    
    /** 变更项映射：键为 dataId 或业务键，值为变更详情。 */
    private final Map<String, ConfigChangeItem> data;
    
    /**
     * 构造配置变更事件。
     *
     * @param data 变更项映射
     */
    public ConfigChangeEvent(Map<String, ConfigChangeItem> data) {
        this.data = data;
    }
    
    /**
     * 按键获取单项变更。
     *
     * @param key 配置键或 dataId
     * @return 对应变更项，不存在时返回 {@code null}
     */
    public ConfigChangeItem getChangeItem(String key) {
        return data.get(key);
    }
    
    /**
     * 获取全部变更项集合。
     *
     * @return 变更项集合视图
     */
    public Collection<ConfigChangeItem> getChangeItems() {
        return data.values();
    }
    
}
