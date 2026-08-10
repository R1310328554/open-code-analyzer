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
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.plugin.datasource.constants.FieldConstant;
import com.alibaba.nacos.plugin.datasource.constants.TableConstant;
import com.alibaba.nacos.plugin.datasource.model.MapperContext;
import com.alibaba.nacos.plugin.datasource.model.MapperResult;

import java.util.Collections;
import java.util.List;

/**
 * 历史配置信息 Mapper 接口。
 *
 * <p>负责 {@code his_config_info} 表的 SQL 映射，支持历史清理、
 * 变更记录查询、删除配置追踪及灰度历史遍历。</p>
 *
 * @author hyx
 **/

public interface HistoryConfigInfoMapper extends Mapper {
    
    /**
     * 删除指定时间之前的历史数据。默认 SQL：DELETE FROM his_config_info WHERE gmt_modified &lt; ? LIMIT ?
     *
     * @param context SQL 参数映射
     * @return 历史清理 SQL 及参数
     */
    MapperResult removeConfigHistory(MapperContext context);
    
    /**
     * 统计指定时间之前的配置历史条数。默认 SQL：SELECT count(*) FROM his_config_info WHERE gmt_modified &lt; ?
     *
     * @param context SQL 参数映射
     * @return 历史计数 SQL 及参数
     */
    default MapperResult findConfigHistoryCountByTime(MapperContext context) {
        return new MapperResult("SELECT count(*) FROM his_config_info WHERE gmt_modified < ?",
            Collections.singletonList(context.getWhereParameter(FieldConstant.START_TIME)));
    }
    
    /**
     * 查询已删除配置记录。默认 SQL：SELECT DISTINCT data_id, group_id, tenant_id FROM his_config_info WHERE
     * op_type = 'D' AND gmt_modified &gt;= ? AND gmt_modified &lt;= ?
     *
     * @param context SQL 参数映射
     * @return 删除配置查询 SQL 及参数
     */
    default MapperResult findDeletedConfig(MapperContext context) {
        return new MapperResult(
            "SELECT id, nid, data_id, group_id, app_name, content, md5, gmt_create, gmt_modified, src_user, src_ip, op_type, tenant_id, "
                + "publish_type, gray_name, ext_info, encrypted_data_key FROM his_config_info WHERE op_type = 'D' AND "
                + "publish_type = ? and gmt_modified >= ? and nid > ? order by nid limit ? ",
            CollectionUtils.list(context.getWhereParameter(FieldConstant.PUBLISH_TYPE),
                context.getWhereParameter(FieldConstant.START_TIME),
                context.getWhereParameter(FieldConstant.LAST_MAX_ID),
                context.getWhereParameter(FieldConstant.PAGE_SIZE)));
    }
    
    /**
     * 列出指定配置的历史变更记录。默认 SQL：SELECT
     * nid,data_id,group_id,tenant_id,app_name,src_ip,src_user,op_type,gmt_create,gmt_modified FROM his_config_info
     * WHERE data_id = ? AND group_id = ? AND tenant_id = ? ORDER BY nid DESC
     *
     * @param context SQL 参数映射
     * @return 历史变更列表 SQL 及参数
     */
    default MapperResult findConfigHistoryFetchRows(MapperContext context) {
        return new MapperResult(
            "SELECT nid,data_id,group_id,tenant_id,app_name,src_ip,src_user,publish_type,gray_name,op_type,"
                + "gmt_create,gmt_modified FROM his_config_info "
                + "WHERE data_id = ? AND group_id = ? AND tenant_id = ? ORDER BY nid DESC",
            CollectionUtils.list(context.getWhereParameter(FieldConstant.DATA_ID),
                context.getWhereParameter(FieldConstant.GROUP_ID),
                context.getWhereParameter(FieldConstant.TENANT_ID)));
    }
    
    /**
     * 分页查询配置历史记录。SELECT
     * nid,data_id,group_id,tenant_id,app_name,src_ip,src_user,op_type,gmt_create,gmt_modified FROM his_config_info
     * WHERE data_id = ? AND group_id = ? AND tenant_id = ? ORDER BY nid DESC limit ?,?
     *
     * @param context 含 pageNo 等分页参数
     * @return 分页历史查询 SQL 及参数
     */
    MapperResult pageFindConfigHistoryFetchRows(MapperContext context);
    
    /**
     * 获取上一版本配置详情。默认 SQL：SELECT
     * nid,data_id,group_id,tenant_id,app_name,content,md5,src_user,src_ip,op_type,gmt_create,gmt_modified FROM
     * his_config_info WHERE nid = (SELECT max(nid) FROM his_config_info WHERE id = ?)
     *
     * @param context SQL 参数映射
     * @return 上一版本详情 SQL 及参数
     */
    default MapperResult detailPreviousConfigHistory(MapperContext context) {
        return new MapperResult(
            "SELECT nid,data_id,group_id,tenant_id,app_name,content,md5,src_user,src_ip,op_type,publish_type,gray_name,ext_info,gmt_create"
                + ",gmt_modified,encrypted_data_key FROM his_config_info WHERE nid = (SELECT max(nid) FROM his_config_info WHERE id = ?)",
            Collections.singletonList(context.getWhereParameter(FieldConstant.ID)));
    }
    
    /**
     * 获取返回表名.
     *
     * @return 表名
     */
    default String getTableName() {
        return TableConstant.HIS_CONFIG_INFO;
    }
    
    /**
     * 获取指定 nid 之后下一条历史配置详情。默认 SQL：SELECT
     * nid,data_id,group_id,tenant_id,app_name,content,md5,src_user,src_ip,op_type,gmt_create,gmt_modified FROM
     * his_config_info WHERE data_id = ? AND group_id = ? AND tenant_id = ? AND publish_type = ? AND gray_name = ?
     * AND nid &gt; ? ORDER BY nid LIMIT 1
     *
     * @param context SQL 参数映射
     * @return 下一条历史详情 SQL 及参数
     */
    default MapperResult getNextHistoryInfo(MapperContext context) {
        String sql =
            "SELECT nid,data_id,group_id,tenant_id,app_name,content,md5,src_user,src_ip,op_type,publish_type,"
                + "gray_name,ext_info,gmt_create,gmt_modified,encrypted_data_key FROM his_config_info "
                + "WHERE data_id = ? AND group_id = ? AND tenant_id = ? AND publish_type = ? "
                + (StringUtils.isBlank(context.getContextParameter(FieldConstant.GRAY_NAME)) ? ""
                    : "AND gray_name = ? ")
                + "AND nid > ? ORDER BY nid LIMIT 1";
        
        List<Object> paramList = CollectionUtils.list(
            context.getWhereParameter(FieldConstant.DATA_ID),
            context.getWhereParameter(FieldConstant.GROUP_ID),
            context.getWhereParameter(FieldConstant.TENANT_ID),
            context.getWhereParameter(FieldConstant.PUBLISH_TYPE),
            context.getWhereParameter(FieldConstant.NID));
        if (!StringUtils.isEmpty(context.getContextParameter(FieldConstant.GRAY_NAME))) {
            paramList.add(4, context.getWhereParameter(FieldConstant.GRAY_NAME));
        }
        
        return new MapperResult(sql, paramList);
    }
}
