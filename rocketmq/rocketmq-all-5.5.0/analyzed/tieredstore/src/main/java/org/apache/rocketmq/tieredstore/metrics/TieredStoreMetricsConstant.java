/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.tieredstore.metrics;

/**
 * 分层存储 OpenTelemetry 指标名与标签常量。
 */
public class TieredStoreMetricsConstant {
    /** API 调用延迟直方图指标名。 */
    public static final String HISTOGRAM_API_LATENCY = "rocketmq_tiered_store_api_latency";
    /** 存储 Provider RPC 延迟直方图指标名。 */
    public static final String HISTOGRAM_PROVIDER_RPC_LATENCY = "rocketmq_tiered_store_provider_rpc_latency";
    /** 上传字节数直方图指标名。 */
    public static final String HISTOGRAM_UPLOAD_BYTES = "rocketmq_tiered_store_provider_upload_bytes";
    /** 下载字节数直方图指标名。 */
    public static final String HISTOGRAM_DOWNLOAD_BYTES = "rocketmq_tiered_store_provider_download_bytes";

    /** 分发滞后消息数 Gauge 指标名。 */
    public static final String GAUGE_DISPATCH_BEHIND = "rocketmq_tiered_store_dispatch_behind";
    /** 分发延迟 Gauge 指标名。 */
    public static final String GAUGE_DISPATCH_LATENCY = "rocketmq_tiered_store_dispatch_latency";
    /** 已分发消息总数 Counter 指标名。 */
    public static final String COUNTER_MESSAGES_DISPATCH_TOTAL = "rocketmq_tiered_store_messages_dispatch_total";
    /** 对外输出消息总数 Counter 指标名。 */
    public static final String COUNTER_MESSAGES_OUT_TOTAL = "rocketmq_tiered_store_messages_out_total";
    /** 拉取消息回退下层 Store 次数 Counter 指标名。 */
    public static final String COUNTER_GET_MESSAGE_FALLBACK_TOTAL = "rocketmq_tiered_store_get_message_fallback_total";

    /** 预读缓存消息条数 Gauge 指标名。 */
    public static final String GAUGE_CACHE_COUNT = "rocketmq_tiered_store_read_ahead_cache_count";
    /** 预读缓存占用字节 Gauge 指标名。 */
    public static final String GAUGE_CACHE_BYTES = "rocketmq_tiered_store_read_ahead_cache_bytes";
    /** 缓存访问次数 Counter 指标名。 */
    public static final String COUNTER_CACHE_ACCESS = "rocketmq_tiered_store_read_ahead_cache_access_total";
    /** 缓存命中次数 Counter 指标名。 */
    public static final String COUNTER_CACHE_HIT = "rocketmq_tiered_store_read_ahead_cache_hit_total";

    /** 消息保留时长 Gauge 指标名。 */
    public static final String GAUGE_STORAGE_MESSAGE_RESERVE_TIME = "rocketmq_storage_message_reserve_time";

    /** 操作类型标签名。 */
    public static final String LABEL_OPERATION = "operation";
    /** 操作是否成功标签名。 */
    public static final String LABEL_SUCCESS = "success";

    /** 文件路径标签名。 */
    public static final String LABEL_PATH = "path";
    /** Topic 标签名。 */
    public static final String LABEL_TOPIC = "topic";
    /** 消费者组标签名。 */
    public static final String LABEL_GROUP = "group";
    /** 队列 ID 标签名。 */
    public static final String LABEL_QUEUE_ID = "queue_id";
    /** 文件段类型标签名。 */
    public static final String LABEL_FILE_TYPE = "file_type";

    // 对象存储介质常量
    /** 对象存储（Blob）介质标识。 */
    public static final String STORAGE_MEDIUM_BLOB = "blob";

    /** getMessage API 操作名。 */
    public static final String OPERATION_API_GET_MESSAGE = "get_message";
    /** getEarliestMessageTime API 操作名。 */
    public static final String OPERATION_API_GET_EARLIEST_MESSAGE_TIME = "get_earliest_message_time";
    /** getMessageStoreTimeStamp API 操作名。 */
    public static final String OPERATION_API_GET_TIME_BY_OFFSET = "get_time_by_offset";
    /** getOffsetInQueueByTime API 操作名。 */
    public static final String OPERATION_API_GET_OFFSET_BY_TIME = "get_offset_by_time";
    /** queryMessage API 操作名。 */
    public static final String OPERATION_API_QUERY_MESSAGE = "query_message";
}
