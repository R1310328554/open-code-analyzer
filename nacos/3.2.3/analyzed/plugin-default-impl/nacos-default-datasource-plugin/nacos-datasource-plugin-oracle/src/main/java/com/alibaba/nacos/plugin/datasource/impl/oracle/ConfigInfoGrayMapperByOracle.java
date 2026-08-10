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

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.plugin.datasource.constants.DataSourceConstant;
import com.alibaba.nacos.plugin.datasource.constants.FieldConstant;
import com.alibaba.nacos.plugin.datasource.mapper.ConfigInfoGrayMapper;
import com.alibaba.nacos.plugin.datasource.model.MapperContext;
import com.alibaba.nacos.plugin.datasource.model.MapperResult;

import java.util.Collections;

/**
 * {@link ConfigInfoGrayMapper} 的 Oracle 实现。
 *
 * <p>灰度配置表的 dump 分页与增量变更拉取，使用 Oracle OFFSET/FETCH 与 FETCH FIRST 语法。</p>
 *
 * @author liam.fu
 **/
public class ConfigInfoGrayMapperByOracle extends AbstractMapperByOracle
    implements ConfigInfoGrayMapper {
    
    /** 按修改时间与上次最大 id 增量拉取灰度配置变更。 */
    @Override
    public MapperResult findChangeConfig(MapperContext context) {
        String sql =
            "SELECT id, data_id, group_id, tenant_id, app_name,content,gray_name,gray_rule,md5, gmt_modified, encrypted_data_key "
                + "FROM config_info_gray WHERE "
                + "gmt_modified >= ? and id > ? order by id fetch first ? rows only";
        return new MapperResult(sql,
            CollectionUtils.list(context.getWhereParameter(FieldConstant.START_TIME),
                context.getWhereParameter(FieldConstant.LAST_MAX_ID),
                context.getWhereParameter(FieldConstant.PAGE_SIZE)));
    }
    
    /** 分页拉取灰度配置用于全量 dump。 */
    @Override
    public MapperResult findAllConfigInfoGrayForDumpAllFetchRows(MapperContext context) {
        String sql =
            " SELECT id,data_id,group_id,tenant_id,gray_name,gray_rule,app_name,content,md5,gmt_modified "
                + " FROM  config_info_gray  ORDER BY id OFFSET " + context.getStartRow()
                + " ROWS FETCH NEXT " + context.getPageSize() + " ROWS ONLY";
        return new MapperResult(sql, Collections.emptyList());
    }
    
    /** 返回 Oracle 数据源标识。 */
    @Override
    public String getDataSource() {
        return DataSourceConstant.ORACLE;
    }
}
