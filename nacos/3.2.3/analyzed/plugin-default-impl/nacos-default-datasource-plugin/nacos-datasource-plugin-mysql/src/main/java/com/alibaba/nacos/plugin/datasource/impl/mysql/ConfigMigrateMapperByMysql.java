/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.datasource.impl.mysql;

import com.alibaba.nacos.plugin.datasource.constants.DataSourceConstant;
import com.alibaba.nacos.plugin.datasource.mapper.ConfigMigrateMapper;

/**
 * {@link ConfigMigrateMapper} 的 MySQL 实现。
 *
 * <p>配置迁移相关 Mapper 的 MySQL 数据源绑定，SQL 语句继承自接口默认定义。</p>
 *
 * @author Sunrisea
 */
public class ConfigMigrateMapperByMysql extends AbstractMapperByMysql
    implements ConfigMigrateMapper {
    
    /** 返回 MySQL 数据源类型标识。 */
    @Override
    public String getDataSource() {
        return DataSourceConstant.MYSQL;
    }
}
