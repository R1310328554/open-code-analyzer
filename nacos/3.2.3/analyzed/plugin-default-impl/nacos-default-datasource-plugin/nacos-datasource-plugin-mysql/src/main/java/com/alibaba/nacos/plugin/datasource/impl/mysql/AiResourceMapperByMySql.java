/*
 * Copyright 1999-2026 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.datasource.impl.mysql;

import com.alibaba.nacos.plugin.datasource.constants.DataSourceConstant;
import com.alibaba.nacos.plugin.datasource.constants.FieldConstant;
import com.alibaba.nacos.plugin.datasource.mapper.AiResourceMapper;
import com.alibaba.nacos.plugin.datasource.mapper.ext.WhereBuilder;
import com.alibaba.nacos.plugin.datasource.model.MapperContext;
import com.alibaba.nacos.plugin.datasource.model.MapperResult;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link AiResourceMapper} 的 MySQL 实现。
 *
 * <p>提供 AI 资源分页查询 SQL，使用 {@code LIMIT ?,?} 分页并支持扩展查询条件。</p>
 *
 * @author nacos
 */
public class AiResourceMapperByMySql extends AbstractMapperByMysql implements AiResourceMapper {
    
    /** 分页查询 AI 资源列表，支持命名空间过滤与排序。 */
    @Override
    public MapperResult findAiResourceFetchRows(MapperContext context) {
        WhereBuilder where = new WhereBuilder(
            "SELECT id,gmt_create,gmt_modified,name,type,c_desc,status,namespace_id,"
                + "biz_tags,ext,c_from,version_info,meta_version,scope,owner,download_count "
                + "FROM ai_resource");
        where.eq("namespace_id", context.getWhereParameter(FieldConstant.NAMESPACE_ID));
        
        appendExtraQueryCondition(where, context);
        
        MapperResult built = where.build();
        String sql = built.getSql() + resolveOrderByClause(context) + " LIMIT ?,?";
        List<Object> params = new ArrayList<>(built.getParamList());
        params.add(context.getStartRow());
        params.add(context.getPageSize());
        return new MapperResult(sql, params);
    }
    
    /** 返回 MySQL 数据源类型标识。 */
    @Override
    public String getDataSource() {
        return DataSourceConstant.MYSQL;
    }
}
