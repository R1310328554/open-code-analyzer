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

import com.alibaba.nacos.plugin.datasource.constants.TableConstant;
import com.alibaba.nacos.plugin.datasource.dialect.DatabaseDialect;
import com.alibaba.nacos.plugin.datasource.manager.DatabaseDialectManager;
import com.alibaba.nacos.plugin.datasource.mapper.AbstractMapper;
import com.alibaba.nacos.plugin.datasource.mapper.ConfigInfoTagMapper;
import com.alibaba.nacos.plugin.datasource.model.MapperContext;
import com.alibaba.nacos.plugin.datasource.model.MapperResult;

import java.util.Collections;

/**
 * {@link ConfigInfoTagMapper} 抽象基类。
 *
 * <p>操作 {@code config_info_tag} 表，提供全量 dump 分页查询； 分页 SQL 由 {@link DatabaseDialect} 生成。</p>
 *
 * @author Long Yu
 **/
public abstract class BaseConfigInfoTagMapper extends AbstractMapper
    implements ConfigInfoTagMapper {
    
    /** 当前数据源的数据库方言。 */
    private DatabaseDialect databaseDialect;
    
    /** 初始化数据库方言。 */
    public BaseConfigInfoTagMapper() {
        databaseDialect = DatabaseDialectManager.getInstance().getDialect(getDataSource());
    }
    
    /** 返回标签配置表名 {@link TableConstant#CONFIG_INFO_TAG}。 */
    @Override
    public String getTableName() {
        return TableConstant.CONFIG_INFO_TAG;
    }
    
    /** 分页拉取全部标签配置用于全量 dump。 */
    @Override
    public MapperResult findAllConfigInfoTagForDumpAllFetchRows(MapperContext context) {
        int startRow = context.getStartRow();
        int pageSize = context.getPageSize();
        String innerSql = databaseDialect
            .getLimitPageSqlWithOffset("SELECT id FROM config_info_tag  ORDER BY id ", startRow,
                pageSize);
        String sql =
            " SELECT t.id,data_id,group_id,tenant_id,tag_id,app_name,content,md5,gmt_modified "
                + " FROM (  "
                + innerSql + "  ) " + "g, config_info_tag t  WHERE g.id = t.id  ";
        return new MapperResult(sql, Collections.emptyList());
    }
    
    /** 委托方言解析数据库函数。 */
    @Override
    public String getFunction(String functionName) {
        return databaseDialect.getFunction(functionName);
    }
    
}
