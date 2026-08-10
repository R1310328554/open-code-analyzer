/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.client.naming.cache;

import com.alibaba.nacos.api.naming.listener.FuzzyWatchEventWatcher;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * 模糊监听回调包装器。
 *
 * <p>为每个 {@link FuzzyWatchEventWatcher} 分配 UUID，维护已同步的 serviceKey 集合与同步版本号，供 {@link NamingFuzzyWatchContext} 对账。</p>
 *
 * @author shiyiyue
 */
public class FuzzyWatchEventWatcherWrapper {
    
    /** 与上下文 serviceKey 集合对齐的版本戳。 */
    long syncVersion = 0;
    
    /** 被包装的模糊监听回调。 */
    FuzzyWatchEventWatcher fuzzyWatchEventWatcher;
    
    /** 监听器实例唯一标识，用于定向通知。 */
    String uuid = UUID.randomUUID().toString();
    
    public FuzzyWatchEventWatcherWrapper(FuzzyWatchEventWatcher fuzzyWatchEventWatcher) {
        this.fuzzyWatchEventWatcher = fuzzyWatchEventWatcher;
    }
    
    /** 已向该监听器同步过的 serviceKey 集合。 */
    private Set<String> syncServiceKeys = new HashSet<>();
    
    /** 返回监听器 UUID。 */
    final String getUuid() {
        return uuid;
    }
    
    /** 返回已同步 serviceKey 集合（可变，供上下文更新）。 */
    Set<String> getSyncServiceKeys() {
        return syncServiceKeys;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FuzzyWatchEventWatcherWrapper that = (FuzzyWatchEventWatcherWrapper) o;
        return Objects.equals(fuzzyWatchEventWatcher, that.fuzzyWatchEventWatcher);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(fuzzyWatchEventWatcher);
    }
}
