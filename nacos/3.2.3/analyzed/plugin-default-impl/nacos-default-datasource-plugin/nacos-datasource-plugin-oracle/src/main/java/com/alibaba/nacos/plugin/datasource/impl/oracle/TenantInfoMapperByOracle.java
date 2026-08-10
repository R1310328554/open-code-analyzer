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

package com.alibaba.nacos.plugin.datasource.impl.oracle;

import com.alibaba.nacos.plugin.datasource.constants.DataSourceConstant;
import com.alibaba.nacos.plugin.datasource.mapper.TenantInfoMapper;

/**
 * {@link TenantInfoMapper} 的 Oracle 实现。
 *
 * <p>租户元数据 Mapper 的 Oracle 数据源绑定，CRUD SQL 由基类 {@link AbstractMapperByOracle} 提供。</p>
 *
 * @author liam.fu
 **/
public class TenantInfoMapperByOracle extends AbstractMapperByOracle implements TenantInfoMapper {
    
    /** 返回 Oracle 数据源标识。 */
    @Override
    public String getDataSource() {
        return DataSourceConstant.ORACLE;
    }
}
