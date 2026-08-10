/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.prometheus.api;

/**
 * Prometheus 服务发现 REST API 路径常量。
 *
 * <p>定义全局、按命名空间、按服务三种 metrics 拉取 URL 模板， 供 {@link com.alibaba.nacos.prometheus.controller.PrometheusController} 与安全配置引用。</p>
 *
 * @author karsonto
 */
public class ApiConstants {
    
    /** 全量实例 Prometheus SD 接口根路径。 */
    public static final String PROMETHEUS_CONTROLLER_PATH = "/prometheus";
    
    /** 按命名空间过滤的 metrics 路径模板。 */
    public static final String PROMETHEUS_CONTROLLER_NAMESPACE_PATH =
        "/prometheus/namespaceId/{namespaceId}";
    
    /** 按命名空间与服务名过滤的 metrics 路径模板。 */
    public static final String PROMETHEUS_CONTROLLER_SERVICE_PATH =
        "/prometheus/namespaceId/{namespaceId}/service/{service}";
    
}
