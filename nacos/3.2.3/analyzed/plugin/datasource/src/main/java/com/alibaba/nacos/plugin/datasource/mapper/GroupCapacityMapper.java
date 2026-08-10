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
 * 分组容量 Mapper 接口。
 *
 * <p>管理 {@code group_capacity} 表的配额与用量，支持按分组查询、
 * 插入初始化及用量增减修正等操作。</p>
 *
 * @author lixiaoshuang
 */
public interface GroupCapacityMapper extends Mapper {
    
    /**
     * 按分组 ID 查询 group_capacity 记录。
     *
     * @param context SQL 参数映射
     * @return 查询 SQL 及参数
     */
    MapperResult select(MapperContext context);
    
    /**
     * INSERT INTO…SELECT 语句，初始化分组容量。
     *
     * @param context SQL 参数映射
     * @return 插入 SQL 及参数
     */
    MapperResult insertIntoSelect(MapperContext context);
    
    /**
     * 带 WHERE 条件的 INSERT INTO…SELECT，将查询结果写入容量表。
     *
     * @param context SQL 参数映射
     * @return 条件插入 SQL 及参数
     */
    MapperResult insertIntoSelectByWhere(MapperContext context);
    
    /**
     * 在配额为零时递增 usage 字段。
     *
     * @param context SQL 参数映射
     * @return 递增 SQL 及参数
     */
    MapperResult incrementUsageByWhereQuotaEqualZero(MapperContext context);
    
    /**
     * 在配额非零时递增 usage 字段。
     *
     * @param context SQL 参数映射
     * @return 递增 SQL 及参数
     */
    MapperResult incrementUsageByWhereQuotaNotEqualZero(MapperContext context);
    
    /**
     * 按条件递增 usage 字段。
     *
     * @param context SQL 参数映射
     * @return 递增 SQL 及参数
     */
    MapperResult incrementUsageByWhere(MapperContext context);
    
    /**
     * 按条件递减 usage 字段。
     *
     * @param context SQL 参数映射
     * @return 递减 SQL 及参数
     */
    MapperResult decrementUsageByWhere(MapperContext context);
    
    /**
     * 直接更新 usage 字段。
     *
     * @param context SQL 参数映射
     * @return 更新 SQL 及参数
     */
    MapperResult updateUsage(MapperContext context);
    
    /**
     * 按 WHERE 条件更新 usage 字段。
     *
     * @param context SQL 参数映射
     * @return 条件更新 SQL 及参数
     */
    MapperResult updateUsageByWhere(MapperContext context);
    
    /**
     * 按分组规模查询分组信息。
     *
     * @param context SQL 参数映射
     * @return 分组信息查询 SQL 及参数
     */
    MapperResult selectGroupInfoBySize(MapperContext context);
    
    /**
     * 返回表名。
     *
     * @return 表名
     */
    default String getTableName() {
        return TableConstant.GROUP_CAPACITY;
    }
}
