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

package com.alibaba.nacos.api.naming.remote.request;

/**
 * 命名服务订阅/取消订阅远程请求。
 *
 * <p>{@link #subscribe} 为 {@code true} 时注册监听，为 {@code false} 时取消；{@link #clusters} 可指定关注的集群列表（逗号分隔）。</p>
 *
 * @author xiweng.yy
 */
public class SubscribeServiceRequest extends AbstractNamingRequest {
    
    /** {@code true} 订阅，{@code false} 取消订阅。 */
    private boolean subscribe;
    
    /** 关注的集群名列表（逗号分隔，可为空）。 */
    private String clusters;
    
    /** 无参构造，供序列化使用。 */
    public SubscribeServiceRequest() {
    }
    
    /**
     * 构造订阅请求。
     *
     * @param namespace   命名空间 ID
     * @param groupName   分组名
     * @param serviceName 服务名
     * @param clusters    集群列表
     * @param subscribe   是否订阅
     */
    public SubscribeServiceRequest(String namespace, String groupName, String serviceName,
        String clusters,
        boolean subscribe) {
        super(namespace, serviceName, groupName);
        this.clusters = clusters;
        this.subscribe = subscribe;
    }
    
    /** 返回关注的集群列表。 */
    public String getClusters() {
        return clusters;
    }
    
    /** 设置关注的集群列表。 */
    public void setClusters(String clusters) {
        this.clusters = clusters;
    }
    
    /** 是否为订阅操作（否则为取消订阅）。 */
    public boolean isSubscribe() {
        return subscribe;
    }
    
    /** 设置订阅或取消订阅。 */
    public void setSubscribe(boolean subscribe) {
        this.subscribe = subscribe;
    }
}
