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

package com.alibaba.nacos.plugin.auth.impl.configuration.autoconfiguration;

import com.alibaba.nacos.plugin.auth.impl.condition.ConditionOnRemoteDatasource;
import com.alibaba.nacos.plugin.auth.impl.configuration.core.NacosAuthPluginCoreConfig;
import com.alibaba.nacos.plugin.auth.impl.configuration.core.NacosAuthPluginRemoteServiceConfig;
import com.alibaba.nacos.plugin.auth.impl.configuration.persistence.NacosAuthPluginPersistenceConfig;
import com.alibaba.nacos.plugin.auth.impl.configuration.web.NacosAuthPluginControllerConfig;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 远程数据源部署模式下的鉴权插件自动配置入口。
 *
 * <p>在 Console 与 Server 分离、通过远程 API 访问用户/角色/权限数据时生效， 导入持久化、远程服务、核心安全与 Controller 配置。</p>
 *
 * @author xiweng.yy
 */
@Configuration
@Conditional(ConditionOnRemoteDatasource.class)
@Import({NacosAuthPluginPersistenceConfig.class, NacosAuthPluginRemoteServiceConfig.class,
    NacosAuthPluginCoreConfig.class, NacosAuthPluginControllerConfig.class})
/** 远程数据源鉴权插件 Spring 配置聚合类。 */
public class NacosAuthPluginRemoteAutoConfig {
    
}
