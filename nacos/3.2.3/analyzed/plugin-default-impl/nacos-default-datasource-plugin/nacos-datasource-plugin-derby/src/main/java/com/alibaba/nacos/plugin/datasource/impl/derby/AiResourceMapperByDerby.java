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

package com.alibaba.nacos.plugin.datasource.impl.derby;

import com.alibaba.nacos.plugin.datasource.constants.DataSourceConstant;
import com.alibaba.nacos.plugin.datasource.constants.FieldConstant;
import com.alibaba.nacos.plugin.datasource.mapper.AiResourceMapper;
import com.alibaba.nacos.plugin.datasource.mapper.ext.WhereBuilder;
import com.alibaba.nacos.plugin.datasource.model.MapperContext;
import com.alibaba.nacos.plugin.datasource.model.MapperResult;

import java.util.List;

/**
 * {@link AiResourceMapper} 的 Derby 实现。
 *
 * <p>使用 Derby {@code OFFSET … ROWS FETCH NEXT … ROWS ONLY} 语法分页查询 AI 资源表。</p>
 *
 * @author nacos
 */
public class AiResourceMapperByDerby extends AbstractMapperByDerby implements AiResourceMapper {
    
    /** 按命名空间与扩展条件分页查询 AI 资源列表。 */
    @Override
    public MapperResult findAiResourceFetchRows(MapperContext context) {
        WhereBuilder where = new WhereBuilder(
            "SELECT id,gmt_create,gmt_modified,name,type,c_desc,status,namespace_id,"
                + "biz_tags,ext,c_from,version_info,meta_version,scope,owner,download_count "
                + "FROM ai_resource");
        where.eq("namespace_id", context.getWhereParameter(FieldConstant.NAMESPACE_ID));
        
        // 追加可选扩展查询条件
        appendExtraQueryCondition(where, context);
        
        MapperResult built = where.build();
        String sql =
            built.getSql() + resolveOrderByClause(context) + " OFFSET " + context.getStartRow()
                + " ROWS FETCH NEXT " + context.getPageSize() + " ROWS ONLY";
        return new MapperResult(sql, built.getParamList());
    }
    
    /** 向 WHERE 追加单字段条件，支持等值、LIKE 及 IN 列表匹配。 */
    @Override
    public void appendSingleAndCondition(WhereBuilder where, String field, Object value,
        boolean likeMatch) {
        if (field == null || value == null) {
            return;
        }
        if (value instanceof List) {
            if (((List<?>) value).isEmpty()) {
                return;
            }
            where.and().in(field, ((List<?>) value).toArray());
            return;
        }
        if (likeMatch) {
            where.and().likeWithEscape(field, value);
        } else {
            where.and().eq(field, value);
        }
    }
    
    /** 返回 Derby 数据源标识。 */
    @Override
    public String getDataSource() {
        return DataSourceConstant.DERBY;
    }
}
