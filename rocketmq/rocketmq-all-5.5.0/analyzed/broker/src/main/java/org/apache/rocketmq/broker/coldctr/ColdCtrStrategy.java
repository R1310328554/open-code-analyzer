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

/**
 * 冷读流控策略 SPI：根据全局冷读量决策加速或减速各 consumer group 的冷读阈值。
 */
public interface ColdCtrStrategy {
    /**
     * 计算加/减速决策因子；正值倾向加速，负值倾向减速。
     * @return 决策因子，简单策略可返回 null
     */
    Double decisionFactor();
    /**
     * 提高指定 consumer group 的冷读阈值（放宽限速）。
     * @param consumerGroup 消费组名（自适应策略带 ||adaptive 后缀）
     * @param currentThreshold 当前阈值
     */
    void promote(String consumerGroup, Long currentThreshold);
    /**
     * 降低指定 consumer group 的冷读阈值（收紧限速）。
     * @param consumerGroup 消费组名
     * @param currentThreshold 当前阈值
     */
    void decelerate(String consumerGroup, Long currentThreshold);
    /**
     * 采集本周期全局冷读累计量，供策略更新内部状态。
     * @param globalAcc 全局冷读字节累计
     */
    void collect(Long globalAcc);
}
