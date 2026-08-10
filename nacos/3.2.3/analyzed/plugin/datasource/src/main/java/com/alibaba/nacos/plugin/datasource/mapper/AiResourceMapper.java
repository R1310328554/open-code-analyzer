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
import java.util.List;
import java.util.Map;

/**
 * AI 资源表（{@code ai_resource}）Mapper 接口。
 *
 * <p>提供资源列表计数、分页查询及动态 OR/AND 条件拼装，排序字段经白名单校验以防 SQL 注入。</p>
 *
 * @author nacos
 * @since 3.2.0
 */
public interface AiResourceMapper extends Mapper {
    
    /** 上下文键：OR 条件组（Map&lt;字段, 值&gt;）。 */
    String QUERY_CONDITION_OR_GROUP = "query_condition_or_group";
    
    /** 上下文键：强制返回空结果集（{@code 1=0}）。 */
    String QUERY_CONDITION_ALWAYS_EMPTY = "query_condition_always_empty";
    
    /**
     * 统计 AI 资源列表行数。
     *
     * <p>过滤条件：namespace_id（必填）、type（可选）、name（可选模糊）、biz_tags（可选模糊）。</p>
     */
    default MapperResult findAiResourceCountRows(MapperContext context) {
        WhereBuilder where = new WhereBuilder("SELECT count(*) FROM ai_resource");
        where.eq("namespace_id", context.getWhereParameter(FieldConstant.NAMESPACE_ID));
        
        appendExtraQueryCondition(where, context);
        
        return where.build();
    }
    
    /**
     * 分页查询 AI 资源列表（具体 SQL 由方言实现类提供）。
     */
    MapperResult findAiResourceFetchRows(MapperContext context);
    
    /**
     * 根据上下文 orderBy 参数解析 ORDER BY 子句，仅接受白名单字段以防
     * prevent SQL injection.
     *
     * @param context mapper context that may contain an {@link FieldConstant#ORDER_BY} parameter
     * @return SQL ORDER BY clause, e.g. {@code " ORDER BY download_count DESC"} or {@code " ORDER BY gmt_modified DESC"}
     */
    default String resolveOrderByClause(MapperContext context) {
        Object orderBy = context.getWhereParameter(FieldConstant.ORDER_BY);
        if (orderBy != null
            && FieldConstant.ORDER_BY_DOWNLOAD_COUNT.equals(String.valueOf(orderBy))) {
            return " ORDER BY download_count DESC";
        }
        return " ORDER BY gmt_modified DESC";
    }
    
    /**
     * 追加由上层转换器生成的额外查询条件（含 OR 组与强制空结果）。
     */
    @SuppressWarnings("unchecked")
    default void appendExtraQueryCondition(WhereBuilder where, MapperContext context) {
        Object alwaysEmptyObj = context.getWhereParameter(QUERY_CONDITION_ALWAYS_EMPTY);
        boolean alwaysEmpty = Boolean.TRUE.equals(alwaysEmptyObj);
        if (alwaysEmpty) {
            where.and().eq("1", 0);
            return;
        }
        appendAndConditions(where, context);
        Map<String, Object> orMap = castToMap(context.getWhereParameter(QUERY_CONDITION_OR_GROUP));
        if (orMap == null || orMap.isEmpty()) {
            return;
        }
        if (orMap.size() == 1) {
            Map.Entry<String, Object> only = orMap.entrySet().iterator().next();
            appendSingleAndCondition(where, only.getKey(), only.getValue(), false);
            return;
        }
        appendOrConditions(where, orMap);
    }
    
    /**
     * 从上下文追加标准 AND 条件（name/biz_tags 模糊，type/scope/owner 精确）。
     */
    default void appendAndConditions(WhereBuilder where, MapperContext context) {
        appendSingleAndCondition(where, "name", context.getWhereParameter(FieldConstant.NAME),
            true);
        appendSingleAndCondition(where, "biz_tags",
            context.getWhereParameter(FieldConstant.BIZ_TAGS), true);
        appendSingleAndCondition(where, "type", context.getWhereParameter(FieldConstant.TYPE),
            false);
        appendSingleAndCondition(where, "scope", context.getWhereParameter(FieldConstant.SCOPE),
            false);
        appendSingleAndCondition(where, "owner", context.getWhereParameter(FieldConstant.OWNER),
            false);
    }
    
    /**
     * 追加单个 AND 条件；列表值转为 IN 子句。
     */
    default void appendSingleAndCondition(WhereBuilder where, String field, Object value,
        boolean likeMatch) {
        if (StringUtils.isBlank(field) || value == null) {
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
            where.and().like(field, value);
        } else {
            where.and().eq(field, value);
        }
    }
    
    /**
     * 以括号包裹追加 OR 条件组。
     */
    default void appendOrConditions(WhereBuilder where, Map<String, Object> orMap) {
        where.and().startParentheses();
        boolean appended = false;
        for (Map.Entry<String, Object> each : orMap.entrySet()) {
            String field = each.getKey();
            Object value = each.getValue();
            if (StringUtils.isBlank(field) || value == null) {
                continue;
            }
            if (appended) {
                where.or();
            }
            if (value instanceof List) {
                if (((List<?>) value).isEmpty()) {
                    continue;
                }
                where.in(field, ((List<?>) value).toArray());
            } else {
                where.eq(field, value);
            }
            appended = true;
        }
        if (!appended) {
            where.eq("1", 0);
        }
        where.endParentheses();
    }
    
    /**
     * 将上下文原始值安全转换为 String 键的 Map。
     */
    @SuppressWarnings("unchecked")
    default Map<String, Object> castToMap(Object value) {
        if (!(value instanceof Map)) {
            return null;
        }
        Map<Object, Object> raw = (Map<Object, Object>) value;
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        for (Map.Entry<Object, Object> each : raw.entrySet()) {
            result.put(each.getKey() == null ? null : String.valueOf(each.getKey()),
                each.getValue());
        }
        return result;
    }
    
    @Override
    default String getTableName() {
        return TableConstant.AI_RESOURCE;
    }
}
