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

package com.alibaba.nacos.plugin.datasource.impl.mysql;

import com.alibaba.nacos.plugin.datasource.constants.DataSourceConstant;
import com.alibaba.nacos.plugin.datasource.mapper.ConfigInfoTagMapper;
import com.alibaba.nacos.plugin.datasource.model.MapperContext;
import com.alibaba.nacos.plugin.datasource.model.MapperResult;

import java.util.Collections;

/**
 * {@link ConfigInfoTagMapper} 的 MySQL 实现。
 *
 * <p>带标签配置全量导出分页查询，通过子查询先分页 id 再关联主表。</p>
 *
 * @author hyx
 **/

public class ConfigInfoTagMapperByMySql extends AbstractMapperByMysql
    implements ConfigInfoTagMapper {
    
    /** 分页拉取带标签配置用于全量 dump。 */
    @Override
    public MapperResult findAllConfigInfoTagForDumpAllFetchRows(MapperContext context) {
        String sql =
            " SELECT t.id,data_id,group_id,tenant_id,tag_id,app_name,content,md5,gmt_modified "
                + " FROM (  SELECT id FROM config_info_tag  ORDER BY id LIMIT "
                + context.getStartRow() + ","
                + context.getPageSize() + " ) " + "g, config_info_tag t  WHERE g.id = t.id  ";
        return new MapperResult(sql, Collections.emptyList());
    }
    
    /** 返回 MySQL 数据源类型标识。 */
    @Override
    public String getDataSource() {
        return DataSourceConstant.MYSQL;
    }
}
