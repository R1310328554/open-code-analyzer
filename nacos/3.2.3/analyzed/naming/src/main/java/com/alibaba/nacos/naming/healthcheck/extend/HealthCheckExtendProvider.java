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

package com.alibaba.nacos.naming.healthcheck.extend;

import com.alibaba.nacos.api.naming.pojo.healthcheck.AbstractHealthChecker;
import com.alibaba.nacos.api.naming.pojo.healthcheck.HealthCheckType;
import com.alibaba.nacos.common.spi.NacosServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * 健康检查扩展提供者。
 *
 * <p>启动时通过 SPI 加载 {@link AbstractHealthChecker} 与扩展处理器，校验类型一一对应后注册到 {@link HealthCheckType}。</p>
 *
 * @author XCXCXCXCX
 */
@Component
public class HealthCheckExtendProvider {
    
    /** 日志记录器。 */
    private static final Logger LOGGER = LoggerFactory.getLogger(HealthCheckExtendProvider.class);
    
    /** SPI 加载的全部健康检查器实现。 */
    private final Collection<AbstractHealthChecker> checkers =
        NacosServiceLoader.load(AbstractHealthChecker.class);
    
    /** 处理器扩展实现，由 Spring 注入。 */
    private AbstractHealthCheckProcessorExtend healthCheckProcessorExtend;
    
    /** 设置处理器扩展 Bean。 */
    public void setHealthCheckProcessorExtend(
        AbstractHealthCheckProcessorExtend healthCheckProcessorExtend) {
        this.healthCheckProcessorExtend = healthCheckProcessorExtend;
    }
    
    /** 初始化并加载健康检查扩展。 */
    public void init() {
        loadExtend();
    }
    
    /** 合并内置与扩展类型，校验无重复且处理器与检查器类型集合一致。 */
    private void loadExtend() {
        Iterator<AbstractHealthChecker> healthCheckerIt = checkers.iterator();
        
        Set<String> origin = new HashSet<>();
        for (HealthCheckType type : HealthCheckType.values()) {
            origin.add(type.name());
        }
        Set<String> processorType = healthCheckProcessorExtend.addProcessor(origin);
        Set<String> healthCheckerType = new HashSet<>(origin);
        
        while (healthCheckerIt.hasNext()) {
            AbstractHealthChecker checker = healthCheckerIt.next();
            String type = checker.getType();
            if (healthCheckerType.contains(type)) {
                throw new RuntimeException(
                    "More than one healthChecker of the same type was found : [type=\"" + type
                        + "\"]");
            }
            healthCheckerType.add(type);
            HealthCheckType.registerHealthChecker(checker.getType(), checker.getClass());
        }
        if (!processorType.equals(healthCheckerType)) {
            throw new RuntimeException(
                "An unmatched processor and healthChecker are detected in the extension package.");
        }
        if (processorType.size() > origin.size()) {
            processorType.removeAll(origin);
            LOGGER.debug("init health plugin : types=" + processorType);
        }
    }
}
