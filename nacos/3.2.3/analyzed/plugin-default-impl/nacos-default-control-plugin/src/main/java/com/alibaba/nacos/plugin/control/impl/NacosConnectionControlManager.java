/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.control.impl;

import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.plugin.control.Loggers;
import com.alibaba.nacos.plugin.control.connection.ConnectionControlManager;
import com.alibaba.nacos.plugin.control.connection.ConnectionMetricsCollector;
import com.alibaba.nacos.plugin.control.connection.request.ConnectionCheckRequest;
import com.alibaba.nacos.plugin.control.connection.response.ConnectionCheckCode;
import com.alibaba.nacos.plugin.control.connection.response.ConnectionCheckResponse;
import com.alibaba.nacos.plugin.control.connection.rule.ConnectionControlRule;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Nacos 默认连接数管控管理器。
 *
 * <p>继承 {@link ConnectionControlManager}，按规则汇总各采集器的连接总数； {@code countLimit < 0} 表示不限制。</p>
 *
 * @author shiyiyue
 */
public class NacosConnectionControlManager extends ConnectionControlManager {
    
    /** 返回插件标识 {@code nacos}。 */
    @Override
    public String getName() {
        return "nacos";
    }
    
    /** 构造默认连接管控管理器。 */
    public NacosConnectionControlManager() {
        super();
    }
    
    /** 更新连接数上限规则并记录日志（当前实现仅记录警告）。 */
    @Override
    public void applyConnectionLimitRule(ConnectionControlRule connectionControlRule) {
        super.connectionControlRule = connectionControlRule;
        Loggers.CONTROL.info(
            "Connection control rule updated to ->" + (this.connectionControlRule == null ? null
                : JacksonUtils.toJson(this.connectionControlRule)));
        Loggers.CONTROL.warn(
            "Connection control updated, But connection control manager is no limit implementation.");
    }
    
    /** 校验当前总连接数是否超过规则上限。 */
    @Override
    public ConnectionCheckResponse check(ConnectionCheckRequest connectionCheckRequest) {
        ConnectionCheckResponse connectionCheckResponse = new ConnectionCheckResponse();
        connectionCheckResponse.setSuccess(true);
        connectionCheckResponse.setCode(ConnectionCheckCode.PASS_BY_TOTAL);
        int totalCountLimit = connectionControlRule.getCountLimit();
        // 上限小于 0 时不做连接数限制
        if (totalCountLimit < 0) {
            return connectionCheckResponse;
        }
        
        // 汇总各指标采集器的连接总数
        Map<String, Integer> metricsTotalCount = metricsCollectorList.stream().collect(
            Collectors.toMap(ConnectionMetricsCollector::getName,
                ConnectionMetricsCollector::getTotalCount));
        int totalCount = metricsTotalCount.values().stream().mapToInt(Integer::intValue).sum();
        if (totalCount >= totalCountLimit) {
            connectionCheckResponse.setSuccess(false);
            connectionCheckResponse.setCode(ConnectionCheckCode.DENY_BY_TOTAL_OVER);
        }
        return connectionCheckResponse;
    }
    
}
