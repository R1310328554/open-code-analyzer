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

package com.alibaba.nacos.client.selector;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.ConcurrentHashSet;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Selector Manager.
 * <p>选择器管理器：按订阅 ID（subId）维护 {@link AbstractSelectorWrapper} 集合，支持增删选择器包装器及查询订阅状态，供命名/配置客户端统一调度实例选择逻辑。</p>
 *
 * @param <S> the type of selector wrapper
 * @author lideyou
 */
public class SelectorManager<S extends AbstractSelectorWrapper<?, ?, ?>> {
    
    /** 订阅 ID 到选择器包装器集合的并发映射 */
    Map<String, Set<S>> selectorMap = new ConcurrentHashMap<>();
    
    /**
     * Add a selectorWrapper to subId.
     * <p>将选择器包装器加入指定订阅；若 subId 尚无集合则创建 {@link com.alibaba.nacos.common.utils.ConcurrentHashSet}。</p>
     *
     * @param subId   subscription id
     * @param wrapper selector wrapper
     */
    public void addSelectorWrapper(String subId, S wrapper) {
        selectorMap.compute(subId, (k, v) -> {
            if (v == null) {
                v = new ConcurrentHashSet<>();
            }
            v.add(wrapper);
            return v;
        });
    }
    
    /**
     * Get all SelectorWrappers by id.
     * <p>按订阅 ID 返回全部选择器包装器；不存在时返回空集合（非 null）。</p>
     *
     * @param subId subscription id
     * @return the set of SelectorWrappers
     */
    public Set<S> getSelectorWrappers(String subId) {
        return selectorMap.getOrDefault(subId, Collections.emptySet());
    }
    
    /**
     * Remove a SelectorWrapper by id.
     * <p>从订阅下移除指定包装器；集合变空时删除 subId 条目以释放内存。</p>
     *
     * @param subId   subscription id
     * @param wrapper selector wrapper
     */
    public void removeSelectorWrapper(String subId, S wrapper) {
        selectorMap.computeIfPresent(subId, (k, v) -> {
            v.remove(wrapper);
            return v.isEmpty() ? null : v;
        });
    }
    
    /**
     * Remove a subscription by id.
     * <p>删除整个订阅及其下所有选择器包装器。</p>
     *
     * @param subId subscription id
     */
    public void removeSubscription(String subId) {
        selectorMap.remove(subId);
    }
    
    /**
     * Get all subscriptions.
     * <p>返回当前已注册的全部订阅 ID 集合（即 selectorMap 的 keySet）。</p>
     *
     * @return all subscriptions
     */
    public Set<String> getSubscriptions() {
        return selectorMap.keySet();
    }
    
    /**
     * Determine whether subId is subscribed.
     * <p>判断 subId 是否仍有关联的非空选择器包装器集合。</p>
     *
     * @param subId subscription id
     * @return true if is subscribed
     */
    public boolean isSubscribed(String subId) {
        return CollectionUtils.isNotEmpty(this.getSelectorWrappers(subId));
    }
}
