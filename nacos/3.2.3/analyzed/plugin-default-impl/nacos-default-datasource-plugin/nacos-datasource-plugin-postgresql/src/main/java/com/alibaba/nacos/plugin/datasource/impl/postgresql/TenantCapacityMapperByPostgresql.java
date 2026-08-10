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

package com.alibaba.nacos.plugin.datasource.impl.postgresql;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.plugin.datasource.constants.DatabaseTypeConstant;
import com.alibaba.nacos.plugin.datasource.constants.FieldConstant;
import com.alibaba.nacos.plugin.datasource.impl.base.BaseTenantCapacityMapper;
import com.alibaba.nacos.plugin.datasource.model.MapperContext;
import com.alibaba.nacos.plugin.datasource.model.MapperResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@link com.alibaba.nacos.plugin.datasource.mapper.TenantCapacityMapper} 的 PostgreSQL 实现。
 *
 * <p>租户级配置容量配额管理：用量增减、校正及从 config_info 初始化容量行。PostgreSQL 中 {@code usage} 无需反引号转义。</p>
 *
 * @author Long Yu
 **/
public class TenantCapacityMapperByPostgresql extends BaseTenantCapacityMapper {
    
    /** 返回 PostgreSQL 数据源标识。 */
    @Override
    public String getDataSource() {
        return DatabaseTypeConstant.POSTGRESQL;
    }
    
    /** 按 tenant_id 查询容量配额与当前用量。 */
    @Override
    public MapperResult select(MapperContext context) {
        String sql =
            "SELECT id, quota, usage, max_size, max_aggr_count, max_aggr_size, tenant_id FROM tenant_capacity "
                + "WHERE tenant_id = ?";
        return new MapperResult(sql,
            Collections.singletonList(context.getWhereParameter(FieldConstant.TENANT_ID)));
    }
    
    /** 配额为零时按 max_size 上限递增租户用量。 */
    @Override
    public MapperResult incrementUsageWithDefaultQuotaLimit(MapperContext context) {
        return new MapperResult(
            "UPDATE tenant_capacity SET usage = usage + 1, gmt_modified = ? WHERE tenant_id = ? AND usage <"
                + " ? AND quota = 0",
            CollectionUtils.list(context.getUpdateParameter(FieldConstant.GMT_MODIFIED),
                context.getWhereParameter(FieldConstant.TENANT_ID),
                context.getWhereParameter(FieldConstant.USAGE)));
    }
    
    /** 配额非零且未超限时将租户用量 +1。 */
    @Override
    public MapperResult incrementUsageWithQuotaLimit(MapperContext context) {
        return new MapperResult(
            "UPDATE tenant_capacity SET usage = usage + 1, gmt_modified = ? WHERE tenant_id = ? AND usage < "
                + "quota AND quota != 0",
            CollectionUtils.list(context.getUpdateParameter(FieldConstant.GMT_MODIFIED),
                context.getWhereParameter(FieldConstant.TENANT_ID)));
    }
    
    /** 无条件将指定租户用量 +1。 */
    @Override
    public MapperResult incrementUsage(MapperContext context) {
        return new MapperResult(
            "UPDATE tenant_capacity SET usage = usage + 1, gmt_modified = ? WHERE tenant_id = ?",
            CollectionUtils.list(context.getUpdateParameter(FieldConstant.GMT_MODIFIED),
                context.getWhereParameter(FieldConstant.TENANT_ID)));
    }
    
    /** 用量大于零时将租户用量 -1。 */
    @Override
    public MapperResult decrementUsage(MapperContext context) {
        return new MapperResult(
            "UPDATE tenant_capacity SET usage = usage - 1, gmt_modified = ? WHERE tenant_id = ? AND usage > 0",
            CollectionUtils.list(context.getUpdateParameter(FieldConstant.GMT_MODIFIED),
                context.getWhereParameter(FieldConstant.TENANT_ID)));
    }
    
    /** 按 config_info 实际计数校正租户用量。 */
    @Override
    public MapperResult correctUsage(MapperContext context) {
        return new MapperResult(
            "UPDATE tenant_capacity SET usage = (SELECT count(*) FROM config_info WHERE tenant_id = ?), "
                + "gmt_modified = ? WHERE tenant_id = ?",
            CollectionUtils.list(context.getWhereParameter(FieldConstant.TENANT_ID),
                context.getUpdateParameter(FieldConstant.GMT_MODIFIED),
                context.getWhereParameter(FieldConstant.TENANT_ID)));
    }
    
    /** 从 config_info 统计后 INSERT SELECT 初始化租户容量行。 */
    @Override
    public MapperResult insertTenantCapacity(MapperContext context) {
        List<Object> paramList = new ArrayList<>();
        paramList.add(context.getUpdateParameter(FieldConstant.TENANT_ID));
        paramList.add(context.getUpdateParameter(FieldConstant.QUOTA));
        paramList.add(context.getUpdateParameter(FieldConstant.MAX_SIZE));
        paramList.add(context.getUpdateParameter(FieldConstant.MAX_AGGR_COUNT));
        paramList.add(context.getUpdateParameter(FieldConstant.MAX_AGGR_SIZE));
        paramList.add(context.getUpdateParameter(FieldConstant.GMT_CREATE));
        paramList.add(context.getUpdateParameter(FieldConstant.GMT_MODIFIED));
        paramList.add(context.getWhereParameter(FieldConstant.TENANT_ID));
        
        return new MapperResult(
            "INSERT INTO tenant_capacity (tenant_id, quota, usage, max_size, max_aggr_count, max_aggr_size, "
                + "max_history_count, gmt_create, gmt_modified)"
                + " SELECT ?, ?, count(*), ?, ?, ?, 0, ?, ? FROM config_info WHERE tenant_id=?",
            paramList);
    }
}
