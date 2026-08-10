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

package com.alibaba.nacos.config.server.service;

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.config.server.service.repository.ConfigInfoPersistService;
import com.alibaba.nacos.core.cluster.health.AbstractModuleHealthChecker;
import com.alibaba.nacos.core.utils.Loggers;
import org.springframework.stereotype.Service;

/**
 * 配置模块就绪探针：通过 {@link ConfigInfoPersistService#configInfoCount} 探测数据库连通性，
 * 供集群健康检查在流量接入前确认配置持久化层可用。
 * Readiness check service for config module.
 *
 * @author xiweng.yy
 */
@Service
public class ConfigReadinessCheckService extends AbstractModuleHealthChecker {
    
    private final ConfigInfoPersistService configInfoPersistService;
    
    /**
     * 注入配置持久化服务，用于就绪态数据库探活。
     *
     * @param configInfoPersistService 配置信息持久化服务
     */
    public ConfigReadinessCheckService(ConfigInfoPersistService configInfoPersistService) {
        this.configInfoPersistService = configInfoPersistService;
    }
    
    /** 就绪检查：数据库 count 成功返回 true，异常记录日志并返回 false。 */
    @Override
    public boolean readiness() {
        // 执行轻量 count 查询验证数据库可用
        try {
            configInfoPersistService.configInfoCount("");
            return true;
        } catch (Exception e) {
            Loggers.CLUSTER.error("Config health check fail.", e);
        }
        return false;
    }
    
    /** 返回配置模块标识 {@link Constants.Config#CONFIG_MODULE}。 */
    @Override
    public String getModuleName() {
        return Constants.Config.CONFIG_MODULE;
    }
}
