/*
 *  Copyright 1999-2021 Alibaba Group Holding Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.alibaba.csp.sentinel.metric.exporter.jmx;

import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.metric.collector.MetricCollector;
import com.alibaba.csp.sentinel.metric.exporter.MetricExporter;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * JMX 指标导出器：定时将 {@link MetricCollector} 采集的数据写入 {@link MetricBean}。
 * 实现 {@link MetricExporter} 的 start/export/shutdown 生命周期；
 * 内部 {@link JMXExportTask} 每秒调度一次导出。
 *
 * @author chenglu
 * @date 2021-07-01 20:02
 * @since 1.8.3
 */
public class JMXMetricExporter implements MetricExporter {
    
    /** 定时调度线程池。 */
    private final ScheduledExecutorService jmxExporterSchedule;
    
    /** JMX 指标写入器，负责注册/更新 {@link MetricBean}。 */
    private final MetricBeanWriter metricBeanWriter = new MetricBeanWriter();
    
    /** 全局指标采集器。 */
    private final MetricCollector metricCollector = new MetricCollector();
    
    public JMXMetricExporter() {
        jmxExporterSchedule = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("sentinel-metrics-jmx-exporter-task", true));
    }
    
    @Override
    public void start() throws Exception {
        jmxExporterSchedule.scheduleAtFixedRate(new JMXExportTask(), 1, 1, TimeUnit.SECONDS);
    }
    
    @Override
    public void export() throws Exception {
        metricBeanWriter.write(metricCollector.collectMetric());
    }
    
    @Override
    public void shutdown() throws Exception {
        jmxExporterSchedule.shutdown();
    }
    
    /** 定时任务：调用 {@link #export()} 刷新 JMX MBean 数据。 */
    class JMXExportTask implements Runnable {
        
        @Override
        public void run() {
            try {
                export();
            } catch (Exception e) {
                RecordLog.warn("[JMX Metric Exporter] export to JMX MetricBean failed.", e);
            }
        }
    }
}
