/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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
 *
 */

package com.alibaba.nacos.istio.model;

import com.alibaba.nacos.istio.common.ResourceSnapshot;

import java.util.HashSet;
import java.util.Set;

/**
 * 向 Istio 客户端（Envoy/MCP）推送资源的请求上下文。
 *
 * <p>携带 {@link ResourceSnapshot}、推送原因、订阅类型及增量删除集合。</p>
 *
 * @author RocketEngine26
 * @date 2022/8/21 下午1:09
 */
public class PushRequest {
    /** 当前资源快照（服务、配置等）。 */
    private ResourceSnapshot resourceSnapshot;
    
    /** 触发本次推送的原因集合（如服务变更、配置刷新）。 */
    private final Set<String> reason = new HashSet<>();
    
    /** 客户端订阅的资源类型集合；null 表示全量推送。 */
    private Set<String> subscribe;
    
    /** 增量推送中需删除的资源名集合。 */
    private final Set<String> removed = new HashSet<>();
    
    /** 是否为全量推送（相对增量/delta）。 */
    private boolean full;
    
    /**
     * 基于快照构造推送请求。
     *
     * @param snapshot 资源快照
     * @param full     是否全量
     */
    public PushRequest(ResourceSnapshot snapshot, boolean full) {
        this.resourceSnapshot = snapshot;
        this.full = full;
    }
    
    /**
     * 仅指定推送原因构造请求（快照后续设置）。
     *
     * @param reason 推送原因
     * @param full   是否全量
     */
    public PushRequest(String reason, boolean full) {
        this.full = full;
        this.reason.add(reason);
    }
    
    public ResourceSnapshot getResourceSnapshot() {
        return resourceSnapshot;
    }
    
    public boolean isFull() {
        return full;
    }
    
    public void setFull(boolean full) {
        this.full = full;
    }
    
    public void setResourceSnapshot(ResourceSnapshot resourceSnapshot) {
        this.resourceSnapshot = resourceSnapshot;
    }
    
    public Set<String> getReason() {
        return reason;
    }
    
    public void addReason(String reason) {
        this.reason.add(reason);
    }
    
    public Set<String> getRemoved() {
        return removed;
    }
    
    public void addRemoved(String remove) {
        this.removed.add(remove);
    }
    
    public Set<String> getSubscribe() {
        return subscribe;
    }
    
    public void setSubscribe(Set<String> subscribe) {
        this.subscribe = subscribe;
    }
}
