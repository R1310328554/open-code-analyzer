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
 */

package com.alibaba.nacos.config.server.service.repository;

import com.alibaba.nacos.config.server.model.ConfigInfo;
import com.alibaba.nacos.config.server.model.ConfigInfoGrayWrapper;
import com.alibaba.nacos.persistence.repository.PaginationHelper;

import java.util.List;

/**
 * 配置迁移持久化服务接口：在跨租户/跨集群迁移场景下，统计冲突、分页拉取待迁移配置并批量写入目标租户。
 * The interface Config migrate persist service.
 *
 * @author Sunrisea
 */
public interface ConfigMigratePersistService {
    
    /**
     * 创建分页查询助手，供迁移任务分批扫描 migrate_config 表。
     * Create pagination helper pagination helper.
     *
     * @param <E> the type parameter
     * @return the pagination helper
     */
    <E> PaginationHelper<E> createPaginationHelper();
    
    /**
     * 统计正式配置迁移中与目标租户冲突的记录数。
     * Config info conflict count integer.
     *
     * @param srcUser the src user
     * @return the integer
     */
    Integer configInfoConflictCount(String srcUser);
    
    /**
     * 统计灰度配置迁移冲突记录数。
     * Config info gray conflict count integer.
     *
     * @param srcUser the src user
     * @return the integer
     */
    Integer configInfoGrayConflictCount(String srcUser);
    
    /**
     * 分页获取待插入的正式配置迁移 ID 列表。
     * Gets migrate config id list.
     *
     * @param startId  the start id
     * @param pageSize the page size
     * @return the migrate config id list
     */
    List<Long> getMigrateConfigInsertIdList(long startId, int pageSize);
    
    /**
     * 分页获取待插入的灰度配置迁移 ID 列表。
     * Gets migrate config gray id list.
     *
     * @param startId  the start id
     * @param pageSize the page size
     * @return the migrate config gray id list
     */
    List<Long> getMigrateConfigGrayInsertIdList(long startId, int pageSize);
    
    /**
     * 分页拉取需更新租户信息的正式配置列表。
     *
     * @param startId      the start id
     * @param pageSize     the page size
     * @param srcTenant    the src tenant
     * @param targetTenant the target tenant
     * @param srcUser      the src user
     * @return the migrate config update list
     */
    List<ConfigInfo> getMigrateConfigUpdateList(long startId, int pageSize, String srcTenant,
        String targetTenant,
        String srcUser);
    
    /**
     * 分页拉取需更新租户信息的灰度配置列表。
     *
     * @param startId      the start id
     * @param pageSize     the page size
     * @param srcTenant    the src tenant
     * @param targetTenant the target tenant
     * @param srcUser      the src user
     * @return the migrate config gray update list
     */
    List<ConfigInfoGrayWrapper> getMigrateConfigGrayUpdateList(long startId, int pageSize,
        String srcTenant,
        String targetTenant, String srcUser);
    
    /**
     * 按 ID 批量执行正式配置迁移插入。
     *
     * @param ids     the ids
     * @param srcUser the src user
     */
    void migrateConfigInsertByIds(List<Long> ids, String srcUser);
    
    /**
     * 按 ID 批量执行灰度配置迁移插入。
     *
     * @param ids     the ids
     * @param srcUser the src user
     */
    void migrateConfigGrayInsertByIds(List<Long> ids, String srcUser);
    
    /**
     * 同步单条灰度配置到目标租户。
     *
     * @param dataId       the data id
     * @param group        the group
     * @param tenant       the tenant
     * @param grayName     the gray name
     * @param targetTenant the target tenant
     * @param srcUser      the src user
     */
    void syncConfigGray(String dataId, String group, String tenant, String grayName,
        String targetTenant,
        String srcUser);
    
    /**
     * 同步单条正式配置到目标租户。
     *
     * @param dataId       the data id
     * @param group        the group
     * @param tenant       the tenant
     * @param targetTenant the target tenant
     * @param srcUser      the src user
     */
    void syncConfig(String dataId, String group, String tenant, String targetTenant,
        String srcUser);
}
