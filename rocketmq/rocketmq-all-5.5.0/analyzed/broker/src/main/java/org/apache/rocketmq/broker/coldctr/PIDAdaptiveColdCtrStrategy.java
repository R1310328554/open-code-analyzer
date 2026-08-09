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
package org.apache.rocketmq.broker.coldctr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 基于 PID 控制的自适应冷读流控：根据全局冷读误差历史动态调节 group 阈值。
 */
public class PIDAdaptiveColdCtrStrategy implements ColdCtrStrategy {
    /**
     * Stores the maximum number of recent et val
     */
    private static final int MAX_STORE_NUMS = 10;
    /**
     * The weights of the three modules of the PID formula
     */
    /** PID 比例、积分、微分权重系数。 */
    private static final Double KP = 0.5, KI = 0.3, KD = 0.2;
    private final List<Long> historyEtValList = new ArrayList<>();
    private final ColdDataCgCtrService coldDataCgCtrService;
    private final Long expectGlobalVal;
    private long et = 0L;

    /** 绑定流控服务并设置期望全局冷读目标值。 */
    public PIDAdaptiveColdCtrStrategy(ColdDataCgCtrService coldDataCgCtrService, Long expectGlobalVal) {
        this.coldDataCgCtrService = coldDataCgCtrService;
        this.expectGlobalVal = expectGlobalVal;
    }

    /** 历史误差样本足够时计算 PID 决策因子。 */
    @Override
    public Double decisionFactor() {
        if (historyEtValList.size() < MAX_STORE_NUMS) {
            return 0.0;
        }
        Long et1 = historyEtValList.get(historyEtValList.size() - 1);
        Long et2 = historyEtValList.get(historyEtValList.size() - 2);
        Long differential = et1 - et2;
        Double integration = 0.0;
        for (Long item: historyEtValList) {
            integration += item;
        }
        return  KP * et + KI * integration + KD * differential;
    }

    /** 决策因子为正时将阈值提高 1.5 倍。 */
    @Override
    public void promote(String consumerGroup, Long currentThreshold) {
        if (decisionFactor() > 0) {
            coldDataCgCtrService.addOrUpdateGroupConfig(consumerGroup, (long)(currentThreshold * 1.5));
        }
    }

    /** 决策因子为负时将阈值降至 0.8 倍（不低于默认 group 阈值）。 */
    @Override
    public void decelerate(String consumerGroup, Long currentThreshold) {
        if (decisionFactor() < 0) {
            long changedThresholdVal = (long)(currentThreshold * 0.8);
            if (changedThresholdVal < coldDataCgCtrService.getBrokerConfig().getCgColdReadThreshold()) {
                changedThresholdVal = coldDataCgCtrService.getBrokerConfig().getCgColdReadThreshold();
            }
            coldDataCgCtrService.addOrUpdateGroupConfig(consumerGroup, changedThresholdVal);
        }
    }

    /** 记录期望与实际全局冷读之差，维护最近 {@link #MAX_STORE_NUMS} 条误差历史。 */
    @Override
    public void collect(Long globalAcc) {
        et = expectGlobalVal - globalAcc;
        historyEtValList.add(et);
        Iterator<Long> iterator = historyEtValList.iterator();
        while (historyEtValList.size() > MAX_STORE_NUMS && iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
    }
}
