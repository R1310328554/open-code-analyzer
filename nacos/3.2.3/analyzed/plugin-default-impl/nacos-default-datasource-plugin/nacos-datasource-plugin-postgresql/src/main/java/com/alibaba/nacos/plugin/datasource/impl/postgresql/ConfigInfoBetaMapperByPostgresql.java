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
import com.alibaba.nacos.plugin.datasource.impl.base.BaseConfigInfoBetaMapper;

/**
 * {@link com.alibaba.nacos.plugin.datasource.mapper.ConfigInfoBetaMapper} 的 PostgreSQL 实现。
 *
 * <p>Beta 配置 Mapper 的 PostgreSQL 数据源绑定，SQL 语句继承自基类 {@link BaseConfigInfoBetaMapper}。</p>
 *
 * @author Long Yu
 **/

public class ConfigInfoBetaMapperByPostgresql extends BaseConfigInfoBetaMapper {
    
    /** 返回 PostgreSQL 数据源类型标识。 */
    @Override
    public String getDataSource() {
        return DatabaseTypeConstant.POSTGRESQL;
    }
    
}
