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

package com.alibaba.nacos.plugin.encryption;

import com.alibaba.nacos.api.plugin.PluginProvider;
import com.alibaba.nacos.api.plugin.PluginType;
import com.alibaba.nacos.plugin.encryption.spi.EncryptionPluginService;

import java.util.Map;

/**
 * 加密插件 Provider 实现。
 *
 * <p>向 Nacos 插件框架暴露 {@link PluginType#ENCRYPTION} 类型
 * 及 {@link EncryptionPluginManager} 中已加载的全部加密服务。</p>
 *
 * @author WangzJi
 * @since 3.2.0
 */
public class EncryptionPluginProvider implements PluginProvider<EncryptionPluginService> {
    
    @Override
    public PluginType getPluginType() {
        return PluginType.ENCRYPTION;
    }
    
    @Override
    public Map<String, EncryptionPluginService> getAllPlugins() {
        return EncryptionPluginManager.instance().getAllPlugins();
    }
}
