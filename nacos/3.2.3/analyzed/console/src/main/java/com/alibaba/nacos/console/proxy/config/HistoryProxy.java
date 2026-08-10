/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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
 *
 */

package com.alibaba.nacos.console.proxy.config;

import com.alibaba.nacos.api.config.model.ConfigBasicInfo;
import com.alibaba.nacos.api.config.model.ConfigHistoryBasicInfo;
import com.alibaba.nacos.api.config.model.ConfigHistoryDetailInfo;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.console.handler.config.HistoryHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 配置历史代理：将历史详情、分页列表与租户配置查询委托给 {@link HistoryHandler}。
 * .
 *
 * @author zhangyukun on:2024/8/16
 */
@Service
public class HistoryProxy {
    
    /** 配置历史 Handler 实现 */
    private final HistoryHandler historyHandler;
    
    /**
     * 注入配置历史 Handler。
     * Constructs a new HistoryProxy with the given HistoryInnerHandler and ConsoleConfig.
     *
     * @param historyHandler HistoryHandler 默认实现
     */
    @Autowired
    public HistoryProxy(HistoryHandler historyHandler) {
        this.historyHandler = historyHandler;
    }
    
    /**
     * 查询指定历史记录的详细配置内容。
     * Query the detailed configuration history information.
     *
     * @param dataId      配置 dataId
     * @param group       配置 group
     * @param namespaceId 命名空间 ID
     * @param nid         历史记录 ID
     * @return 历史详情
     * @throws NacosException 操作失败时抛出
     */
    public ConfigHistoryDetailInfo getConfigHistoryInfo(String dataId, String group,
        String namespaceId, Long nid)
        throws NacosException {
        return historyHandler.getConfigHistoryInfo(dataId, group, namespaceId, nid);
    }
    
    /**
     * 分页查询配置变更历史列表。
     * Query the list of configuration history.
     *
     * @param dataId      配置 dataId
     * @param group       配置 group
     * @param namespaceId 命名空间 ID
     * @param pageNo      页码
     * @param pageSize    每页条数
     * @return 历史分页列表
     * @throws NacosException 操作失败时抛出
     */
    public Page<ConfigHistoryBasicInfo> listConfigHistory(String dataId, String group,
        String namespaceId, Integer pageNo,
        Integer pageSize) throws NacosException {
        return historyHandler.listConfigHistory(dataId, group, namespaceId, pageNo, pageSize);
    }
    
    /**
     * 查询指定历史记录的前一版本详情。
     * Query the previous configuration history information.
     *
     * @param dataId      配置 dataId
     * @param group       配置 group
     * @param namespaceId 命名空间 ID
     * @param id          当前历史记录 ID
     * @return 前一版本历史详情
     * @throws NacosException 操作失败时抛出
     */
    public ConfigHistoryDetailInfo getPreviousConfigHistoryInfo(String dataId, String group,
        String namespaceId, Long id)
        throws NacosException {
        return historyHandler.getPreviousConfigHistoryInfo(dataId, group, namespaceId, id);
    }
    
    /**
     * 按命名空间查询全部配置列表。
     * Query the list of configurations by namespace.
     *
     * @param namespaceId 命名空间 ID
     * @return 配置列表
     * @throws NacosException 操作失败时抛出
     */
    public List<ConfigBasicInfo> getConfigsByTenant(String namespaceId) throws NacosException {
        return historyHandler.getConfigsByTenant(namespaceId);
    }
}
