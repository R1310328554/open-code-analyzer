/*
 * Copyright 1999-2026 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.auth.impl;

import com.alibaba.nacos.plugin.auth.impl.configuration.AuthConfigs;
import com.alibaba.nacos.plugin.auth.impl.constant.AuthConstants;
import com.alibaba.nacos.plugin.auth.impl.persistence.PermissionPersistService;
import com.alibaba.nacos.plugin.auth.impl.persistence.RolePersistService;
import com.alibaba.nacos.plugin.auth.impl.persistence.User;
import com.alibaba.nacos.plugin.auth.impl.persistence.UserPersistService;
import com.alibaba.nacos.plugin.auth.impl.utils.PasswordEncoderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.UUID;

/**
 * AI 匿名访问启用时，初始化系统预留的匿名用户、角色与默认权限。
 *
 * <p>流程与 Nacos 管理员用户初始化一致：{@link PostConstruct} 阶段写入用户表、角色绑定及 {@code public:*:ai/*} 只读权限。</p>
 *
 * @author nacos
 */
public class AnonymousAccessInitializer {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AnonymousAccessInitializer.class);
    
    /** 匿名角色默认 AI 资源权限表达式。 */
    private static final String DEFAULT_ANONYMOUS_PERMISSION_RESOURCE = "public:*:ai/*";
    
    /** 匿名默认权限动作：只读（r）。 */
    private static final String DEFAULT_ANONYMOUS_PERMISSION_ACTION = "r";
    
    private final AuthConfigs authConfigs;
    
    private final UserPersistService userPersistService;
    
    private final RolePersistService rolePersistService;
    
    private final PermissionPersistService permissionPersistService;
    
    public AnonymousAccessInitializer(AuthConfigs authConfigs,
        UserPersistService userPersistService,
        RolePersistService rolePersistService, PermissionPersistService permissionPersistService) {
        this.authConfigs = authConfigs;
        this.userPersistService = userPersistService;
        this.rolePersistService = rolePersistService;
        this.permissionPersistService = permissionPersistService;
    }
    
    /** 若开启 AI 匿名访问，则确保匿名用户、角色与默认权限存在。 */

    @PostConstruct
    public void init() {
        if (!authConfigs.isAiAnonymousEnabled()) {
            LOGGER.info("[ANONYMOUS-INIT] AI anonymous access is disabled, skip initialization.");
            return;
        }
        try {
            ensureAnonymousUser();
            ensureAnonymousRole();
            ensureDefaultPermission();
            LOGGER
                .info("[ANONYMOUS-INIT] Anonymous user/role/permission initialized successfully.");
        } catch (Exception e) {
            LOGGER.error("[ANONYMOUS-INIT] Failed to initialize anonymous access", e);
        }
    }
    
    /** 创建匿名用户（密码随机 BCrypt，不可用于登录）。 */
    private void ensureAnonymousUser() {
        User existing = userPersistService.findUserByUsername(AuthConstants.ANONYMOUS_USER);
        if (existing != null) {
            LOGGER.info("[ANONYMOUS-INIT] Anonymous user already exists, skip creation.");
            return;
        }
        String randomPassword = PasswordEncoderUtil.encode(UUID.randomUUID().toString());
        userPersistService.createUser(AuthConstants.ANONYMOUS_USER, randomPassword);
        LOGGER.info("[ANONYMOUS-INIT] Created anonymous user: {}", AuthConstants.ANONYMOUS_USER);
    }
    
    /** 为匿名用户绑定 {@link AuthConstants#ANONYMOUS_ROLE} 角色。 */
    private void ensureAnonymousRole() {
        try {
            rolePersistService.addRole(AuthConstants.ANONYMOUS_ROLE, AuthConstants.ANONYMOUS_USER);
            LOGGER.info("[ANONYMOUS-INIT] Created anonymous role: {}",
                AuthConstants.ANONYMOUS_ROLE);
        } catch (Exception e) {
            LOGGER.debug("[ANONYMOUS-INIT] Anonymous role binding may already exist: {}",
                e.getMessage());
        }
    }
    
    /** 为匿名角色授予 public AI 资源只读权限。 */
    private void ensureDefaultPermission() {
        try {
            permissionPersistService.addPermission(AuthConstants.ANONYMOUS_ROLE,
                DEFAULT_ANONYMOUS_PERMISSION_RESOURCE, DEFAULT_ANONYMOUS_PERMISSION_ACTION);
            LOGGER.info("[ANONYMOUS-INIT] Added default anonymous permission: {} {} {}",
                AuthConstants.ANONYMOUS_ROLE, DEFAULT_ANONYMOUS_PERMISSION_RESOURCE,
                DEFAULT_ANONYMOUS_PERMISSION_ACTION);
        } catch (Exception e) {
            LOGGER.debug("[ANONYMOUS-INIT] Default anonymous permission may already exist: {}",
                e.getMessage());
        }
    }
}
