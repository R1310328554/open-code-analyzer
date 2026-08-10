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
import com.alibaba.nacos.plugin.datasource.mapper.ConfigInfoBetaMapper;
import com.alibaba.nacos.plugin.datasource.model.MapperContext;
import com.alibaba.nacos.plugin.datasource.model.MapperResult;

import java.util.Collections;

/**
 * {@link ConfigInfoBetaMapper} 抽象基类。
 *
 * <p>通过 {@link DatabaseDialect} 适配分页 SQL 与数据库函数， 子类仅需声明 {@link #getDataSource()} 数据源类型。</p>
 *
 * @author Long Yu
 **/
public abstract class BaseConfigInfoBetaMapper extends AbstractMapper
    implements ConfigInfoBetaMapper {
    
    /** 当前数据源对应的数据库方言。 */
    private DatabaseDialect databaseDialect;
    
    /** 根据数据源类型初始化方言实例。 */
    public BaseConfigInfoBetaMapper() {
        databaseDialect = DatabaseDialectManager.getInstance().getDialect(getDataSource());
    }
    
    /** 返回 Beta 配置表名 {@link TableConstant#CONFIG_INFO_BETA}。 */
    @Override
    public String getTableName() {
        return TableConstant.CONFIG_INFO_BETA;
    }
    
    /** 为 SQL 追加带偏移量的分页子句。 */
    public String getLimitPageSqlWithOffset(String sql, int startRow, int pageSize) {
        return databaseDialect.getLimitPageSqlWithOffset(sql, startRow, pageSize);
    }
    
    /** 分页拉取全部 Beta 配置用于全量 dump（子查询 + 关联）。 */
    @Override
    public MapperResult findAllConfigInfoBetaForDumpAllFetchRows(MapperContext context) {
        int startRow = context.getStartRow();
        int pageSize = context.getPageSize();
        String sqlInner =
            getLimitPageSqlWithOffset("SELECT id FROM config_info_beta  ORDER BY id ", startRow,
                pageSize);
        String sql =
            " SELECT t.id,data_id,group_id,tenant_id,app_name,content,md5,gmt_modified,beta_ips,encrypted_data_key "
                + " FROM ( " + sqlInner + "  )" + "  g, config_info_beta t WHERE g.id = t.id ";
        return new MapperResult(sql, Collections.emptyList());
    }
    
    /** 委托方言解析数据库函数名。 */
    @Override
    public String getFunction(String functionName) {
        return databaseDialect.getFunction(functionName);
    }
    
}
