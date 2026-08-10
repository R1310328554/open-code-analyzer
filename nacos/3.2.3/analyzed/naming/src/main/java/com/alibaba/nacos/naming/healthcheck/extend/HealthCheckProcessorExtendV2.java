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

import com.alibaba.nacos.common.spi.NacosServiceLoader;
import com.alibaba.nacos.naming.healthcheck.v2.processor.HealthCheckProcessorV2;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * V2 健康检查处理器扩展。
 *
 * <p>通过 {@link NacosServiceLoader} 加载 {@link HealthCheckProcessorV2} 实现，注册为 Spring 单例并返回扩展后的类型集合。</p>
 *
 * @author sunmengying
 */
@Component
public class HealthCheckProcessorExtendV2 extends AbstractHealthCheckProcessorExtend {
    
    /** SPI 加载的 V2 处理器集合。 */
    private final Collection<HealthCheckProcessorV2> processors =
        NacosServiceLoader.load(HealthCheckProcessorV2.class);
    
    /** 遍历 SPI 处理器，校验类型唯一并注册单例 Bean。 */
    @Override
    public Set<String> addProcessor(Set<String> origin) {
        Iterator<HealthCheckProcessorV2> processorIt = processors.iterator();
        Set<String> processorType = new HashSet<>(origin);
        while (processorIt.hasNext()) {
            HealthCheckProcessorV2 processor = processorIt.next();
            String type = processor.getType();
            if (processorType.contains(type)) {
                throw new RuntimeException(
                    "More than one processor of the same type was found : [type=\"" + type + "\"]");
            }
            processorType.add(type);
            registry.registerSingleton(lowerFirstChar(processor.getClass().getSimpleName()),
                processor);
        }
        return processorType;
    }
}
