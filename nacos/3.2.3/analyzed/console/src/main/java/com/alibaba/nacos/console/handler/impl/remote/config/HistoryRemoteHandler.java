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

package com.alibaba.nacos.console.handler.impl.remote.config;

import com.alibaba.nacos.api.config.model.ConfigBasicInfo;
import com.alibaba.nacos.api.config.model.ConfigHistoryBasicInfo;
import com.alibaba.nacos.api.config.model.ConfigHistoryDetailInfo;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.console.handler.config.HistoryHandler;
import com.alibaba.nacos.console.handler.impl.ConditionFunctionEnabled;
import com.alibaba.nacos.console.handler.impl.remote.EnabledRemoteHandler;
import com.alibaba.nacos.console.handler.impl.remote.NacosMaintainerClientHolder;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 配置历史远程 Handler：查询变更记录、上一版本及按命名空间列举配置，通过 {@link NacosMaintainerClientHolder} 调用远端 Config Maintainer API。
 * Remote Implementation of HistoryHandler for handling internal configuration operations.
 *
 * @author xiweng.yy
 */
@Service
@EnabledRemoteHandler
@Conditional(ConditionFunctionEnabled.ConditionConfigEnabled.class)
public class HistoryRemoteHandler implements HistoryHandler {
    
    /** 运维客户端持有者，提供 Config Maintainer 远程访问能力 */
    private final NacosMaintainerClientHolder clientHolder;
    
    /** 注入运维客户端持有者 */
    public HistoryRemoteHandler(NacosMaintainerClientHolder clientHolder) {
        this.clientHolder = clientHolder;
    }
    
    /** 按 nid 获取远端单条配置历史详情。 */
    @Override
    public ConfigHistoryDetailInfo getConfigHistoryInfo(String dataId, String group,
        String namespaceId, Long nid)
        throws NacosException {
        return clientHolder.getConfigMaintainerService().getConfigHistoryInfo(dataId, group,
            namespaceId, nid);
    }
    
    /** 分页列出远端指定配置的历史变更记录。 */
    @Override
    public Page<ConfigHistoryBasicInfo> listConfigHistory(String dataId, String group,
        String namespaceId,
        Integer pageNo, Integer pageSize) throws NacosException {
        return clientHolder.getConfigMaintainerService()
            .listConfigHistory(dataId, group, namespaceId, pageNo, pageSize);
    }
    
    /** 获取远端指定历史记录的前一版本详情。 */
    @Override
    public ConfigHistoryDetailInfo getPreviousConfigHistoryInfo(String dataId, String group,
        String namespaceId,
        Long id) throws NacosException {
        return clientHolder.getConfigMaintainerService().getPreviousConfigHistoryInfo(dataId, group,
            namespaceId, id);
    }
    
    /** 按命名空间（租户）列出远端全部配置基本信息。 */
    @Override
    public List<ConfigBasicInfo> getConfigsByTenant(String namespaceId) throws NacosException {
        return clientHolder.getConfigMaintainerService().getConfigListByNamespace(namespaceId);
    }
    
}
