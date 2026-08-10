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
import com.alibaba.nacos.plugin.datasource.impl.enums.postgresql.TrustedPostgresqlFunctionEnum;
import com.alibaba.nacos.plugin.datasource.mapper.AbstractMapper;
import com.alibaba.nacos.plugin.datasource.mapper.HistoryConfigInfoMapper;
import com.alibaba.nacos.plugin.datasource.model.MapperContext;
import com.alibaba.nacos.plugin.datasource.model.MapperResult;

/**
 * {@link HistoryConfigInfoMapper} 的 PostgreSQL 实现。
 *
 * <p>配置历史表 his_config_info 的过期清理与按 dataId/group/tenant 分页查询；删除语句使用 CTE 分批执行以适配 PostgreSQL 语法。</p>
 *
 * @author Long Yu
 **/
public class HistoryConfigInfoMapperByPostgresql extends AbstractMapper
    implements HistoryConfigInfoMapper {
    
    /** 通过 CTE 子查询分批删除早于指定时间的过期历史记录。 */
    @Override
    public MapperResult removeConfigHistory(MapperContext context) {
        String sql =
            "WITH temp_table as (SELECT id FROM his_config_info WHERE gmt_modified < ? LIMIT ? ) "
                + "DELETE FROM his_config_info WHERE id in (SELECT id FROM temp_table) ";
        return new MapperResult(sql,
            CollectionUtils.list(context.getWhereParameter(FieldConstant.START_TIME),
                context.getWhereParameter(FieldConstant.LIMIT_SIZE)));
    }
    
    /** 按 dataId/group/tenant 倒序分页查询配置变更历史（OFFSET/LIMIT 分页）。 */
    @Override
    public MapperResult pageFindConfigHistoryFetchRows(MapperContext context) {
        String sql =
            "SELECT nid,data_id,group_id,tenant_id,app_name,src_ip,src_user,op_type,ext_info,publish_type,gray_name,gmt_create,gmt_modified "
                + "FROM his_config_info WHERE data_id = ? AND group_id = ? AND tenant_id = ? ORDER BY nid DESC LIMIT "
                + context.getPageSize() + " OFFSET " + context.getStartRow();
        return new MapperResult(sql,
            CollectionUtils.list(context.getWhereParameter(FieldConstant.DATA_ID),
                context.getWhereParameter(FieldConstant.GROUP_ID),
                context.getWhereParameter(FieldConstant.TENANT_ID)));
    }
    
    /** 返回 PostgreSQL 数据源标识。 */
    @Override
    public String getDataSource() {
        return DatabaseTypeConstant.POSTGRESQL;
    }
    
    /** 按名称解析 PostgreSQL 可信 SQL 函数表达式。 */
    @Override
    public String getFunction(String functionName) {
        return TrustedPostgresqlFunctionEnum.getFunctionByName(functionName);
    }
    
}
