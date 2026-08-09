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
package com.alibaba.csp.sentinel.slots.statistic;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.slotchain.ProcessorSlotExitCallback;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParameterMetric;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParameterMetricStorage;

/**
 * 热点参数流控 exit 回调：请求正常退出 {@link StatisticSlot} 时递减参数并发计数。
 * 若 entry 已被阻断（{@code getBlockError() != null}）则跳过。
 *
 * @author Eric Zhao
 * @since 0.2.0
 */
public class ParamFlowStatisticExitCallback implements ProcessorSlotExitCallback {

    @Override
    public void onExit(Context context, ResourceWrapper resourceWrapper, int count, Object... args) {
        if (context.getCurEntry().getBlockError() == null) {
            ParameterMetric parameterMetric = ParameterMetricStorage.getParamMetric(resourceWrapper);

            if (parameterMetric != null) {
                parameterMetric.decreaseThreadCount(args);
            }
        }
    }
}