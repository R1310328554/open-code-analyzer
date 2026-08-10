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

package com.alibaba.nacos.core.plugin.sync;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.core.plugin.condition.ConditionOnStandaloneMode;
import com.alibaba.nacos.core.plugin.storage.PluginPersistenceException;
import com.alibaba.nacos.core.plugin.storage.PluginStatePersistenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 单机模式插件状态同步器：先应用变更再写入本地文件，不进行集群同步。
 * 仅在 {@code nacos.standalone=true} 时由 {@link ConditionOnStandaloneMode} 激活。
 * Standalone plugin state synchronizer.
 * Only persists state locally without cluster synchronization.
 * Only activated in standalone mode (nacos.standalone=true).
 *
 * @author WangzJi
 * @since 3.2.0
 */
@Component
@Conditional(ConditionOnStandaloneMode.class)
public class StandalonePluginStateSynchronizer implements PluginStateSynchronizer {
    
    private static final Logger LOGGER =
        LoggerFactory.getLogger(StandalonePluginStateSynchronizer.class);
    
    /** 本地插件状态持久化服务。 */
    private final PluginStatePersistenceService persistence;
    
    /** 插件状态运行时应用器。 */
    private final PluginStateApplier applier;
    
    /** 注入持久化服务与应用器。 */
    public StandalonePluginStateSynchronizer(PluginStatePersistenceService persistence,
        PluginStateApplier applier) {
        this.persistence = persistence;
        this.applier = applier;
        LOGGER.info("[StandalonePluginStateSynchronizer] Initialized in standalone mode");
    }
    
    /** 应用状态变更并持久化到本地 JSON 文件。 */
    @Override
    public void syncStateChange(String pluginId, boolean enabled) throws NacosApiException {
        try {
            applier.applyStateChange(pluginId, enabled);
            persistence.saveState(pluginId, enabled);
        } catch (PluginPersistenceException e) {
            throw new NacosApiException(NacosException.SERVER_ERROR, ErrorCode.SERVER_ERROR, e,
                "Failed to persist plugin state: " + pluginId);
        }
    }
    
    /** 应用配置变更并持久化到本地 JSON 文件。 */
    @Override
    public void syncConfigChange(String pluginId, Map<String, String> config)
        throws NacosApiException {
        try {
            applier.applyConfigChange(pluginId, config);
            persistence.saveConfig(pluginId, config);
        } catch (PluginPersistenceException e) {
            throw new NacosApiException(NacosException.SERVER_ERROR, ErrorCode.SERVER_ERROR, e,
                "Failed to persist plugin config: " + pluginId);
        }
    }
}
