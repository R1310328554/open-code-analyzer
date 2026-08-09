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
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slotchain.ProcessorSlotEntryCallback;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParameterMetric;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParameterMetricStorage;

/**
 * 热点参数流控 entry 回调：请求通过 {@link StatisticSlot} 时递增参数并发计数。
 * 仅当资源已配置参数流控规则且存在 {@link ParameterMetric} 时生效。
 *
 * @author Eric Zhao
 * @since 0.2.0
 */
public class ParamFlowStatisticEntryCallback implements ProcessorSlotEntryCallback<DefaultNode> {

    @Override
    public void onPass(Context context, ResourceWrapper resourceWrapper, DefaultNode node, int count, Object... args) {
        // 仅当资源存在参数流控规则时才有 ParameterMetric
        ParameterMetric parameterMetric = ParameterMetricStorage.getParamMetric(resourceWrapper);

        if (parameterMetric != null) {
            parameterMetric.addThreadCount(args);
        }
    }

    @Override
    public void onBlocked(BlockException ex, Context context, ResourceWrapper resourceWrapper, DefaultNode param,
                          int count, Object... args) {
        // 阻断计数不在此处理，避免判断 BlockException 类型影响性能；
        // 在抛出 ParamFlowException 时再累加 block 计数
    }
}
