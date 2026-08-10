/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.plugin.datasource.constants.FieldConstant;
import com.alibaba.nacos.plugin.datasource.constants.TableConstant;
import com.alibaba.nacos.plugin.datasource.mapper.ext.WhereBuilder;
import com.alibaba.nacos.plugin.datasource.model.MapperContext;
import com.alibaba.nacos.plugin.datasource.model.MapperResult;

/**
 * AI 资源版本表（{@code ai_resource_version}）Mapper 接口。
 *
 * <p>按命名空间、资源名及可选 type/status/version 过滤版本记录。</p>
 *
 * @author nacos
 * @since 3.2.0
 */
public interface AiResourceVersionMapper extends Mapper {
    
    /**
     * 统计 AI 资源版本列表行数。
     *
     * <p>过滤：namespace_id（必填）、name（必填）、type/status/version（可选）。</p>
     */
    default MapperResult findAiResourceVersionCountRows(MapperContext context) {
        WhereBuilder where = new WhereBuilder("SELECT count(*) FROM ai_resource_version");
        where.eq("namespace_id", context.getWhereParameter(FieldConstant.NAMESPACE_ID));
        where.and().eq("name", context.getWhereParameter(FieldConstant.NAME));
        
        Object type = context.getWhereParameter(FieldConstant.TYPE);
        if (type != null && StringUtils.isNotBlank(String.valueOf(type))) {
            where.and().eq("type", type);
        }
        
        Object status = context.getWhereParameter(FieldConstant.STATUS);
        if (status != null && StringUtils.isNotBlank(String.valueOf(status))) {
            where.and().eq("status", status);
        }
        
        Object version = context.getWhereParameter(FieldConstant.VERSION);
        if (version != null && StringUtils.isNotBlank(String.valueOf(version))) {
            where.and().eq("version", version);
        }
        
        return where.build();
    }
    
    /**
     * 分页查询 AI 资源版本列表（由方言实现类提供 SQL）。
     */
    MapperResult findAiResourceVersionFetchRows(MapperContext context);
    
    @Override
    default String getTableName() {
        return TableConstant.AI_RESOURCE_VERSION;
    }
}
