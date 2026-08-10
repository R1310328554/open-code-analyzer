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

import com.alibaba.nacos.naming.core.v2.client.Client;
import com.alibaba.nacos.naming.core.v2.pojo.HealthCheckInstancePublishInfo;
import com.alibaba.nacos.naming.core.v2.pojo.Service;

/**
 * 实例心跳检查器接口，负责对客户端上报心跳的实例执行超时与健康状态判定。
 *
 * <p>典型实现如 {@link UnhealthyInstanceChecker}，在心跳超时时将实例标记为不健康。</p>
 *
 * @author xiweng.yy
 */
public interface InstanceBeatChecker {
    
    /**
     * 对指定实例执行心跳检查逻辑。
     *
     * @param client   client
     * @param service  service of instance
     * @param instance instance publish info
     */
    void doCheck(Client client, Service service, HealthCheckInstancePublishInfo instance);
}
