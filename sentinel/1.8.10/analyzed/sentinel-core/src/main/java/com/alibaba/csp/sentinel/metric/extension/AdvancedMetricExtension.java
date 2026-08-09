/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.metric.extension;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;

/**
 * 扩展版 {@link MetricExtension}，各指标采集方法的入参携带 {@link ResourceWrapper} 等更丰富上下文。
 *
 * @author bill_yip
 * @author Eric Zhao
 * @since 1.8.0
 */
public interface AdvancedMetricExtension extends MetricExtension {

    /**
     * 累加资源通过（放行）次数。
     *
     * @param rw          资源表示（含资源名、流量类型等）
     * @param batchCount  待累加次数
     * @param args        资源附加参数；若资源为方法名，则为方法参数
     */
    void onPass(ResourceWrapper rw, int batchCount, Object[] args);

    /**
     * 累加资源被限流/阻断次数。
     *
     * @param rw         资源表示（含资源名、流量类型等）
     * @param batchCount 待累加次数
     * @param origin     调用方来源（若有）
     * @param e          关联的 {@code BlockException}
     * @param args       资源附加参数；若资源为方法名，则为方法参数
     */
    void onBlocked(ResourceWrapper rw, int batchCount, String origin, BlockException e,
                   Object[] args);

    /**
     * 累加资源调用完成次数。
     *
     * @param rw         资源表示（含资源名、流量类型等）
     * @param batchCount 待累加次数
     * @param rt         本次调用响应时间
     * @param args       资源附加参数
     */
    void onComplete(ResourceWrapper rw, long rt, int batchCount, Object[] args);

    /**
     * 累加资源调用异常次数。
     *
     * @param rw         资源表示（含资源名、流量类型等）
     * @param batchCount 待累加次数
     * @param throwable  关联异常
     * @param args       资源附加参数
     */
    void onError(ResourceWrapper rw, Throwable throwable, int batchCount, Object[] args);
}
