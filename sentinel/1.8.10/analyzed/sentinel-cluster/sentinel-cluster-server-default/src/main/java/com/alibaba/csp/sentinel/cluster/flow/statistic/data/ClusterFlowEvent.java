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
package com.alibaba.csp.sentinel.cluster.flow.statistic.data;

/**
 * 集群流控统计事件类型枚举。
 *
 * @author Eric Zhao
 * @since 1.4.0
 */
public enum ClusterFlowEvent {

    /**
     * 正常通过。
     */
    PASS,
    /**
     * 正常限流。
     */
    BLOCK,
    /**
     * 客户端令牌请求通过。
     */
    PASS_REQUEST,
    /**
     * 客户端令牌请求被限流。
     */
    BLOCK_REQUEST,
    /**
     * 通过（预占用即将到来的时间桶）。
     */
    OCCUPIED_PASS,
    /**
     * 限流（预占用即将到来的时间桶失败）。
     */
    OCCUPIED_BLOCK,
    /**
     * 因流控整形或等待下一时间桶刻度而等待。
     */
    WAITING
}
