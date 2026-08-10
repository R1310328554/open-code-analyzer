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

package com.alibaba.nacos.core.namespace.repository;

import com.alibaba.nacos.core.namespace.model.TenantInfo;

import java.util.List;

/**
 * 命名空间（tenant_info 表）持久化服务接口：内嵌与外置存储各有一个 {@code @Conditional} 实现。
 * Database service, providing access to other table in the database.
 *
 * @author lixiaoshuang
 */
public interface NamespacePersistService {
    
    /** 模糊搜索通配符，{@link #generateLikeArgument(String)} 会将其转为 SQL {@code %}。 */
    String PATTERN_STR = "*";
    
    //------------------------------------------ 插入 ---------------------------------------------//
    
    /**
     * 原子插入一条 tenant_info 记录。
     *
     * @param kp             kp
     * @param tenantId       tenant Id
     * @param tenantName     tenant name
     * @param tenantDesc     tenant description
     * @param createResource create resource
     * @param time           time
     */
    void insertTenantInfoAtomic(String kp, String tenantId, String tenantName, String tenantDesc,
        String createResource,
        final long time);
    
    //------------------------------------------ 删除 ---------------------------------------------//
    
    /**
     * 原子删除指定 kp + tenantId 的 tenant_info 记录。
     *
     * @param kp       kp
     * @param tenantId tenant id
     */
    void removeTenantInfoAtomic(final String kp, final String tenantId);
    
    //------------------------------------------ 更新 ---------------------------------------------//
    
    /**
     * 原子更新命名空间名称与描述。
     *
     * @param kp         kp
     * @param tenantId   tenant Id
     * @param tenantName tenant name
     * @param tenantDesc tenant description
     */
    void updateTenantNameAtomic(String kp, String tenantId, String tenantName, String tenantDesc);
    
    //------------------------------------------ 查询 ---------------------------------------------//
    
    /**
     * 按 kp 查询该分区下全部命名空间。
     *
     * @param kp kp
     * @return {@link TenantInfo} list
     */
    List<TenantInfo> findTenantByKp(String kp);
    
    /**
     * 按 kp + tenantId 查询单个命名空间。
     *
     * @param kp       kp
     * @param tenantId tenant id
     * @return {@link TenantInfo}
     */
    TenantInfo findTenantByKp(String kp, String tenantId);
    
    /**
     * 将用户输入的模糊搜索串转为 SQL LIKE 参数。
     *
     * @param s origin string
     * @return fuzzy search Sql
     */
    String generateLikeArgument(String s);
    
    /**
     * 判断指定表是否存在于当前数据源。
     *
     * @param tableName table name
     * @return {@code true} if table exist
     */
    boolean isExistTable(String tableName);
    
    /**
     * 按 tenantId 统计记录数，用于判断命名空间是否已存在。
     *
     * @param tenantId tenant Id
     * @return count by tenantId
     */
    int tenantInfoCountByTenantId(String tenantId);
}
