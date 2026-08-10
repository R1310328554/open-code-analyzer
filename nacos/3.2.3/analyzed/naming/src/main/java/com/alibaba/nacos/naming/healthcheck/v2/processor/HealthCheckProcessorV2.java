/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.naming.healthcheck.v2.processor;

import com.alibaba.nacos.naming.core.v2.metadata.ClusterMetadata;
import com.alibaba.nacos.naming.core.v2.pojo.Service;
import com.alibaba.nacos.naming.healthcheck.v2.HealthCheckTaskV2;

/**
 * V2 健康检查处理器接口，按集群健康检查类型（TCP/HTTP/MySQL 等）执行具体探测。
 *
 * <p>由 {@link HealthCheckProcessorV2Delegate} 按 {@link ClusterMetadata#getHealthyCheckType()} 路由。</p>
 *
 * @author nkorange
 */
public interface HealthCheckProcessorV2 {
    
    /**
     * 对指定服务与集群元数据执行一次健康探测。
     *
     * @param task     health check task v2
     * @param service  service of current process
     * @param metadata cluster metadata of current process
     */
    void process(HealthCheckTaskV2 task, Service service, ClusterMetadata metadata);
    
    /**
     * 返回处理器对应的健康检查类型字符串（参见 HealthCheckType 枚举）。
     *
     * @return check type
     */
    String getType();
}
