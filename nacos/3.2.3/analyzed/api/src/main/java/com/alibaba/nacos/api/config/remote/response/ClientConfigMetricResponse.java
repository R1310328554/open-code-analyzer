/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.config.remote.response;

import com.alibaba.nacos.api.remote.response.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * 客户端配置指标响应。
 *
 * <p>服务端采集客户端配置模块的运行指标（如缓存大小、监听数等）后通过本响应返回。</p>
 *
 * @author liuzunfei
 * @version $Id: ClientConfigMetricResponse.java, v 0.1 2020年12月30日 2:59 PM liuzunfei Exp $
 */
public class ClientConfigMetricResponse extends Response {
    
    /** 指标键值对集合。 */
    private Map<String, Object> metrics = new HashMap<>();
    
    /** 获取全部指标数据。 */
    public Map<String, Object> getMetrics() {
        return metrics;
    }
    
    /** 设置指标数据映射。 */
    public void setMetrics(Map<String, Object> metrics) {
        this.metrics = metrics;
    }
    
    /**
     * 添加或更新单个指标项。
     *
     * @param key   指标名称
     * @param value 指标值
     */
    public void putMetric(String key, Object value) {
        metrics.put(key, value);
    }
    
}
