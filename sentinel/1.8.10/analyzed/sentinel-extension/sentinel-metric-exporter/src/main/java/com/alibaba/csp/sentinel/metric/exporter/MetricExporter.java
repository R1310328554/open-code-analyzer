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

package com.alibaba.csp.sentinel.metric.exporter;

/**
 * 指标导出 SPI：将 Sentinel 运行时指标导出到目标监控系统。
 * 可实现此接口自定义导出方式（如 JMX、Prometheus 等）。
 *
 * @author chenglu
 * @date 2021-07-01 21:16
 */
public interface MetricExporter {
    
    /**
     * 启动导出器（如注册定时任务）。
     *
     * @throws Exception start exception.
     */
    void start() throws Exception;
    
    /**
     * 执行一次指标导出。
     *
     * @throws Exception export exception.
     */
    void export() throws Exception;
    
    /**
     * 关闭导出器并释放资源。
     *
     * @throws Exception shutdown exception.
     */
    void shutdown() throws Exception;
}
