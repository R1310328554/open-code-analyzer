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

package com.alibaba.csp.sentinel.metric;

import com.alibaba.csp.sentinel.init.InitFunc;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.metric.collector.MetricCollector;
import com.alibaba.csp.sentinel.metric.exporter.MetricExporter;
import com.alibaba.csp.sentinel.metric.exporter.jmx.JMXMetricExporter;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link InitFunc} 实现：加载并启动 {@link MetricExporter}，注册 JVM 关闭钩子。
 *
 * @author chenglu
 * @date 2021-07-01 19:58
 * @since 1.8.3
 */
public class MetricExporterInit implements InitFunc {
    
    /** 已注册的指标导出器列表。 */
    private static List<MetricExporter> metricExporters = new ArrayList<>();
    
    /* 静态加载指标导出器 */
    static {
        // 当前以硬编码方式注册 JMX 导出器
        metricExporters.add(new JMXMetricExporter());
    }
    
    @Override
    public void init() throws Exception {
        RecordLog.info("[MetricExporterInit] MetricExporter start init.");
        // 启动各 MetricExporter
        for (MetricExporter metricExporter : metricExporters) {
           try {
               metricExporter.start();
           } catch (Exception e) {
               RecordLog.warn("[MetricExporterInit] MetricExporterInit start the metricExport[{}] failed, will ignore it.",
                       metricExporter.getClass().getName(), e);
           }
        }
        
        // 注册关闭钩子以优雅停止导出器
        Runtime.getRuntime().addShutdownHook(new Thread(
                () -> metricExporters.forEach(metricExporter -> {
                    try {
                        metricExporter.shutdown();
                    } catch (Exception e) {
                        RecordLog.warn("[MetricExporterInit] MetricExporterInit shutdown the metricExport[{}] failed, will ignore it.",
                                metricExporter.getClass().getName(), e);
                    }
                })
        ));
    }
}
