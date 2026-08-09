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
package org.apache.rocketmq.remoting.metrics;

import com.google.common.collect.Lists;
import io.netty.util.concurrent.Future;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.metrics.ViewBuilder;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import org.apache.rocketmq.common.Pair;
import org.apache.rocketmq.common.metrics.NopLongHistogram;

import static org.apache.rocketmq.remoting.metrics.RemotingMetricsConstant.HISTOGRAM_RPC_LATENCY;
import static org.apache.rocketmq.remoting.metrics.RemotingMetricsConstant.LABEL_PROTOCOL_TYPE;
import static org.apache.rocketmq.remoting.metrics.RemotingMetricsConstant.PROTOCOL_TYPE_REMOTING;
import static org.apache.rocketmq.remoting.metrics.RemotingMetricsConstant.RESULT_CANCELED;
import static org.apache.rocketmq.remoting.metrics.RemotingMetricsConstant.RESULT_SUCCESS;
import static org.apache.rocketmq.remoting.metrics.RemotingMetricsConstant.RESULT_WRITE_CHANNEL_FAILED;

/**
 * Remoting 指标管理器：注册 RPC 延迟直方图并统一构建 OpenTelemetry 属性。
 */
public class RemotingMetricsManager {
    /** RPC 延迟直方图，未初始化时为无操作实现。 */
    private LongHistogram rpcLatency = new NopLongHistogram();
    private Supplier<AttributesBuilder> attributesBuilderSupplier;

    public RemotingMetricsManager() {
    }

    /** 创建带默认 protocol_type=remoting 标签的属性构建器。 */
    public AttributesBuilder newAttributesBuilder() {
        if (this.attributesBuilderSupplier == null) {
            return Attributes.builder();
        }
        return this.attributesBuilderSupplier.get()
            .put(LABEL_PROTOCOL_TYPE, PROTOCOL_TYPE_REMOTING);
    }

    /**
     * 注册 RPC 延迟直方图并绑定公共属性提供者。
     *
     * @param meter OpenTelemetry Meter
     * @param attributesBuilderSupplier 公共标签构建器工厂
     */
    public void initMetrics(Meter meter, Supplier<AttributesBuilder> attributesBuilderSupplier) {
        this.attributesBuilderSupplier = attributesBuilderSupplier;
        this.rpcLatency = meter.histogramBuilder(HISTOGRAM_RPC_LATENCY)
            .setDescription("RPC 调用延迟")
            .setUnit("milliseconds")
            .ofLongs()
            .build();
    }

    /** 返回 RPC 延迟直方图的桶边界视图配置。 */
    public List<Pair<InstrumentSelector, ViewBuilder>> getMetricsView() {
        List<Double> rpcCostTimeBuckets = Arrays.asList(
            (double) Duration.ofMillis(1).toMillis(),
            (double) Duration.ofMillis(3).toMillis(),
            (double) Duration.ofMillis(5).toMillis(),
            (double) Duration.ofMillis(7).toMillis(),
            (double) Duration.ofMillis(10).toMillis(),
            (double) Duration.ofMillis(100).toMillis(),
            (double) Duration.ofSeconds(1).toMillis(),
            (double) Duration.ofSeconds(2).toMillis(),
            (double) Duration.ofSeconds(3).toMillis()
        );
        InstrumentSelector selector = InstrumentSelector.builder()
            .setType(InstrumentType.HISTOGRAM)
            .setName(HISTOGRAM_RPC_LATENCY)
            .build();
        ViewBuilder viewBuilder = View.builder()
            .setAggregation(Aggregation.explicitBucketHistogram(rpcCostTimeBuckets));
        return Lists.newArrayList(new Pair<>(selector, viewBuilder));
    }

    /** 根据 Netty writeAndFlush 的 {@link Future} 状态映射为 result 标签值。 */
    public String getWriteAndFlushResult(Future<?> future) {
        String result = RESULT_SUCCESS;
        if (future.isCancelled()) {
            result = RESULT_CANCELED;
        } else if (!future.isSuccess()) {
            result = RESULT_WRITE_CHANNEL_FAILED;
        }
        return result;
    }

    // 供外部读取已注册指标的 getter
    public LongHistogram getRpcLatency() {
        return rpcLatency;
    }

    public Supplier<AttributesBuilder> getAttributesBuilderSupplier() {
        return attributesBuilderSupplier;
    }

    // 供单元测试注入 mock 依赖的 setter
    public void setAttributesBuilderSupplier(Supplier<AttributesBuilder> attributesBuilderSupplier) {
        this.attributesBuilderSupplier = attributesBuilderSupplier;
    }


}
