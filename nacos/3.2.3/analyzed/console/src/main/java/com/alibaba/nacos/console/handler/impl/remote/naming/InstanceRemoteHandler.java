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

package com.alibaba.nacos.console.handler.impl.remote.naming;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.console.handler.impl.ConditionFunctionEnabled;
import com.alibaba.nacos.console.handler.impl.remote.EnabledRemoteHandler;
import com.alibaba.nacos.console.handler.impl.remote.NacosMaintainerClientHolder;
import com.alibaba.nacos.console.handler.naming.InstanceHandler;
import com.alibaba.nacos.core.utils.PageUtil;
import com.alibaba.nacos.naming.model.form.InstanceForm;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 服务实例远程 Handler：分页列表、更新与注销实例，通过 {@link NacosMaintainerClientHolder} 调用远端 Naming Maintainer API。
 * Remote Implementation of InstanceHandler that handles instance-related operations.
 *
 * @author xiweng.yy
 */
@Service
@EnabledRemoteHandler
@Conditional(ConditionFunctionEnabled.ConditionNamingEnabled.class)
public class InstanceRemoteHandler implements InstanceHandler {
    
    /** 运维客户端持有者，提供 Naming Maintainer 远程访问能力 */
    private final NacosMaintainerClientHolder clientHolder;
    
    /** 注入运维客户端持有者 */
    public InstanceRemoteHandler(NacosMaintainerClientHolder clientHolder) {
        this.clientHolder = clientHolder;
    }
    
    /** 从远端拉取实例列表并在本地内存分页。 */
    @Override
    public Page<? extends Instance> listInstances(String namespaceId,
        String serviceNameWithoutGroup, String groupName,
        String clusterName, int page, int pageSize) throws NacosException {
        List<Instance> instances = clientHolder.getNamingMaintainerService()
            .listInstances(namespaceId, groupName, serviceNameWithoutGroup, clusterName, false);
        return PageUtil.subPage(instances, page, pageSize);
    }
    
    /** 更新远端持久化实例元数据。 */
    @Override
    public void updateInstance(InstanceForm instanceForm, Instance instance) throws NacosException {
        clientHolder.getNamingMaintainerService()
            .updateInstance(instanceForm.getNamespaceId(), instanceForm.getGroupName(),
                instanceForm.getServiceName(), instance);
    }
    
    /** 从远端服务注销指定实例。 */
    @Override
    public void removeInstance(InstanceForm instanceForm, Instance instance) throws NacosException {
        clientHolder.getNamingMaintainerService()
            .deregisterInstance(instanceForm.getNamespaceId(), instanceForm.getGroupName(),
                instanceForm.getServiceName(), instance);
    }
}
