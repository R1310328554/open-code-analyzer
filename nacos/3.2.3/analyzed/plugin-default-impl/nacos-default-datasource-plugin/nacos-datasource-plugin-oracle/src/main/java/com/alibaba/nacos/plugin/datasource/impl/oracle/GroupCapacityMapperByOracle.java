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
import com.alibaba.nacos.common.utils.NamespaceUtil;
import com.alibaba.nacos.plugin.datasource.constants.DataSourceConstant;
import com.alibaba.nacos.plugin.datasource.constants.FieldConstant;
import com.alibaba.nacos.plugin.datasource.mapper.GroupCapacityMapper;
import com.alibaba.nacos.plugin.datasource.model.MapperContext;
import com.alibaba.nacos.plugin.datasource.model.MapperResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@link GroupCapacityMapper} 的 Oracle 实现。
 *
 * <p>管理默认命名空间下 group 级配置容量：配额、用量增减及从 config_info 统计回填。 分页使用 Oracle {@code FETCH FIRST} 语法。</p>
 *
 * @author liam.fu
 **/
public class GroupCapacityMapperByOracle extends AbstractMapperByOracle
    implements GroupCapacityMapper {
    
    /** 返回 Oracle 数据源标识。 */
    @Override
    public String getDataSource() {
        return DataSourceConstant.ORACLE;
    }
    
    /** 按 id 游标分页扫描 group 容量表。 */
    @Override
    public MapperResult selectGroupInfoBySize(MapperContext context) {
        String sql = "SELECT id, group_id FROM group_capacity WHERE id > ? FETCH FIRST ? ROWS ONLY";
        return new MapperResult(sql, CollectionUtils
            .list(context.getWhereParameter(FieldConstant.ID), context.getPageSize()));
    }
    
    /** 按 group_id 查询容量配额与用量。 */
    @Override
    public MapperResult select(MapperContext context) {
        String sql =
            "SELECT id, quota, usage, max_size, max_aggr_count, max_aggr_size, group_id FROM group_capacity "
                + "WHERE group_id = ?";
        return new MapperResult(sql,
            Collections.singletonList(context.getWhereParameter(FieldConstant.GROUP_ID)));
    }
    
    /** 从 config_info 统计 count 并插入新 group 容量行。 */
    @Override
    public MapperResult insertIntoSelect(MapperContext context) {
        List<Object> paramList = new ArrayList<>();
        paramList.add(context.getUpdateParameter(FieldConstant.GROUP_ID));
        paramList.add(context.getUpdateParameter(FieldConstant.QUOTA));
        paramList.add(context.getUpdateParameter(FieldConstant.MAX_SIZE));
        paramList.add(context.getUpdateParameter(FieldConstant.MAX_AGGR_COUNT));
        paramList.add(context.getUpdateParameter(FieldConstant.MAX_AGGR_SIZE));
        paramList.add(context.getUpdateParameter(FieldConstant.GMT_CREATE));
        paramList.add(context.getUpdateParameter(FieldConstant.GMT_MODIFIED));
        
        String sql =
            "INSERT INTO group_capacity (group_id, quota, usage, max_size, max_aggr_count, max_aggr_size, gmt_create, gmt_modified) "
                + "VALUES (?, ?, (SELECT COUNT(*) FROM config_info), ?, ?, ?, ?, ?)";
        return new MapperResult(sql, paramList);
    }
    
    /** 按 group 与默认 tenant 统计后插入容量行。 */
    @Override
    public MapperResult insertIntoSelectByWhere(MapperContext context) {
        String sql =
            "INSERT INTO group_capacity (group_id, quota, usage, max_size, max_aggr_count, max_aggr_size, gmt_create, gmt_modified) "
                + "VALUES (?, ?, (SELECT COUNT(*) FROM config_info WHERE group_id=? AND tenant_id = '"
                + NamespaceUtil.getNamespaceDefaultId() + "'), ?, ?, ?, ?, ?)";
        
        List<Object> paramList = new ArrayList<>();
        paramList.add(context.getUpdateParameter(FieldConstant.GROUP_ID));
        paramList.add(context.getUpdateParameter(FieldConstant.QUOTA));
        paramList.add(context.getWhereParameter(FieldConstant.GROUP_ID));
        paramList.add(context.getUpdateParameter(FieldConstant.MAX_SIZE));
        paramList.add(context.getUpdateParameter(FieldConstant.MAX_AGGR_COUNT));
        paramList.add(context.getUpdateParameter(FieldConstant.MAX_AGGR_SIZE));
        paramList.add(context.getUpdateParameter(FieldConstant.GMT_CREATE));
        paramList.add(context.getUpdateParameter(FieldConstant.GMT_MODIFIED));
        
        return new MapperResult(sql, paramList);
    }
    
    /** 配额为零时按 max_size 上限递增用量。 */
    @Override
    public MapperResult incrementUsageByWhereQuotaEqualZero(MapperContext context) {
        return new MapperResult(
            "UPDATE group_capacity SET usage = usage + 1, gmt_modified = ? WHERE group_id = ? AND usage < ? AND quota = 0",
            CollectionUtils.list(context.getUpdateParameter(FieldConstant.GMT_MODIFIED),
                context.getWhereParameter(FieldConstant.GROUP_ID),
                context.getWhereParameter(FieldConstant.USAGE)));
    }
    
    /** 配额非零且未超限时将用量 +1。 */
    @Override
    public MapperResult incrementUsageByWhereQuotaNotEqualZero(MapperContext context) {
        return new MapperResult(
            "UPDATE group_capacity SET usage = usage + 1, gmt_modified = ? WHERE group_id = ? AND usage < quota AND quota != 0",
            CollectionUtils.list(context.getUpdateParameter(FieldConstant.GMT_MODIFIED),
                context.getWhereParameter(FieldConstant.GROUP_ID)));
    }
    
    /** 无条件将指定 group 用量 +1。 */
    @Override
    public MapperResult incrementUsageByWhere(MapperContext context) {
        return new MapperResult(
            "UPDATE group_capacity SET usage = usage + 1, gmt_modified = ? WHERE group_id = ?",
            CollectionUtils.list(context.getUpdateParameter(FieldConstant.GMT_MODIFIED),
                context.getWhereParameter(FieldConstant.GROUP_ID)));
    }
    
    /** 用量大于零时将指定 group 用量 -1。 */
    @Override
    public MapperResult decrementUsageByWhere(MapperContext context) {
        return new MapperResult(
            "UPDATE group_capacity SET usage = usage - 1, gmt_modified = ? WHERE group_id = ? AND usage > 0",
            CollectionUtils.list(context.getUpdateParameter(FieldConstant.GMT_MODIFIED),
                context.getWhereParameter(FieldConstant.GROUP_ID)));
    }
    
    /** 按全表 config_info 计数校正 group 用量。 */
    @Override
    public MapperResult updateUsage(MapperContext context) {
        return new MapperResult(
            "UPDATE group_capacity SET usage = (SELECT count(*) FROM config_info), gmt_modified = ? WHERE group_id = ?",
            CollectionUtils.list(context.getUpdateParameter(FieldConstant.GMT_MODIFIED),
                context.getWhereParameter(FieldConstant.GROUP_ID)));
    }
    
    /** 按 group 与默认 tenant 统计校正用量。 */
    @Override
    public MapperResult updateUsageByWhere(MapperContext context) {
        return new MapperResult(
            "UPDATE group_capacity SET usage = (SELECT count(*) FROM config_info WHERE group_id=? AND tenant_id = '"
                + NamespaceUtil.getNamespaceDefaultId() + "'),"
                + " gmt_modified = ? WHERE group_id= ?",
            CollectionUtils.list(context.getWhereParameter(FieldConstant.GROUP_ID),
                context.getUpdateParameter(FieldConstant.GMT_MODIFIED),
                context.getWhereParameter(FieldConstant.GROUP_ID)));
    }
}
