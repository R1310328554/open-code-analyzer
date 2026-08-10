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
import com.alibaba.nacos.persistence.datasource.DataSourceService;
import com.alibaba.nacos.persistence.datasource.DynamicDataSource;
import com.alibaba.nacos.plugin.auth.impl.persistence.extrnal.AuthExternalPaginationHelperImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.annotation.PostConstruct;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.alibaba.nacos.plugin.auth.impl.persistence.AuthRowMapperManager.ROLE_INFO_ROW_MAPPER;

/**
 * 外部数据源（MySQL 等）角色持久化服务实现。
 *
 * <p>通过 {@link JdbcTemplate} 访问 {@code roles} 表， 支持分页查询、模糊搜索及角色增删；分页委托 {@link AuthExternalPaginationHelperImpl} 按数据源类型适配 SQL。</p>
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class ExternalRolePersistServiceImpl implements RolePersistService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("com.alibaba.nacos.persistence");
    
    /** 动态数据源提供的 JDBC 模板。 */
    private JdbcTemplate jt;
    
    /** 当前数据源类型，用于选择分页适配器。 */
    private String dataSourceType = "";
    
    private static final String PATTERN_STR = "*";
    
    /** 初始化 JDBC 模板与数据源类型。 */
    @PostConstruct
    protected void init() {
        DataSourceService dataSource = DynamicDataSource.getInstance().getDataSource();
        jt = dataSource.getJdbcTemplate();
        dataSourceType = dataSource.getDataSourceType();
    }
    
    /** 分页查询全部角色（按 role 去重计数）。 */
    @Override
    public Page<RoleInfo> getRoles(int pageNo, int pageSize) {
        
        AuthPaginationHelper<RoleInfo> helper = createPaginationHelper();
        
        String sqlCountRows = "SELECT count(*) FROM (SELECT DISTINCT role FROM roles) roles WHERE ";
        
        String sqlFetchRows = "SELECT role,username FROM roles WHERE ";
        
        String where = " 1=1 ";
        
        try {
            Page<RoleInfo> pageInfo = helper.fetchPage(sqlCountRows + where, sqlFetchRows + where,
                new ArrayList<String>().toArray(), pageNo, pageSize, ROLE_INFO_ROW_MAPPER);
            if (pageInfo == null) {
                pageInfo = new Page<>();
                pageInfo.setTotalCount(0);
                pageInfo.setPageItems(new ArrayList<>());
            }
            return pageInfo;
        } catch (CannotGetJdbcConnectionException e) {
            LOGGER.error("[db-error] " + e.toString(), e);
            throw e;
        }
    }
    
    /** 按用户名与角色名精确过滤后分页查询。 */
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
        
        try {
            return helper.fetchPage(sqlCountRows + where, sqlFetchRows + where, params.toArray(),
                pageNo, pageSize,
                ROLE_INFO_ROW_MAPPER);
        } catch (CannotGetJdbcConnectionException e) {
            LOGGER.error("[db-error] " + e.toString(), e);
            throw e;
        }
    }
    
    /**
     * 向 {@code roles} 表插入用户-角色绑定记录。
     *
     * @param role     role string value.
     * @param userName username string value.
     */
    @Override
    public void addRole(String role, String userName) {
        
        String sql = "INSERT INTO roles (role, username) VALUES (?, ?)";
        
        try {
            jt.update(sql, role, userName);
        } catch (CannotGetJdbcConnectionException e) {
            LOGGER.error("[db-error] " + e.toString(), e);
            throw e;
        }
    }
    
    /**
     * 按角色名删除该角色的全部绑定记录。
     *
     * @param role role string value.
     */
    @Override
    public void deleteRole(String role) {
        String sql = "DELETE FROM roles WHERE role=?";
        try {
            jt.update(sql, role);
        } catch (CannotGetJdbcConnectionException e) {
            LOGGER.error("[db-error] " + e.toString(), e);
            throw e;
        }
    }
    
    /**
     * 删除指定用户与角色的单条绑定。
     *
     * @param role     role string value.
     * @param username username string value.
     */
    @Override
    public void deleteRole(String role, String username) {
        String sql = "DELETE FROM roles WHERE role=? AND username=?";
        try {
            jt.update(sql, role, username);
        } catch (CannotGetJdbcConnectionException e) {
            LOGGER.error("[db-error] " + e.toString(), e);
            throw e;
        }
    }
    
    /** 按角色名模糊匹配，返回角色名列表。 */
    @Override
    public List<String> findRolesLikeRoleName(String role) {
        String sql = "SELECT role FROM roles WHERE role LIKE ?";
        List<String> users =
            this.jt.queryForList(sql, new String[] {String.format("%%%s%%", role)}, String.class);
        return users;
    }
    
    /** 将通配符 {@code *} 转为 SQL {@code %}，并转义下划线。 */
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
    
    /** 用户名与角色名模糊查询并分页。 */
    @Override
    public Page<RoleInfo> findRolesLike4Page(String username, String role, int pageNo,
        int pageSize) {
        String sqlCountRows = "SELECT count(*) FROM roles";
        String sqlFetchRows = "SELECT role, username FROM roles";
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
        
        AuthPaginationHelper<RoleInfo> helper = createPaginationHelper();
        try {
            return helper.fetchPage(sqlCountRows + where, sqlFetchRows + where, params.toArray(),
                pageNo, pageSize,
                ROLE_INFO_ROW_MAPPER);
        } catch (CannotGetJdbcConnectionException e) {
            LOGGER.error("[db-error] " + e.toString(), e);
            throw e;
        }
    }
    
    /** 创建外部数据源鉴权分页助手。 */
    @Override
    public <E> AuthPaginationHelper<E> createPaginationHelper() {
        return new AuthExternalPaginationHelperImpl<>(jt, dataSourceType);
    }
    
    private static final class RoleInfoRowMapper implements RowMapper<RoleInfo> {
        
        @Override
        public RoleInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            RoleInfo roleInfo = new RoleInfo();
            roleInfo.setRole(rs.getString("role"));
            roleInfo.setUsername(rs.getString("username"));
            return roleInfo;
        }
    }
}
