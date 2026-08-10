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

package com.alibaba.nacos.plugin.datasource.mapper;

import com.alibaba.nacos.plugin.datasource.constants.TableConstant;
import com.alibaba.nacos.plugin.datasource.model.MapperContext;
import com.alibaba.nacos.plugin.datasource.model.MapperResult;

/**
 * 租户容量 Mapper 接口。
 *
 * <p>管理 {@code tenant_capacity} 表的配额与用量，
 * 支持按租户查询、用量增减、配额修正及初始化插入。</p>
 *
 * @author KiteSoar
 **/
public interface TenantCapacityMapper extends Mapper {
    
    /**
     * 按租户 ID 查询 tenant_capacity 记录。
     *
     * @param context SQL 参数映射
     * @return 查询 SQL 及参数
     */
    MapperResult select(MapperContext context);
    
    /**
     * 在默认配额限制下递增用量。
     *
     * @param context SQL 参数映射
     * @return 递增 SQL 及参数
     */
    MapperResult incrementUsageWithDefaultQuotaLimit(MapperContext context);
    
    /**
     * 在自定义配额限制下递增用量。
     *
     * @param context SQL 参数映射
     * @return 递增 SQL 及参数
     */
    MapperResult incrementUsageWithQuotaLimit(MapperContext context);
    
    /**
     * 无条件递增用量。
     *
     * @param context SQL 参数映射
     * @return 递增 SQL 及参数
     */
    MapperResult incrementUsage(MapperContext context);
    
    /**
     * 递减用量。
     *
     * @param context SQL 参数映射
     * @return 递减 SQL 及参数
     */
    MapperResult decrementUsage(MapperContext context);
    
    /**
     * 修正用量至实际值。
     *
     * @param context SQL 参数映射
     * @return 修正 SQL 及参数
     */
    MapperResult correctUsage(MapperContext context);
    
    /**
     * 获取租户容量列表，仅含 id 与 tenantId 字段，供用量修正任务使用。
     *
     * @param context SQL 参数映射
     * @return 容量列表查询 SQL 及参数
     */
    MapperResult getCapacityList4CorrectUsage(MapperContext context);
    
    /**
     * 插入租户容量记录。
     *
     * @param context SQL 参数映射
     * @return 插入 SQL 及参数
     */
    MapperResult insertTenantCapacity(MapperContext context);
    
    /**
     * 获取表名。
     *
     * @return 表名
     */
    default String getTableName() {
        return TableConstant.TENANT_CAPACITY;
    }
}
