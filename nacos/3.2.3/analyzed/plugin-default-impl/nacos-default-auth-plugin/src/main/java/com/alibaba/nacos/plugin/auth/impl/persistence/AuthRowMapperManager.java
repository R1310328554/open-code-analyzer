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

import com.alibaba.nacos.persistence.repository.RowMapperManager;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 鉴权插件 JDBC {@link RowMapper} 注册与管理。
 *
 * <p>静态块将用户、角色、权限 RowMapper 注册到 {@link RowMapperManager} 供全局复用。</p>
 *
 * @author xiweng.yy
 */
public class AuthRowMapperManager {
    
    /** 用户表 ResultSet → {@link User} 映射器。 */
    public static final RowMapper<User> USER_ROW_MAPPER = new UserRowMapper();
    
    /** 角色表 ResultSet → {@link RoleInfo} 映射器。 */
    public static final RoleInfoRowMapper ROLE_INFO_ROW_MAPPER = new RoleInfoRowMapper();
    
    /** 权限表 ResultSet → {@link PermissionInfo} 映射器。 */
    public static final PermissionRowMapper PERMISSION_ROW_MAPPER = new PermissionRowMapper();
    
    static {
        // 注册用户 RowMapper
        RowMapperManager.registerRowMapper(USER_ROW_MAPPER.getClass().getCanonicalName(),
            USER_ROW_MAPPER);
        
        // 注册角色 RowMapper
        RowMapperManager.registerRowMapper(ROLE_INFO_ROW_MAPPER.getClass().getCanonicalName(),
            ROLE_INFO_ROW_MAPPER);
        
        // 注册权限 RowMapper
        RowMapperManager.registerRowMapper(PERMISSION_ROW_MAPPER.getClass().getCanonicalName(),
            PERMISSION_ROW_MAPPER);
    }
    
    /** 将 users 表行映射为 {@link User}。 */
    public static final class UserRowMapper implements RowMapper<User> {
        
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setUsername(rs.getString("username"));
            user.setPassword(rs.getString("password"));
            return user;
        }
    }
    
    /** 将 roles 表行映射为 {@link RoleInfo}。 */
    public static final class RoleInfoRowMapper implements RowMapper<RoleInfo> {
        
        @Override
        public RoleInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            RoleInfo roleInfo = new RoleInfo();
            roleInfo.setRole(rs.getString("role"));
            roleInfo.setUsername(rs.getString("username"));
            return roleInfo;
        }
    }
    
    /** 将 permissions 表行映射为 {@link PermissionInfo}。 */
    public static final class PermissionRowMapper implements RowMapper<PermissionInfo> {
        
        @Override
        public PermissionInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            PermissionInfo info = new PermissionInfo();
            info.setResource(rs.getString("resource"));
            info.setAction(rs.getString("action"));
            info.setRole(rs.getString("role"));
            return info;
        }
    }
}
