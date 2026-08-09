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
package com.alibaba.csp.sentinel.cluster.flow.statistic.concurrent;

import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;

import java.util.UUID;

/**
 * 并发令牌缓存节点，由 {@link TokenCacheNodeManager} 以 ConcurrentLinkedHashMap 存储。
 * 定期清理过期 token 时需据此更新 nowCalls，故需保存 flowId 等信息。
 *
 * @author yunfeiyanggzq
 */
public class TokenCacheNode {
    /**
     * 令牌 ID。
     */
    private Long tokenId;
    /**
     * 客户端离线检测截止时间（绝对时间戳）。
     */
    private Long clientTimeout;
    /**
     * 资源调用超时检测截止时间（绝对时间戳）。
     */
    private Long resourceTimeout;
    /**
     * 令牌对应的流控规则 ID。
     */
    private Long flowId;
    /**
     * 该令牌占用的并发配额数。
     */
    private int acquireCount;

    /**
     * 持有该令牌的客户端地址。
     */
    private String clientAddress;

    public TokenCacheNode() {
    }

    public static TokenCacheNode generateTokenCacheNode(FlowRule rule, int acquireCount, String clientAddress) {
        TokenCacheNode node = new TokenCacheNode();
        // getMostSignificantBits() 取 UUID 128 位值的高 64 位作为 tokenId。
        // 碰撞概率极低。
        node.setTokenId(UUID.randomUUID().getMostSignificantBits());
        node.setFlowId(rule.getClusterConfig().getFlowId());
        node.setClientTimeout(rule.getClusterConfig().getClientOfflineTime());
        node.setResourceTimeout(rule.getClusterConfig().getResourceTimeout());
        node.setAcquireCount(acquireCount);
        node.setClientAddress(clientAddress);
        return node;
    }

    public Long getTokenId() {
        return tokenId;
    }

    public void setTokenId(Long tokenId) {
        this.tokenId = tokenId;
    }

    public Long getClientTimeout() {
        return clientTimeout;
    }

    public void setClientTimeout(Long clientTimeout) {
        this.clientTimeout = clientTimeout + System.currentTimeMillis();
    }

    public Long getResourceTimeout() {
        return this.resourceTimeout;
    }

    public void setResourceTimeout(Long resourceTimeout) {
        this.resourceTimeout = resourceTimeout + System.currentTimeMillis();
    }

    public Long getFlowId() {
        return flowId;
    }

    public void setFlowId(Long flowId) {
        this.flowId = flowId;
    }

    public int getAcquireCount() {
        return acquireCount;
    }

    public void setAcquireCount(int acquireCount) {
        this.acquireCount = acquireCount;
    }

    public String getClientAddress() {
        return clientAddress;
    }

    public void setClientAddress(String clientAddress) {
        this.clientAddress = clientAddress;
    }

    @Override
    public String toString() {
        return "TokenCacheNode{" +
                "tokenId=" + tokenId +
                ", clientTimeout=" + clientTimeout +
                ", resourceTimeout=" + resourceTimeout +
                ", flowId=" + flowId +
                ", acquireCount=" + acquireCount +
                ", clientAddress='" + clientAddress + '\'' +
                '}';
    }
}
