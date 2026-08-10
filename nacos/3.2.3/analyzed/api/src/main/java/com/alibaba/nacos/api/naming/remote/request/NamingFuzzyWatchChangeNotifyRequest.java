/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.naming.remote.request;

import static com.alibaba.nacos.api.common.Constants.FUZZY_WATCH_RESOURCE_CHANGED;

/**
 * 命名模糊监听单服务变更通知请求。
 *
 * <p>当匹配模糊监听规则的服务发生变更时，服务端向客户端推送此请求，携带 {@link #serviceKey} 与 {@link #changedType}；同步类型固定为 {@code FUZZY_WATCH_RESOURCE_CHANGED}。</p>
 *
 * @author tanyongquan
 */
public class NamingFuzzyWatchChangeNotifyRequest extends AbstractFuzzyWatchNotifyRequest {
    
    /** 发生变更的服务键（group@@service 格式）。 */
    private String serviceKey;
    
    /** 变更类型（如 ADD、DELETE、MODIFY）。 */
    private String changedType;
    
    /** 无参构造，供反序列化使用。 */
    public NamingFuzzyWatchChangeNotifyRequest() {
        
    }
    
    /**
     * 构造服务变更通知请求。
     *
     * @param serviceKey  变更的服务键
     * @param changedType 变更类型
     */
    public NamingFuzzyWatchChangeNotifyRequest(String serviceKey, String changedType) {
        super(FUZZY_WATCH_RESOURCE_CHANGED);
        this.serviceKey = serviceKey;
        this.changedType = changedType;
    }
    
    /** 返回变更的服务键。 */
    public String getServiceKey() {
        return serviceKey;
    }
    
    /** 设置变更的服务键。 */
    public void setServiceKey(String serviceKey) {
        this.serviceKey = serviceKey;
    }
    
    /** 返回变更类型。 */
    public String getChangedType() {
        return changedType;
    }
    
    /** 设置变更类型。 */
    public void setChangedType(String changedType) {
        this.changedType = changedType;
    }
}
