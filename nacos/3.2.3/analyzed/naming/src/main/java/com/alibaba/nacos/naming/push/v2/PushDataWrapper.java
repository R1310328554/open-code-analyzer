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

package com.alibaba.nacos.naming.push.v2;

import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.naming.core.v2.metadata.ServiceMetadata;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 推送数据包装器，携带原始实例列表与处理后缓存。
 *
 * <p>推送执行器可经 {@link #addProcessedPushData} 缓存 SPI 或过滤后的数据，避免重复计算。</p>
 *
 * @author xiweng.yy
 */
public class PushDataWrapper {
    
    /** 服务元数据（保护阈值、推送开关等）。 */
    private final ServiceMetadata serviceMetadata;
    
    /** 原始 {@link ServiceInfo} 实例列表。 */
    private final ServiceInfo originalData;
    
    /** 按 key 缓存的已处理推送数据。 */
    private final Map<String, Object> processedDatum;
    
    public PushDataWrapper(ServiceMetadata serviceMetadata, ServiceInfo originalData) {
        this.serviceMetadata = serviceMetadata;
        this.originalData = originalData;
        processedDatum = new HashMap<>(1);
    }
    
    /** 返回原始服务实例信息。 */
    public ServiceInfo getOriginalData() {
        return originalData;
    }
    
    /** 返回服务元数据。 */
    public ServiceMetadata getServiceMetadata() {
        return serviceMetadata;
    }
    
    /** 按 key 获取已处理的推送数据（泛型 Optional）。 */
    public <T> Optional<T> getProcessedPushData(String key) {
        return Optional.ofNullable((T) processedDatum.get(key));
    }
    
    /** 缓存一条已处理的推送数据。 */
    public void addProcessedPushData(String key, Object processedData) {
        processedDatum.put(key, processedData);
    }
}
