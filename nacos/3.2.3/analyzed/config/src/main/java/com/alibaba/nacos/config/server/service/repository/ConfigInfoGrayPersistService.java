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
import com.alibaba.nacos.config.server.model.ConfigInfoGrayWrapper;
import com.alibaba.nacos.config.server.model.ConfigInfoStateWrapper;
import com.alibaba.nacos.config.server.model.ConfigOperateResult;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.persistence.repository.PaginationHelper;

import java.sql.Timestamp;
import java.util.List;

/**
 * 灰度配置持久化服务接口：访问数据库 {@code config_info_gray} 表，提供灰度配置的增删改查与 dump 分页查询。
 * Database service, providing access to config_info_gray in the database.
 *
 * @author rong
 */
public interface ConfigInfoGrayPersistService {
    
    /**
     * create Pagination utils.
     *
     * @param <E> Generic object
     * @return {@link PaginationHelper}
      * <p>灰度配置持久化接口方法；详见上方说明。</p>
     */
    <E> PaginationHelper<E> createPaginationHelper();
    
    //------------------------------------------insert 插入---------------------------------------------//
    
    /**
     * get gray config info state.
     *
     * @param dataId   dataId.
     * @param group    group.
     * @param tenant   tenant.
     * @param grayName gray name.
     * @return config info state.
      * <p>灰度配置持久化接口方法；详见上方说明。</p>
     */
    ConfigInfoStateWrapper findConfigInfo4GrayState(final String dataId, final String group,
        final String tenant,
        String grayName);
    
    /**
     * Add gray configuration information and publish data change events.
     *
     * @param configInfo        config info
     * @param grayName          gray name
     * @param grayRule          gray rule
     * @param srcIp             remote ip
     * @param srcUser           user
     * @return config operation result.
      * <p>灰度配置持久化接口方法；详见上方说明。</p>
     */
    ConfigOperateResult addConfigInfo4Gray(ConfigInfo configInfo, String grayName, String grayRule,
        String srcIp, String srcUser);
    
    /**
     * Adds configuration information with database atomic operations, minimizing SQL actions and avoiding business
     * encapsulation.
     *
     * @param configGrayId the ID for the gray configuration
     * @param configInfo   the configuration information to be added
     * @param grayName     the name of the gray configuration
     * @param grayRule     the rule of the gray configuration
     * @param srcIp        the IP address of the source
     * @param srcUser      the user who performs the addition
      * <p>灰度配置持久化接口方法；详见上方说明。</p>
     */
    void addConfigInfoGrayAtomic(final long configGrayId, final ConfigInfo configInfo,
        final String grayName, final String grayRule,
        final String srcIp, final String srcUser);
    
    /**
     * insert or update gray config.
     *
     * @param configInfo        config info
     * @param grayName          gray name
     * @param grayRule          gray rule
     * @param srcIp             remote ip
     * @param srcUser           user
     * @return config operation result.
      * <p>灰度配置持久化接口方法；详见上方说明。</p>
     */
    ConfigOperateResult insertOrUpdateGray(final ConfigInfo configInfo, final String grayName,
        final String grayRule,
        final String srcIp, final String srcUser);
    
    /**
     * insert or update gray config cas.
     *
     * @param configInfo config info.
     * @param grayName   gray name
     * @param grayRule   gray rule
     * @param srcIp      remote ip.
     * @param srcUser    user.
     * @return config operation result.
      * <p>灰度配置持久化接口方法；详见上方说明。</p>
     */
    ConfigOperateResult insertOrUpdateGrayCas(final ConfigInfo configInfo, final String grayName,
        final String grayRule,
        final String srcIp, final String srcUser);
    //------------------------------------------delete 删除---------------------------------------------//
    
    /**
     * Delete configuration; database atomic operation, minimum SQL action, no business encapsulation.
     *
     * @param dataId   dataId
     * @param group    group
     * @param tenant   tenant
     * @param grayName gray name
     * @param srcIp    remote ip
     * @param srcUser  user
      * <p>灰度配置持久化接口方法；详见上方说明。</p>
     */
    void removeConfigInfoGray(final String dataId, final String group, final String tenant,
        final String grayName,
        final String srcIp, final String srcUser);
    //------------------------------------------update 更新---------------------------------------------//
    
    /**
     * Update gray configuration information.
     *
     * @param configInfo config info
     * @param grayName   gray name
     * @param grayRule   gray rule
     * @param srcIp      remote ip
     * @param srcUser    user
     * @return config operation result.
      * <p>灰度配置持久化接口方法；详见上方说明。</p>
     */
    ConfigOperateResult updateConfigInfo4Gray(ConfigInfo configInfo, String grayName,
        String grayRule,
        String srcIp, String srcUser);
    
    /**
     * Update gray configuration information.
     *
     * @param configInfo config info
     * @param grayName   gray name
     * @param grayRule   gray rule
     * @param srcIp      remote ip
     * @param srcUser    user
     * @return success or not.
      * <p>灰度配置持久化接口方法；详见上方说明。</p>
     */
    ConfigOperateResult updateConfigInfo4GrayCas(ConfigInfo configInfo, String grayName,
        String grayRule,
        String srcIp, String srcUser);
    //------------------------------------------select 查询---------------------------------------------//
    
    /**
     * Query gray configuration information based on dataId and group.
     *
     * @param dataId   data id
     * @param group    group
     * @param tenant   tenant
     * @param grayName gray name
     * @return ConfigInfoGrayWrapper gray model instance.
      * <p>灰度配置持久化接口方法；详见上方说明。</p>
     */
    ConfigInfoGrayWrapper findConfigInfo4Gray(final String dataId, final String group,
        final String tenant,
        final String grayName);
    
    /**
     * Returns the number of gray configuration items.
     *
     * @return number of configuration items.
      * <p>灰度配置持久化接口方法；详见上方说明。</p>
     */
    int configInfoGrayCount();
    
    /**
     * Query all gray config info for dump task.
     *
     * @param pageNo   page numbser
     * @param pageSize page sizxe
     * @return {@link Page} with {@link ConfigInfoGrayWrapper} generation
      * <p>灰度配置持久化接口方法；详见上方说明。</p>
     */
    Page<ConfigInfoGrayWrapper> findAllConfigInfoGrayForDumpAll(final int pageNo,
        final int pageSize);
    
    /**
     * Query all gray config info for dump task.
     *
     * @param startTime startTime
     * @param lastMaxId lastMaxId
     * @param pageSize  pageSize
     * @return {@link Page} with {@link ConfigInfoGrayWrapper} generation
      * <p>灰度配置持久化接口方法；详见上方说明。</p>
     */
    List<ConfigInfoGrayWrapper> findChangeConfig(final Timestamp startTime, long lastMaxId,
        final int pageSize);
    
    /**
     * found all config grays.
     *
     * @param dataId dataId.
     * @param group  group.
     * @param tenant tenant.
     * @return
      * <p>灰度配置持久化接口方法；详见上方说明。</p>
     */
    List<String> findConfigInfoGrays(final String dataId, final String group, final String tenant);
}
