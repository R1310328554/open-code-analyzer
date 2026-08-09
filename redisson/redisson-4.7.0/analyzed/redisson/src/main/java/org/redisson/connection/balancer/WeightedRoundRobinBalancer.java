/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.connection.balancer;

import org.redisson.client.protocol.RedisCommand;
import org.redisson.misc.WrappedLock;
import org.redisson.connection.ClientConnectionsEntry;
import org.redisson.misc.RedisURI;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 加权轮询负载均衡器，按各从节点配置的权重比例分配连接。
 * <p>
 * 每个节点维护 weightCounter，每被选中一次减 1；全部归零后重置，开始新一轮加权轮询。
 *
 * @author Nikita Koksharov
 *
 */
public class WeightedRoundRobinBalancer implements LoadBalancer {

    /** 单个节点的权重及当前轮询剩余次数。 */
    static class WeightEntry {

        /** 配置的固定权重值。 */
        final int weight;
        /** 当前轮次剩余可选次数，归零后 reset。 */
        int weightCounter;

        WeightEntry(int weight) {
            this.weight = weight;
            this.weightCounter = weight;
        }

        /** 当前轮次是否已用完该节点的选取配额。 */
        public boolean isWeightCounterZero() {
            return weightCounter == 0;
        }

        /** 节点被选中后递减剩余次数。 */
        public void decWeightCounter() {
            weightCounter--;
        }

        /** 新一轮加权轮询开始时重置为初始权重。 */
        public void resetWeightCounter() {
            weightCounter = weight;
        }

    }

    /** 在有权重的候选节点间做简单轮询的计数器。 */
    private final AtomicInteger index = new AtomicInteger(-1);

    /** 各从节点地址到权重条目的映射。 */
    private final Map<RedisURI, WeightEntry> weights = new ConcurrentHashMap<>();

    /** 未在 weights 映射中显式配置的从节点默认权重。 */
    private final int defaultWeight;

    /** 保护权重选取与递减的互斥锁。 */
    private final WrappedLock lock = new WrappedLock();

    /**
     * 构造加权轮询均衡器。
     *
     * @param weights 从节点地址（<code>redis://host:port</code>）到权重的映射
     * @param defaultWeight 未在 weights 中定义的从节点使用的默认权重
     */
    public WeightedRoundRobinBalancer(Map<String, Integer> weights, int defaultWeight) {
        for (Entry<String, Integer> entry : weights.entrySet()) {
            RedisURI uri = new RedisURI(entry.getKey());
            if (entry.getValue() <= 0) {
                throw new IllegalArgumentException("Weight can't be less than or equal zero");
            }
            this.weights.put(uri, new WeightEntry(entry.getValue()));
        }
        if (defaultWeight <= 0) {
            throw new IllegalArgumentException("Weight can't be less than or equal zero");
        }

        this.defaultWeight = defaultWeight;
    }

    /** 忽略命令类型，按权重轮询选取。 */
    @Override
    public ClientConnectionsEntry getEntry(List<ClientConnectionsEntry> clients) {
        return getEntry(clients, null);
    }

    @Override
    public ClientConnectionsEntry getEntry(List<ClientConnectionsEntry> clients, RedisCommand<?> redisCommand) {
        // 为尚未注册权重的新从节点补充默认权重条目
        List<ClientConnectionsEntry> usedClients = findClients(clients, weights);
        for (ClientConnectionsEntry e : clients) {
            if (usedClients.contains(e)) {
                continue;
            }
            weights.put(e.getClient().getConfig().getAddress(), new WeightEntry(defaultWeight));
        }

        return lock.execute(() -> {
            Map<RedisURI, WeightEntry> weightsCopy = new HashMap<>(weights);
            weightsCopy.values().removeIf(WeightEntry::isWeightCounterZero);

            if (weightsCopy.isEmpty()) {
                for (WeightEntry entry : weights.values()) {
                    entry.resetWeightCounter();
                }

                weightsCopy = weights;
            }

            List<ClientConnectionsEntry> clientsCopy = findClients(clients, weightsCopy);

            // 所有节点 weightCounter 均已归零时重置并重新选取；
            // 最坏情况下主节点连接仍应可用。
            if (clientsCopy.isEmpty()) {
                for (WeightEntry entry : weights.values()) {
                    entry.resetWeightCounter();
                }

                weightsCopy = weights;
                clientsCopy = findClients(clients, weightsCopy);
            }

            int ind = Math.floorMod(index.incrementAndGet(), clientsCopy.size());
            ClientConnectionsEntry entry = clientsCopy.get(ind);
            for (Entry<RedisURI, WeightEntry> weightEntry : weightsCopy.entrySet()) {
                if (weightEntry.getKey().equals(entry.getClient().getAddr())) {
                    weightEntry.getValue().decWeightCounter();
                    break;
                }
            }
            return entry;
        });
    }

    /** 从候选列表中筛选出地址出现在 weightsCopy 中的连接入口。 */
    private List<ClientConnectionsEntry> findClients(List<ClientConnectionsEntry> clients,
                                                        Map<RedisURI, WeightEntry> weightsCopy) {
        return clients.stream()
                        .filter(e -> {
                            for (RedisURI redisURI : weightsCopy.keySet()) {
                                if (redisURI.equals(e.getClient().getAddr())) {
                                    return true;
                                }
                            }
                            return false;
                        })
                        .collect(Collectors.toList());
    }

}
