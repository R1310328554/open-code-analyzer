/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.auth.impl.configuration.core;

import com.alibaba.nacos.plugin.auth.impl.AnonymousAccessInitializer;
import com.alibaba.nacos.plugin.auth.impl.configuration.AuthConfigs;
import com.alibaba.nacos.plugin.auth.impl.condition.ConditionOnInnerDatasource;
import com.alibaba.nacos.plugin.auth.impl.persistence.PermissionPersistService;
import com.alibaba.nacos.plugin.auth.impl.persistence.RolePersistService;
import com.alibaba.nacos.plugin.auth.impl.persistence.UserPersistService;
import com.alibaba.nacos.plugin.auth.impl.roles.NacosRoleService;
import com.alibaba.nacos.plugin.auth.impl.roles.NacosRoleServiceDirectImpl;
import com.alibaba.nacos.plugin.auth.impl.users.NacosUserService;
import com.alibaba.nacos.plugin.auth.impl.users.NacosUserServiceDirectImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Import;

/**
 * 内嵌数据源模式下的鉴权业务服务 Bean 配置。
 *
 * <p>注册直连数据库的用户、角色服务及 AI 匿名访问初始化器， 适用于 Console 内嵌部署且本地持久化用户/角色/权限数据。</p>
 *
 * @author xiweng.yy
 */
@Import({AuthConfigs.class})
@Conditional(ConditionOnInnerDatasource.class)
public class NacosAuthPluginInnerServiceConfig {
    
    /** 创建直连持久化层的角色服务实现。 */
    @Bean
    public NacosRoleService nacosRoleService(AuthConfigs authConfigs,
        RolePersistService rolePersistService,
        NacosUserService userDetailsService, PermissionPersistService permissionPersistService) {
        return new NacosRoleServiceDirectImpl(authConfigs, rolePersistService, userDetailsService,
            permissionPersistService);
    }
    
    /** 创建直连持久化层的用户详情服务。 */
    @Bean
    public NacosUserService nacosUserService(AuthConfigs authConfigs,
        UserPersistService userPersistService) {
        return new NacosUserServiceDirectImpl(authConfigs, userPersistService);
    }
    
    /** 注册 AI 公共资源匿名用户/角色初始化器。 */
    @Bean
    public AnonymousAccessInitializer anonymousAccessInitializer(AuthConfigs authConfigs,
        UserPersistService userPersistService, RolePersistService rolePersistService,
        PermissionPersistService permissionPersistService) {
        return new AnonymousAccessInitializer(authConfigs, userPersistService, rolePersistService,
            permissionPersistService);
    }
}
