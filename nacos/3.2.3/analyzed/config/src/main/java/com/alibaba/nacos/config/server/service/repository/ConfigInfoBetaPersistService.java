/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.config.server.service.repository;

import com.alibaba.nacos.config.server.model.ConfigInfo;
import com.alibaba.nacos.config.server.model.ConfigInfo4Beta;
import com.alibaba.nacos.config.server.model.ConfigInfoBetaWrapper;
import com.alibaba.nacos.config.server.model.ConfigInfoStateWrapper;
import com.alibaba.nacos.config.server.model.ConfigInfoWrapper;
import com.alibaba.nacos.config.server.model.ConfigOperateResult;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.persistence.repository.PaginationHelper;

/**
 * Beta 配置持久化服务接口：访问数据库 {@code config_info_beta} 表（2.5.0 起已废弃，由 {@link ConfigInfoGrayPersistService} 替代，仅保留兼容）。
 * Database service, providing access to config_info_beta in the database.
 * Deprecated since 2.5.0，only support on compatibility,replaced with ConfigInfoGray model, will be  soon removed on further version.
 * @author lixiaoshuang
 */
@Deprecated
public interface ConfigInfoBetaPersistService {
    
    /**
     * create Pagination utils.
     *
     * @param <E> Generic object
     * @return {@link PaginationHelper}
      * <p>Beta 配置持久化接口方法；详见上方说明。</p>
     */
    <E> PaginationHelper<E> createPaginationHelper();
    
    //------------------------------------------insert 插入---------------------------------------------//
    
    /**
     * get config info beta.
     *
     * @param dataId dataId.
     * @param group  group.
     * @param tenant tenant.
     * @return config info state.
      * <p>Beta 配置持久化接口方法；详见上方说明。</p>
     */
    ConfigInfoStateWrapper findConfigInfo4BetaState(final String dataId, final String group,
        final String tenant);
    
    /**
     * Add beta configuration information and publish data change events.
     *
     * @param configInfo config info
     * @param betaIps    ip for push
     * @param srcIp      remote ip
     * @param srcUser    user
     * @return config operation result.
      * <p>Beta 配置持久化接口方法；详见上方说明。</p>
     */
    ConfigOperateResult addConfigInfo4Beta(ConfigInfo configInfo, String betaIps, String srcIp,
        String srcUser);
    
    /**
     * insert or update beta config.
     *
     * @param configInfo config info
     * @param betaIps    ip for push
     * @param srcIp      remote ip
     * @param srcUser    user
     * @return config operation result.
      * <p>Beta 配置持久化接口方法；详见上方说明。</p>
     */
    ConfigOperateResult insertOrUpdateBeta(final ConfigInfo configInfo, final String betaIps,
        final String srcIp,
        final String srcUser);
    
    /**
     * insert or update beta config cas.
     *
     * @param configInfo config info
     * @param betaIps    ip for push
     * @param srcIp      remote ip
     * @param srcUser    user
     * @return success or not.
      * <p>Beta 配置持久化接口方法；详见上方说明。</p>
     */
    ConfigOperateResult insertOrUpdateBetaCas(final ConfigInfo configInfo, final String betaIps,
        final String srcIp,
        final String srcUser);
    
    //------------------------------------------delete 删除---------------------------------------------//
    
    /**
     * Delete configuration information, physical deletion.
     *
     * @param dataId data id
     * @param group  group
     * @param tenant tenant
      * <p>Beta 配置持久化接口方法；详见上方说明。</p>
     */
    void removeConfigInfo4Beta(final String dataId, final String group, final String tenant);
    
    //------------------------------------------update 更新---------------------------------------------//
    
    /**
     * Update beta configuration information.
     *
     * @param configInfo config info
     * @param betaIps    ip for push
     * @param srcIp      remote ip
     * @param srcUser    user
     * @return config operation result.
      * <p>Beta 配置持久化接口方法；详见上方说明。</p>
     */
    ConfigOperateResult updateConfigInfo4Beta(ConfigInfo configInfo, String betaIps, String srcIp,
        String srcUser);
    
    /**
     * Update beta configuration information.
     *
     * @param configInfo config info
     * @param betaIps    ip for push
     * @param srcIp      remote ip
     * @param srcUser    user
     * @return success or not.
      * <p>Beta 配置持久化接口方法；详见上方说明。</p>
     */
    ConfigOperateResult updateConfigInfo4BetaCas(ConfigInfo configInfo, String betaIps,
        String srcIp, String srcUser);
    
    //------------------------------------------select 查询---------------------------------------------//
    
    /**
     * Query beta configuration information based on dataId and group.
     *
     * @param dataId data id
     * @param group  group
     * @param tenant tenant
     * @return {@link ConfigInfo4Beta}
      * <p>Beta 配置持久化接口方法；详见上方说明。</p>
     */
    ConfigInfoBetaWrapper findConfigInfo4Beta(final String dataId, final String group,
        final String tenant);
    
    /**
     * Returns the number of beta configuration items.
     *
     * @return number of configuration items..
      * <p>Beta 配置持久化接口方法；详见上方说明。</p>
     */
    int configInfoBetaCount();
    
    /**
     * Query all beta config info for dump task.
     *
     * @param pageNo   page number
     * @param pageSize page size
     * @return {@link Page} with {@link ConfigInfoWrapper} generation
      * <p>Beta 配置持久化接口方法；详见上方说明。</p>
     */
    Page<ConfigInfoBetaWrapper> findAllConfigInfoBetaForDumpAll(final int pageNo,
        final int pageSize);
    
}
