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
import com.alibaba.nacos.plugin.datasource.constants.FieldConstant;
import com.alibaba.nacos.plugin.datasource.dialect.DatabaseDialect;
import com.alibaba.nacos.plugin.datasource.manager.DatabaseDialectManager;
import com.alibaba.nacos.plugin.datasource.mapper.AbstractMapper;
import com.alibaba.nacos.plugin.datasource.mapper.TenantCapacityMapper;
import com.alibaba.nacos.plugin.datasource.model.MapperContext;
import com.alibaba.nacos.plugin.datasource.model.MapperResult;

/**
 * {@link TenantCapacityMapper} 抽象基类。
 *
 * <p>操作 tenant_capacity 表，分页拉取租户容量记录用于用量校正； 分页语法委托 {@link DatabaseDialect}。</p>
 *
 * @author Long Yu
 **/
public abstract class BaseTenantCapacityMapper extends AbstractMapper
    implements TenantCapacityMapper {
    
    /** 当前数据源的数据库方言。 */
    private DatabaseDialect databaseDialect;
    
    /** 初始化数据库方言。 */
    public BaseTenantCapacityMapper() {
        databaseDialect = DatabaseDialectManager.getInstance().getDialect(getDataSource());
    }
    
    /** 按 id 游标分页查询租户容量列表（校正用量）。 */
    @Override
    public MapperResult getCapacityList4CorrectUsage(MapperContext context) {
        String sql = databaseDialect
            .getLimitTopSqlWithMark("SELECT id, tenant_id FROM tenant_capacity WHERE id>?");
        return new MapperResult(sql,
            CollectionUtils.list(context.getWhereParameter(FieldConstant.ID),
                context.getWhereParameter(FieldConstant.LIMIT_SIZE)));
    }
    
    /** 委托方言解析数据库函数。 */
    @Override
    public String getFunction(String functionName) {
        return databaseDialect.getFunction(functionName);
    }
    
}
