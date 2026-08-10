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
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

import static com.alibaba.nacos.plugin.auth.impl.persistence.AuthRowMapperManager.USER_ROW_MAPPER;

/**
 * 外部数据源用户持久化服务实现。
 *
 * <p>操作 {@code users} 表完成用户 CRUD、密码更新与分页/模糊查询； 默认新建用户 {@code enabled=true}，查询无结果时返回 {@code null}。</p>
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class ExternalUserPersistServiceImpl implements UserPersistService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("com.alibaba.nacos.persistence");
    
    private JdbcTemplate jt;
    
    private String dataSourceType = "";
    
    private static final String PATTERN_STR = "*";
    
    /** 从动态数据源获取 JDBC 模板与类型。 */
    @PostConstruct
    protected void init() {
        DataSourceService dataSource = DynamicDataSource.getInstance().getDataSource();
        jt = dataSource.getJdbcTemplate();
        dataSourceType = dataSource.getDataSourceType();
    }
    
    /**
     * 创建用户并写入加密前的密码字段。
     *
     * @param username username string value.
     * @param password password string value.
     */
    @Override
    public void createUser(String username, String password) {
        String sql = "INSERT INTO users (username, password, enabled) VALUES (?, ?, ?)";
        
        try {
            jt.update(sql, username, password, true);
        } catch (CannotGetJdbcConnectionException e) {
            LOGGER.error("[db-error] " + e.toString(), e);
            throw e;
        }
    }
    
    /**
     * 按用户名删除用户记录。
     *
     * @param username username string value.
     */
    @Override
    public void deleteUser(String username) {
        String sql = "DELETE FROM users WHERE username=?";
        try {
            jt.update(sql, username);
        } catch (CannotGetJdbcConnectionException e) {
            LOGGER.error("[db-error] " + e.toString(), e);
            throw e;
        }
    }
    
    /**
     * 更新指定用户的密码。
     *
     * @param username username string value.
     * @param password password string value.
     */
    @Override
    public void updateUserPassword(String username, String password) {
        try {
            jt.update("UPDATE users SET password = ? WHERE username=?", password, username);
        } catch (CannotGetJdbcConnectionException e) {
            LOGGER.error("[db-error] " + e.toString(), e);
            throw e;
        }
    }
    
    /**
     * 按用户名精确查询单个用户。
     *
     * @param username username string value.
     * @return User model.
     */
    @Override
    public User findUserByUsername(String username) {
        String sql = "SELECT username,password FROM users WHERE username=? ";
        try {
            return this.jt.queryForObject(sql, new Object[] {username}, USER_ROW_MAPPER);
        } catch (CannotGetJdbcConnectionException e) {
            LOGGER.error("[db-error] " + e.toString(), e);
            throw e;
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (Exception e) {
            LOGGER.error("[db-other-error]" + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
    
    /** 分页查询用户，可选按用户名精确过滤。 */
    @Override
    public Page<User> getUsers(int pageNo, int pageSize, String username) {
        
        AuthPaginationHelper<User> helper = createPaginationHelper();
        
        String sqlCountRows = "SELECT count(*) FROM users ";
        
        String sqlFetchRows = "SELECT username,password FROM users ";
        
        StringBuilder where = new StringBuilder(" WHERE 1 = 1 ");
        List<String> params = new ArrayList<>();
        if (StringUtils.isNotBlank(username)) {
            where.append(" AND username = ? ");
            params.add(username);
        }
        
        try {
            Page<User> pageInfo = helper.fetchPage(sqlCountRows + where, sqlFetchRows + where,
                params.toArray(), pageNo,
                pageSize, USER_ROW_MAPPER);
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
    
    /** 用户名模糊匹配，返回用户名列表。 */
    @Override
    public List<String> findUserLikeUsername(String username) {
        String sql = "SELECT username FROM users WHERE username LIKE ?";
        List<String> users = this.jt.queryForList(sql,
            new String[] {String.format("%%%s%%", username)}, String.class);
        return users;
    }
    
    /** 用户名模糊查询并分页返回用户实体。 */
    @Override
    public Page<User> findUsersLike4Page(String username, int pageNo, int pageSize) {
        String sqlCountRows = "SELECT count(*) FROM users ";
        String sqlFetchRows = "SELECT username,password FROM users ";
        
        StringBuilder where = new StringBuilder(" WHERE 1 = 1 ");
        List<String> params = new ArrayList<>();
        if (StringUtils.isNotBlank(username)) {
            where.append(" AND username LIKE ? ");
            params.add(generateLikeArgument(username));
        }
        
        AuthPaginationHelper<User> helper = createPaginationHelper();
        try {
            return helper.fetchPage(sqlCountRows + where, sqlFetchRows + where, params.toArray(),
                pageNo, pageSize,
                USER_ROW_MAPPER);
        } catch (CannotGetJdbcConnectionException e) {
            LOGGER.error("[db-error] " + e.toString(), e);
            throw e;
        }
    }
    
    /** 构造 SQL LIKE 参数字符串（通配符与转义处理）。 */
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
    
    /** 创建外部数据源分页助手实例。 */
    @Override
    public <E> AuthPaginationHelper<E> createPaginationHelper() {
        return new AuthExternalPaginationHelperImpl<>(jt, dataSourceType);
    }
}
