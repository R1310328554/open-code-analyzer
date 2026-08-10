/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.auth.impl.persistence;

import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.persistence.repository.embedded.EmbeddedStorageContextHolder;
import com.alibaba.nacos.persistence.repository.embedded.operate.DatabaseOperate;
import com.alibaba.nacos.plugin.auth.impl.persistence.embedded.AuthEmbeddedPaginationHelperImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.alibaba.nacos.plugin.auth.impl.persistence.AuthRowMapperManager.PERMISSION_ROW_MAPPER;

/**
 * 内嵌 Derby 数据源下的权限持久化实现。
 *
 * <p>permissions 表无自增主键，通过 role+resource+action 组合唯一标识权限记录。</p>
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class EmbeddedPermissionPersistServiceImpl implements PermissionPersistService {
    
    private final DatabaseOperate databaseOperate;
    
    /** 模糊搜索通配符（映射为 SQL LIKE %）。 */
    private static final String PATTERN_STR = "*";
    
    /** Derby LIKE 子句转义后缀。 */
    private static final String SQL_DERBY_ESCAPE_BACK_SLASH_FOR_LIKE = " ESCAPE '\\' ";
    
    /** 注入内嵌 {@link DatabaseOperate}。 */
    public EmbeddedPermissionPersistServiceImpl(DatabaseOperate databaseOperate) {
        this.databaseOperate = databaseOperate;
    }
    
    /** 按角色精确分页查询权限列表。 */
    @Override
    public Page<PermissionInfo> getPermissions(String role, int pageNo, int pageSize) {
        AuthPaginationHelper<PermissionInfo> helper = createPaginationHelper();
        
        String sqlCountRows = "SELECT count(*) FROM permissions WHERE ";
        
        String sqlFetchRows = "SELECT role,resource,action FROM permissions WHERE ";
        
        String where = " role= ? ";
        List<String> params = new ArrayList<>();
        if (StringUtils.isNotBlank(role)) {
            params = Collections.singletonList(role);
        } else {
            where = " 1=1 ";
        }
        
        Page<PermissionInfo> pageInfo =
            helper.fetchPage(sqlCountRows + where, sqlFetchRows + where, params.toArray(),
                pageNo, pageSize, PERMISSION_ROW_MAPPER);
        
        if (pageInfo == null) {
            pageInfo = new Page<>();
            pageInfo.setTotalCount(0);
            pageInfo.setPageItems(new ArrayList<>());
        }
        return pageInfo;
    }
    
    /** 向 permissions 表插入一条角色权限记录。 */

    @Override
    public void addPermission(String role, String resource, String action) {
        String sql = "INSERT INTO permissions (role, resource, action) VALUES (?, ?, ?)";
        EmbeddedStorageContextHolder.addSqlContext(sql, role, resource, action);
        databaseOperate.blockUpdate();
    }
    
    /** 按 role+resource+action 删除权限记录。 */

    @Override
    public void deletePermission(String role, String resource, String action) {
        String sql = "DELETE FROM permissions WHERE role=? AND resource=? AND action=?";
        EmbeddedStorageContextHolder.addSqlContext(sql, role, resource, action);
        databaseOperate.blockUpdate();
    }
    
    /** 按角色名模糊分页查询权限。 */
    @Override
    public Page<PermissionInfo> findPermissionsLike4Page(String role, int pageNo, int pageSize) {
        AuthPaginationHelper<PermissionInfo> helper = createPaginationHelper();
        
        String sqlCountRows = "SELECT count(*) FROM permissions ";
        
        String sqlFetchRows = "SELECT role,resource,action FROM permissions ";
        
        StringBuilder where = new StringBuilder(" WHERE 1=1");
        List<String> params = new ArrayList<>();
        if (StringUtils.isNotBlank(role)) {
            where.append(" AND role LIKE ?");
            where.append(SQL_DERBY_ESCAPE_BACK_SLASH_FOR_LIKE);
            params.add(generateLikeArgument(role));
        }
        
        Page<PermissionInfo> pageInfo =
            helper.fetchPage(sqlCountRows + where, sqlFetchRows + where, params.toArray(),
                pageNo, pageSize, PERMISSION_ROW_MAPPER);
        
        if (pageInfo == null) {
            pageInfo = new Page<>();
            pageInfo.setTotalCount(0);
            pageInfo.setPageItems(new ArrayList<>());
        }
        return pageInfo;
    }
    
    /** 将 * 转为 %、转义 _ 以适配 SQL LIKE。 */
    @Override
    public String generateLikeArgument(String s) {
        String underscore = "_";
        if (s.contains(underscore)) {
            s = s.replaceAll(underscore, "\\\\_");
        }
        String fuzzySearchSign = "\\*";
        String sqlLikePercentSign = "%";
        if (s.contains(PATTERN_STR)) {
            return s.replaceAll(fuzzySearchSign, sqlLikePercentSign);
        } else {
            return s;
        }
    }
    
    /** 创建内嵌数据源分页助手。 */
    @Override
    public <E> AuthPaginationHelper<E> createPaginationHelper() {
        return new AuthEmbeddedPaginationHelperImpl<>(databaseOperate);
    }
}
