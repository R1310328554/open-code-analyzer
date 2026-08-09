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
package org.apache.rocketmq.store.metrics;

import io.opentelemetry.api.common.AttributesBuilder;
import java.util.List;
import java.util.function.Supplier;
import org.apache.rocketmq.common.Pair;
import org.apache.rocketmq.store.MessageStore;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.ViewBuilder;

/**
 * 存储指标管理器接口：为不同 MessageStore 实现提供统一的 OpenTelemetry 指标接入。
 */
public interface StoreMetricsManager {

    /**
     * 初始化指标采集。
     *
     * @param meter OpenTelemetry Meter
     * @param attributesBuilderSupplier 指标属性构建器供应者
     * @param messageStore MessageStore 实例
     */
    void init(Meter meter, Supplier<AttributesBuilder> attributesBuilderSupplier, MessageStore messageStore);

    /**
     * 获取指标 View 配置。
     *
     * @return InstrumentSelector 与 ViewBuilder 对列表
     */
    List<Pair<InstrumentSelector, ViewBuilder>> getMetricsView();

}
