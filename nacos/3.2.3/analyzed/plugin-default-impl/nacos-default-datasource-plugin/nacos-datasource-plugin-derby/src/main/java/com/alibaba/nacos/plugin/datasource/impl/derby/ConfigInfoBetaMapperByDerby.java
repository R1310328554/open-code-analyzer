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
import com.alibaba.nacos.plugin.datasource.mapper.ConfigInfoBetaMapper;
import com.alibaba.nacos.plugin.datasource.model.MapperContext;
import com.alibaba.nacos.plugin.datasource.model.MapperResult;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link ConfigInfoBetaMapper} 的 Derby 实现。
 *
 * <p>Beta 配置全量导出时使用子查询 + 关联方式分页，避免 Derby 大结果集一次性加载。</p>
 *
 * @author hyx
 **/

public class ConfigInfoBetaMapperByDerby extends AbstractMapperByDerby
    implements ConfigInfoBetaMapper {
    
    /** 分页拉取 Beta 配置用于全量 dump 导出。 */
    @Override
    public MapperResult findAllConfigInfoBetaForDumpAllFetchRows(MapperContext context) {
        Integer startRow = context.getStartRow();
        int pageSize = context.getPageSize();
        
        String sql =
            "SELECT t.id,data_id,group_id,tenant_id,app_name,content,md5,gmt_modified,beta_ips "
                + " FROM (  SELECT id FROM config_info_beta ORDER BY id OFFSET " + startRow
                + " ROWS FETCH NEXT "
                + pageSize + " ROWS ONLY  )" + " g, config_info_beta t WHERE g.id = t.id";
        
        List<Object> paramList = new ArrayList<>();
        paramList.add(startRow);
        paramList.add(pageSize);
        
        return new MapperResult(sql, paramList);
    }
    
    /** 返回 Derby 数据源标识。 */
    @Override
    public String getDataSource() {
        return DataSourceConstant.DERBY;
    }
}
