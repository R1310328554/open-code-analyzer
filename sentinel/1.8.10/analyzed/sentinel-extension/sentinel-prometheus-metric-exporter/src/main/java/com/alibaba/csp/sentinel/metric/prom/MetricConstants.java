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
package com.alibaba.csp.sentinel.metric.prom;

/**
 * Prometheus 指标导出常量：指标族名称、标签键与各 QPS/RT 类型字段名。
 *
 * @author karl-sy
 * @date 2023-08-08 09:30
 * @since 2.0.0
 */
public final class MetricConstants {

    /** Prometheus 指标族 help 文本。 */
    public static final String METRIC_HELP = "sentinel_metrics";

    /** 资源名标签键。 */
    public static final String RESOURCE = "resource";

    /** 资源分类标签键。 */
    public static final String CLASSIFICATION = "classification";

    /** 指标类型标签键（passQps、blockQps 等）。 */
    public static final String METRIC_TYPE = "type";

    /** 通过 QPS 指标类型。 */
    public static final String PASS_QPS = "passQps";

    /** 阻断 QPS 指标类型。 */
    public static final String BLOCK_QPS = "blockQps";

    /** 成功 QPS 指标类型。 */
    public static final String SUCCESS_QPS = "successQps";

    /** 异常 QPS 指标类型。 */
    public static final String EXCEPTION_QPS = "exceptionQps";

    /** 平均响应时间指标类型。 */
    public static final String RT = "rt";

    /** 占用通过 QPS 指标类型。 */
    public static final String OCC_PASS_QPS = "occupiedPassQps";

    /** 并发线程数指标类型。 */
    public static final String CONCURRENCY = "concurrency";

    private MetricConstants() {
    }
}
