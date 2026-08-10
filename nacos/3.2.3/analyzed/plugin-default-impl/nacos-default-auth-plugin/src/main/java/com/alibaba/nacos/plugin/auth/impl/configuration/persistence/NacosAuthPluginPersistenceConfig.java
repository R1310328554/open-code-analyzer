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

package com.alibaba.nacos.plugin.auth.impl.configuration.persistence;

import org.springframework.context.annotation.Import;

/**
 * 鉴权插件持久化层配置聚合入口。
 *
 * <p>同时导入嵌入式与外部存储配置，由条件注解在运行时择一激活。</p>
 *
 * @author xiweng.yy
 */
@Import({NacosAuthPluginEmbeddedStorageConfig.class, NacosAuthPluginExternalStorageConfig.class})
/** 用户/角色/权限持久化 Bean 配置导入类。 */
public class NacosAuthPluginPersistenceConfig {
    
}
