/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.config.server.configuration;

import com.alibaba.nacos.config.server.constant.PropertiesConstant;
import com.alibaba.nacos.core.config.AbstractDynamicConfig;
import com.alibaba.nacos.sys.env.EnvUtil;

/**
 * Config 兼容模式动态配置：控制命名空间 ID 是否启用旧版兼容处理，
 * 对应属性 {@link com.alibaba.nacos.config.server.constant.PropertiesConstant#NAMESPACE_COMPATIBLE_MODE}。
 * The type Config compatible config.
 *
 * @author Sunrisea
 */
public class ConfigCompatibleConfig extends AbstractDynamicConfig {
    
    /** 动态配置注册名 */
    private static final String CONFIG_NAME = "configCompatible";
    
    /** 命名空间兼容模式开关，默认开启以平滑迁移旧客户端 */
    private boolean namespaceCompatibleMode = true;
    
    private static final ConfigCompatibleConfig INSTANCE = new ConfigCompatibleConfig();
    
    protected ConfigCompatibleConfig() {
        super(CONFIG_NAME);
        resetConfig();
    }
    
    /** 是否启用命名空间兼容模式 */
    public boolean isNamespaceCompatibleMode() {
        return namespaceCompatibleMode;
    }
    
    @Override
    protected void getConfigFromEnv() {
        namespaceCompatibleMode =
            EnvUtil.getProperty(PropertiesConstant.NAMESPACE_COMPATIBLE_MODE, Boolean.class, true);
    }
    
    @Override
    protected String printConfig() {
        return "ConfigCompatibleConfig{" + "namespaceCompatibleMode=" + namespaceCompatibleMode
            + '}';
    }
    
    /** 获取兼容配置单例 */
    public static ConfigCompatibleConfig getInstance() {
        return INSTANCE;
    }
}
