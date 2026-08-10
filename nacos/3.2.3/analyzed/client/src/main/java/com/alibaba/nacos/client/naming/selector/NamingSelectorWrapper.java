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

package com.alibaba.nacos.client.naming.selector;

import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.selector.NamingContext;
import com.alibaba.nacos.api.naming.selector.NamingSelector;
import com.alibaba.nacos.client.naming.event.InstancesChangeEvent;
import com.alibaba.nacos.client.naming.event.InstancesDiff;
import com.alibaba.nacos.client.naming.listener.NamingChangeEvent;
import com.alibaba.nacos.client.selector.AbstractSelectorWrapper;
import com.alibaba.nacos.common.utils.CollectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * 命名选择器包装器。
 *
 * <p>将 {@link NamingSelector} 与 {@link EventListener} 绑定，在 {@link InstancesChangeEvent} 到达时按选择器过滤实例并构造 {@link NamingChangeEvent} 回调监听器。</p>
 *
 * @author lideyou
 */
public class NamingSelectorWrapper
    extends AbstractSelectorWrapper<NamingSelector, NamingEvent, InstancesChangeEvent> {
    
    /** 服务名。 */
    private String serviceName;
    
    /** 分组名。 */
    private String groupName;
    
    /** 集群列表（逗号分隔）。 */
    private String clusters;
    
    /** 可复用的命名上下文，避免每次选择都分配新对象。 */
    private final InnerNamingContext namingContext = new InnerNamingContext();
    
    /** 内部 {@link NamingContext} 实现，持有当前待筛选实例列表。 */
    private class InnerNamingContext implements NamingContext {
        
        /** 当前上下文中的实例列表。 */
        private List<Instance> instances;
        
        @Override
        public String getServiceName() {
            return serviceName;
        }
        
        @Override
        public String getGroupName() {
            return groupName;
        }
        
        @Override
        public String getClusters() {
            return clusters;
        }
        
        @Override
        public List<Instance> getInstances() {
            return instances;
        }
        
        /** 更新待筛选实例列表（包内可见）。 */
        private void setInstances(List<Instance> instances) {
            this.instances = instances;
        }
    }
    
    /** 构造包装器，仅绑定选择器与监听器（服务维度由后续 setter 或子类填充）。 */
    public NamingSelectorWrapper(NamingSelector selector, EventListener listener) {
        super(selector, new NamingListenerInvoker(listener));
    }
    
    /** 构造包装器并绑定服务名、分组、集群与选择器、监听器。 */
    public NamingSelectorWrapper(String serviceName, String groupName, String clusters,
        NamingSelector selector,
        EventListener listener) {
        this(selector, listener);
        this.serviceName = serviceName;
        this.groupName = groupName;
        this.clusters = clusters;
    }
    
    /** 事件非空且含 hosts 与 instancesDiff 时才可进入选择流程。 */
    @Override
    protected boolean isSelectable(InstancesChangeEvent event) {
        return event != null && event.getHosts() != null && event.getInstancesDiff() != null;
    }
    
    /** 仅当存在新增、删除或修改实例时才回调监听器。 */
    @Override
    public boolean isCallable(NamingEvent event) {
        if (event == null) {
            return false;
        }
        NamingChangeEvent changeEvent = (NamingChangeEvent) event;
        return changeEvent.isAdded() || changeEvent.isRemoved() || changeEvent.isModified();
    }
    
    /** 对当前全量实例与各 diff 分片分别执行选择器，组装 {@link NamingChangeEvent}。 */
    @Override
    protected NamingEvent buildListenerEvent(InstancesChangeEvent event) {
        List<Instance> currentIns = Collections.emptyList();
        if (CollectionUtils.isNotEmpty(event.getHosts())) {
            currentIns = doSelect(event.getHosts());
        }
        
        InstancesDiff diff = event.getInstancesDiff();
        InstancesDiff newDiff = new InstancesDiff();
        if (diff.isAdded()) {
            newDiff.setAddedInstances(doSelect(diff.getAddedInstances()));
        }
        if (diff.isRemoved()) {
            newDiff.setRemovedInstances(doSelect(diff.getRemovedInstances()));
        }
        if (diff.isModified()) {
            newDiff.setModifiedInstances(doSelect(diff.getModifiedInstances()));
        }
        
        return new NamingChangeEvent(serviceName, groupName, clusters, currentIns, newDiff);
    }
    
    /** 在命名上下文中执行选择器并返回结果列表。 */
    private List<Instance> doSelect(List<Instance> instances) {
        NamingContext context = getNamingContext(instances);
        return this.getSelector().select(context).getResult();
    }
    
    /** 填充内部上下文并返回供选择器使用的 {@link NamingContext}。 */
    private NamingContext getNamingContext(final List<Instance> instances) {
        namingContext.setInstances(instances);
        return namingContext;
    }
}
