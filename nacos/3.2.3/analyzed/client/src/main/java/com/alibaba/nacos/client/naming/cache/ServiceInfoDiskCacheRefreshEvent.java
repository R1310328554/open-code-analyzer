/*
 * Copyright 1999-2026 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.client.naming.cache;

import com.alibaba.nacos.api.naming.pojo.ServiceInfo;

/**
 * 服务信息磁盘缓存异步刷新事件。
 *
 * <p>由 {@link ServiceInfoHolder} 在实例变更后发布，携带最新 {@link ServiceInfo} 快照与缓存目录，供 {@link ServiceInfoDiskCacheRefresher} 批量落盘。</p>
 *
 * @author Zhengcy05
 */
public class ServiceInfoDiskCacheRefreshEvent {
    
    /** 不含集群后缀的服务键（group@@service）。 */
    private final String serviceKey;
    
    /** 待持久化的最新服务信息快照。 */
    private final ServiceInfo serviceInfo;
    
    /** 本地磁盘缓存根目录。 */
    private final String cacheDir;
    
    /**
     * 构造磁盘缓存刷新事件。
     *
     * @param serviceKey 不含集群后缀的服务键
     * @param serviceInfo 最新服务信息快照
     * @param cacheDir 磁盘缓存目录
     */
    public ServiceInfoDiskCacheRefreshEvent(String serviceKey, ServiceInfo serviceInfo,
        String cacheDir) {
        this.serviceKey = serviceKey;
        this.serviceInfo = serviceInfo;
        this.cacheDir = cacheDir;
    }
    
    /**
     * 获取服务键。
     *
     * @return 服务键
     */
    public String getServiceKey() {
        return serviceKey;
    }
    
    /**
     * 获取服务信息快照。
     *
     * @return 服务信息快照
     */
    public ServiceInfo getServiceInfo() {
        return serviceInfo;
    }
    
    /**
     * 获取磁盘缓存目录。
     *
     * @return 磁盘缓存目录
     */
    public String getCacheDir() {
        return cacheDir;
    }
}
