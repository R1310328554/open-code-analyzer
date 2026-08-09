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
package com.alibaba.csp.sentinel.cluster.flow.statistic.concurrent.expire;

import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.flow.statistic.concurrent.CurrentConcurrencyManager;
import com.alibaba.csp.sentinel.cluster.flow.statistic.concurrent.TokenCacheNode;
import com.alibaba.csp.sentinel.cluster.server.connection.ConnectionManager;
import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 定期清理过期并发令牌的策略实现。
 * <p>需考虑令牌客户端离线或资源调用超时的情况，可通过 sourceTimeout 与 clientTimeout 检测。
 * 资源调用超时检测在令牌客户端侧触发；若资源调用超时，客户端会向令牌服务端请求释放或刷新令牌。
 * 客户端离线检测在令牌服务端侧触发；若超过离线检测时间，服务端会探测客户端状态。
 * 若客户端已离线，服务端删除对应 tokenId；否则继续保留。
 *
 * @author yunfeiyanggzq
 **/
public class RegularExpireStrategy implements ExpireStrategy {
    /**
     * 每次任务最多删除的令牌数量，单次清理的过期键值对不超过此值。
     */
    private long executeCount = 1000;
    /**
     * 单次任务执行的时间上限（毫秒）。
     */
    private long executeDuration = 800;
    /**
     * 定时清理任务的执行间隔（毫秒）。
     */
    private long executeRate = 1000;
    /**
     * tokenId 的本地缓存。
     */
    private ConcurrentLinkedHashMap<Long, TokenCacheNode> localCache;

    @SuppressWarnings("PMD.ThreadPoolCreationRule")
    private static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1,
            new NamedThreadFactory("regular clear expired token thread", true));


    public RegularExpireStrategy(ConcurrentLinkedHashMap<Long, TokenCacheNode> localCache) {
        AssertUtil.isTrue(localCache != null, " local cache can't be null");
        this.localCache = localCache;
    }

    @Override
    public void startClearTaskRegularly() {
        executor.scheduleAtFixedRate(new ClearExpiredTokenTask(), 0, executeRate, TimeUnit.MILLISECONDS);
    }

    private class ClearExpiredTokenTask implements Runnable {
        @Override
        public void run() {
            try {
                clearToken();
            } catch (Throwable e) {
                e.printStackTrace();
                RecordLog.warn("[RegularExpireStrategy] undefined throwable during clear token: ", e);
            }
        }
    }

    private void clearToken() {
        long start = System.currentTimeMillis();
        List<Long> keyList = new ArrayList<>(localCache.keySet());
        for (int i = 0; i < executeCount && i < keyList.size(); i++) {
            // 执行超时则退出本轮清理
            if (System.currentTimeMillis() - start > executeDuration) {
                RecordLog.info("[RegularExpireStrategy] End the process of expired token detection because of execute time is more than executeDuration: {}", executeDuration);
                break;
            }
            Long key = keyList.get(i);
            TokenCacheNode node = localCache.get(key);
            if (node == null) {
                continue;
            }

            // 移除客户端已离线且保存时间超过 clientTimeout 的令牌
            if (!ConnectionManager.isClientOnline(node.getClientAddress()) && node.getClientTimeout() - System.currentTimeMillis() < 0) {
                removeToken(key, node);
                RecordLog.info("[RegularExpireStrategy] Delete the expired token<{}> because of client offline for ruleId<{}>", node.getTokenId(), node.getFlowId());
                continue;
            }

            // 若令牌保存时间超过资源调用超时阈值，则判定为超时并清理。
            long resourceTimeout = ClusterFlowRuleManager.getFlowRuleById(node.getFlowId()).getClusterConfig().getResourceTimeout();
            if (System.currentTimeMillis() - node.getResourceTimeout() > resourceTimeout) {
                removeToken(key, node);
                RecordLog.info("[RegularExpireStrategy] Delete the expired token<{}> because of resource timeout for ruleId<{}>", node.getTokenId(), node.getFlowId());
            }
        }
    }

    private void removeToken(long tokenId, TokenCacheNode node) {
        if (localCache.remove(tokenId) == null) {
            RecordLog.info("[RegularExpireStrategy] Token<{}> is already released for ruleId<{}>", tokenId, node.getFlowId());
            return;
        }
        AtomicInteger nowCalls = CurrentConcurrencyManager.get(node.getFlowId());
        if (nowCalls == null) {
            return;
        }
        nowCalls.getAndAdd(node.getAcquireCount() * -1);
    }
}
