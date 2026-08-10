/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.maintainer.client.config;

import com.alibaba.nacos.api.annotation.Since;
import com.alibaba.nacos.api.config.model.ConfigHistoryBasicInfo;
import com.alibaba.nacos.api.config.model.ConfigHistoryDetailInfo;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.Page;

/**
 * 配置历史维护服务：分页查询历史列表、详情及上一版本。
 *
 * @author xiweng.yy
 */
public interface ConfigHistoryMaintainerService {
    
    /**
     * 分页查询配置变更历史列表。
     *
     * @param dataId      Configuration data ID (required).
     * @param groupName   Configuration group name (required).
     * @param namespaceId Namespace ID (optional, defaults to "public").
     * @param pageNo      Page number (required).
     * @param pageSize    Page size (required, max 500).
     * @return A paginated list of configuration history.
     * @throws NacosException If the query fails.
     */
    @Since("3.0.0")
    Page<ConfigHistoryBasicInfo> listConfigHistory(String dataId, String groupName,
        String namespaceId, int pageNo,
        int pageSize) throws NacosException;
    
    /**
     * 按历史记录 ID（nid）查询配置历史详情。
     *
     * @param dataId      Configuration data ID (required).
     * @param groupName   Configuration group name (required).
     * @param namespaceId Namespace ID (optional, defaults to "public").
     * @param nid         History record ID (required).
     * @return Detailed configuration history information.
     * @throws NacosException If the history record does not exist or the query fails.
     */
    @Since("3.0.0")
    ConfigHistoryDetailInfo getConfigHistoryInfo(String dataId, String groupName,
        String namespaceId, Long nid)
        throws NacosException;
    
    /**
     * 查询当前历史记录的前一版本详情。
     *
     * @param dataId      Configuration data ID (required).
     * @param groupName   Configuration group name (required).
     * @param namespaceId Namespace ID (optional, defaults to "public").
     * @param id          Current history record ID (required).
     * @return Previous configuration history information.
     * @throws NacosException If the previous history record does not exist or the query fails.
     */
    @Since("3.0.0")
    ConfigHistoryDetailInfo getPreviousConfigHistoryInfo(String dataId, String groupName,
        String namespaceId, Long id)
        throws NacosException;
}
