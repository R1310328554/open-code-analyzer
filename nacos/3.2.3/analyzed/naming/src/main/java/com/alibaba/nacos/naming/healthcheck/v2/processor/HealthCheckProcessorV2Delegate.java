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

package com.alibaba.nacos.naming.healthcheck.v2.processor;

import com.alibaba.nacos.naming.core.v2.metadata.ClusterMetadata;
import com.alibaba.nacos.naming.core.v2.pojo.Service;
import com.alibaba.nacos.naming.healthcheck.extend.HealthCheckExtendProvider;
import com.alibaba.nacos.naming.healthcheck.extend.HealthCheckProcessorExtendV2;
import com.alibaba.nacos.naming.healthcheck.v2.HealthCheckTaskV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * V2 健康检查处理器委托类，按集群配置的类型路由到具体 Processor 实现。
 *
 * <p>Spring 注入全部 {@link HealthCheckProcessorV2} Bean 并注册扩展；未知类型回退到 {@link NoneHealthCheckProcessor}。</p>
 *
 * @author nacos
 */
@Component("healthCheckDelegateV2")
public class HealthCheckProcessorV2Delegate implements HealthCheckProcessorV2 {
    
    /** 健康检查类型 → 处理器实例映射。 */
    private final Map<String, HealthCheckProcessorV2> healthCheckProcessorMap = new HashMap<>();
    
    /** 初始化 SPI 扩展提供者并加载自定义健康检查处理器。 */
    public HealthCheckProcessorV2Delegate(HealthCheckExtendProvider provider,
        HealthCheckProcessorExtendV2 healthCheckProcessorExtend) {
        provider.setHealthCheckProcessorExtend(healthCheckProcessorExtend);
        provider.init();
    }
    
    /** Spring 收集全部 HealthCheckProcessorV2 Bean 并按 getType 注册。 */
    /**
     * Add health check processors.
      * <p>Nacos 命名健康检查：心跳判定、拦截链过滤与 V2 探测调度；详见上方类/接口说明。</p>
     */
    @Autowired
    public void addProcessor(Collection<HealthCheckProcessorV2> processors) {
        healthCheckProcessorMap.putAll(processors.stream()
            .filter(processor -> processor.getType() != null)
            .collect(Collectors.toMap(HealthCheckProcessorV2::getType, processor -> processor)));
    }
    
    /** 按 metadata.healthyCheckType 选取处理器，缺省使用 None 处理器。 */
    @Override
    public void process(HealthCheckTaskV2 task, Service service, ClusterMetadata metadata) {
        String type = metadata.getHealthyCheckType();
        HealthCheckProcessorV2 processor = healthCheckProcessorMap.get(type);
        if (processor == null) {
            processor = healthCheckProcessorMap.get(NoneHealthCheckProcessor.TYPE);
        }
        processor.process(task, service, metadata);
    }
    
    /** 委托类本身无固定类型，返回 null。 */
    @Override
    public String getType() {
        return null;
    }
}
