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
package com.alibaba.csp.sentinel.slots.block.flow;

import java.util.Comparator;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;

/**
 * 流控规则比较器，用于规则加载时的排序。
 * <p>集群模式规则排在末尾；默认来源（default）规则优先级最低。</p>
 *
 * @author jialiang.linjl
 */
public class FlowRuleComparator implements Comparator<FlowRule> {

    /** 比较两条流控规则的优先级顺序。 */
    @Override
    public int compare(FlowRule o1, FlowRule o2) {
        // the FlowRule in Clustered mode will be put at the end.
        if (o1.isClusterMode() && !o2.isClusterMode()) {
            return 1;
        }

        if (!o1.isClusterMode() && o2.isClusterMode()) {
            return -1;
        }

        if (o1.getLimitApp() == null) {
            return 0;
        }

        if (o1.getLimitApp().equals(o2.getLimitApp())) {
            return 0;
        }

        if (RuleConstant.LIMIT_APP_DEFAULT.equals(o1.getLimitApp())) {
            return 1;
        } else if (RuleConstant.LIMIT_APP_DEFAULT.equals(o2.getLimitApp())) {
            return -1;
        } else {
            return 0;
        }
    }

}
