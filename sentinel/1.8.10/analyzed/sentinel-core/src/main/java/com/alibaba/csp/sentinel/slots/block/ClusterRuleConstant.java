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
package com.alibaba.csp.sentinel.slots.block;

/**
 * 集群流控规则相关常量。
 *
 * @author Eric Zhao
 * @since 1.4.0
 */
public final class ClusterRuleConstant {

    /** 普通集群流控策略。 */
    public static final int FLOW_CLUSTER_STRATEGY_NORMAL = 0;
    /** 借用令牌（Ref）集群流控策略。 */
    public static final int FLOW_CLUSTER_STRATEGY_BORROW_REF = 1;

    /** 阈值按本地实例平均分配。 */
    public static final int FLOW_THRESHOLD_AVG_LOCAL = 0;
    /** 阈值为集群全局阈值。 */
    public static final int FLOW_THRESHOLD_GLOBAL = 1;

    /** 集群流控默认采样窗口数。 */
    public static final int DEFAULT_CLUSTER_SAMPLE_COUNT = 10;

    private ClusterRuleConstant() {}
}
