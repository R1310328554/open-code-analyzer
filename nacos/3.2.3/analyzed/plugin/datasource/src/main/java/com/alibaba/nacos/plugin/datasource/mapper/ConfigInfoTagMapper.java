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

package com.alibaba.nacos.plugin.datasource.mapper;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.plugin.datasource.constants.FieldConstant;
import com.alibaba.nacos.plugin.datasource.constants.TableConstant;
import com.alibaba.nacos.plugin.datasource.model.MapperContext;
import com.alibaba.nacos.plugin.datasource.model.MapperResult;

/**
 * 配置标签信息 Mapper 接口。
 *
 * <p>负责 {@code config_info_tag} 表的 SQL 映射，支持带标签的配置 CAS 更新及全量导出分页查询。</p>
 *
 * @author hyx
 **/

public interface ConfigInfoTagMapper extends Mapper {
    
    /**
     * 按标签 CAS 更新配置内容。
     * 默认 SQL：
     * UPDATE config_info_tag SET content=?, md5 = ?, src_ip=?,src_user=?,gmt_modified=?,app_name=? WHERE
     * data_id=? AND group_id=? AND tenant_id=? AND tag_id=? AND (md5=? or md5 is null or md5='')
     *
     * @param context SQL 参数映射
     * @return 更新标签配置的 SQL 及参数列表
     */
    default MapperResult updateConfigInfo4TagCas(MapperContext context) {
        Object content = context.getUpdateParameter(FieldConstant.CONTENT);
        Object md5 = context.getUpdateParameter(FieldConstant.MD5);
        Object srcIp = context.getUpdateParameter(FieldConstant.SRC_IP);
        Object srcUser = context.getUpdateParameter(FieldConstant.SRC_USER);
        Object gmtModified = context.getUpdateParameter(FieldConstant.GMT_MODIFIED);
        Object appName = context.getUpdateParameter(FieldConstant.APP_NAME);
        
        Object dataId = context.getWhereParameter(FieldConstant.DATA_ID);
        Object groupId = context.getWhereParameter(FieldConstant.GROUP_ID);
        Object tenantId = context.getWhereParameter(FieldConstant.TENANT_ID);
        Object tagId = context.getWhereParameter(FieldConstant.TAG_ID);
        Object oldMd5 = context.getWhereParameter(FieldConstant.MD5);
        String sql =
            "UPDATE config_info_tag SET content = ?, md5 = ?, src_ip = ?,src_user = ?,gmt_modified = ?,app_name = ? "
                + "WHERE data_id = ? AND group_id = ? AND tenant_id = ? AND tag_id = ? AND (md5 = ? OR md5 IS NULL OR md5 = '')";
        return new MapperResult(sql,
            CollectionUtils.list(content, md5, srcIp, srcUser, gmtModified, appName, dataId,
                groupId, tenantId,
                tagId, oldMd5));
    }
    
    /**
     * 分页查询全部标签配置，供 dump 任务使用。
     * 默认 SQL：
     * SELECT t.id,data_id,group_id,tenant_id,tag_id,app_name,content,md5,gmt_modified
     * FROM (  SELECT id FROM config_info_tag  ORDER BY id LIMIT startRow,pageSize ) g,
     * config_info_tag t  WHERE g.id = t.id
     *
     * @param context 分页起始行等查询参数
     * @return 分页导出标签配置的 SQL 及参数
     */
    MapperResult findAllConfigInfoTagForDumpAllFetchRows(MapperContext context);
    
    /**
     * 获取返回表名.
     *
     * @return 表名
     */
    default String getTableName() {
        return TableConstant.CONFIG_INFO_TAG;
    }
}
