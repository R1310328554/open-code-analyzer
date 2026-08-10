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

package com.alibaba.nacos.naming.cluster;

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.core.cluster.health.AbstractModuleHealthChecker;
import com.alibaba.nacos.core.utils.Loggers;
import org.springframework.stereotype.Service;

/**
 * 命名模块就绪探针：供 K8s/负载均衡判断节点是否可接收流量。
 *
 * <p>继承 {@link AbstractModuleHealthChecker}，当 {@link ServerStatus} 为 {@link ServerStatus#UP} 时视为就绪。</p>
 *
 * @author xiweng.yy
 */
@Service
public class NamingReadinessCheckService extends AbstractModuleHealthChecker {
    
    /** 命名服务端状态管理器。 */
    private final ServerStatusManager serverStatusManager;
    
    public NamingReadinessCheckService(ServerStatusManager serverStatusManager) {
        this.serverStatusManager = serverStatusManager;
    }
    
    /** 检查当前节点命名服务是否处于 UP 状态。 */
    @Override
    public boolean readiness() {
        try {
            return ServerStatus.UP.equals(serverStatusManager.getServerStatus());
        } catch (Exception e) {
            Loggers.CLUSTER.error("Naming health check fail.", e);
        }
        return false;
    }
    
    /** 返回命名模块标识常量。 */
    @Override
    public String getModuleName() {
        return Constants.Naming.NAMING_MODULE;
    }
}
