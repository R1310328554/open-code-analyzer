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

package com.alibaba.nacos.plugin.control.connection;

import com.alibaba.nacos.common.executor.ExecutorFactory;
import com.alibaba.nacos.common.spi.NacosServiceLoader;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.plugin.control.Loggers;
import com.alibaba.nacos.plugin.control.connection.request.ConnectionCheckRequest;
import com.alibaba.nacos.plugin.control.connection.response.ConnectionCheckResponse;
import com.alibaba.nacos.plugin.control.connection.rule.ConnectionControlRule;
import com.alibaba.nacos.plugin.control.rule.parser.ConnectionControlRuleParser;
import com.alibaba.nacos.plugin.control.rule.parser.NacosConnectionControlRuleParser;
import com.alibaba.nacos.plugin.control.rule.storage.RuleStorageProxy;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 连接数管控管理器抽象基类。
 *
 * <p>启动时加载 SPI 连接指标采集器、解析并应用连接限制规则，
 * 并周期性上报各采集器的连接数汇总指标。</p>
 *
 * @author shiyiyu
 */
public abstract class ConnectionControlManager {
    
    /** 连接限制规则解析器。 */
    private final ConnectionControlRuleParser connectionControlRuleParser;
    
    /** 当前生效的连接限制规则。 */
    protected ConnectionControlRule connectionControlRule;
    
    /** SPI 加载的全部连接指标采集器。 */
    protected Collection<ConnectionMetricsCollector> metricsCollectorList;
    
    /** 连接指标定时上报线程池。 */
    private ScheduledExecutorService executorService;
    
    public ConnectionControlManager() {
        metricsCollectorList = NacosServiceLoader.load(ConnectionMetricsCollector.class);
        Loggers.CONTROL.info("Load connection metrics collector,size={},{}",
            metricsCollectorList.size(),
            metricsCollectorList);
        this.connectionControlRuleParser = buildConnectionControlRuleParser();
        initConnectionRule();
        if (!metricsCollectorList.isEmpty()) {
            initExecuteService();
            startConnectionMetricsReport();
        }
    }
    
    /**
     * 返回管理器实现名称。
     *
     * @return 管理器名称
     */
    public abstract String getName();
    
    /**
     * 构建连接规则解析器，子类可覆盖以使用自定义解析逻辑。
     *
     * @return 规则解析器实例
     */
    protected ConnectionControlRuleParser buildConnectionControlRuleParser() {
        return new NacosConnectionControlRuleParser();
    }
    
    /**
     * 获取连接规则解析器。
     *
     * @return 规则解析器
     */
    public ConnectionControlRuleParser getConnectionControlRuleParser() {
        return connectionControlRuleParser;
    }
    
    /** 初始化单线程定时调度器，用于周期性上报连接指标。 */
    private void initExecuteService() {
        executorService = ExecutorFactory.newSingleScheduledExecutorService(r -> {
            Thread thread = new Thread(r, "nacos.plugin.control.connection.reporter");
            thread.setDaemon(true);
            return thread;
        });
    }
    
    /** 从本地或外部存储加载连接限制规则并解析。 */
    private void initConnectionRule() {
        RuleStorageProxy ruleStorageProxy = RuleStorageProxy.getInstance();
        String localRuleContent = ruleStorageProxy.getLocalDiskStorage().getConnectionRule();
        if (StringUtils.isNotBlank(localRuleContent)) {
            Loggers.CONTROL.info("Found local disk connection rule content on start up,value  ={}",
                localRuleContent);
        } else if (ruleStorageProxy.getExternalStorage() != null
            && ruleStorageProxy.getExternalStorage().getConnectionRule() != null) {
            localRuleContent = ruleStorageProxy.getExternalStorage().getConnectionRule();
            if (StringUtils.isNotBlank(localRuleContent)) {
                Loggers.CONTROL.info(
                    "Found persist disk connection rule content on start up ,value  ={}",
                    localRuleContent);
            }
        }
        
        if (StringUtils.isNotBlank(localRuleContent)) {
            connectionControlRule = connectionControlRuleParser.parseRule(localRuleContent);
            Loggers.CONTROL.info("init connection rule end");
            
        } else {
            Loggers.CONTROL.info("No connection rule content found ,use default empty rule ");
            connectionControlRule = connectionControlRuleParser.parseRule("");
        }
    }
    
    /** 启动连接指标定时上报任务，初始延迟与间隔均为 3 秒。 */
    private void startConnectionMetricsReport() {
        executorService.scheduleWithFixedDelay(new ConnectionMetricsReporter(), 3000, 3000,
            TimeUnit.MILLISECONDS);
    }
    
    /**
     * 获取当前连接限制规则。
     *
     * @return 连接限制规则
     */
    public ConnectionControlRule getConnectionLimitRule() {
        return connectionControlRule;
    }
    
    /**
     * 应用新的连接限制规则。
     *
     * @param connectionControlRule 非空的连接限制规则
     */
    public abstract void applyConnectionLimitRule(ConnectionControlRule connectionControlRule);
    
    /**
     * 校验新连接是否允许建立。
     *
     * @param connectionCheckRequest 连接校验请求
     * @return 连接校验响应
     */
    public abstract ConnectionCheckResponse check(ConnectionCheckRequest connectionCheckRequest);
    
    /** 周期性汇总各采集器连接数并写入日志。 */
    class ConnectionMetricsReporter implements Runnable {
        
        @Override
        public void run() {
            Map<String, Integer> metricsTotalCount = metricsCollectorList.stream().collect(
                Collectors.toMap(ConnectionMetricsCollector::getName,
                    ConnectionMetricsCollector::getTotalCount));
            int totalCount = metricsTotalCount.values().stream().mapToInt(Integer::intValue).sum();
            
            Loggers.CONNECTION.info("ConnectionMetrics, totalCount = {}, detail = {}", totalCount,
                metricsTotalCount);
        }
    }
}
