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

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.plugin.datasource.constants.DataSourceConstant;
import com.alibaba.nacos.plugin.datasource.constants.FieldConstant;
import com.alibaba.nacos.plugin.datasource.mapper.HistoryConfigInfoMapper;
import com.alibaba.nacos.plugin.datasource.model.MapperContext;
import com.alibaba.nacos.plugin.datasource.model.MapperResult;

/**
 * {@link HistoryConfigInfoMapper} 的 Derby 实现。
 *
 * <p>配置历史表 his_config_info 的清理、分页查询与删除事件增量同步。</p>
 *
 * @author hyx
 **/

public class HistoryConfigInfoMapperByDerby extends AbstractMapperByDerby
    implements HistoryConfigInfoMapper {
    
    /** 分批删除早于指定时间的过期历史记录。 */
    @Override
    public MapperResult removeConfigHistory(MapperContext context) {
        String sql = "DELETE FROM his_config_info WHERE nid IN( "
            + "SELECT nid FROM his_config_info WHERE gmt_modified < ? OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY)";
        return new MapperResult(sql,
            CollectionUtils.list(context.getWhereParameter(FieldConstant.START_TIME),
                context.getWhereParameter(FieldConstant.LIMIT_SIZE)));
    }
    
    /** 按 dataId/group/tenant 倒序分页查询配置变更历史。 */
    @Override
    public MapperResult pageFindConfigHistoryFetchRows(MapperContext context) {
        String sql =
            "SELECT nid,data_id,group_id,tenant_id,app_name,src_ip,src_user,op_type,gray_name,ext_info,publish_type,gmt_create,gmt_modified "
                + "FROM his_config_info "
                + "WHERE data_id = ? AND group_id = ? AND tenant_id = ? ORDER BY nid DESC  OFFSET "
                + context.getStartRow() + " ROWS FETCH NEXT " + context.getPageSize()
                + " ROWS ONLY";
        return new MapperResult(sql,
            CollectionUtils.list(context.getWhereParameter(FieldConstant.DATA_ID),
                context.getWhereParameter(FieldConstant.GROUP_ID),
                context.getWhereParameter(FieldConstant.TENANT_ID)));
    }
    
    /** 返回 Derby 数据源标识。 */
    @Override
    public String getDataSource() {
        return DataSourceConstant.DERBY;
    }
    
    /** 增量拉取删除类型（op_type='D'）的历史配置。 */
    @Override
    public MapperResult findDeletedConfig(MapperContext context) {
        return new MapperResult(
            "SELECT id, nid, data_id, group_id, app_name, content, md5, gmt_create, gmt_modified, src_user, src_ip, op_type, tenant_id, "
                + "publish_type,gray_name, ext_info, encrypted_data_key FROM his_config_info WHERE op_type = 'D' AND "
                + "publish_type = ? and gmt_modified >= ? and nid > ? order by nid OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY",
            CollectionUtils.list(context.getWhereParameter(FieldConstant.PUBLISH_TYPE),
                context.getWhereParameter(FieldConstant.START_TIME),
                context.getWhereParameter(FieldConstant.LAST_MAX_ID),
                context.getWhereParameter(FieldConstant.PAGE_SIZE)));
    }
}
