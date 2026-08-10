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

package com.alibaba.nacos.plugin.datasource.impl.postgresql;

import com.alibaba.nacos.plugin.datasource.constants.DatabaseTypeConstant;
import com.alibaba.nacos.plugin.datasource.impl.enums.postgresql.TrustedPostgresqlFunctionEnum;
import com.alibaba.nacos.plugin.datasource.impl.base.BaseConfigInfoMapper;

/**
 * {@link com.alibaba.nacos.plugin.datasource.mapper.ConfigInfoMapper} 的 PostgreSQL 实现。
 *
 * <p>配置中心核心表的 PostgreSQL SQL 方言适配，通用 CRUD 与分页逻辑继承自 {@link BaseConfigInfoMapper}。</p>
 *
 * @author Long Yu
 **/
public class ConfigInfoMapperByPostgresql extends BaseConfigInfoMapper {
    
    /** 返回 PostgreSQL 数据源类型标识。 */
    @Override
    public String getDataSource() {
        return DatabaseTypeConstant.POSTGRESQL;
    }
    
    /** 从 PostgreSQL 可信函数白名单解析 SQL 函数片段。 */
    @Override
    public String getFunction(String functionName) {
        return TrustedPostgresqlFunctionEnum.getFunctionByName(functionName);
    }
}
