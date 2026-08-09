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

import java.util.ArrayList;
import java.util.List;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.spi.SpiLoader;

/**
 * 通过 SPI 加载并持有全部 {@link MetricExtension} 实例。
 *
 * @author Carpenter Lee
 * @since 1.6.1
 */
public class MetricExtensionProvider {
    private static List<MetricExtension> metricExtensions = new ArrayList<>();

    static {
        resolveInstance();
    }

    private static void resolveInstance() {
        List<MetricExtension> extensions = SpiLoader.of(MetricExtension.class).loadInstanceList();

        if (extensions.isEmpty()) {
            RecordLog.info("[MetricExtensionProvider] No existing MetricExtension found");
        } else {
            metricExtensions.addAll(extensions);
            RecordLog.info("[MetricExtensionProvider] MetricExtension resolved, size={}", extensions.size());
        }
    }

    /**
     * <p>获取已注册的全部指标扩展。</p>
     * <p>请勿直接修改返回列表，应使用 {@link #addMetricExtension(MetricExtension)}。</p>
     *
     * @return 已注册的全部指标扩展
     */
    public static List<MetricExtension> getMetricExtensions() {
        return metricExtensions;
    }

    /**
     * 添加指标扩展。
     * <p>
     * 注意：本方法非线程安全。
     * </p>
     *
     * @param metricExtension 待添加的指标扩展
     */
    public static void addMetricExtension(MetricExtension metricExtension) {
        metricExtensions.add(metricExtension);
    }

}
