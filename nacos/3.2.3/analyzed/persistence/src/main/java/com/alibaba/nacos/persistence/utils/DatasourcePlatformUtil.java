/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.persistence.utils;

import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.persistence.constants.PersistenceConstant;
import com.alibaba.nacos.sys.env.EnvUtil;

/**
 * 读取持久化层数据源平台（derby/mysql 等）的工具类。
 *
 * <p>优先读取新配置项，兼容旧版 property 名称。</p>
 *
 * @author lixiaoshuang
 */
public class DatasourcePlatformUtil {
    
    /**
     * 从环境变量/配置中获取数据源平台标识。
     *
     * <p>新键 {@link PersistenceConstant#DATASOURCE_PLATFORM_PROPERTY} 为空时回退到 {@link PersistenceConstant#DATASOURCE_PLATFORM_PROPERTY_OLD}。</p>
     *
     * @param defaultPlatform default platform.
     * @return
     */
    public static String getDatasourcePlatform(String defaultPlatform) {
        String platform =
            EnvUtil.getProperty(PersistenceConstant.DATASOURCE_PLATFORM_PROPERTY, defaultPlatform);
        // 新配置项未设置时使用旧版 spring.datasource.platform
            platform = EnvUtil.getProperty(PersistenceConstant.DATASOURCE_PLATFORM_PROPERTY_OLD,
                defaultPlatform);
        }
        return platform;
    }
}
