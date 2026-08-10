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

import com.alibaba.nacos.plugin.datasource.constants.DataSourceConstant;
import com.alibaba.nacos.plugin.datasource.mapper.TenantInfoMapper;

/**
 * {@link TenantInfoMapper} 的 Derby 实现。
 *
 * <p>租户元数据 Mapper 的 Derby 绑定，CRUD SQL 由基类 {@link AbstractMapperByDerby} 提供。</p>
 *
 * @author hyx
 **/

public class TenantInfoMapperByDerby extends AbstractMapperByDerby implements TenantInfoMapper {
    
    /** 返回 Derby 嵌入式数据源平台标识。 */
    @Override
    public String getDataSource() {
        return DataSourceConstant.DERBY;
    }
}
