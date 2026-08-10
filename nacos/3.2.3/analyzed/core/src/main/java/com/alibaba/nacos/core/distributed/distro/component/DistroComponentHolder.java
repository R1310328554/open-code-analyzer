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

package com.alibaba.nacos.core.distributed.distro.component;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Distro 组件注册中心：按资源类型索引传输代理、数据存储、失败任务处理器与数据处理器。
 * Distro component holder.
 *
 * @author xiweng.yy
 */
@Component
public class DistroComponentHolder {
    
    /** 资源类型 → 传输代理。 */
    private final Map<String, DistroTransportAgent> transportAgentMap = new HashMap<>();
    
    /** 资源类型 → 本地数据存储。 */
    private final Map<String, DistroDataStorage> dataStorageMap = new HashMap<>();
    
    /** 资源类型 → 失败任务重试处理器。 */
    private final Map<String, DistroFailedTaskHandler> failedTaskHandlerMap = new HashMap<>();
    
    /** 处理类型 → 入站数据处理器。 */
    private final Map<String, DistroDataProcessor> dataProcessorMap = new HashMap<>();
    
    /** 按资源类型查找传输代理。 */
    public DistroTransportAgent findTransportAgent(String type) {
        return transportAgentMap.get(type);
    }
    
    /** 注册传输代理。 */
    public void registerTransportAgent(String type, DistroTransportAgent transportAgent) {
        transportAgentMap.put(type, transportAgent);
    }
    
    /** 按资源类型查找数据存储。 */
    public DistroDataStorage findDataStorage(String type) {
        return dataStorageMap.get(type);
    }
    
    /** 注册本地数据存储。 */
    public void registerDataStorage(String type, DistroDataStorage dataStorage) {
        dataStorageMap.put(type, dataStorage);
    }
    
    /** 返回已注册的数据存储类型集合。 */
    public Set<String> getDataStorageTypes() {
        return dataStorageMap.keySet();
    }
    
    /** 按资源类型查找失败任务处理器。 */
    public DistroFailedTaskHandler findFailedTaskHandler(String type) {
        return failedTaskHandlerMap.get(type);
    }
    
    /** 注册失败任务处理器。 */
    public void registerFailedTaskHandler(String type, DistroFailedTaskHandler failedTaskHandler) {
        failedTaskHandlerMap.put(type, failedTaskHandler);
    }
    
    /** 按 {@link DistroDataProcessor#processType()} 注册入站处理器（幂等）。 */
    public void registerDataProcessor(DistroDataProcessor dataProcessor) {
        dataProcessorMap.putIfAbsent(dataProcessor.processType(), dataProcessor);
    }
    
    /** 按处理类型查找数据处理器。 */
    public DistroDataProcessor findDataProcessor(String processType) {
        return dataProcessorMap.get(processType);
    }
}
