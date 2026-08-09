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
package org.apache.rocketmq.broker.metrics;

import com.google.common.collect.Lists;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramData;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramPointData;
import io.opentelemetry.sdk.metrics.data.HistogramData;
import io.opentelemetry.sdk.metrics.data.HistogramPointData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricDataType;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.data.SumData;
import io.opentelemetry.sdk.metrics.data.SummaryPointData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableExponentialHistogramData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableGaugeData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableHistogramData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSumData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSummaryData;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.IntSupplier;

/**
 * {@link MetricExporter} 装饰器：将大批量指标拆分为小子批次再委托底层导出器。
 *
 * <p>解决 OTLP 经 gRPC 导出时的 32MB 载荷限制。高基数指标（如 consumer_group × topic
 * 的消费滞后）可能使单次导出超限导致全部失败。
 *
 * <p>拆分依据为数据点总数（非 MetricData 对象数），单对象可含数千点。
 * 未超阈值时走快速路径直接透传。
 *
 * <p>单 MetricData 超限时将其内部点拆成多个较小对象，保留原 resource/scope/名称等元数据。
 */
public final class BatchSplittingMetricExporter
    implements MetricExporter {

    /** 日志记录器。 */
    private static final Logger LOGGER =
        LoggerFactory.getLogger(
            LoggerName.BROKER_LOGGER_NAME);

    /** 委托的底层指标导出器。 */
    private final MetricExporter delegate;

    /** 运行时提供每批最大数据点数的供应器。 */
    private final IntSupplier maxBatchSizeSupplier;

    /**
     * 创建批量拆分指标导出器。
     *
     * @param metricExporter 底层 MetricExporter
     * @param batchSizeSupplier 每批最大数据点数供应器，返回值须 &gt; 0
     */
    public BatchSplittingMetricExporter(
        final MetricExporter metricExporter,
        final IntSupplier batchSizeSupplier) {
        if (metricExporter == null) {
            throw new NullPointerException(
                "metricExporter must not be null");
        }
        if (batchSizeSupplier == null) {
            throw new NullPointerException(
                "batchSizeSupplier must not be null");
        }
        this.delegate = metricExporter;
        this.maxBatchSizeSupplier = batchSizeSupplier;
    }

    /** {@inheritDoc} */
    /** 按数据点阈值拆分批次后委托底层导出。 */
    @Override
    public CompletableResultCode export(
        final Collection<MetricData> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            return delegate.export(metrics);
        }

        // 快照避免 OTel SDK 序列化时并发修改 AIOOBE
        List<MetricData> snapshotted =
            snapshotAllMetrics(metrics);

        int maxBatchSize =
            maxBatchSizeSupplier.getAsInt();

        int totalDataPoints = 0;
        for (MetricData md : snapshotted) {
            totalDataPoints +=
                md.getData().getPoints().size();
        }

        if (totalDataPoints <= maxBatchSize) {
            return delegate.export(snapshotted);
        }

        List<List<MetricData>> batches =
            splitIntoBatches(snapshotted, maxBatchSize);

        LOGGER.debug(
            "Splitting metrics export: "
                + "totalDataPoints={}, "
                + "maxBatchSize={}, "
                + "batchCount={}",
            totalDataPoints, maxBatchSize,
            batches.size());

        List<CompletableResultCode> results =
            new ArrayList<>(batches.size());
        for (int i = 0; i < batches.size(); i++) {
            final List<MetricData> batch =
                batches.get(i);
            final int batchIndex = i;
            CompletableResultCode r =
                delegate.export(batch);
            r.whenComplete(() -> {
                if (!r.isSuccess()) {
                    logFailedBatch(batchIndex, batch);
                }
            });
            results.add(r);
        }

        return CompletableResultCode.ofAll(results);
    }

    /**
     * Logs details of a failed batch export.
     *
     * @param batchIndex the index of the failed batch
     * @param batch the batch that failed
     */
    private static void logFailedBatch(
        final int batchIndex,
        final List<MetricData> batch) {
        StringBuilder names = new StringBuilder();
        for (MetricData md : batch) {
            if (names.length() > 0) {
                names.append(",");
            }
            names.append(md.getName())
                .append("(")
                .append(
                    md.getData()
                        .getPoints().size())
                .append("pts)");
        }
        LOGGER.warn(
            "Batch {} failed. Metrics: {}",
            batchIndex, names);
    }

    /**
     * Creates defensive snapshots of all MetricData by
     * copying their data point collections into new
     * ArrayLists. This prevents
     * {@link ArrayIndexOutOfBoundsException} in the OTel
     * SDK marshaling code when callback threads
     * concurrently modify point collections during export.
     *
     * @param metrics the original metrics collection
     * @return list of snapshotted MetricData
     */
    private static List<MetricData> snapshotAllMetrics(
        final Collection<MetricData> metrics) {
        List<MetricData> result =
            new ArrayList<>(metrics.size());
        for (MetricData md : metrics) {
            try {
                result.add(snapshotMetricData(md));
            } catch (Exception e) {
                LOGGER.warn(
                    "Failed to snapshot MetricData:"
                        + " {}, using original",
                    md.getName(), e);
                result.add(md);
            }
        }
        return result;
    }

    /**
     * Creates a snapshot of a single MetricData by copying
     * its points into a new ArrayList and reconstructing
     * the MetricData with immutable internal data.
     *
     * @param md the original MetricData
     * @return a new MetricData with snapshotted points
     */
    private static MetricData snapshotMetricData(
        final MetricData md) {
        List<PointData> points =
            new ArrayList<>(md.getData().getPoints());
        return createMetricDataForType(
            md, md.getType(), points);
    }

    /**
     * Splits metrics into sub-batches by data point count.
     * When a single MetricData has more points than the
     * batch limit, its points are split into multiple
     * smaller MetricData objects.
     *
     * @param metrics the full metrics collection
     * @param maxBatchSize max data points per batch
     * @return list of sub-batches
     */
    private List<List<MetricData>> splitIntoBatches(
        final Collection<MetricData> metrics,
        final int maxBatchSize) {
        List<List<MetricData>> batches =
            new ArrayList<>();
        List<MetricData> currentBatch =
            new ArrayList<>();
        int currentPoints = 0;

        for (MetricData md : metrics) {
            int pts =
                md.getData().getPoints().size();

            if (pts > maxBatchSize) {
                // Flush current batch first
                if (!currentBatch.isEmpty()) {
                    batches.add(currentBatch);
                    currentBatch = new ArrayList<>();
                    currentPoints = 0;
                }
                // Split the large MetricData
                List<MetricData> subMetrics =
                    splitMetricData(
                        md, maxBatchSize);
                for (MetricData sub : subMetrics) {
                    int subPts = sub.getData()
                        .getPoints().size();
                    if (currentPoints > 0
                        && currentPoints + subPts
                            > maxBatchSize) {
                        batches.add(currentBatch);
                        currentBatch =
                            new ArrayList<>();
                        currentPoints = 0;
                    }
                    currentBatch.add(sub);
                    currentPoints += subPts;
                }
                continue;
            }

            if (currentPoints > 0
                && currentPoints + pts
                    > maxBatchSize) {
                batches.add(currentBatch);
                currentBatch = new ArrayList<>();
                currentPoints = 0;
            }

            currentBatch.add(md);
            currentPoints += pts;
        }

        if (!currentBatch.isEmpty()) {
            batches.add(currentBatch);
        }

        return batches;
    }

    /**
     * Splits a single MetricData into multiple MetricData
     * objects, each containing at most maxChunkSize points.
     * The original metadata (resource, scope, name, etc.)
     * is preserved in each resulting MetricData.
     *
     * <p>NOTE: This method and the createXxx helpers below
     * use OTel SDK internal APIs (ImmutableMetricData,
     * ImmutableGaugeData, etc. from the
     * io.opentelemetry.sdk.metrics.internal.data package).
     * These are not public API and may change across SDK
     * upgrades. When upgrading the OTel SDK version, check
     * for breaking changes in these factory methods.
     *
     * @param md the MetricData to split
     * @param maxChunkSize max points per chunk
     * @return list of MetricData, each with &lt;=
     *     maxChunkSize points
     */
    @SuppressWarnings("unchecked")
    private static List<MetricData> splitMetricData(
        final MetricData md,
        final int maxChunkSize) {

        List<PointData> allPoints =
            new ArrayList<>(
                md.getData().getPoints());
        MetricDataType type = md.getType();
        List<List<PointData>> chunks =
            Lists.partition(allPoints, maxChunkSize);

        List<MetricData> result =
            new ArrayList<>(chunks.size());
        for (List<PointData> chunk : chunks) {
            result.add(
                createMetricDataForType(
                    md, type, chunk));
        }
        return result;
    }

    /**
     * Creates a new MetricData of the given type with the
     * specified subset of data points. Preserves the
     * original metadata from the source MetricData.
     *
     * @param src the original MetricData
     * @param type the MetricDataType
     * @param pts the subset of points
     * @return a new MetricData with the given points
     */
    @SuppressWarnings("unchecked")
    private static MetricData createMetricDataForType(
        final MetricData src,
        final MetricDataType type,
        final List<PointData> pts) {

        switch (type) {
            case LONG_GAUGE:
                return ImmutableMetricData
                    .createLongGauge(
                        src.getResource(),
                        src
                          .getInstrumentationScopeInfo(),
                        src.getName(),
                        src.getDescription(),
                        src.getUnit(),
                        ImmutableGaugeData.create(
                            (Collection<LongPointData>)
                                (Collection<?>) pts));
            case DOUBLE_GAUGE:
                return ImmutableMetricData
                    .createDoubleGauge(
                        src.getResource(),
                        src
                          .getInstrumentationScopeInfo(),
                        src.getName(),
                        src.getDescription(),
                        src.getUnit(),
                        ImmutableGaugeData.create(
                            (Collection<DoublePointData>)
                                (Collection<?>) pts));
            case LONG_SUM:
                return createLongSum(src, pts);
            case DOUBLE_SUM:
                return createDoubleSum(src, pts);
            case HISTOGRAM:
                return createHistogram(src, pts);
            case EXPONENTIAL_HISTOGRAM:
                return createExpHistogram(
                    src, pts);
            case SUMMARY:
                return createSummary(src, pts);
            default:
                throw new IllegalArgumentException(
                    "Unsupported MetricDataType: "
                        + type);
        }
    }

    /**
     * Creates a LONG_SUM MetricData with a subset of
     * points.
     *
     * @param src the original MetricData
     * @param pts the subset of points
     * @return new MetricData
     */
    @SuppressWarnings("unchecked")
    private static MetricData createLongSum(
        final MetricData src,
        final List<PointData> pts) {
        SumData<LongPointData> sumData =
            src.getLongSumData();
        return ImmutableMetricData.createLongSum(
            src.getResource(),
            src.getInstrumentationScopeInfo(),
            src.getName(),
            src.getDescription(),
            src.getUnit(),
            ImmutableSumData.create(
                sumData.isMonotonic(),
                sumData
                    .getAggregationTemporality(),
                (Collection<LongPointData>)
                    (Collection<?>) pts));
    }

    /**
     * Creates a DOUBLE_SUM MetricData with a subset of
     * points.
     *
     * @param src the original MetricData
     * @param pts the subset of points
     * @return new MetricData
     */
    @SuppressWarnings("unchecked")
    private static MetricData createDoubleSum(
        final MetricData src,
        final List<PointData> pts) {
        SumData<DoublePointData> sumData =
            src.getDoubleSumData();
        return ImmutableMetricData.createDoubleSum(
            src.getResource(),
            src.getInstrumentationScopeInfo(),
            src.getName(),
            src.getDescription(),
            src.getUnit(),
            ImmutableSumData.create(
                sumData.isMonotonic(),
                sumData
                    .getAggregationTemporality(),
                (Collection<DoublePointData>)
                    (Collection<?>) pts));
    }

    /**
     * Creates a HISTOGRAM MetricData with a subset of
     * points.
     *
     * @param src the original MetricData
     * @param pts the subset of points
     * @return new MetricData
     */
    @SuppressWarnings("unchecked")
    private static MetricData createHistogram(
        final MetricData src,
        final List<PointData> pts) {
        HistogramData histData =
            src.getHistogramData();
        return ImmutableMetricData
            .createDoubleHistogram(
                src.getResource(),
                src
                  .getInstrumentationScopeInfo(),
                src.getName(),
                src.getDescription(),
                src.getUnit(),
                ImmutableHistogramData.create(
                    histData
                        .getAggregationTemporality(),
                    (Collection<HistogramPointData>)
                        (Collection<?>) pts));
    }

    /**
     * Creates an EXPONENTIAL_HISTOGRAM MetricData with a
     * subset of points.
     *
     * @param src the original MetricData
     * @param pts the subset of points
     * @return new MetricData
     */
    @SuppressWarnings("unchecked")
    private static MetricData createExpHistogram(
        final MetricData src,
        final List<PointData> pts) {
        ExponentialHistogramData ehData =
            src.getExponentialHistogramData();
        return ImmutableMetricData
            .createExponentialHistogram(
                src.getResource(),
                src
                  .getInstrumentationScopeInfo(),
                src.getName(),
                src.getDescription(),
                src.getUnit(),
                ImmutableExponentialHistogramData
                    .create(
                        ehData
                          .getAggregationTemporality(),
                        (Collection<
                          ExponentialHistogramPointData>)
                            (Collection<?>) pts));
    }

    /**
     * Creates a SUMMARY MetricData with a subset of points.
     *
     * @param src the original MetricData
     * @param pts the subset of points
     * @return new MetricData
     */
    @SuppressWarnings("unchecked")
    private static MetricData createSummary(
        final MetricData src,
        final List<PointData> pts) {
        return ImmutableMetricData
            .createDoubleSummary(
                src.getResource(),
                src
                  .getInstrumentationScopeInfo(),
                src.getName(),
                src.getDescription(),
                src.getUnit(),
                ImmutableSummaryData.create(
                    (Collection<SummaryPointData>)
                        (Collection<?>) pts));
    }

    /** {@inheritDoc} */
    @Override
    public AggregationTemporality
        getAggregationTemporality(
            final InstrumentType instrumentType) {
        return delegate.getAggregationTemporality(
            instrumentType);
    }

    /** {@inheritDoc} */
    @Override
    public Aggregation getDefaultAggregation(
        final InstrumentType instrumentType) {
        return delegate
            .getDefaultAggregation(instrumentType);
    }

    /** {@inheritDoc} */
    /** 委托底层导出器 flush。 */
    @Override
    public CompletableResultCode flush() {
        return delegate.flush();
    }

    /** {@inheritDoc} */
    /** 委托底层导出器 shutdown。 */
    @Override
    public CompletableResultCode shutdown() {
        return delegate.shutdown();
    }
}
