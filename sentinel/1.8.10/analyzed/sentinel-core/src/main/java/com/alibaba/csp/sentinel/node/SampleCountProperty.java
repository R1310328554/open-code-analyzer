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
package com.alibaba.csp.sentinel.node;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.property.SimplePropertyListener;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;

/**
 * 保存每秒统计桶（采样窗口）数量配置。
 *
 * @author jialiang.linjl
 * @author CarpenterLee
 */
public class SampleCountProperty {

    /**
     * <p>
     * 每秒统计桶数量，决定 QPS 计算的灵敏度。
     * 请勿直接修改此值，应使用 {@link #updateSampleCount(int)}，否则修改不会生效。
     * </p>
     * 注意：该值必须是 1000 的约数。
     */
    public static volatile int SAMPLE_COUNT = 2;

    public static void register2Property(SentinelProperty<Integer> property) {
        property.addListener(new SimplePropertyListener<Integer>() {
            @Override
            public void configUpdate(Integer value) {
                if (value != null) {
                    updateSampleCount(value);
                }
            }
        });
    }

    /**
     * 更新 {@link #SAMPLE_COUNT}。若 newSampleCount 与当前值不同，
     * 将重置所有 {@link ClusterNode}。
     *
     * @param newSampleCount 新的采样数，必须是 1000 的约数
     */
    public static void updateSampleCount(int newSampleCount) {
        if (newSampleCount != SAMPLE_COUNT) {
            SAMPLE_COUNT = newSampleCount;
            ClusterBuilderSlot.resetClusterNodes();
        }
        RecordLog.info("SAMPLE_COUNT updated to: {}", SAMPLE_COUNT);
    }
}
