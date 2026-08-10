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
import com.alibaba.nacos.plugin.datasource.mapper.GroupCapacityMapper;
import com.alibaba.nacos.plugin.datasource.model.MapperContext;
import com.alibaba.nacos.plugin.datasource.model.MapperResult;

/**
 * {@link GroupCapacityMapper} 抽象基类。
 *
 * <p>操作 group_capacity 表，提供按 id 游标分页查询分组容量信息， Top-N SQL 由 {@link DatabaseDialect} 适配。</p>
 *
 * @author Long Yu
 **/
public abstract class BaseGroupCapacityMapper extends AbstractMapper
    implements GroupCapacityMapper {
    
    /** 当前数据源的数据库方言。 */
    private DatabaseDialect databaseDialect;
    
    /** 初始化数据库方言。 */
    public BaseGroupCapacityMapper() {
        databaseDialect = DatabaseDialectManager.getInstance().getDialect(getDataSource());
    }
    
    /** 按 id 游标分页查询 group_id 列表（用于容量校正）。 */
    @Override
    public MapperResult selectGroupInfoBySize(MapperContext context) {
        String sql = databaseDialect
            .getLimitTopSqlWithMark("SELECT id, group_id FROM group_capacity WHERE id > ?");
        return new MapperResult(sql,
            CollectionUtils.list(context.getWhereParameter(FieldConstant.ID),
                context.getPageSize()));
    }
    
    /** 委托方言解析数据库函数。 */
    @Override
    public String getFunction(String functionName) {
        return databaseDialect.getFunction(functionName);
    }
    
}
