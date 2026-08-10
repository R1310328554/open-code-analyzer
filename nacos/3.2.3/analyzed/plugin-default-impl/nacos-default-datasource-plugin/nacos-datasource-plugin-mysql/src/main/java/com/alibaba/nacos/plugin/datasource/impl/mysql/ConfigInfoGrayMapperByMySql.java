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

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.plugin.datasource.constants.DataSourceConstant;
import com.alibaba.nacos.plugin.datasource.mapper.ConfigInfoGrayMapper;
import com.alibaba.nacos.plugin.datasource.model.MapperContext;
import com.alibaba.nacos.plugin.datasource.model.MapperResult;

/**
 * {@link ConfigInfoGrayMapper} 的 MySQL 实现。
 *
 * <p>灰度配置全量导出分页查询，按 id 排序并使用 {@code LIMIT ?,?} 分页。</p>
 *
 * @author rong
 **/

public class ConfigInfoGrayMapperByMySql extends AbstractMapperByMysql
    implements ConfigInfoGrayMapper {
    
    /** 分页拉取灰度配置用于全量 dump。 */
    @Override
    public MapperResult findAllConfigInfoGrayForDumpAllFetchRows(MapperContext context) {
        String sql =
            " SELECT id,data_id,group_id,tenant_id,gray_name,gray_rule,app_name,content,md5,gmt_modified "
                + " FROM  config_info_gray  ORDER BY id LIMIT ?,?";
        return new MapperResult(sql,
            CollectionUtils.list(context.getStartRow(), context.getPageSize()));
    }
    
    /** 返回 MySQL 数据源类型标识。 */
    @Override
    public String getDataSource() {
        return DataSourceConstant.MYSQL;
    }
}
