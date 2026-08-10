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

import com.alibaba.nacos.plugin.datasource.dialect.DatabaseDialect;
import com.alibaba.nacos.plugin.datasource.manager.DatabaseDialectManager;
import com.alibaba.nacos.plugin.datasource.mapper.AbstractMapper;
import com.alibaba.nacos.plugin.datasource.mapper.TenantInfoMapper;

/**
 * {@link TenantInfoMapper} 抽象基类。
 *
 * <p>租户信息 Mapper 的方言适配基类， 子类继承 {@link AbstractMapper} 并实现具体 SQL；本类仅封装 {@link DatabaseDialect} 函数解析。</p>
 *
 * @author Long Yu
 **/
public abstract class BaseTenantInfoMapper extends AbstractMapper implements TenantInfoMapper {
    
    /** 当前数据源的数据库方言。 */
    private DatabaseDialect databaseDialect;
    
    /** 根据数据源类型初始化方言。 */
    public BaseTenantInfoMapper() {
        databaseDialect = DatabaseDialectManager.getInstance().getDialect(getDataSource());
    }
    
    /** 委托方言将逻辑函数名映射为数据库原生函数。 */
    @Override
    public String getFunction(String functionName) {
        return databaseDialect.getFunction(functionName);
    }
    
}
