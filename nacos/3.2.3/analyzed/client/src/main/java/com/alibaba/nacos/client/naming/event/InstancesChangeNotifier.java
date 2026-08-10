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

package com.alibaba.nacos.client.naming.event;

import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.api.naming.utils.NamingUtils;
import com.alibaba.nacos.client.naming.selector.NamingSelectorWrapper;
import com.alibaba.nacos.client.selector.SelectorManager;
import com.alibaba.nacos.common.JustForTest;
import com.alibaba.nacos.common.notify.Event;
import com.alibaba.nacos.common.notify.listener.Subscriber;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * 实例变更事件订阅者与监听器路由。
 *
 * <p>订阅 {@link InstancesChangeEvent}，按 group@@service 将事件分发给已注册的 {@link NamingSelectorWrapper} 监听器。</p>
 *
 * @author horizonzy
 * @since 1.4.1
 */
public class InstancesChangeNotifier extends Subscriber<InstancesChangeEvent> {
    
    /** 本 Notifier 的事件作用域。 */
    private final String eventScope;
    
    /** 订阅 ID 到选择器包装器的管理器。 */
    private final SelectorManager<NamingSelectorWrapper> selectorManager = new SelectorManager<>();
    
    /** 测试用构造器，随机生成 eventScope。 */
    @JustForTest
    public InstancesChangeNotifier() {
        this.eventScope = UUID.randomUUID().toString();
    }
    
    public InstancesChangeNotifier(String eventScope) {
        this.eventScope = eventScope;
    }
    
    /**
     * 注册服务变更监听器。
     *
     * @param groupName   分组名
     * @param serviceName 服务名
     * @param wrapper     选择器包装器
     */
    public void registerListener(String groupName, String serviceName,
        NamingSelectorWrapper wrapper) {
        if (wrapper == null) {
            return;
        }
        String subId = NamingUtils.getGroupedName(serviceName, groupName);
        selectorManager.addSelectorWrapper(subId, wrapper);
    }
    
    /**
     * 注销服务变更监听器。
     *
     * @param groupName   分组名
     * @param serviceName 服务名
     * @param wrapper     选择器包装器
     */
    public void deregisterListener(String groupName, String serviceName,
        NamingSelectorWrapper wrapper) {
        if (wrapper == null) {
            return;
        }
        String subId = NamingUtils.getGroupedName(serviceName, groupName);
        selectorManager.removeSelectorWrapper(subId, wrapper);
    }
    
    /**
     * 判断指定服务是否仍有订阅。
     *
     * @param groupName   分组名
     * @param serviceName 服务名
     * @return 已订阅返回 true
     */
    public boolean isSubscribed(String groupName, String serviceName) {
        String subId = NamingUtils.getGroupedName(serviceName, groupName);
        return selectorManager.isSubscribed(subId);
    }
    
    /** 返回当前所有已订阅服务的 ServiceInfo 列表。 */
    public List<ServiceInfo> getSubscribeServices() {
        List<ServiceInfo> serviceInfos = new ArrayList<>();
        for (String key : selectorManager.getSubscriptions()) {
            serviceInfos.add(ServiceInfo.fromKey(key));
        }
        return serviceInfos;
    }
    
    /** 收到变更事件后向该服务下所有监听器分发。 */
    @Override
    public void onEvent(InstancesChangeEvent event) {
        String subId = NamingUtils.getGroupedName(event.getServiceName(), event.getGroupName());
        Collection<NamingSelectorWrapper> selectorWrappers =
            selectorManager.getSelectorWrappers(subId);
        for (NamingSelectorWrapper selectorWrapper : selectorWrappers) {
            selectorWrapper.notifyListener(event);
        }
    }
    
    /** 订阅事件类型为 InstancesChangeEvent。 */
    @Override
    public Class<? extends Event> subscribeType() {
        return InstancesChangeEvent.class;
    }
    
    /** 仅处理与本 Notifier scope 匹配的事件。 */
    @Override
    public boolean scopeMatches(InstancesChangeEvent event) {
        return this.eventScope.equals(event.scope());
    }
}
