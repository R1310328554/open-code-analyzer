/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.controller.elect.impl;

import org.apache.rocketmq.controller.elect.ElectPolicy;
import org.apache.rocketmq.controller.impl.heartbeat.BrokerLiveInfo;
import org.apache.rocketmq.controller.helper.BrokerLiveInfoGetter;
import org.apache.rocketmq.controller.helper.BrokerValidPredicate;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * 默认选主策略：先在 SyncStateSet 内、再在全副本中
 * 按存活过滤、优先保留旧 Master 或指定 ID，否则按 epoch/offset 排序。
 */
public class DefaultElectPolicy implements ElectPolicy {

    /** 判断副本是否具备被选为 Master 的存活资格。 */
    private BrokerValidPredicate validPredicate;

    /** 获取副本心跳详情用于排序比较。 */
    private BrokerLiveInfoGetter brokerLiveInfoGetter;

    /** 按 epoch 降序、maxOffset 降序、electionPriority 升序比较副本。 */
    private final Comparator<BrokerLiveInfo> comparator = (o1, o2) -> {
        if (o1.getEpoch() == o2.getEpoch()) {
            return o1.getMaxOffset() == o2.getMaxOffset() ? o1.getElectionPriority() - o2.getElectionPriority() :
                (int) (o2.getMaxOffset() - o1.getMaxOffset());
        } else {
            return o2.getEpoch() - o1.getEpoch();
        }
    };

    public DefaultElectPolicy(BrokerValidPredicate validPredicate, BrokerLiveInfoGetter brokerLiveInfoGetter) {
        this.validPredicate = validPredicate;
        this.brokerLiveInfoGetter = brokerLiveInfoGetter;
    }

    public DefaultElectPolicy() {

    }

    /**
     * 依次在 SyncStateSet 与全副本中尝试选主：
     * 1. 用 validPredicate 过滤存活副本；
     * 2. 旧 Master 仍有效且未指定其他优先 ID 则保留；
     * 3. 否则按 epoch/offset/优先级排序取最优，或随机取一。
     *
     * @param clusterName       集群名
     * @param syncStateBrokers  同步副本 ID 集合
     * @param allReplicaBrokers 全部副本 ID 集合
     * @param oldMaster         原 Master brokerId
     * @param preferBrokerId    优先选举的 brokerId
     * @return 选出的新 Master brokerId
     */
    @Override
    public Long elect(String clusterName, String brokerName, Set<Long> syncStateBrokers, Set<Long> allReplicaBrokers,
        Long oldMaster, Long preferBrokerId) {
        Long newMaster = null;
        // try to elect in syncStateBrokers
        if (syncStateBrokers != null) {
            newMaster = tryElect(clusterName, brokerName, syncStateBrokers, oldMaster, preferBrokerId);
        }
        if (newMaster != null) {
            return newMaster;
        }

        // try to elect in all allReplicaBrokers
        if (allReplicaBrokers != null) {
            newMaster = tryElect(clusterName, brokerName, allReplicaBrokers, oldMaster, preferBrokerId);
        }
        return newMaster;
    }

    /** 在给定副本 ID 集合内执行一轮选主逻辑。 */
    private Long tryElect(String clusterName, String brokerName, Set<Long> brokers, Long oldMaster,
        Long preferBrokerId) {
        if (this.validPredicate != null) {
            brokers = brokers.stream().filter(brokerAddr -> this.validPredicate.check(clusterName, brokerName, brokerAddr)).collect(Collectors.toSet());
        }
        if (!brokers.isEmpty()) {
            // if old master is still valid, and preferBrokerAddr is blank or is equals to oldMaster
            if (brokers.contains(oldMaster) && (preferBrokerId == null || preferBrokerId.equals(oldMaster))) {
                return oldMaster;
            }

            // if preferBrokerAddr is valid, we choose it, otherwise we choose nothing
            if (preferBrokerId != null) {
                return brokers.contains(preferBrokerId) ? preferBrokerId : null;
            }

            if (this.brokerLiveInfoGetter != null) {
                // sort brokerLiveInfos by (epoch,maxOffset)
                TreeSet<BrokerLiveInfo> brokerLiveInfos = new TreeSet<>(this.comparator);
                brokers.forEach(brokerAddr -> brokerLiveInfos.add(this.brokerLiveInfoGetter.get(clusterName, brokerName, brokerAddr)));
                if (brokerLiveInfos.size() >= 1) {
                    return brokerLiveInfos.first().getBrokerId();
                }
            }
            // elect random
            return brokers.iterator().next();
        }
        return null;
    }


    public void setBrokerLiveInfoGetter(BrokerLiveInfoGetter brokerLiveInfoGetter) {
        this.brokerLiveInfoGetter = brokerLiveInfoGetter;
    }

    public void setValidPredicate(BrokerValidPredicate validPredicate) {
        this.validPredicate = validPredicate;
    }

    public BrokerLiveInfoGetter getBrokerLiveInfoGetter() {
        return brokerLiveInfoGetter;
    }
}
