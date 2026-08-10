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

package com.alibaba.nacos.client.naming.event;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.common.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 相对上次回调的实例变更差异。
 *
 * <p>由 {@link InstancesDiffer} 计算，包含新增、删除与修改的 {@link Instance} 列表，供变更事件与用户监听器感知具体变化。</p>
 *
 * @author lideyou
 */
public class InstancesDiff {
    
    /** 新增实例列表。 */
    private final List<Instance> addedInstances = new ArrayList<>();
    
    /** 移除实例列表。 */
    private final List<Instance> removedInstances = new ArrayList<>();
    
    /** 属性变更的实例列表。 */
    private final List<Instance> modifiedInstances = new ArrayList<>();
    
    public InstancesDiff() {
    }
    
    public InstancesDiff(List<Instance> addedInstances, List<Instance> removedInstances,
        List<Instance> modifiedInstances) {
        setAddedInstances(addedInstances);
        setRemovedInstances(removedInstances);
        setModifiedInstances(modifiedInstances);
    }
    
    public List<Instance> getAddedInstances() {
        return addedInstances;
    }
    
    public void setAddedInstances(Collection<Instance> addedInstances) {
        this.addedInstances.clear();
        if (CollectionUtils.isNotEmpty(addedInstances)) {
            this.addedInstances.addAll(addedInstances);
        }
    }
    
    public List<Instance> getRemovedInstances() {
        return removedInstances;
    }
    
    public void setRemovedInstances(Collection<Instance> removedInstances) {
        this.removedInstances.clear();
        if (CollectionUtils.isNotEmpty(removedInstances)) {
            this.removedInstances.addAll(removedInstances);
        }
    }
    
    public List<Instance> getModifiedInstances() {
        return modifiedInstances;
    }
    
    public void setModifiedInstances(Collection<Instance> modifiedInstances) {
        this.modifiedInstances.clear();
        if (CollectionUtils.isNotEmpty(modifiedInstances)) {
            this.modifiedInstances.addAll(modifiedInstances);
        }
    }
    
    /**
     * 是否存在任意实例变更。
     *
     * @return 有增删改任一变化时返回 true
     */
    public boolean hasDifferent() {
        return isAdded() || isRemoved() || isModified();
    }
    
    /**
     * 是否存在新增实例。
     *
     * @return 有新增实例时返回 true
     */
    public boolean isAdded() {
        return CollectionUtils.isNotEmpty(this.addedInstances);
    }
    
    /**
     * 是否存在移除实例。
     *
     * @return 有移除实例时返回 true
     */
    /** 判断是否有实例被移除。 */
    public boolean isRemoved() {
        return CollectionUtils.isNotEmpty(this.removedInstances);
    }
    
    /**
     * 是否存在修改过的实例。
     *
     * @return 有修改实例时返回 true
     */
    /** 判断是否有实例属性变更。 */
    public boolean isModified() {
        return CollectionUtils.isNotEmpty(this.modifiedInstances);
    }
}
