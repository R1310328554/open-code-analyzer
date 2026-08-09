/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.adapter.gateway.common.rule;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowItem;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;

/**
 * 网关规则转换器，将 {@link GatewayFlowRule} 转换为内部 {@link ParamFlowRule}。
 *
 * @author Eric Zhao
 * @since 1.6.0
 */
final class GatewayRuleConverter {

    static FlowRule toFlowRule(/*@Valid*/ GatewayFlowRule rule) {
        return new FlowRule(rule.getResource())
            .setControlBehavior(rule.getControlBehavior())
            .setCount(rule.getCount())
            .setGrade(rule.getGrade())
            .setMaxQueueingTimeMs(rule.getMaxQueueingTimeoutMs());
    }

    static ParamFlowItem generateNonMatchPassParamItem() {
        return new ParamFlowItem().setClassType(String.class.getName())
            .setCount(1000_0000)
            .setObject(SentinelGatewayConstants.GATEWAY_NOT_MATCH_PARAM);
    }

    static ParamFlowItem generateNonMatchBlockParamItem() {
        return new ParamFlowItem().setClassType(String.class.getName())
            .setCount(0)
            .setObject(SentinelGatewayConstants.GATEWAY_NOT_MATCH_PARAM);
    }

    static ParamFlowRule applyNonParamToParamRule(/*@Valid*/ GatewayFlowRule gatewayRule, int idx) {
        return new ParamFlowRule(gatewayRule.getResource())
            .setCount(gatewayRule.getCount())
            .setGrade(gatewayRule.getGrade())
            .setDurationInSec(gatewayRule.getIntervalSec())
            .setBurstCount(gatewayRule.getBurst())
            .setControlBehavior(gatewayRule.getControlBehavior())
            .setMaxQueueingTimeMs(gatewayRule.getMaxQueueingTimeoutMs())
            .setParamIdx(idx);
    }

    /**
     * 将网关规则转换为参数流控规则，并将生成的参数索引写入规则的 {@link GatewayParamFlowItem}。
     *
     * @param gatewayRule 有效的网关规则，应包含有效的参数项
     * @param idx 生成的参数索引（调用方应保证唯一且递增）
     * @return 转换后的参数流控规则
     */
    static ParamFlowRule applyToParamRule(/*@Valid*/ GatewayFlowRule gatewayRule, int idx) {
        ParamFlowRule paramRule = new ParamFlowRule(gatewayRule.getResource())
            .setCount(gatewayRule.getCount())
            .setGrade(gatewayRule.getGrade())
            .setDurationInSec(gatewayRule.getIntervalSec())
            .setBurstCount(gatewayRule.getBurst())
            .setControlBehavior(gatewayRule.getControlBehavior())
            .setMaxQueueingTimeMs(gatewayRule.getMaxQueueingTimeoutMs())
            .setParamIdx(idx);
        GatewayParamFlowItem gatewayItem = gatewayRule.getParamItem();
        // 将当前 idx 写入网关规则项。
        gatewayItem.setIndex(idx);
        // 为基于 pattern 的参数添加非匹配放行项。
        String valuePattern = gatewayItem.getPattern();
        if (valuePattern != null) {
            paramRule.getParamFlowItemList().add(generateNonMatchPassParamItem());
        }
        return paramRule;
    }

    private GatewayRuleConverter() {}
}
