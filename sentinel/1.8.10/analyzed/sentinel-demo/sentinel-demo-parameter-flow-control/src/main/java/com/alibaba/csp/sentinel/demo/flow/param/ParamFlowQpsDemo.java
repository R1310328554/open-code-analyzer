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
package com.alibaba.csp.sentinel.demo.flow.param;

import java.util.Collections;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowItem;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;

/**
 * 热点参数流控演示：按第 0 个参数值分别统计 QPS，并为特定参数设置例外阈值。
 *
 * @author Eric Zhao
 * @since 0.2.0
 */
public class ParamFlowQpsDemo {

    private static final int PARAM_A = 1;
    private static final int PARAM_B = 2;
    private static final int PARAM_C = 3;
    private static final int PARAM_D = 4;

    /** 准备多种参数值，用于验证热点参数流控效果。 */
    private static final Integer[] PARAMS = new Integer[] {PARAM_A, PARAM_B, PARAM_C, PARAM_D};

    private static final String RESOURCE_KEY = "resA";

    public static void main(String[] args) throws Exception {
        initParamFlowRules();

        final int threadCount = 20;
        ParamFlowQpsRunner<Integer> runner = new ParamFlowQpsRunner<>(PARAMS, RESOURCE_KEY, threadCount, 120);
        runner.tick();

        Thread.sleep(1000);
        runner.simulateTraffic();
    }

    /** 加载热点参数流控规则：全局 QPS=5，参数 PARAM_B 例外 QPS=10。 */
    private static void initParamFlowRules() {
        // QPS 模式：第 0 个参数（热点参数）全局阈值为 5
        ParamFlowRule rule = new ParamFlowRule(RESOURCE_KEY)
            .setParamIdx(0)
            .setGrade(RuleConstant.FLOW_GRADE_QPS)
            //.setDurationInSec(3)
            //.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER)
            //.setMaxQueueingTimeMs(600)
            .setCount(5);

        // 可为特定参数值单独设置阈值：PARAM_B 的 QPS 阈值为 10，而非全局 5
        ParamFlowItem item = new ParamFlowItem().setObject(String.valueOf(PARAM_B))
            .setClassType(int.class.getName())
            .setCount(10);
        rule.setParamFlowItemList(Collections.singletonList(item));
        ParamFlowRuleManager.loadRules(Collections.singletonList(rule));
    }
}
