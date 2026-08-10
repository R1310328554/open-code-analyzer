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

import com.alibaba.nacos.plugin.auth.impl.condition.ConditionOnInnerDatasource;
import com.alibaba.nacos.plugin.auth.impl.configuration.core.NacosAuthPluginCoreConfig;
import com.alibaba.nacos.plugin.auth.impl.configuration.core.NacosAuthPluginInnerServiceConfig;
import com.alibaba.nacos.plugin.auth.impl.configuration.persistence.NacosAuthPluginPersistenceConfig;
import com.alibaba.nacos.plugin.auth.impl.configuration.web.NacosAuthPluginWebConfig;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 内嵌数据源部署模式下的鉴权插件自动配置入口。
 *
 * <p>在 {@code merged} 或 {@code server} 部署且数据源为 Console 内嵌时生效， 聚合持久化、内嵌服务、核心安全与 Web 层配置。</p>
 *
 * @author xiweng.yy
 */
@Configuration
@Conditional(ConditionOnInnerDatasource.class)
@Import({NacosAuthPluginPersistenceConfig.class, NacosAuthPluginInnerServiceConfig.class,
    NacosAuthPluginCoreConfig.class, NacosAuthPluginWebConfig.class})
/** 内嵌数据源鉴权插件 Spring 配置聚合类。 */
public class NacosAuthPluginInnerAutoConfig {
    
}
