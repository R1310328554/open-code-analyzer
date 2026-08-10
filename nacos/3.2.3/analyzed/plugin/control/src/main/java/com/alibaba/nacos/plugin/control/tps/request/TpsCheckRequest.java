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

package com.alibaba.nacos.plugin.control.tps.request;

/**
 * TPS 限流校验请求。
 *
 * <p>封装限流点、连接标识、客户端 IP 及计数信息，供 TPS 管控管理器执行准入校验。</p>
 *
 * @author shiyiyue
 */
public class TpsCheckRequest {
    
    /** 限流点名称。 */
    private String pointName;
    
    /** 请求时间戳，默认当前毫秒时间。 */
    private long timestamp = System.currentTimeMillis();
    
    /** 客户端连接标识。 */
    private String connectionId;
    
    /** 客户端 IP 地址。 */
    private String clientIp;
    
    /** 本次请求计数增量，默认为 1。 */
    private long count = 1;
    
    /** 无参构造，供序列化或框架反射使用。 */
    public TpsCheckRequest() {
        
    }
    
    /**
     * 构造带限流点与连接上下文的校验请求。
     *
     * @param pointName    限流点名称
     * @param connectionId 连接标识
     * @param clientIp     客户端 IP
     */
    public TpsCheckRequest(String pointName, String connectionId, String clientIp) {
        this.connectionId = connectionId;
        this.clientIp = clientIp;
        this.pointName = pointName;
    }
    
    /**
     * 获取请求时间戳。
     *
     * @return 毫秒时间戳
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * 设置请求时间戳。
     *
     * @param timestamp 毫秒时间戳
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * 获取连接标识。
     *
     * @return 连接 ID
     */
    public String getConnectionId() {
        return connectionId;
    }
    
    /**
     * 获取本次计数增量。
     *
     * @return 计数增量
     */
    public long getCount() {
        return count;
    }
    
    /**
     * 设置本次计数增量。
     *
     * @param count 计数增量
     */
    public void setCount(long count) {
        this.count = count;
    }
    
    /**
     * 设置连接标识。
     *
     * @param connectionId 连接 ID
     */
    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }
    
    /**
     * 获取客户端 IP。
     *
     * @return 客户端 IP 地址
     */
    public String getClientIp() {
        return clientIp;
    }
    
    /**
     * 设置客户端 IP。
     *
     * @param clientIp 客户端 IP 地址
     */
    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }
    
    /**
     * 获取限流点名称。
     *
     * @return 限流点名称
     */
    public String getPointName() {
        return pointName;
    }
    
    /**
     * 设置限流点名称。
     *
     * @param pointName 限流点名称
     */
    public void setPointName(String pointName) {
        this.pointName = pointName;
    }
    
}
