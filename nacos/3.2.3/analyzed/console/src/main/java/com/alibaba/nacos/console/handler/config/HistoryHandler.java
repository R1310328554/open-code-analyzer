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

package com.alibaba.nacos.console.handler.config;

import com.alibaba.nacos.api.config.model.ConfigBasicInfo;
import com.alibaba.nacos.api.config.model.ConfigHistoryBasicInfo;
import com.alibaba.nacos.api.config.model.ConfigHistoryDetailInfo;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.Page;

import java.util.List;

/**
 * 配置变更历史控制台处理器接口：按 nid 查询详情、分页列举及上一版本对比。
 * Interface for handling configuration history related operations.
 *
 * @author zhangyukun
 */
public interface HistoryHandler {
    
    /**
      * 查询指定 nid 的配置变更历史详情。
     * Query the detailed configuration history information.
     *
     * @param dataId      the ID of the data
     * @param group       the group ID
     * @param namespaceId the namespace ID
     * @param nid         the history record ID
     * @return the detailed configuration history information
     * @throws NacosException if any error occurs during the operation
     */
    ConfigHistoryDetailInfo getConfigHistoryInfo(String dataId, String group, String namespaceId,
        Long nid)
        throws NacosException;
    
    /**
      * 分页查询配置变更历史列表。
     * Query the list of configuration history.
     *
     * @param dataId      the ID of the data
     * @param group       the group ID
     * @param namespaceId the namespace ID
     * @param pageNo      页码
     * @param pageSize    the number of items per page
     * @return the paginated list of configuration history
     * @throws NacosException if any error occurs during the operation
     */
    Page<ConfigHistoryBasicInfo> listConfigHistory(String dataId, String group, String namespaceId,
        Integer pageNo,
        Integer pageSize) throws NacosException;
    
    /**
      * 查询上一版本配置历史详情。
     * Query the previous configuration history information.
     *
     * @param dataId      the ID of the data
     * @param group       the group ID
     * @param namespaceId the namespace ID
     * @param id          the configuration ID
     * @return the previous configuration history information
     * @throws NacosException if any error occurs during the operation
     */
    ConfigHistoryDetailInfo getPreviousConfigHistoryInfo(String dataId, String group,
        String namespaceId, Long id)
        throws NacosException;
    
    /**
      * 按命名空间列举全部配置摘要。
     * Query the list of configurations by namespace.
     *
     * @param namespaceId the namespace ID
     * @return the list of configurations
     * @throws NacosException if any error occurs during the operation
     */
    List<ConfigBasicInfo> getConfigsByTenant(String namespaceId) throws NacosException;
}
