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

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.ObservableLongGauge;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.metrics.ViewBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.apache.rocketmq.common.Pair;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.metrics.NopLongCounter;
import org.apache.rocketmq.common.metrics.NopLongHistogram;
import org.apache.rocketmq.common.metrics.NopObservableLongGauge;
import org.apache.rocketmq.store.MessageStore;
import org.apache.rocketmq.store.exception.ConsumeQueueException;
import org.apache.rocketmq.tieredstore.MessageStoreConfig;
import org.apache.rocketmq.tieredstore.common.FileSegmentType;
import org.apache.rocketmq.tieredstore.core.MessageStoreFetcher;
import org.apache.rocketmq.tieredstore.core.MessageStoreFetcherImpl;
import org.apache.rocketmq.tieredstore.file.FlatFileStore;
import org.apache.rocketmq.tieredstore.file.FlatMessageFile;
import org.apache.rocketmq.tieredstore.metadata.MetadataStore;
import org.apache.rocketmq.tieredstore.util.MessageStoreUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.rocketmq.store.metrics.DefaultStoreMetricsConstant.GAUGE_STORAGE_SIZE;
import static org.apache.rocketmq.store.metrics.DefaultStoreMetricsConstant.LABEL_STORAGE_MEDIUM;
import static org.apache.rocketmq.store.metrics.DefaultStoreMetricsConstant.LABEL_STORAGE_TYPE;
import static org.apache.rocketmq.tieredstore.metrics.TieredStoreMetricsConstant.COUNTER_CACHE_ACCESS;
import static org.apache.rocketmq.tieredstore.metrics.TieredStoreMetricsConstant.COUNTER_CACHE_HIT;
import static org.apache.rocketmq.tieredstore.metrics.TieredStoreMetricsConstant.COUNTER_GET_MESSAGE_FALLBACK_TOTAL;
import static org.apache.rocketmq.tieredstore.metrics.TieredStoreMetricsConstant.COUNTER_MESSAGES_DISPATCH_TOTAL;
import static org.apache.rocketmq.tieredstore.metrics.TieredStoreMetricsConstant.COUNTER_MESSAGES_OUT_TOTAL;
import static org.apache.rocketmq.tieredstore.metrics.TieredStoreMetricsConstant.GAUGE_CACHE_BYTES;
import static org.apache.rocketmq.tieredstore.metrics.TieredStoreMetricsConstant.GAUGE_CACHE_COUNT;
import static org.apache.rocketmq.tieredstore.metrics.TieredStoreMetricsConstant.GAUGE_DISPATCH_BEHIND;
import static org.apache.rocketmq.tieredstore.metrics.TieredStoreMetricsConstant.GAUGE_DISPATCH_LATENCY;
import static org.apache.rocketmq.tieredstore.metrics.TieredStoreMetricsConstant.GAUGE_STORAGE_MESSAGE_RESERVE_TIME;
import static org.apache.rocketmq.tieredstore.metrics.TieredStoreMetricsConstant.HISTOGRAM_API_LATENCY;
import static org.apache.rocketmq.tieredstore.metrics.TieredStoreMetricsConstant.HISTOGRAM_DOWNLOAD_BYTES;
import static org.apache.rocketmq.tieredstore.metrics.TieredStoreMetricsConstant.HISTOGRAM_PROVIDER_RPC_LATENCY;
import static org.apache.rocketmq.tieredstore.metrics.TieredStoreMetricsConstant.HISTOGRAM_UPLOAD_BYTES;
import static org.apache.rocketmq.tieredstore.metrics.TieredStoreMetricsConstant.LABEL_FILE_TYPE;
import static org.apache.rocketmq.tieredstore.metrics.TieredStoreMetricsConstant.LABEL_QUEUE_ID;
import static org.apache.rocketmq.tieredstore.metrics.TieredStoreMetricsConstant.LABEL_TOPIC;
import static org.apache.rocketmq.tieredstore.metrics.TieredStoreMetricsConstant.STORAGE_MEDIUM_BLOB;

/**
 * 分层存储指标管理器：注册 OpenTelemetry 指标并采集分发、缓存与存储用量。
 */
public class TieredStoreMetricsManager {

    /** 分层存储模块日志。 */
    private static final Logger log = LoggerFactory.getLogger(MessageStoreUtil.TIERED_STORE_LOGGER_NAME);
    /** 全局 Attributes 构建器供应器。 */
    public static Supplier<AttributesBuilder> attributesBuilderSupplier;
    /** 当前存储介质标识（默认 blob）。 */
    private static String storageMedium = STORAGE_MEDIUM_BLOB;

    /** 分层 Store API 延迟直方图。 */
    public static LongHistogram apiLatency = new NopLongHistogram();

    // 分层存储 Provider 侧指标
    /** Provider RPC 延迟直方图。 */
    public static LongHistogram providerRpcLatency = new NopLongHistogram();
    /** 上传字节数直方图。 */
    public static LongHistogram uploadBytes = new NopLongHistogram();
    /** 下载字节数直方图。 */
    public static LongHistogram downloadBytes = new NopLongHistogram();

    /** 分发滞后消息数 Gauge。 */
    public static ObservableLongGauge dispatchBehind = new NopObservableLongGauge();
    /** 分发延迟 Gauge。 */
    public static ObservableLongGauge dispatchLatency = new NopObservableLongGauge();
    /** 已分发消息总数 Counter。 */
    public static LongCounter messagesDispatchTotal = new NopLongCounter();
    /** 对外输出消息总数 Counter。 */
    public static LongCounter messagesOutTotal = new NopLongCounter();
    /** 拉取回退下层 Store 次数 Counter。 */
    public static LongCounter fallbackTotal = new NopLongCounter();

    /** 预读缓存消息条数 Gauge。 */
    public static ObservableLongGauge cacheCount = new NopObservableLongGauge();
    /** 预读缓存占用字节 Gauge。 */
    public static ObservableLongGauge cacheBytes = new NopObservableLongGauge();
    /** 缓存访问次数 Counter。 */
    public static LongCounter cacheAccess = new NopLongCounter();
    /** 缓存命中次数 Counter。 */
    public static LongCounter cacheHit = new NopLongCounter();

    /** Broker 分层存储占用字节 Gauge。 */
    public static ObservableLongGauge storageSize = new NopObservableLongGauge();
    /** 消息保留时长 Gauge。 */
    public static ObservableLongGauge storageMessageReserveTime = new NopObservableLongGauge();

    /** 返回分层存储直方图的自定义分桶 View 配置。 */
    public static List<Pair<InstrumentSelector, ViewBuilder>> getMetricsView() {
        ArrayList<Pair<InstrumentSelector, ViewBuilder>> res = new ArrayList<>();

        InstrumentSelector providerRpcLatencySelector = InstrumentSelector.builder()
            .setType(InstrumentType.HISTOGRAM)
            .setName(HISTOGRAM_PROVIDER_RPC_LATENCY)
            .build();

        InstrumentSelector rpcLatencySelector = InstrumentSelector.builder()
            .setType(InstrumentType.HISTOGRAM)
            .setName(HISTOGRAM_API_LATENCY)
            .build();

        ViewBuilder rpcLatencyViewBuilder = View.builder()
            .setAggregation(Aggregation.explicitBucketHistogram(Arrays.asList(1d, 3d, 5d, 7d, 10d, 100d, 200d, 400d, 600d, 800d, 1d * 1000, 1d * 1500, 1d * 3000)))
            .setDescription("tiered_store_rpc_latency_view");

        InstrumentSelector uploadBufferSizeSelector = InstrumentSelector.builder()
            .setType(InstrumentType.HISTOGRAM)
            .setName(HISTOGRAM_UPLOAD_BYTES)
            .build();

        InstrumentSelector downloadBufferSizeSelector = InstrumentSelector.builder()
            .setType(InstrumentType.HISTOGRAM)
            .setName(HISTOGRAM_DOWNLOAD_BYTES)
            .build();

        ViewBuilder bufferSizeViewBuilder = View.builder()
            .setAggregation(Aggregation.explicitBucketHistogram(Arrays.asList(1d * MessageStoreUtil.KB, 10d * MessageStoreUtil.KB, 100d * MessageStoreUtil.KB, 1d * MessageStoreUtil.MB, 10d * MessageStoreUtil.MB, 32d * MessageStoreUtil.MB, 50d * MessageStoreUtil.MB, 100d * MessageStoreUtil.MB)))
            .setDescription("tiered_store_buffer_size_view");

        res.add(new Pair<>(rpcLatencySelector, rpcLatencyViewBuilder));
        res.add(new Pair<>(providerRpcLatencySelector, rpcLatencyViewBuilder));
        res.add(new Pair<>(uploadBufferSizeSelector, bufferSizeViewBuilder));
        res.add(new Pair<>(downloadBufferSizeSelector, bufferSizeViewBuilder));
        return res;
    }

    /** 设置存储介质标签值。 */
    public static void setStorageMedium(String storageMedium) {
        TieredStoreMetricsManager.storageMedium = storageMedium;
    }

    /** 注册全部 OpenTelemetry 指标并绑定回调采集逻辑。 */
    public static void init(Meter meter, Supplier<AttributesBuilder> attributesBuilderSupplier,
        MessageStoreConfig storeConfig, MessageStoreFetcher fetcher,
        FlatFileStore flatFileStore, MessageStore next) {

        TieredStoreMetricsManager.attributesBuilderSupplier = attributesBuilderSupplier;

        apiLatency = meter.histogramBuilder(HISTOGRAM_API_LATENCY)
            .setDescription("Tiered store rpc latency")
            .setUnit("milliseconds")
            .ofLongs()
            .build();

        providerRpcLatency = meter.histogramBuilder(HISTOGRAM_PROVIDER_RPC_LATENCY)
            .setDescription("Tiered store rpc latency")
            .setUnit("milliseconds")
            .ofLongs()
            .build();

        uploadBytes = meter.histogramBuilder(HISTOGRAM_UPLOAD_BYTES)
            .setDescription("Tiered store upload buffer size")
            .setUnit("bytes")
            .ofLongs()
            .build();

        downloadBytes = meter.histogramBuilder(HISTOGRAM_DOWNLOAD_BYTES)
            .setDescription("Tiered store download buffer size")
            .setUnit("bytes")
            .ofLongs()
            .build();

        dispatchBehind = meter.gaugeBuilder(GAUGE_DISPATCH_BEHIND)
            .setDescription("Tiered store dispatch behind message count")
            .ofLongs()
            .buildWithCallback(measurement -> {
                for (FlatMessageFile flatFile : flatFileStore.deepCopyFlatFileToList()) {
                    try {

                        MessageQueue mq = flatFile.getMessageQueue();
                        long maxOffset = next.getMaxOffsetInQueue(mq.getTopic(), mq.getQueueId());
                        long maxTimestamp = next.getMessageStoreTimeStamp(mq.getTopic(), mq.getQueueId(), maxOffset - 1);
                        if (maxTimestamp > 0 && System.currentTimeMillis() - maxTimestamp > TimeUnit.HOURS.toMillis(flatFile.getFileReservedHours())) {
                            continue;
                        }

                        Attributes commitLogAttributes = newAttributesBuilder()
                            .put(LABEL_TOPIC, mq.getTopic())
                            .put(LABEL_QUEUE_ID, mq.getQueueId())
                            .put(LABEL_FILE_TYPE, FileSegmentType.COMMIT_LOG.name().toLowerCase())
                            .build();

                        Attributes consumeQueueAttributes = newAttributesBuilder()
                            .put(LABEL_TOPIC, mq.getTopic())
                            .put(LABEL_QUEUE_ID, mq.getQueueId())
                            .put(LABEL_FILE_TYPE, FileSegmentType.CONSUME_QUEUE.name().toLowerCase())
                            .build();
                        measurement.record(Math.max(maxOffset - flatFile.getConsumeQueueMaxOffset(), 0), consumeQueueAttributes);
                    } catch (ConsumeQueueException e) {
                        // TODO: handle exception here
                    }
                }
            });

        dispatchLatency = meter.gaugeBuilder(GAUGE_DISPATCH_LATENCY)
            .setDescription("Tiered store dispatch latency")
            .setUnit("milliseconds")
            .ofLongs()
            .buildWithCallback(measurement -> {
                for (FlatMessageFile flatFile : flatFileStore.deepCopyFlatFileToList()) {
                    try {
                        MessageQueue mq = flatFile.getMessageQueue();

                        long maxOffset = next.getMaxOffsetInQueue(mq.getTopic(), mq.getQueueId());
                        long maxTimestamp = next.getMessageStoreTimeStamp(mq.getTopic(), mq.getQueueId(), maxOffset - 1);
                        if (maxTimestamp > 0 && System.currentTimeMillis() - maxTimestamp > TimeUnit.HOURS.toMillis(flatFile.getFileReservedHours())) {
                            continue;
                        }

                        Attributes commitLogAttributes = newAttributesBuilder()
                            .put(LABEL_TOPIC, mq.getTopic())
                            .put(LABEL_QUEUE_ID, mq.getQueueId())
                            .put(LABEL_FILE_TYPE, FileSegmentType.COMMIT_LOG.name().toLowerCase())
                            .build();

                        Attributes consumeQueueAttributes = newAttributesBuilder()
                            .put(LABEL_TOPIC, mq.getTopic())
                            .put(LABEL_QUEUE_ID, mq.getQueueId())
                            .put(LABEL_FILE_TYPE, FileSegmentType.CONSUME_QUEUE.name().toLowerCase())
                            .build();
                        long consumeQueueDispatchOffset = flatFile.getConsumeQueueMaxOffset();
                        long consumeQueueDispatchLatency = next.getMessageStoreTimeStamp(mq.getTopic(), mq.getQueueId(), consumeQueueDispatchOffset);
                        if (maxOffset <= consumeQueueDispatchOffset || consumeQueueDispatchLatency < 0) {
                            measurement.record(0, consumeQueueAttributes);
                        } else {
                            measurement.record(System.currentTimeMillis() - consumeQueueDispatchLatency, consumeQueueAttributes);
                        }
                    } catch (ConsumeQueueException e) {
                        // TODO: handle exception
                    }
                }
            });

        messagesDispatchTotal = meter.counterBuilder(COUNTER_MESSAGES_DISPATCH_TOTAL)
            .setDescription("Total number of dispatch messages")
            .build();

        messagesOutTotal = meter.counterBuilder(COUNTER_MESSAGES_OUT_TOTAL)
            .setDescription("Total number of outgoing messages")
            .build();

        fallbackTotal = meter.counterBuilder(COUNTER_GET_MESSAGE_FALLBACK_TOTAL)
            .setDescription("Total times of fallback to next store when getting message")
            .build();

        cacheCount = meter.gaugeBuilder(GAUGE_CACHE_COUNT)
            .setDescription("Tiered store cache message count")
            .ofLongs()
            .buildWithCallback(measurement -> {
                if (fetcher instanceof MessageStoreFetcherImpl) {
                    long count = ((MessageStoreFetcherImpl) fetcher).getFetcherCache().estimatedSize();
                    measurement.record(count, newAttributesBuilder().build());
                }
            });

        cacheBytes = meter.gaugeBuilder(GAUGE_CACHE_BYTES)
            .setDescription("Tiered store cache message bytes")
            .setUnit("bytes")
            .ofLongs()
            .buildWithCallback(measurement -> {
                if (fetcher instanceof MessageStoreFetcherImpl) {
                    long bytes = ((MessageStoreFetcherImpl) fetcher).getFetcherCache().policy().eviction()
                        .map(eviction -> eviction.weightedSize().orElse(0L))
                        .orElse(0L);
                    measurement.record(bytes, newAttributesBuilder().build());
                }
            });

        cacheAccess = meter.counterBuilder(COUNTER_CACHE_ACCESS)
            .setDescription("Tiered store cache access count")
            .build();

        cacheHit = meter.counterBuilder(COUNTER_CACHE_HIT)
            .setDescription("Tiered store cache hit count")
            .build();

        storageSize = meter.gaugeBuilder(GAUGE_STORAGE_SIZE)
            .setDescription("Broker storage size")
            .setUnit("bytes")
            .ofLongs()
            .buildWithCallback(measurement -> {
                Map<String, Map<FileSegmentType, Long>> topicFileSizeMap = new HashMap<>();
                try {
                    MetadataStore metadataStore = flatFileStore.getMetadataStore();
                    metadataStore.iterateFileSegment(fileSegment -> {
                        Map<FileSegmentType, Long> subMap =
                            topicFileSizeMap.computeIfAbsent(fileSegment.getPath(), k -> new HashMap<>());
                        FileSegmentType fileSegmentType =
                            FileSegmentType.valueOf(fileSegment.getType());
                        Long size = subMap.computeIfAbsent(fileSegmentType, k -> 0L);
                        subMap.put(fileSegmentType, size + fileSegment.getSize());
                    });
                } catch (Exception e) {
                    log.error("Failed to get storage size", e);
                }
                topicFileSizeMap.forEach((topic, subMap) -> {
                    subMap.forEach((fileSegmentType, size) -> {
                        Attributes attributes = newAttributesBuilder()
                            .put(LABEL_TOPIC, topic)
                            .put(LABEL_FILE_TYPE, fileSegmentType.name().toLowerCase())
                            .build();
                        measurement.record(size, attributes);
                    });
                });
            });

        storageMessageReserveTime = meter.gaugeBuilder(GAUGE_STORAGE_MESSAGE_RESERVE_TIME)
            .setDescription("Broker message reserve time")
            .setUnit("milliseconds")
            .ofLongs()
            .buildWithCallback(measurement -> {
                for (FlatMessageFile flatFile : flatFileStore.deepCopyFlatFileToList()) {
                    long timestamp = flatFile.getMinStoreTimestamp();
                    if (timestamp > 0) {
                        MessageQueue mq = flatFile.getMessageQueue();
                        Attributes attributes = newAttributesBuilder()
                            .put(LABEL_TOPIC, mq.getTopic())
                            .put(LABEL_QUEUE_ID, mq.getQueueId())
                            .build();
                        measurement.record(System.currentTimeMillis() - timestamp, attributes);
                    }
                }
            });
    }

    /** 创建带 storage_type=tiered 与介质标签的 Attributes 构建器。 */
    public static AttributesBuilder newAttributesBuilder() {
        AttributesBuilder builder = attributesBuilderSupplier != null ? attributesBuilderSupplier.get() : Attributes.builder();
        return builder.put(LABEL_STORAGE_TYPE, "tiered")
            .put(LABEL_STORAGE_MEDIUM, storageMedium);
    }
}
