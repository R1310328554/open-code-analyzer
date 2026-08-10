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

import com.alibaba.nacos.common.spi.NacosServiceLoader;
import com.alibaba.nacos.naming.core.v2.client.impl.IpPortBasedClient;
import com.alibaba.nacos.naming.core.v2.pojo.HealthCheckInstancePublishInfo;
import com.alibaba.nacos.naming.core.v2.pojo.Service;
import com.alibaba.nacos.naming.interceptor.Interceptable;

import java.util.LinkedList;
import java.util.List;

/**
 * 单实例心跳检查任务。
 *
 * <p>拦截链通过后依次调用内置与 SPI 加载的 {@link InstanceBeatChecker} 执行不健康与过期检测。</p>
 *
 * @author xiweng.yy
 */
public class InstanceBeatCheckTask implements Interceptable {
    
    /** 静态检查器链：不健康、过期及 SPI 扩展。 */
    private static final List<InstanceBeatChecker> CHECKERS = new LinkedList<>();
    
    /** 实例所属客户端。 */
    private final IpPortBasedClient client;
    
    /** 实例所属服务。 */
    private final Service service;
    
    /** 待检查的实例发布信息。 */
    private final HealthCheckInstancePublishInfo instancePublishInfo;
    
    /** 注册默认检查器并加载 SPI 扩展。 */
    static {
        CHECKERS.add(new UnhealthyInstanceChecker());
        CHECKERS.add(new ExpiredInstanceChecker());
        CHECKERS.addAll(NacosServiceLoader.load(InstanceBeatChecker.class));
    }
    
    /** 构造针对单个实例的检查任务上下文。 */
    public InstanceBeatCheckTask(IpPortBasedClient client, Service service,
        HealthCheckInstancePublishInfo instancePublishInfo) {
        this.client = client;
        this.service = service;
        this.instancePublishInfo = instancePublishInfo;
    }
    
    /** 依次执行全部检查器的 {@link InstanceBeatChecker#doCheck}。 */
    @Override
    public void passIntercept() {
        for (InstanceBeatChecker each : CHECKERS) {
            each.doCheck(client, service, instancePublishInfo);
        }
    }
    
    /** 拦截结束后的空实现。 */
    @Override
    public void afterIntercept() {
    }
    
    /** 返回关联客户端。 */
    public IpPortBasedClient getClient() {
        return client;
    }
    
    /** 返回关联服务。 */
    public Service getService() {
        return service;
    }
    
    /** 返回实例发布信息。 */
    public HealthCheckInstancePublishInfo getInstancePublishInfo() {
        return instancePublishInfo;
    }
}
