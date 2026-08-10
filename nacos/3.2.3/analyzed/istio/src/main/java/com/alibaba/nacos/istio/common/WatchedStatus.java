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

package com.alibaba.nacos.istio.common;

import java.util.HashSet;
import java.util.Set;

/**
 * 客户端对某类 Istio 资源的订阅与 ACK 状态：记录最新/已确认的版本、nonce 及订阅资源集合。
 *
 * <p>用于 XDS/MCP 长连接上的增量推送与 ACK 校验。</p>
 *
 * @author special.fy
 */
public class WatchedStatus {
    
    /** 资源类型或 MCP collection 名称。 */
    private String type;
    
    /** 上一次响应是否为 ACK（非 NACK）。 */
    private boolean lastAckOrNack;
    
    /** 客户端最近一次订阅的资源名称集合。 */
    private Set<String> lastSubscribe;
    
    /** 服务端已推送的最新版本号。 */
    private String latestVersion;
    
    /** 服务端已推送的最新 nonce。 */
    private String latestNonce;
    
    /** 客户端已 ACK 的版本号。 */
    private String ackedVersion;
    
    /** 客户端已 ACK 的 nonce。 */
    private String ackedNonce;
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getLatestVersion() {
        return latestVersion;
    }
    
    public void setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
    }
    
    public String getLatestNonce() {
        return latestNonce;
    }
    
    public void setLatestNonce(String latestNonce) {
        this.latestNonce = latestNonce;
    }
    
    public String getAckedVersion() {
        return ackedVersion;
    }
    
    public void setAckedVersion(String ackedVersion) {
        this.ackedVersion = ackedVersion;
    }
    
    public String getAckedNonce() {
        return ackedNonce;
    }
    
    public void setAckedNonce(String ackedNonce) {
        this.ackedNonce = ackedNonce;
    }
    
    public boolean isLastAckOrNack() {
        return lastAckOrNack;
    }
    
    public void setLastAckOrNack(boolean lastAckOrNack) {
        this.lastAckOrNack = lastAckOrNack;
    }
    
    public Set<String> getLastSubscribe() {
        return lastSubscribe;
    }
    
    public void setLastSubscribe(Set<String> lastSubscribe) {
        this.lastSubscribe = new HashSet<>(lastSubscribe);
    }
}
