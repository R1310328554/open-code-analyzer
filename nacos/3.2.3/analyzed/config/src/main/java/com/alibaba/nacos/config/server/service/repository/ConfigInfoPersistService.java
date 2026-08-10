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

import com.alibaba.nacos.api.config.model.SameConfigPolicy;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.config.server.model.ConfigAdvanceInfo;
import com.alibaba.nacos.config.server.model.ConfigAllInfo;
import com.alibaba.nacos.config.server.model.ConfigInfo;
import com.alibaba.nacos.config.server.model.ConfigInfoBase;
import com.alibaba.nacos.config.server.model.ConfigInfoStateWrapper;
import com.alibaba.nacos.config.server.model.ConfigInfoWrapper;
import com.alibaba.nacos.config.server.model.ConfigOperateResult;
import com.alibaba.nacos.persistence.repository.PaginationHelper;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * 正式配置持久化服务接口：访问数据库 {@code config_info} 主表，提供配置的 CRUD、批量导入、分页查询与变更追踪。
 * Database service, providing access to config_info in the database.
 *
 * @author lixiaoshuang
 */
public interface ConfigInfoPersistService {
    
    /** 模糊查询通配符 */
    String PATTERN_STR = "*";
    /** 空参数数组常量 */
    Object[] EMPTY_ARRAY = new Object[] {};
    
    /**
     * create Pagination utils.
     *
     * @param <E> Generic object
     * @return {@link PaginationHelper}
      * <p>正式配置持久化接口方法；详见上方说明。</p>
     */
    <E> PaginationHelper<E> createPaginationHelper();
    
    /**
     * Generate fuzzy search Sql.
     *
     * @param s origin string
     * @return fuzzy search Sql
      * <p>正式配置持久化接口方法；详见上方说明。</p>
     */
    String generateLikeArgument(String s);
    
    //------------------------------------------insert 插入---------------------------------------------//
    
    /**
     * Add common configuration information and publish data change events.
     *
     * @param srcIp             remote ip
     * @param srcUser           user
     * @param configInfo        config info
     * @param configAdvanceInfo advance info
     * @return config operation result.
      * <p>正式配置持久化接口方法；详见上方说明。</p>
     */
    ConfigOperateResult addConfigInfo(final String srcIp, final String srcUser,
        final ConfigInfo configInfo,
        final Map<String, Object> configAdvanceInfo);
    
    /**
     * Update config info metadata config operate result.
     *
     * @param dataId      the data id
     * @param group       the group
     * @param tenant      the tenant
     * @param configTags  the config tags
     * @param description the description
     * @return the config operate result
     * @throws NacosException the nacos exception
      * <p>正式配置持久化接口方法；详见上方说明。</p>
     */
    ConfigOperateResult updateConfigInfoMetadata(final String dataId, final String group,
        final String tenant,
        final String configTags, final String description) throws NacosException;
    
    /**
     * insert or update.
     *
     * @param srcIp             remote ip
     * @param srcUser           user
     * @param configInfo        config info
     * @param configAdvanceInfo advance info
     * @return config operation result.
      * <p>正式配置持久化接口方法；详见上方说明。</p>
     */
    ConfigOperateResult insertOrUpdate(String srcIp, String srcUser, ConfigInfo configInfo,
        Map<String, Object> configAdvanceInfo);
    
    /**
     * Write to the main table, insert or update cas.
     *
     * @param srcIp             remote ip
     * @param srcUser           user
     * @param configInfo        config info
     * @param configAdvanceInfo advance info
     * @return success or not.
      * <p>正式配置持久化接口方法；详见上方说明。</p>
     */
    ConfigOperateResult insertOrUpdateCas(String srcIp, String srcUser, ConfigInfo configInfo,
        Map<String, Object> configAdvanceInfo);
    
    /**
     * Add configuration; database atomic operation, minimum sql action, no business encapsulation.
     *
     * @param id                id
     * @param srcIp             ip
     * @param srcUser           user
     * @param configInfo        info
     * @param configAdvanceInfo advance info
     * @return execute sql result
      * <p>正式配置持久化接口方法；详见上方说明。</p>
     */
    long addConfigInfoAtomic(final long id, final String srcIp, final String srcUser,
        final ConfigInfo configInfo,
        Map<String, Object> configAdvanceInfo);
    
    /**
     * Add configuration; database atomic operation, minimum sql action, no business encapsulation.
     *
     * @param configId id
     * @param tagName  tag
     * @param dataId   data id
     * @param group    group
     * @param tenant   tenant
      * <p>正式配置持久化接口方法；详见上方说明。</p>
     */
    void addConfigTagRelationAtomic(long configId, String tagName, String dataId, String group,
        String tenant);
    
    /**
     * Add configuration; database atomic operation.
     *
     * @param configId   config id
     * @param configTags tags
     * @param dataId     dataId
     * @param group      group
     * @param tenant     tenant
      * <p>正式配置持久化接口方法；详见上方说明。</p>
     */
    void addConfigTagsRelation(long configId, String configTags, String dataId, String group,
        String tenant);
    
    /**
     * batch operation,insert or update the format of the returned: succCount: number of successful imports skipCount:
     * number of import skips (only with skip for the same configs) failData: import failed data (only with abort for
     * the same configs) skipData: data skipped at import  (only with skip for the same configs).
     *
     * @param configInfoList    config info list
     * @param srcUser           user
     * @param srcIp             remote ip
     * @param configAdvanceInfo advance info
     * @param policy            {@link SameConfigPolicy}
     * @return map containing the number of affected rows
     * @throws NacosException nacos exception
      * <p>正式配置持久化接口方法；详见上方说明。</p>
     */
    Map<String, Object> batchInsertOrUpdate(List<ConfigAllInfo> configInfoList, String srcUser,
        String srcIp,
        Map<String, Object> configAdvanceInfo, SameConfigPolicy policy) throws NacosException;
    
    //------------------------------------------delete 删除---------------------------------------------//
    
    /**
     * Delete configuration information, physical deletion.
     *
     * @param dataId  data id
     * @param group   group
     * @param tenant  tenant
     * @param srcIp   remote ip
     * @param srcUser user
      * <p>正式配置持久化接口方法；详见上方说明。</p>
     */
    void removeConfigInfo(final String dataId, final String group, final String tenant,
        final String srcIp,
        final String srcUser);
    
    /**
     * Delete config info by ids.
     *
     * @param ids     id list
     * @param srcIp   remote ip
     * @param srcUser user
     * @return {@link ConfigAllInfo} list
     * @author klw
      * <p>正式配置持久化接口方法；详见上方说明。</p>
     */
    @Deprecated
    List<ConfigAllInfo> removeConfigInfoByIds(final List<Long> ids, final String srcIp,
        final String srcUser);
    
    /**
     * Delete tag.
     *
     * @param id id
      * <p>正式配置持久化接口方法；详见上方说明。</p>
     */
    void removeTagByIdAtomic(long id);
    
    /**
     * Remove configuration; database atomic operation, minimum SQL action, no business encapsulation.
     *
     * @param dataId  dataId
     * @param group   group
     * @param tenant  tenant
     * @param srcIp   ip
     * @param srcUser user
      * <p>正式配置持久化接口方法；详见上方说明。</p>
     */
    void removeConfigInfoAtomic(final String dataId, final String group, final String tenant,
        final String srcIp,
        final String srcUser);
    
    /**
     * Remove configuration; database atomic operation, minimum SQL action, no business encapsulation.
     *
     * @param ids ids
      * <p>正式配置持久化接口方法；详见上方说明。</p>
     */
    void removeConfigInfoByIdsAtomic(final String ids);
    
    //------------------------------------------update 更新---------------------------------------------//
    
    /**
     * Update common configuration information.
     *
     * @param configInfo        config info
     * @param srcIp             remote ip
     * @param srcUser           user
     * @param configAdvanceInfo advance info
     * @return config operation result.
      * <p>正式配置持久化接口方法；详见上方说明。</p>
     */
    ConfigOperateResult updateConfigInfo(final ConfigInfo configInfo, final String srcIp,
        final String srcUser,
        final Map<String, Object> configAdvanceInfo);
    
    /**
     * Update common configuration information.
     *
     * @param configInfo        config info
     * @param srcIp             remote ip
     * @param srcUser           user
     * @param configAdvanceInfo advance info
     * @return config operation result.
      * <p>正式配置持久化接口方法；详见上方说明。</p>
     */
    ConfigOperateResult updateConfigInfoCas(final ConfigInfo configInfo, final String srcIp,
        final String srcUser,
        final Map<String, Object> configAdvanceInfo);
    
    /**
     * Update configuration; database atomic operation, minimum SQL action, no business encapsulation.
     *
     * @param configInfo        config info
     * @param srcIp             remote ip
     * @param srcUser           user
     * @param configAdvanceInfo advance info
      * <p>正式配置持久化接口方法；详见上方说明。</p>
     */
    void updateConfigInfoAtomic(final ConfigInfo configInfo, final String srcIp,
        final String srcUser,
        Map<String, Object> configAdvanceInfo);
    
    //------------------------------------------select 查询---------------------------------------------//
    
    /**
     * Get the maxId.
     *
     * @return config max id
      * <p>正式配置持久化接口方法；详见上方说明。</p>
     */
    long findConfigMaxId();
    
    /**
     * Query configuration information by primary key ID.
     *
     * @param id id
     * @return {@link ConfigInfo}
      * <p>正式配置持久化接口方法；详见上方说明。</p>
     */
    ConfigInfo findConfigInfo(long id);
    
    /**
     * Query configuration information; database atomic operation, minimum SQL action, no business encapsulation.
     *
     * @param dataId dataId
     * @param group  group
     * @param tenant tenant
     * @return config info
      * <p>正式配置持久化接口方法；详见上方说明。</p>
     */
    ConfigInfoWrapper findConfigInfo(final String dataId, final String group, final String tenant);
    
    /**
     * find config info.
     *
     * @param pageNo            page number
     * @param pageSize          page size
     * @param dataId            data id
     * @param group             group
     * @param tenant            tenant
     * @param configAdvanceInfo advance info
     * @return {@link Page} with {@link ConfigInfo} generation
      * <p>正式配置持久化接口方法；详见上方说明。</p>
     */
    Page<ConfigInfo> findConfigInfo4Page(final int pageNo, final int pageSize, final String dataId,
        final String group,
        final String tenant, final Map<String, Object> configAdvanceInfo);
    
    /**
     * Returns the number of configuration items.
     *
     * @return number of configuration items.
      * <p>正式配置持久化接口方法；详见上方说明。</p>
     */
    int configInfoCount();
    
    /**
     * Returns the number of configuration items.
     *
     * @param tenant tenant
     * @return number of configuration items.
      * <p>正式配置持久化接口方法；详见上方说明。</p>
     */
    int configInfoCount(String tenant);
    
    /**
     * get tenant id list  by page.
     *
     * @param page     page number
     * @param pageSize page size
     * @return tenant id list
      * <p>正式配置持久化接口方法；详见上方说明。</p>
     */
    List<String> getTenantIdList(int page, int pageSize);
    
    /**
     * get group id list  by page.
     *
     * @param page     page number
     * @param pageSize page size
     * @return group id list
      * <p>正式配置持久化接口方法；详见上方说明。</p>
     */
    List<String> getGroupIdList(int page, int pageSize);
    
    /**
     * Query all config info.
     *
     * @param lastMaxId   last max id
     * @param pageSize    page size
     * @param needContent need content or not.
     * @return {@link Page} with {@link ConfigInfoWrapper} generation
      * <p>正式配置持久化接口方法；详见上方说明。</p>
     */
    Page<ConfigInfoWrapper> findAllConfigInfoFragment(final long lastMaxId, final int pageSize,
        boolean needContent);
    
    /**
     * Query config info.
     *
     * @param pageNo            page number
     * @param pageSize          page size
     * @param dataId            data id
     * @param group             group
     * @param tenant            tenant
     * @param configAdvanceInfo advance info
     * @return {@link Page} with {@link ConfigInfo} generation
      * <p>正式配置持久化接口方法；详见上方说明。</p>
     */
    Page<ConfigInfo> findConfigInfoLike4Page(final int pageNo, final int pageSize,
        final String dataId,
        final String group, final String tenant, final Map<String, Object> configAdvanceInfo);
    
    /**
     * Query change config.order by id asc.
     *
     * @param startTime start time
     * @param lastMaxId lastMaxId
     * @param pageSize  pageSize
     * @return {@link ConfigInfoWrapper} list
      * <p>正式配置持久化接口方法；详见上方说明。</p>
     */
    List<ConfigInfoStateWrapper> findChangeConfig(final Timestamp startTime, long lastMaxId,
        final int pageSize);
    
    /**
     * Query tag list.
     *
     * @param dataId data id
     * @param group  group
     * @param tenant tenant
     * @return tag list
      * <p>正式配置持久化接口方法；详见上方说明。</p>
     */
    List<String> selectTagByConfig(String dataId, String group, String tenant);
    
    /**
     * find ConfigInfo by ids.
     *
     * @param ids id list
     * @return {@link com.alibaba.nacos.config.server.model.ConfigInfo} list
     * @author klw
     * @date 2019/7/5 16:37
      * <p>正式配置持久化接口方法；详见上方说明。</p>
     */
    List<ConfigInfo> findConfigInfosByIds(final String ids);
    
    /**
     * Query configuration information; database atomic operation, minimum SQL action, no business encapsulation.
     *
     * @param dataId dataId
     * @param group  group
     * @param tenant tenant
     * @return advance info
      * <p>正式配置持久化接口方法；详见上方说明。</p>
     */
    ConfigAdvanceInfo findConfigAdvanceInfo(final String dataId, final String group,
        final String tenant);
    
    /**
     * Query configuration information; database atomic operation, minimum SQL action, no business encapsulation.
     *
     * @param dataId dataId
     * @param group  group
     * @param tenant tenant
     * @return advance info
      * <p>正式配置持久化接口方法；详见上方说明。</p>
     */
    ConfigAllInfo findConfigAllInfo(final String dataId, final String group, final String tenant);
    
    /**
     * get config info state.
     *
     * @param dataId dataId.
     * @param group  group.
     * @param tenant tenant.
     * @return config info state.
      * <p>正式配置持久化接口方法；详见上方说明。</p>
     */
    ConfigInfoStateWrapper findConfigInfoState(final String dataId, final String group,
        final String tenant);
    
    /**
     * query all configuration information according to group, appName, tenant (for export).
     *
     * @param dataId  data id
     * @param group   group
     * @param tenant  tenant
     * @param appName appName
     * @param ids     ids
     * @return Collection of ConfigInfo objects
      * <p>正式配置持久化接口方法；详见上方说明。</p>
     */
    List<ConfigAllInfo> findAllConfigInfo4Export(final String dataId, final String group,
        final String tenant,
        final String appName, final List<Long> ids);
    
    /**
     * Query dataId list by namespace.
     *
     * @param tenantId tenantId
     * @return {@link ConfigInfoBase}
      * <p>正式配置持久化接口方法；详见上方说明。</p>
     */
    List<ConfigInfoWrapper> queryConfigInfoByNamespace(final String tenantId);
    
}
