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

package com.alibaba.nacos.plugin.datasource.impl.derby;

import com.alibaba.nacos.common.utils.ArrayUtils;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.NamespaceUtil;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.plugin.datasource.constants.ContextConstant;
import com.alibaba.nacos.plugin.datasource.constants.DataSourceConstant;
import com.alibaba.nacos.plugin.datasource.constants.FieldConstant;
import com.alibaba.nacos.plugin.datasource.mapper.ConfigInfoMapper;
import com.alibaba.nacos.plugin.datasource.mapper.ext.WhereBuilder;
import com.alibaba.nacos.plugin.datasource.model.MapperContext;
import com.alibaba.nacos.plugin.datasource.model.MapperResult;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@link ConfigInfoMapper} 的 Derby 实现。
 *
 * <p>配置中心核心表的 Derby SQL 方言适配：LIKE 转义、OFFSET/FETCH 分页及模糊查询。</p>
 *
 * @author hyx
 **/

public class ConfigInfoMapperByDerby extends AbstractMapperByDerby implements ConfigInfoMapper {
    
    /** Derby LIKE 子句反斜杠转义后缀。 */
    private static final String SQL_DERBY_ESCAPE_BACK_SLASH_FOR_LIKE = " ESCAPE '\\' ";
    
    /** 按租户与应用名分页查询配置。 */
    @Override
    public MapperResult findConfigInfoByAppFetchRows(MapperContext context) {
        final String appName = (String) context.getWhereParameter(FieldConstant.APP_NAME);
        final String tenantId = (String) context.getWhereParameter(FieldConstant.TENANT_ID);
        
        String sql =
            "SELECT ID,data_id,group_id,tenant_id,app_name,content FROM config_info WHERE tenant_id LIKE ?"
                + SQL_DERBY_ESCAPE_BACK_SLASH_FOR_LIKE + "AND app_name = ?" + " ORDER BY id OFFSET "
                + context.getStartRow()
                + " ROWS FETCH NEXT " + context.getPageSize() + " ROWS ONLY";
        
        return new MapperResult(sql, CollectionUtils.list(tenantId, appName));
    }
    
    /** 分页获取非默认命名空间的租户 id 列表。 */
    @Override
    public MapperResult getTenantIdList(MapperContext context) {
        
        return new MapperResult(
            "SELECT tenant_id FROM config_info WHERE tenant_id != '"
                + NamespaceUtil.getNamespaceDefaultId()
                + "' GROUP BY tenant_id ORDER BY tenant_id OFFSET " + context.getStartRow()
                + " ROWS FETCH NEXT "
                + context.getPageSize() + " ROWS ONLY",
            Collections.emptyList());
    }
    
    /** 分页获取默认命名空间下的 group_id 列表。 */
    @Override
    public MapperResult getGroupIdList(MapperContext context) {
        
        return new MapperResult(
            "SELECT group_id FROM config_info WHERE tenant_id ='"
                + NamespaceUtil.getNamespaceDefaultId()
                + "' GROUP BY group_id ORDER BY group_id OFFSET " + context.getStartRow()
                + " ROWS FETCH NEXT "
                + context.getPageSize() + " ROWS ONLY",
            Collections.emptyList());
    }
    
    /** 分页拉取配置键（data_id/group_id/app_name）。 */
    @Override
    public MapperResult findAllConfigKey(MapperContext context) {
        
        String sql = " SELECT data_id,group_id,app_name FROM "
            + " ( SELECT id FROM config_info WHERE tenant_id LIKE ?"
            + SQL_DERBY_ESCAPE_BACK_SLASH_FOR_LIKE
            + "ORDER BY id OFFSET " + context.getStartRow()
            + " ROWS FETCH NEXT " + context.getPageSize() + " ROWS ONLY ) "
            + "g, config_info t  WHERE g.id = t.id ";
        return new MapperResult(sql,
            CollectionUtils.list(context.getWhereParameter(FieldConstant.TENANT_ID)));
    }
    
    /** 分页拉取基础配置内容（含 md5）。 */
    @Override
    public MapperResult findAllConfigInfoBaseFetchRows(MapperContext context) {
        
        return new MapperResult(
            "SELECT t.id,data_id,group_id,content,md5 "
                + " FROM ( SELECT id FROM config_info ORDER BY id OFFSET "
                + context.getStartRow() + " ROWS FETCH NEXT " + context.getPageSize()
                + " ROWS ONLY )  "
                + " g, config_info t WHERE g.id = t.id ",
            Collections.emptyList());
    }
    
    /** 按 id 游标分页拉取配置片段，可按需包含 content。 */
    @Override
    public MapperResult findAllConfigInfoFragment(MapperContext context) {
        String contextParameter = context.getContextParameter(ContextConstant.NEED_CONTENT);
        boolean needContent = contextParameter != null && Boolean.parseBoolean(contextParameter);
        return new MapperResult(
            "SELECT id,data_id,group_id,tenant_id,app_name," + (needContent ? "content," : "")
                + "md5,gmt_modified,type FROM config_info WHERE id > ? " + "ORDER BY id ASC OFFSET "
                + context.getStartRow() + " ROWS FETCH NEXT " + context.getPageSize()
                + " ROWS ONLY",
            CollectionUtils.list(context.getWhereParameter(FieldConstant.ID)));
    }
    
    /** 按多条件与时间范围分页查询变更配置。 */
    @Override
    public MapperResult findChangeConfigFetchRows(MapperContext context) {
        final String tenant = (String) context.getWhereParameter(FieldConstant.TENANT);
        final String dataId = (String) context.getWhereParameter(FieldConstant.DATA_ID);
        final String group = (String) context.getWhereParameter(FieldConstant.GROUP_ID);
        final String appName = (String) context.getWhereParameter(FieldConstant.APP_NAME);
        
        final Timestamp startTime = (Timestamp) context.getWhereParameter(FieldConstant.START_TIME);
        final Timestamp endTime = (Timestamp) context.getWhereParameter(FieldConstant.END_TIME);
        
        List<Object> paramList = new ArrayList<>();
        
        final String sqlFetchRows =
            "SELECT id,data_id,group_id,tenant_id,app_name,content,type,md5,gmt_modified FROM"
                + " config_info WHERE ";
        String where = " 1=1 ";
        
        if (!StringUtils.isBlank(dataId)) {
            where += " AND data_id LIKE ?" + SQL_DERBY_ESCAPE_BACK_SLASH_FOR_LIKE;
            paramList.add(dataId);
        }
        if (!StringUtils.isBlank(group)) {
            where += " AND group_id LIKE ?" + SQL_DERBY_ESCAPE_BACK_SLASH_FOR_LIKE;
            paramList.add(group);
        }
        
        if (!StringUtils.isBlank(tenant)) {
            where += " AND tenant_id = ? ";
            paramList.add(tenant);
        }
        
        if (!StringUtils.isBlank(appName)) {
            where += " AND app_name = ? ";
            paramList.add(appName);
        }
        if (startTime != null) {
            where += " AND gmt_modified >=? ";
            paramList.add(startTime);
        }
        if (endTime != null) {
            where += " AND gmt_modified <=? ";
            paramList.add(endTime);
        }
        return new MapperResult(
            sqlFetchRows + where + " ORDER BY id OFFSET " + context.getStartRow()
                + " ROWS FETCH NEXT " + context.getPageSize()
                + " ROWS ONLY",
            paramList);
    }
    
    /** 分页列出配置的 group 键与 md5 摘要。 */
    @Override
    public MapperResult listGroupKeyMd5ByPageFetchRows(MapperContext context) {
        
        return new MapperResult(
            " SELECT t.id,data_id,group_id,tenant_id,app_name,type,md5,gmt_modified "
                + "FROM ( SELECT id FROM config_info ORDER BY id OFFSET " + context.getStartRow()
                + " ROWS FETCH NEXT "
                + context.getPageSize() + " ROWS ONLY ) g, config_info t WHERE g.id = t.id",
            Collections.emptyList());
    }
    
    /** 默认命名空间下按 dataId/group/content 模糊分页查询。 */
    @Override
    public MapperResult findConfigInfoBaseLikeFetchRows(MapperContext context) {
        final String tenant = (String) context.getWhereParameter(FieldConstant.TENANT);
        final String dataId = (String) context.getWhereParameter(FieldConstant.DATA_ID);
        final String group = (String) context.getWhereParameter(FieldConstant.GROUP_ID);
        
        List<Object> paramList = new ArrayList<>();
        final String sqlFetchRows =
            "SELECT id,data_id,group_id,tenant_id,content FROM config_info WHERE ";
        String where = " 1=1 AND tenant_id='" + NamespaceUtil.getNamespaceDefaultId() + "' ";
        if (!StringUtils.isBlank(dataId)) {
            where += " AND data_id LIKE ?" + SQL_DERBY_ESCAPE_BACK_SLASH_FOR_LIKE;
            paramList.add(dataId);
        }
        if (!StringUtils.isBlank(group)) {
            where += " AND group_id LIKE ?" + SQL_DERBY_ESCAPE_BACK_SLASH_FOR_LIKE;
            paramList.add(group);
        }
        if (!StringUtils.isBlank(tenant)) {
            where += " AND content LIKE ?" + SQL_DERBY_ESCAPE_BACK_SLASH_FOR_LIKE;
            paramList.add(tenant);
        }
        return new MapperResult(
            sqlFetchRows + where + " ORDER BY id OFFSET " + context.getStartRow()
                + " ROWS FETCH NEXT " + context.getPageSize()
                + " ROWS ONLY",
            paramList);
    }
    
    /** 控制台分页精确查询配置详情。 */
    @Override
    public MapperResult findConfigInfo4PageFetchRows(MapperContext context) {
        final String tenantId = (String) context.getWhereParameter(FieldConstant.TENANT_ID);
        final String dataId = (String) context.getWhereParameter(FieldConstant.DATA_ID);
        final String group = (String) context.getWhereParameter(FieldConstant.GROUP_ID);
        final String appName = (String) context.getWhereParameter(FieldConstant.APP_NAME);
        final String content = (String) context.getWhereParameter(FieldConstant.CONTENT);
        
        List<Object> paramList = new ArrayList<>();
        
        // Derby 版本：简单查询，不使用聚合函数
        final String sql =
            "SELECT id,data_id,group_id,tenant_id,app_name,content,md5,type,encrypted_data_key,c_desc FROM config_info";
        
        StringBuilder where = new StringBuilder(" WHERE ");
        where.append(" tenant_id=? ");
        paramList.add(tenantId);
        if (StringUtils.isNotBlank(dataId)) {
            where.append(" AND data_id=? ");
            paramList.add(dataId);
        }
        if (StringUtils.isNotBlank(group)) {
            where.append(" AND group_id=? ");
            paramList.add(group);
        }
        if (StringUtils.isNotBlank(appName)) {
            where.append(" AND app_name=? ");
            paramList.add(appName);
        }
        if (!StringUtils.isBlank(content)) {
            where.append(" AND content LIKE ?");
            where.append(SQL_DERBY_ESCAPE_BACK_SLASH_FOR_LIKE);
            paramList.add(content);
        }
        
        // Derby 分页语法
        return new MapperResult(
            sql + where + " ORDER BY id OFFSET " + context.getStartRow() + " ROWS FETCH NEXT "
                + context.getPageSize()
                + " ROWS ONLY",
            paramList);
    }
    
    /** 按 group 与 tenant 分页查询基础配置。 */
    @Override
    public MapperResult findConfigInfoBaseByGroupFetchRows(MapperContext context) {
        return new MapperResult(
            "SELECT id,data_id,group_id,content FROM config_info WHERE group_id=? "
                + "AND tenant_id=?" + " ORDER BY id OFFSET "
                + context.getStartRow() + " ROWS FETCH NEXT " + context.getPageSize()
                + " ROWS ONLY",
            CollectionUtils.list(context.getWhereParameter(FieldConstant.GROUP_ID),
                context.getWhereParameter(FieldConstant.TENANT_ID)));
    }
    
    /** 模糊查询配置总数（供分页计数）。 */
    @Override
    public MapperResult findConfigInfoLike4PageCountRows(MapperContext context) {
        final String dataId = (String) context.getWhereParameter(FieldConstant.DATA_ID);
        final String group = (String) context.getWhereParameter(FieldConstant.GROUP_ID);
        final String content = (String) context.getWhereParameter(FieldConstant.CONTENT);
        final String appName = (String) context.getWhereParameter(FieldConstant.APP_NAME);
        final String tenantId = (String) context.getWhereParameter(FieldConstant.TENANT_ID);
        final String[] types = (String[]) context.getWhereParameter(FieldConstant.TYPE);
        
        WhereBuilder where = new WhereBuilder("SELECT count(*) FROM config_info");
        
        where.likeWithEscape("tenant_id", tenantId);
        if (StringUtils.isNotBlank(dataId)) {
            where.and().likeWithEscape("data_id", dataId);
        }
        if (StringUtils.isNotBlank(group)) {
            where.and().likeWithEscape("group_id", group);
        }
        if (StringUtils.isNotBlank(appName)) {
            where.and().eq("app_name", appName);
        }
        if (StringUtils.isNotBlank(content)) {
            where.and().likeWithEscape("content", content);
        }
        if (!ArrayUtils.isEmpty(types)) {
            where.and().in("type", types);
        }
        return where.build();
    }
    
    /** 模糊分页查询配置列表。 */
    @Override
    public MapperResult findConfigInfoLike4PageFetchRows(MapperContext context) {
        
        final String tenantId = (String) context.getWhereParameter(FieldConstant.TENANT_ID);
        final String dataId = (String) context.getWhereParameter(FieldConstant.DATA_ID);
        final String group = (String) context.getWhereParameter(FieldConstant.GROUP_ID);
        final String appName = (String) context.getWhereParameter(FieldConstant.APP_NAME);
        final String content = (String) context.getWhereParameter(FieldConstant.CONTENT);
        final String[] types = (String[]) context.getWhereParameter(FieldConstant.TYPE);
        
        WhereBuilder where = new WhereBuilder(
            "SELECT id,data_id,group_id,tenant_id,app_name,content,md5,encrypted_data_key,type,c_desc,gmt_modified FROM config_info");
        
        where.likeWithEscape("tenant_id", tenantId);
        if (StringUtils.isNotBlank(dataId)) {
            where.and().likeWithEscape("data_id", dataId);
        }
        if (StringUtils.isNotBlank(group)) {
            where.and().likeWithEscape("group_id", group);
        }
        if (StringUtils.isNotBlank(appName)) {
            where.and().eq("app_name", appName);
        }
        if (StringUtils.isNotBlank(content)) {
            where.and().likeWithEscape("content", content);
        }
        if (!ArrayUtils.isEmpty(types)) {
            where.and().in("type", types);
        }
        
        where.orderBy("id").offset(context.getStartRow(), context.getPageSize());
        return where.build();
    }
    
    /** 按租户模糊匹配分页拉取全部配置。 */
    @Override
    public MapperResult findAllConfigInfoFetchRows(MapperContext context) {
        return new MapperResult(" SELECT t.id,data_id,group_id,tenant_id,app_name,content,md5 "
            + " FROM ( SELECT id FROM config_info  WHERE tenant_id LIKE ?"
            + SQL_DERBY_ESCAPE_BACK_SLASH_FOR_LIKE
            + "ORDER BY id OFFSET ? ROWS FETCH NEXT ? ROWS ONLY )"
            + " g, config_info t  WHERE g.id = t.id ",
            CollectionUtils.list(context.getWhereParameter(FieldConstant.TENANT_ID),
                context.getStartRow(),
                context.getPageSize()));
    }
    
    /** 返回 Derby 数据源标识。 */
    @Override
    public String getDataSource() {
        return DataSourceConstant.DERBY;
    }
    
    /** 增量拉取配置变更（按 gmt_modified 与 id 游标）。 */
    @Override
    public MapperResult findChangeConfig(MapperContext context) {
        String sql =
            "SELECT id, data_id, group_id, tenant_id, app_name, content, gmt_modified, encrypted_data_key FROM config_info WHERE "
                + "gmt_modified >= ? and id > ? order by id OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY";
        return new MapperResult(sql,
            CollectionUtils.list(context.getWhereParameter(FieldConstant.START_TIME),
                context.getWhereParameter(FieldConstant.LAST_MAX_ID),
                context.getWhereParameter(FieldConstant.PAGE_SIZE)));
    }
}
