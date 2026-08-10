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
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.persistence.repository.embedded.EmbeddedStorageContextHolder;
import com.alibaba.nacos.persistence.repository.embedded.operate.DatabaseOperate;
import com.alibaba.nacos.plugin.auth.impl.persistence.embedded.AuthEmbeddedPaginationHelperImpl;

import java.util.ArrayList;
import java.util.List;

import static com.alibaba.nacos.plugin.auth.impl.persistence.AuthRowMapperManager.ROLE_INFO_ROW_MAPPER;

/**
 * 内嵌 Derby 数据源下的角色持久化实现。
 *
 * <p>roles 表记录用户与角色的多对多绑定，无自增主键。</p>
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class EmbeddedRolePersistServiceImpl implements RolePersistService {
    
    private final DatabaseOperate databaseOperate;
    
    /** 模糊搜索通配符。 */
    private static final String PATTERN_STR = "*";
    
    private static final String SQL_DERBY_ESCAPE_BACK_SLASH_FOR_LIKE = " ESCAPE '\\' ";
    
    /** 注入内嵌 {@link DatabaseOperate}。 */
    public EmbeddedRolePersistServiceImpl(DatabaseOperate databaseOperate) {
        this.databaseOperate = databaseOperate;
    }
    
    /** 分页查询全部角色（去重 role 计数）。 */
    @Override
    public Page<RoleInfo> getRoles(int pageNo, int pageSize) {
        
        AuthPaginationHelper<RoleInfo> helper = createPaginationHelper();
        
        String sqlCountRows = "SELECT count(*) FROM (SELECT DISTINCT role FROM roles) roles WHERE ";
        
        String sqlFetchRows = "SELECT role,username FROM roles WHERE ";
        
        String where = " 1=1 ";
        
        Page<RoleInfo> pageInfo = helper.fetchPage(sqlCountRows + where, sqlFetchRows + where,
            new ArrayList<String>().toArray(), pageNo, pageSize, ROLE_INFO_ROW_MAPPER);
        if (pageInfo == null) {
            pageInfo = new Page<>();
            pageInfo.setTotalCount(0);
            pageInfo.setPageItems(new ArrayList<>());
        }
        return pageInfo;
        
    }
    
    /** 按用户名与角色名精确分页查询。 */
    @Override
    public Page<RoleInfo> getRolesByUserNameAndRoleName(String username, String role, int pageNo,
        int pageSize) {
        
        AuthPaginationHelper<RoleInfo> helper = createPaginationHelper();
        
        String sqlCountRows = "SELECT count(*) FROM roles ";
        
        String sqlFetchRows = "SELECT role,username FROM roles ";
        
        StringBuilder where = new StringBuilder(" WHERE 1 = 1 ");
        List<String> params = new ArrayList<>();
        if (StringUtils.isNotBlank(username)) {
            where.append(" AND username = ? ");
            params.add(username);
        }
        if (StringUtils.isNotBlank(role)) {
            where.append(" AND role = ? ");
            params.add(role);
        }
        
        return helper.fetchPage(sqlCountRows + where, sqlFetchRows + where, params.toArray(),
            pageNo, pageSize,
            ROLE_INFO_ROW_MAPPER);
        
    }
    
    /** 插入用户角色绑定记录。 */

    @Override
    public void addRole(String role, String userName) {
        
        String sql = "INSERT INTO roles (role, username) VALUES (?, ?)";
        
        try {
            EmbeddedStorageContextHolder.addSqlContext(sql, role, userName);
            databaseOperate.update(EmbeddedStorageContextHolder.getCurrentSqlContext());
        } finally {
            EmbeddedStorageContextHolder.cleanAllContext();
        }
    }
    
    /** 删除指定角色下全部用户绑定。 */

    @Override
    public void deleteRole(String role) {
        String sql = "DELETE FROM roles WHERE role=?";
        try {
            EmbeddedStorageContextHolder.addSqlContext(sql, role);
            databaseOperate.update(EmbeddedStorageContextHolder.getCurrentSqlContext());
        } finally {
            EmbeddedStorageContextHolder.cleanAllContext();
        }
    }
    
    /** 删除指定用户在某角色下的绑定。 */

    @Override
    public void deleteRole(String role, String username) {
        String sql = "DELETE FROM roles WHERE role=? AND username=?";
        try {
            EmbeddedStorageContextHolder.addSqlContext(sql, role, username);
            databaseOperate.update(EmbeddedStorageContextHolder.getCurrentSqlContext());
        } finally {
            EmbeddedStorageContextHolder.cleanAllContext();
        }
    }
    
    /** 按角色名模糊查询匹配的角色名列表。 */
    @Override
    public List<String> findRolesLikeRoleName(String role) {
        String sql =
            "SELECT role FROM roles WHERE role LIKE ? " + SQL_DERBY_ESCAPE_BACK_SLASH_FOR_LIKE;
        return databaseOperate.queryMany(sql, new String[] {"%" + role + "%"}, String.class);
    }
    
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
    
    /** 按用户名/角色名模糊分页查询。 */
    @Override
    public Page<RoleInfo> findRolesLike4Page(String username, String role, int pageNo,
        int pageSize) {
        StringBuilder where = new StringBuilder(" WHERE 1 = 1 ");
        List<String> params = new ArrayList<>();
        
        if (StringUtils.isNotBlank(username)) {
            where.append(" AND username LIKE ? ");
            params.add(generateLikeArgument(username));
        }
        if (StringUtils.isNotBlank(role)) {
            where.append(" AND role LIKE ? ");
            params.add(generateLikeArgument(role));
        }
        if (CollectionUtils.isNotEmpty(params)) {
            where.append(SQL_DERBY_ESCAPE_BACK_SLASH_FOR_LIKE);
        }
        String sqlCountRows = "SELECT count(*) FROM roles";
        String sqlFetchRows = "SELECT role, username FROM roles";
        
        AuthPaginationHelper<RoleInfo> helper = createPaginationHelper();
        return helper.fetchPage(sqlCountRows + where, sqlFetchRows + where, params.toArray(),
            pageNo, pageSize,
            ROLE_INFO_ROW_MAPPER);
    }
    
    @Override
    public <E> AuthPaginationHelper<E> createPaginationHelper() {
        return new AuthEmbeddedPaginationHelperImpl<>(databaseOperate);
    }
}
