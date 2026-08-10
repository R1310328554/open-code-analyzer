/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.naming.healthcheck.heartbeat;

import com.alibaba.nacos.common.task.AbstractExecuteTask;
import com.alibaba.nacos.naming.consistency.KeyBuilder;
import com.alibaba.nacos.naming.core.v2.client.impl.IpPortBasedClient;
import com.alibaba.nacos.naming.core.v2.pojo.HealthCheckInstancePublishInfo;
import com.alibaba.nacos.naming.core.v2.pojo.Service;
import com.alibaba.nacos.naming.healthcheck.NacosHealthCheckTask;
import com.alibaba.nacos.naming.misc.GlobalConfig;
import com.alibaba.nacos.naming.misc.Loggers;
import com.alibaba.nacos.sys.utils.ApplicationUtils;

import java.util.Collection;

/**
 * V2 客户端心跳检查任务。
 *
 * <p>针对 {@link IpPortBasedClient} 发布的全部服务实例，经拦截链执行 {@link InstanceBeatCheckTask} 超时与健康状态检查。</p>
 *
 * @author nkorange
 */
public class ClientBeatCheckTaskV2 extends AbstractExecuteTask
    implements BeatCheckTask, NacosHealthCheckTask {
    
    /** 待检查的 IP:Port 客户端。 */
    private final IpPortBasedClient client;
    
    /** 健康检查任务 ID（通常为 responsibleId）。 */
    private final String taskId;
    
    /** 实例心跳检查拦截链单例。 */
    private final InstanceBeatCheckTaskInterceptorChain interceptorChain;
    
    /** 绑定客户端并初始化任务 ID 与拦截链。 */
    public ClientBeatCheckTaskV2(IpPortBasedClient client) {
        this.client = client;
        this.taskId = client.getResponsibleId();
        this.interceptorChain = InstanceBeatCheckTaskInterceptorChain.getInstance();
    }
    
    /** 获取全局命名配置 Bean。 */
    public GlobalConfig getGlobalConfig() {
        return ApplicationUtils.getBean(GlobalConfig.class);
    }
    
    /** 生成服务元数据维度的任务键。 */
    @Override
    public String taskKey() {
        return KeyBuilder.buildServiceMetaKey(client.getClientId(),
            String.valueOf(client.isEphemeral()));
    }
    
    /** 返回负责任务标识。 */
    @Override
    public String getTaskId() {
        return taskId;
    }
    
    /** 遍历客户端发布的服务，经拦截链执行实例心跳检查。 */
    @Override
    public void doHealthCheck() {
        try {
            Collection<Service> services = client.getAllPublishedService();
            for (Service each : services) {
                HealthCheckInstancePublishInfo instance = (HealthCheckInstancePublishInfo) client
                    .getInstancePublishInfo(each);
                interceptorChain.doInterceptor(new InstanceBeatCheckTask(client, each, instance));
            }
        } catch (Exception e) {
            Loggers.SRV_LOG.warn("Exception while processing client beat time out.", e);
        }
    }
    
    /** 调度线程入口，委托 {@link #doHealthCheck()}。 */
    @Override
    public void run() {
        doHealthCheck();
    }
    
    /** 拦截通过后直接执行健康检查。 */
    @Override
    public void passIntercept() {
        doHealthCheck();
    }
    
    /** 拦截结束后的空实现钩子。 */
    @Override
    public void afterIntercept() {
    }
}
