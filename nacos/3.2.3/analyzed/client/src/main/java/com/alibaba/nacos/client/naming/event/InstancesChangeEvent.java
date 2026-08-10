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

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.common.notify.Event;

import java.util.List;

/**
 * 服务实例列表变更事件。
 *
 * <p>由 {@link ServiceInfoHolder} 在检测到 {@link InstancesDiff} 后发布，供 {@link InstancesChangeNotifier} 分发给订阅监听器。</p>
 *
 * @author horizonzy
 * @since 1.4.1
 */
public class InstancesChangeEvent extends Event {
    
    private static final long serialVersionUID = -8823087028212249603L;
    
    /** 事件作用域，与 Notifier 注册 scope 匹配。 */
    private final String eventScope;
    
    /** 服务名。 */
    private final String serviceName;
    
    /** 分组名。 */
    private final String groupName;
    
    /** 集群列表字符串。 */
    private final String clusters;
    
    /** 变更后的完整实例列表。 */
    private final List<Instance> hosts;
    
    /** 相对上次回调的实例增删改差异。 */
    private InstancesDiff instancesDiff;
    
    public InstancesChangeEvent(String eventScope, String serviceName, String groupName,
        String clusters, List<Instance> hosts, InstancesDiff diff) {
        this.eventScope = eventScope;
        this.serviceName = serviceName;
        this.groupName = groupName;
        this.clusters = clusters;
        this.hosts = hosts;
        this.instancesDiff = diff;
    }
    
    /** 获取服务名。 */
    public String getServiceName() {
        return serviceName;
    }
    
    /** 获取分组名。 */
    public String getGroupName() {
        return groupName;
    }
    
    /** 获取集群列表。 */
    public String getClusters() {
        return clusters;
    }
    
    /** 获取变更后实例列表。 */
    public List<Instance> getHosts() {
        return hosts;
    }
    
    /** 获取实例差异详情。 */
    public InstancesDiff getInstancesDiff() {
        return instancesDiff;
    }
    
    /** 返回事件作用域标识。 */
    @Override
    public String scope() {
        return this.eventScope;
    }
}
