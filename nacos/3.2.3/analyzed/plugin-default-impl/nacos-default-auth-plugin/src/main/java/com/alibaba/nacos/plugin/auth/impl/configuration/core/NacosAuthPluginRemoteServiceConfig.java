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

import com.alibaba.nacos.plugin.auth.impl.configuration.AuthConfigs;
import com.alibaba.nacos.plugin.auth.impl.condition.ConditionOnRemoteDatasource;
import com.alibaba.nacos.plugin.auth.impl.roles.NacosRoleService;
import com.alibaba.nacos.plugin.auth.impl.roles.NacosRoleServiceRemoteImpl;
import com.alibaba.nacos.plugin.auth.impl.users.NacosUserService;
import com.alibaba.nacos.plugin.auth.impl.users.NacosUserServiceRemoteImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Import;

/**
 * 远程数据源模式下的鉴权业务服务 Bean 配置。
 *
 * <p>注册通过 HTTP/RPC 访问 Server 侧用户与角色数据的远程实现， 适用于 Console 与 Server 分离部署场景。</p>
 *
 * @author xiweng.yy
 */
@Import({AuthConfigs.class})
@Conditional(ConditionOnRemoteDatasource.class)
public class NacosAuthPluginRemoteServiceConfig {
    
    /** 创建远程角色服务实现。 */
    @Bean
    public NacosRoleService nacosRoleService(AuthConfigs authConfigs) {
        return new NacosRoleServiceRemoteImpl(authConfigs);
    }
    
    /** 创建远程用户详情服务实现。 */
    @Bean
    public NacosUserService nacosUserService(AuthConfigs authConfigs) {
        return new NacosUserServiceRemoteImpl(authConfigs);
    }
}
