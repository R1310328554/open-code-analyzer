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

package com.alibaba.nacos.plugin.datasource.mapper;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.plugin.datasource.constants.FieldConstant;
import com.alibaba.nacos.plugin.datasource.constants.TableConstant;
import com.alibaba.nacos.plugin.datasource.model.MapperContext;
import com.alibaba.nacos.plugin.datasource.model.MapperResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 配置迁移 Mapper 接口。
 *
 * <p>支持将空租户（{@code ''}）配置迁移至 {@code public} 命名空间，
 * 涵盖正式配置与灰度配置的冲突检测、增量扫描及批量插入。</p>
 *
 * @author Sunrisea
 */
public interface ConfigMigrateMapper extends Mapper {
    
    /**
     * 统计 public 与空租户间存在 MD5 冲突的配置数量。
     *
     * @param context 查询上下文，含 {@code src_user} 等条件
     * @return 冲突配置计数 SQL 及参数
     */
    default MapperResult getConfigConflictCount(MapperContext context) {
        String sql = "SELECT COUNT(*) AS count FROM config_info ci1"
            + " WHERE ci1.tenant_id = 'public' AND (ci1.src_user <> ? OR ci1.src_user IS NULL) "
            + " AND EXISTS (SELECT 1 FROM config_info ci2"
            + " WHERE ci2.data_id = ci1.data_id AND ci2.group_id = ci1.group_id AND ci2.md5 <> ci1.md5"
            + " AND ci2.tenant_id = '' AND (ci2.src_user <> ? OR ci2.src_user IS NULL))";
        Object srcUser = context.getWhereParameter(FieldConstant.SRC_USER);
        return new MapperResult(sql, CollectionUtils.list(srcUser, srcUser));
    }
    
    /**
     * 分页查找需从空租户插入到 public 的配置 ID。
     *
     * @param context 含游标 ID 与分页大小
     * @return 待插入配置 ID 查询 SQL 及参数
     */
    default MapperResult findConfigIdNeedInsertMigrate(MapperContext context) {
        String sql = "SELECT ci.id FROM config_info ci WHERE ci.tenant_id = '' AND NOT EXISTS "
            + " ( SELECT 1 FROM config_info ci2  WHERE ci2.data_id = ci.data_id AND ci2.group_id = ci.group_id AND ci2.tenant_id = 'public' )"
            + " AND ci.id > ?" + " ORDER BY ci.id LIMIT ?";
        return new MapperResult(sql,
            CollectionUtils.list(context.getWhereParameter(FieldConstant.ID),
                context.getPageSize()));
    }
    
    /**
     * 分页查找需从空租户更新到 public 的配置（public 侧内容较旧）。
     *
     * @param context 含源/目标租户、src_user、游标 ID 与分页大小
     * @return 待更新配置查询 SQL 及参数
     */
    default MapperResult findConfigNeedUpdateMigrate(MapperContext context) {
        String sql = "SELECT ci.id, ci.data_id, ci.group_id, ci.tenant_id"
            + " FROM config_info ci WHERE ci.tenant_id = ? AND "
            + " (ci.src_user <> ? OR ci.src_user IS NULL) AND EXISTS "
            + " ( SELECT 1 FROM config_info ci2 WHERE ci2.data_id = ci.data_id AND ci2.group_id = ci.group_id "
            + " AND ci2.tenant_id = ? AND ci2.src_user = ? AND ci2.md5 <> ci.md5 "
            + " AND ci2.gmt_modified < ci.gmt_modified )"
            + " AND id > ?" + " ORDER BY id LIMIT ?";
        return new MapperResult(sql,
            CollectionUtils.list(context.getWhereParameter(FieldConstant.SRC_TENANT),
                context.getWhereParameter(FieldConstant.SRC_USER),
                context.getWhereParameter(FieldConstant.TARGET_TENANT),
                context.getWhereParameter(FieldConstant.SRC_USER),
                context.getWhereParameter(FieldConstant.ID),
                context.getPageSize()));
    }
    
    /**
     * 分页查找需从空租户更新到 public 的灰度配置。
     *
     * @param context 含源/目标租户、src_user、游标 ID 与分页大小
     * @return 待更新灰度配置查询 SQL 及参数
     */
    default MapperResult findConfigGrayNeedUpdateMigrate(MapperContext context) {
        String sql = "SELECT ci.id, ci.data_id, ci.group_id, ci.tenant_id, ci.gray_name "
            + " FROM config_info_gray ci WHERE ci.tenant_id = ? AND "
            + " (ci.src_user <> ? OR ci.src_user IS NULL) AND EXISTS "
            + " ( SELECT 1 FROM config_info_gray ci2 WHERE ci2.data_id = ci.data_id AND ci2.group_id = ci.group_id "
            + " AND ci2.gray_name = ci.gray_name AND ci2.tenant_id = ? AND ci2.src_user = ? AND ci2.md5 <> ci.md5 "
            + " AND ci2.gmt_modified < ci.gmt_modified )"
            + " AND ci.id > ?" + " ORDER BY ci.id LIMIT ?";
        return new MapperResult(sql,
            CollectionUtils.list(context.getWhereParameter(FieldConstant.SRC_TENANT),
                context.getWhereParameter(FieldConstant.SRC_USER),
                context.getWhereParameter(FieldConstant.TARGET_TENANT),
                context.getWhereParameter(FieldConstant.SRC_USER),
                context.getWhereParameter(FieldConstant.ID),
                context.getPageSize()));
    }
    
    /**
     * 按 ID 列表将空租户配置批量插入 public 命名空间。
     *
     * @param context 含待迁移 ID 列表与 src_user
     * @return INSERT…SELECT 迁移 SQL 及参数
     */
    default MapperResult migrateConfigInsertByIds(MapperContext context) {
        ArrayList<Object> paramList = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "INSERT INTO config_info (data_id, group_id, content, md5, src_user, src_ip, "
                + "app_name, tenant_id, c_desc, type, encrypted_data_key) "
                + "select data_id, group_id, content, md5, ?, src_ip, "
                + "app_name, 'public', c_desc, type, encrypted_data_key from config_info WHERE ");
        sql.append("id IN (");
        List<Long> ids = (List<Long>) context.getWhereParameter(FieldConstant.IDS);
        paramList.add(context.getWhereParameter(FieldConstant.SRC_USER));
        for (int i = 0; i < ids.size(); i++) {
            sql.append("? ");
            if (i < ids.size() - 1) {
                sql.append(", ");
            }
            paramList.add(ids.get(i));
        }
        sql.append(") ");
        return new MapperResult(sql.toString(), paramList);
    }
    
    /**
     * 统计 public 与空租户间灰度配置 MD5 冲突数量。
     *
     * @param context 查询上下文，含 {@code src_user} 等条件
     * @return 灰度冲突计数 SQL 及参数
     */
    default MapperResult getConfigGrayConflictCount(MapperContext context) {
        String sql =
            "SELECT COUNT(*) AS count FROM config_info_gray ci1"
                + " WHERE ci1.tenant_id = 'public' AND (ci1.src_user <> ? OR ci1.src_user IS NULL)"
                + " AND EXISTS (SELECT 1 FROM config_info_gray ci2"
                + " WHERE ci2.data_id = ci1.data_id AND ci2.group_id = ci1.group_id AND ci2.gray_name = ci1.gray_name"
                + " AND ci2.tenant_id = '' AND ci2.md5 <> ci1.md5 AND (ci2.src_user <> ? OR ci2.src_user IS NULL))";
        Object srcUser = context.getWhereParameter(FieldConstant.SRC_USER);
        return new MapperResult(sql, CollectionUtils.list(srcUser, srcUser));
    }
    
    /**
     * 分页查找需从空租户插入到 public 的灰度配置 ID。
     *
     * @param context 含游标 ID 与分页大小
     * @return 待插入灰度配置 ID 查询 SQL 及参数
     */
    default MapperResult findConfigGrayIdNeedInsertMigrate(MapperContext context) {
        String sql = "SELECT ci.id FROM config_info_gray ci WHERE ci.tenant_id = '' AND NOT EXISTS "
            + " ( SELECT 1 FROM config_info_gray ci2  WHERE ci2.data_id = ci.data_id AND ci2.group_id = ci.group_id"
            + " AND ci2.tenant_id = 'public' AND ci2.gray_name = ci.gray_name )" + " AND ci.id > ?"
            + " ORDER BY ci.id LIMIT ?";
        return new MapperResult(sql,
            CollectionUtils.list(context.getWhereParameter(FieldConstant.ID),
                context.getPageSize()));
    }
    
    /**
     * 按 ID 列表将空租户灰度配置批量插入 public 命名空间。
     *
     * @param context 含待迁移 ID 列表与 src_user
     * @return 灰度配置 INSERT…SELECT 迁移 SQL 及参数
     */
    default MapperResult migrateConfigGrayInsertByIds(MapperContext context) {
        StringBuilder sql = new StringBuilder(
            "INSERT INTO config_info_gray (data_id, group_id, content, md5, src_user, src_ip, "
                + "app_name, tenant_id, gray_name, gray_rule, encrypted_data_key) "
                + "select data_id, group_id, content, md5, ?, src_ip, "
                + "app_name, 'public', gray_name, gray_rule, encrypted_data_key from config_info_gray WHERE ");
        sql.append("id IN (");
        ArrayList<Object> paramList = new ArrayList<>();
        List<Long> ids = (List<Long>) context.getWhereParameter(FieldConstant.IDS);
        paramList.add(context.getWhereParameter(FieldConstant.SRC_USER));
        for (int i = 0; i < ids.size(); i++) {
            sql.append("? ");
            if (i < ids.size() - 1) {
                sql.append(", ");
            }
            paramList.add(ids.get(i));
        }
        sql.append(") ");
        return new MapperResult(sql.toString(), paramList);
    }
    
    /**
     * 获取逻辑表名；{@code migrate_config} 表实际不存在，仅用于满足 Mapper 接口约定。
     *
     * @return 迁移逻辑表名常量
     */
    @Override
    default String getTableName() {
        return TableConstant.MIGRATE_CONFIG;
    }
    
}
