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

package com.alibaba.nacos.plugin.datasource.impl.base;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.plugin.datasource.constants.ContextConstant;
import com.alibaba.nacos.plugin.datasource.constants.FieldConstant;
import com.alibaba.nacos.plugin.datasource.constants.TableConstant;
import com.alibaba.nacos.plugin.datasource.dialect.DatabaseDialect;
import com.alibaba.nacos.plugin.datasource.manager.DatabaseDialectManager;
import com.alibaba.nacos.plugin.datasource.mapper.AbstractMapper;
import com.alibaba.nacos.plugin.datasource.mapper.ConfigInfoMapper;
import com.alibaba.nacos.plugin.datasource.model.MapperContext;
import com.alibaba.nacos.plugin.datasource.model.MapperResult;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@link ConfigInfoMapper} 抽象基类。
 *
 * <p>封装 config_info 表各类分页查询、模糊搜索与变更追踪 SQL， 分页语法由 {@link DatabaseDialect} 按 MySQL/Derby 等方言改写。</p>
 *
 * @author Long Yu
 **/
public abstract class BaseConfigInfoMapper extends AbstractMapper implements ConfigInfoMapper {
    
    /** 当前数据源的数据库方言。 */
    private DatabaseDialect databaseDialect;
    
    /** 初始化数据库方言。 */
    public BaseConfigInfoMapper() {
        databaseDialect = DatabaseDialectManager.getInstance().getDialect(getDataSource());
    }
    
    /** 追加 OFFSET/LIMIT 风格分页子句。 */
    public String getLimitPageSqlWithOffset(String sql, int startOffset, int pageSize) {
        return databaseDialect.getLimitPageSqlWithOffset(sql, startOffset, pageSize);
    }
    
    /** 追加占位符风格的分页子句（参数由调用方绑定）。 */
    public String getLimitPageSqlWithMark(String sql) {
        return databaseDialect.getLimitPageSqlWithMark(sql);
    }
    
    /** 按租户与应用名分页查询配置。 */
    @Override
    public MapperResult findConfigInfoByAppFetchRows(MapperContext context) {
        int startRow = context.getStartRow();
        int pageSize = context.getPageSize();
        final String appName = (String) context.getWhereParameter(FieldConstant.APP_NAME);
        final String tenantId = (String) context.getWhereParameter(FieldConstant.TENANT_ID);
        String sql = getLimitPageSqlWithOffset(
            "SELECT id,data_id,group_id,tenant_id,app_name,content FROM config_info"
                + " WHERE tenant_id LIKE ? AND app_name= ?",
            startRow, pageSize);
        return new MapperResult(sql, CollectionUtils.list(tenantId, appName));
    }
    
    /** 分页获取非空 tenant_id 去重列表。 */
    @Override
    public MapperResult getTenantIdList(MapperContext context) {
        int startRow = context.getStartRow();
        int pageSize = context.getPageSize();
        String sql = getLimitPageSqlWithOffset(
            "SELECT tenant_id FROM config_info WHERE tenant_id != '' GROUP BY tenant_id ", startRow,
            pageSize);
        return new MapperResult(sql, Collections.emptyList());
    }
    
    /** 分页获取默认命名空间下的 group_id 列表。 */
    @Override
    public MapperResult getGroupIdList(MapperContext context) {
        int startRow = context.getStartRow();
        int pageSize = context.getPageSize();
        String sql = getLimitPageSqlWithOffset(
            "SELECT group_id FROM config_info WHERE tenant_id ='' GROUP BY group_id ", +startRow,
            pageSize);
        return new MapperResult(sql, Collections.emptyList());
    }
    
    /** 分页查询指定租户下的配置键（dataId/groupId/appName）。 */
    @Override
    public MapperResult findAllConfigKey(MapperContext context) {
        int startRow = context.getStartRow();
        int pageSize = context.getPageSize();
        String innerSql = getLimitPageSqlWithOffset(
            " SELECT id FROM config_info WHERE tenant_id LIKE ? ORDER BY id ",
            startRow, pageSize);
        // 修复子查询括号缺失问题
        String sql = " SELECT data_id,group_id,app_name  FROM ( " + innerSql
            + " ) g, config_info t WHERE g.id = t.id  ";
        return new MapperResult(sql,
            CollectionUtils.list(context.getWhereParameter(FieldConstant.TENANT_ID)));
    }
    
    /** 分页拉取默认命名空间配置基础字段。 */
    @Override
    public MapperResult findAllConfigInfoBaseFetchRows(MapperContext context) {
        int startRow = context.getStartRow();
        int pageSize = context.getPageSize();
        String innerSql = getLimitPageSqlWithMark(" SELECT id FROM config_info ORDER BY id ");
        String sql = " SELECT t.id,data_id,group_id,content,md5" + " FROM ( " + innerSql + "  ) "
            + " g, config_info t  WHERE g.id = t.id ";
        return new MapperResult(sql, CollectionUtils.list(startRow, pageSize));
    }
    
    /** 按 id 游标分页拉取配置片段（可选是否含 content）。 */
    @Override
    public MapperResult findAllConfigInfoFragment(MapperContext context) {
        int startRow = context.getStartRow();
        int pageSize = context.getPageSize();
        String contextParameter = context.getContextParameter(ContextConstant.NEED_CONTENT);
        boolean needContent = Boolean.parseBoolean(contextParameter);
        String sql = getLimitPageSqlWithOffset(
            "SELECT id,data_id,group_id,tenant_id,app_name," + (needContent ? "content," : "")
                + "md5,gmt_modified,type,encrypted_data_key FROM config_info WHERE id > ? ORDER BY id ASC ",
            startRow, pageSize);
        return new MapperResult(sql,
            CollectionUtils.list(context.getWhereParameter(FieldConstant.ID)));
    }
    
    /** 按多条件与时间范围分页查询变更配置。 */
    @Override
    public MapperResult findChangeConfigFetchRows(MapperContext context) {
        final String tenant = (String) context.getWhereParameter(FieldConstant.TENANT_ID);
        final String dataId = (String) context.getWhereParameter(FieldConstant.DATA_ID);
        final String group = (String) context.getWhereParameter(FieldConstant.GROUP_ID);
        final String appName = (String) context.getWhereParameter(FieldConstant.APP_NAME);
        final String tenantTmp = StringUtils.isBlank(tenant) ? StringUtils.EMPTY : tenant;
        final Timestamp startTime = (Timestamp) context.getWhereParameter(FieldConstant.START_TIME);
        final Timestamp endTime = (Timestamp) context.getWhereParameter(FieldConstant.END_TIME);
        final long lastMaxId = (long) context.getWhereParameter(FieldConstant.LAST_MAX_ID);
        final int pageSize = context.getPageSize();
        List<Object> paramList = new ArrayList<>();
        
        final String sqlFetchRows =
            "SELECT id,data_id,group_id,tenant_id,app_name,content,type,md5,gmt_modified FROM config_info WHERE ";
        String where = " 1=1 ";
        if (!StringUtils.isBlank(dataId)) {
            where += " AND data_id LIKE ? ";
            paramList.add(dataId);
        }
        if (!StringUtils.isBlank(group)) {
            where += " AND group_id LIKE ? ";
            paramList.add(group);
        }
        if (!StringUtils.isBlank(tenantTmp)) {
            where += " AND tenant_id = ? ";
            paramList.add(tenantTmp);
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
        String originSql = sqlFetchRows + where + " AND id > " + lastMaxId + " ORDER BY id ASC";
        String sql = getLimitPageSqlWithOffset(originSql, 0, pageSize);
        return new MapperResult(sql, paramList);
    }
    
    /** 分页返回配置的 group 键及 md5 等元数据。 */
    @Override
    public MapperResult listGroupKeyMd5ByPageFetchRows(MapperContext context) {
        int startRow = context.getStartRow();
        int pageSize = context.getPageSize();
        String innerSql = getLimitPageSqlWithOffset(" SELECT id FROM config_info ORDER BY id ",
            startRow, pageSize);
        String sql =
            " SELECT t.id,data_id,group_id,tenant_id,app_name,md5,type,gmt_modified,encrypted_data_key FROM "
                + "( "
                + innerSql + " ) g, config_info t WHERE g.id = t.id";
        return new MapperResult(sql, Collections.emptyList());
    }
    
    /** 默认命名空间下按 dataId/group/content 模糊分页查询。 */
    @Override
    public MapperResult findConfigInfoBaseLikeFetchRows(MapperContext context) {
        final String dataId = (String) context.getWhereParameter(FieldConstant.DATA_ID);
        final String group = (String) context.getWhereParameter(FieldConstant.GROUP_ID);
        final String content = (String) context.getWhereParameter(FieldConstant.CONTENT);
        final String sqlFetchRows =
            "SELECT id,data_id,group_id,tenant_id,content FROM config_info WHERE ";
        String where = " 1=1 AND tenant_id='' ";
        List<Object> paramList = new ArrayList<>();
        if (!StringUtils.isBlank(dataId)) {
            where += " AND data_id LIKE ? ";
            paramList.add(dataId);
        }
        if (!StringUtils.isBlank(group)) {
            where += " AND group_id LIKE ? ";
            paramList.add(group);
        }
        if (!StringUtils.isBlank(content)) {
            where += " AND content LIKE ? ";
            paramList.add(content);
        }
        int startRow = context.getStartRow();
        int pageSize = context.getPageSize();
        String sql = getLimitPageSqlWithOffset(sqlFetchRows + where, startRow, pageSize);
        return new MapperResult(sql, paramList);
    }
    
    /** 精确条件分页查询租户配置列表。 */
    @Override
    public MapperResult findConfigInfo4PageFetchRows(MapperContext context) {
        final String tenant = (String) context.getWhereParameter(FieldConstant.TENANT_ID);
        final String dataId = (String) context.getWhereParameter(FieldConstant.DATA_ID);
        final String group = (String) context.getWhereParameter(FieldConstant.GROUP_ID);
        final String appName = (String) context.getWhereParameter(FieldConstant.APP_NAME);
        final String content = (String) context.getWhereParameter(FieldConstant.CONTENT);
        List<Object> paramList = new ArrayList<>();
        final String sql =
            "SELECT id,data_id,group_id,tenant_id,app_name,content,md5,type,encrypted_data_key,c_desc FROM config_info";
        StringBuilder where = new StringBuilder(" WHERE ");
        where.append(" tenant_id=? ");
        paramList.add(tenant);
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
            where.append(" AND content LIKE ? ");
            paramList.add(content);
        }
        int startRow = context.getStartRow();
        int pageSize = context.getPageSize();
        String resultSql = getLimitPageSqlWithOffset(sql + where, startRow, pageSize);
        return new MapperResult(resultSql, paramList);
    }
    
    /** 按 group 与 tenant 分页查询配置内容。 */
    @Override
    public MapperResult findConfigInfoBaseByGroupFetchRows(MapperContext context) {
        int startRow = context.getStartRow();
        int pageSize = context.getPageSize();
        String sql =
            "SELECT id,data_id,group_id,content FROM config_info WHERE group_id=? AND tenant_id=? ";
        String resultSql = getLimitPageSqlWithOffset(sql, startRow, pageSize);
        return new MapperResult(resultSql,
            CollectionUtils.list(context.getWhereParameter(FieldConstant.GROUP_ID),
                context.getWhereParameter(FieldConstant.TENANT_ID)));
    }
    
    /** 租户下多字段模糊分页查询配置。 */
    @Override
    public MapperResult findConfigInfoLike4PageFetchRows(MapperContext context) {
        final String tenant = (String) context.getWhereParameter(FieldConstant.TENANT_ID);
        final String dataId = (String) context.getWhereParameter(FieldConstant.DATA_ID);
        final String group = (String) context.getWhereParameter(FieldConstant.GROUP_ID);
        final String appName = (String) context.getWhereParameter(FieldConstant.APP_NAME);
        final String content = (String) context.getWhereParameter(FieldConstant.CONTENT);
        final String sqlFetchRows =
            "SELECT id,data_id,group_id,tenant_id,app_name,content,md5,encrypted_data_key,type,c_desc FROM config_info";
        StringBuilder where = new StringBuilder(" WHERE ");
        where.append(" tenant_id LIKE ? ");
        List<Object> paramList = new ArrayList<>();
        paramList.add(tenant);
        if (!StringUtils.isBlank(dataId)) {
            where.append(" AND data_id LIKE ? ");
            paramList.add(dataId);
        }
        if (!StringUtils.isBlank(group)) {
            where.append(" AND group_id LIKE ? ");
            paramList.add(group);
        }
        if (!StringUtils.isBlank(appName)) {
            where.append(" AND app_name = ? ");
            paramList.add(appName);
        }
        if (!StringUtils.isBlank(content)) {
            where.append(" AND content LIKE ? ");
            paramList.add(content);
        }
        int startRow = context.getStartRow();
        int pageSize = context.getPageSize();
        String sql = getLimitPageSqlWithOffset(sqlFetchRows + where, startRow, pageSize);
        return new MapperResult(sql, paramList);
    }
    
    /** 分页拉取指定租户全部配置（含 content/md5）。 */
    @Override
    public MapperResult findAllConfigInfoFetchRows(MapperContext context) {
        String innerSql = getLimitPageSqlWithMark(
            "SELECT id FROM config_info WHERE tenant_id LIKE ? ORDER BY id ");
        String sql = " SELECT t.id,data_id,group_id,tenant_id,app_name,content,md5 " + " FROM ( "
            + innerSql + " )"
            + " g, config_info t  WHERE g.id = t.id ";
        return new MapperResult(sql, CollectionUtils
            .list(context.getWhereParameter(FieldConstant.TENANT_ID), context.getStartRow(),
                context.getPageSize()));
    }
    
    /** 返回主配置表名 {@link TableConstant#CONFIG_INFO}。 */
    @Override
    public String getTableName() {
        return TableConstant.CONFIG_INFO;
    }
    
    /** 委托方言解析数据库函数。 */
    @Override
    public String getFunction(String functionName) {
        return databaseDialect.getFunction(functionName);
    }
}
