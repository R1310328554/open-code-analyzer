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

import com.alibaba.nacos.common.utils.ArrayUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.plugin.datasource.constants.FieldConstant;
import com.alibaba.nacos.plugin.datasource.constants.TableConstant;
import com.alibaba.nacos.plugin.datasource.mapper.ext.WhereBuilder;
import com.alibaba.nacos.plugin.datasource.model.MapperContext;
import com.alibaba.nacos.plugin.datasource.model.MapperResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 配置与标签关联 Mapper 接口。
 *
 * <p>通过 {@code config_info} 与 {@code config_tags_relation} 联表，
 * 支持按标签精确/模糊分页检索配置。</p>
 *
 * @author hyx
 **/

public interface ConfigTagsRelationMapper extends Mapper {
    
    /**
     * 统计符合标签条件的配置数量。
     * 默认 SQL：
     * SELECT count(*) FROM config_info WHERE ...
     *
     * @param context 参数映射，键含 dataId、groupId、tenantId、appName、tagArr 等
     * @return 配置计数 SQL 及参数
     */
    default MapperResult findConfigInfo4PageCountRows(final MapperContext context) {
        final String appName = (String) context.getWhereParameter(FieldConstant.APP_NAME);
        final String tenantId = (String) context.getWhereParameter(FieldConstant.TENANT_ID);
        final String dataId = (String) context.getWhereParameter(FieldConstant.DATA_ID);
        final String group = (String) context.getWhereParameter(FieldConstant.GROUP_ID);
        final String content = (String) context.getWhereParameter(FieldConstant.CONTENT);
        final String[] tagArr = (String[]) context.getWhereParameter(FieldConstant.TAG_ARR);
        
        List<Object> paramList = new ArrayList<>();
        StringBuilder where = new StringBuilder(" WHERE ");
        final String sqlCount =
            "SELECT count(*) FROM config_info  a LEFT JOIN config_tags_relation b ON a.id=b.id";
        
        where.append(" a.tenant_id=? ");
        paramList.add(tenantId);
        if (StringUtils.isNotBlank(dataId)) {
            where.append(" AND a.data_id=? ");
            paramList.add(dataId);
        }
        if (StringUtils.isNotBlank(group)) {
            where.append(" AND a.group_id=? ");
            paramList.add(group);
        }
        if (StringUtils.isNotBlank(appName)) {
            where.append(" AND a.app_name=? ");
            paramList.add(appName);
        }
        if (!StringUtils.isBlank(content)) {
            where.append(" AND a.content LIKE ? ");
            paramList.add(content);
        }
        where.append(" AND b.tag_name IN (");
        for (int i = 0; i < tagArr.length; i++) {
            if (i != 0) {
                where.append(", ");
            }
            where.append('?');
            paramList.add(tagArr[i]);
            
        }
        where.append(") ");
        return new MapperResult(sqlCount + where, paramList);
    }
    
    /**
     * 分页查询符合标签条件的配置列表。
     * 默认 SQL：
     * SELECT a.id,a.data_id,a.group_id,a.tenant_id,a.app_name,a.content FROM config_info  a LEFT JOIN
     * config_tags_relation b ON a.id=b.i ...
     *
     * @param context 查询键值，含 dataId、group 等
     * @return 配置分页查询 SQL 及参数
     */
    MapperResult findConfigInfo4PageFetchRows(final MapperContext context);
    
    /**
     * 按标签关联模糊条件统计配置数量。
     * 默认 SQL：
     * SELECT count(*) FROM config_info  a LEFT JOIN config_tags_relation b ON a.id=b.id
     *
     * @param context 查询键值，含 dataId、group、tagArr、type 等
     * @return 模糊计数 SQL 及参数
     */
    default MapperResult findConfigInfoLike4PageCountRows(final MapperContext context) {
        final String appName = (String) context.getWhereParameter(FieldConstant.APP_NAME);
        final String tenantId = (String) context.getWhereParameter(FieldConstant.TENANT_ID);
        final String dataId = (String) context.getWhereParameter(FieldConstant.DATA_ID);
        final String group = (String) context.getWhereParameter(FieldConstant.GROUP_ID);
        final String content = (String) context.getWhereParameter(FieldConstant.CONTENT);
        final String[] tagArr = (String[]) context.getWhereParameter(FieldConstant.TAG_ARR);
        final String[] types = (String[]) context.getWhereParameter(FieldConstant.TYPE);
        
        WhereBuilder where = new WhereBuilder(
            "SELECT count(*) FROM config_info a LEFT JOIN config_tags_relation b ON a.id=b.id");
        
        where.like("a.tenant_id", tenantId);
        if (StringUtils.isNotBlank(dataId)) {
            where.and().like("a.data_id", dataId);
        }
        if (StringUtils.isNotBlank(group)) {
            where.and().like("a.group_id", group);
        }
        if (StringUtils.isNotBlank(appName)) {
            where.and().eq("a.app_name", appName);
        }
        if (StringUtils.isNotBlank(content)) {
            where.and().like("a.content", content);
        }
        if (!ArrayUtils.isEmpty(tagArr)) {
            where.and().startParentheses();
            for (int i = 0; i < tagArr.length; i++) {
                if (i != 0) {
                    where.or();
                }
                where.like("b.tag_name", tagArr[i]);
            }
            where.endParentheses();
        }
        if (!ArrayUtils.isEmpty(types)) {
            where.and().in("a.type", types);
        }
        
        return where.build();
    }
    
    /**
     * 按标签关联模糊条件分页查询配置。
     * 默认 SQL：
     * SELECT a.id,a.data_id,a.group_id,a.tenant_id,a.app_name,a.content,a.type,a.md5
     * FROM config_info a LEFT JOIN config_tags_relation b ON a.id=b.id
     *
     * @param context 查询键值，含 dataId、group 等
     * @return 配置模糊分页查询 SQL 及参数
     */
    MapperResult findConfigInfoLike4PageFetchRows(final MapperContext context);
    
    /**
     * 获取返回表名.
     *
     * @return 表名
     */
    default String getTableName() {
        return TableConstant.CONFIG_TAGS_RELATION;
    }
}
