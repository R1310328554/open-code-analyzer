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
package org.apache.rocketmq.common;

import java.io.File;
import java.util.Arrays;
import org.apache.rocketmq.common.metrics.MetricsExporterType;

/**
 * Controller 节点配置：DLedger/jRaft 类型、选举、线程池、指标导出等。
 */
public class ControllerConfig {
    /** RocketMQ 安装根目录。 */
    private String rocketmqHome = MixAll.ROCKETMQ_HOME_DIR;
    /** 配置文件存储路径。 */
    private String configStorePath = System.getProperty("user.home") + File.separator + "controller" + File.separator + "controller.properties";
    /** DLedger 型 Controller 类型标识。 */
    public static final String DLEDGER_CONTROLLER = "DLedger";
    /** jRaft 型 Controller 类型标识。 */
    public static final String JRAFT_CONTROLLER = "jRaft";

    /** jRaft 相关子配置。 */
    private JraftConfig jraftConfig = new JraftConfig();

    /** Controller 实现类型，默认 DLedger。 */
    private String controllerType = DLEDGER_CONTROLLER;
    /** 周期性扫描非活跃 Broker 的间隔（毫秒）。 */
    private long scanNotActiveBrokerInterval = 5 * 1000;

    /** 处理 Broker/操作请求（如 REGISTER_BROKER）的线程数。 */
    private int controllerThreadPoolNums = 16;

    /** 客户端请求队列容量。 */
    private int controllerRequestThreadPoolQueueCapacity = 50000;

    /** DLedger Controller 组名。 */
    private String controllerDLegerGroup;
    /** DLedger 对等节点列表。 */
    private String controllerDLegerPeers;
    /** 本节点 DLedger SelfId。 */
    private String controllerDLegerSelfId;
    /** 映射文件大小（字节），默认 1GB。 */
    private int mappedFileSize = 1024 * 1024 * 1024;
    /** Controller 数据存储路径。 */
    private String controllerStorePath = "";

    /** 选举 Master 失败时的最大重试次数。 */
    private int electMasterMaxRetryCount = 3;


    /** 是否允许选举不在 syncStateSet 中的 Master（非干净选举）。 */
    private boolean enableElectUncleanMaster = false;

    /** 是否处理读事件。 */
    private boolean isProcessReadEvent = false;

    /** Broker 角色变更时是否通知 Broker。 */
    private volatile boolean notifyBrokerRoleChanged = true;
    /** 每个 Broker-Set 内扫描非活跃 Master 的间隔（毫秒）。 */
    private long scanInactiveMasterInterval = 5 * 1000;

    /** 指标导出器类型。 */
    private MetricsExporterType metricsExporterType = MetricsExporterType.DISABLE;

    /** gRPC 指标导出目标地址。 */
    private String metricsGrpcExporterTarget = "";
    /** gRPC 指标导出请求头。 */
    private String metricsGrpcExporterHeader = "";
    /** gRPC 指标导出超时（毫秒）。 */
    private long metricGrpcExporterTimeOutInMills = 3 * 1000;
    /** gRPC 指标导出间隔（毫秒）。 */
    private long metricGrpcExporterIntervalInMills = 60 * 1000;
    /** 日志指标导出间隔（毫秒）。 */
    private long metricLoggingExporterIntervalInMills = 10 * 1000;

    /** Prometheus 指标导出端口。 */
    private int metricsPromExporterPort = 5557;
    /** Prometheus 指标导出监听地址。 */
    private String metricsPromExporterHost = "";

    // 指标标签 CSV，格式 Key:Value，如 instance_id:xxx,uid:xxx
    /** 指标标签 CSV 字符串。 */
    private String metricsLabel = "";

    /** 是否以增量方式导出指标。 */
    private boolean metricsInDelta = false;

    /** 命令行不可热更新的配置黑名单，需重启进程修改。 */
    private String configBlackList = "configBlackList;configStorePath";

    /** 获取配置黑名单。 */
    public String getConfigBlackList() {
        return configBlackList;
    }

    /** 设置配置黑名单。 */
    public void setConfigBlackList(String configBlackList) {
        this.configBlackList = configBlackList;
    }

    /** 获取 RocketMQ 安装目录。 */
    public String getRocketmqHome() {
        return rocketmqHome;
    }

    /** 设置 RocketMQ 安装目录。 */
    public void setRocketmqHome(String rocketmqHome) {
        this.rocketmqHome = rocketmqHome;
    }

    /** 获取配置文件路径。 */
    public String getConfigStorePath() {
        return configStorePath;
    }

    /** 设置配置文件路径。 */
    public void setConfigStorePath(String configStorePath) {
        this.configStorePath = configStorePath;
    }

    /** 获取扫描非活跃 Broker 间隔。 */
    public long getScanNotActiveBrokerInterval() {
        return scanNotActiveBrokerInterval;
    }

    /** 设置扫描非活跃 Broker 间隔。 */
    public void setScanNotActiveBrokerInterval(long scanNotActiveBrokerInterval) {
        this.scanNotActiveBrokerInterval = scanNotActiveBrokerInterval;
    }

    /** 获取 Controller 线程池大小。 */
    public int getControllerThreadPoolNums() {
        return controllerThreadPoolNums;
    }

    /** 设置 Controller 线程池大小。 */
    public void setControllerThreadPoolNums(int controllerThreadPoolNums) {
        this.controllerThreadPoolNums = controllerThreadPoolNums;
    }

    /** 获取请求队列容量。 */
    public int getControllerRequestThreadPoolQueueCapacity() {
        return controllerRequestThreadPoolQueueCapacity;
    }

    /** 设置请求队列容量。 */
    public void setControllerRequestThreadPoolQueueCapacity(int controllerRequestThreadPoolQueueCapacity) {
        this.controllerRequestThreadPoolQueueCapacity = controllerRequestThreadPoolQueueCapacity;
    }

    /** 获取 DLedger 组名。 */
    public String getControllerDLegerGroup() {
        return controllerDLegerGroup;
    }

    /** 设置 DLedger 组名。 */
    public void setControllerDLegerGroup(String controllerDLegerGroup) {
        this.controllerDLegerGroup = controllerDLegerGroup;
    }

    /** 获取 DLedger 对等节点。 */
    public String getControllerDLegerPeers() {
        return controllerDLegerPeers;
    }

    /** 设置 DLedger 对等节点。 */
    public void setControllerDLegerPeers(String controllerDLegerPeers) {
        this.controllerDLegerPeers = controllerDLegerPeers;
    }

    /** 获取本节点 DLedger SelfId。 */
    public String getControllerDLegerSelfId() {
        return controllerDLegerSelfId;
    }

    /** 设置本节点 DLedger SelfId。 */
    public void setControllerDLegerSelfId(String controllerDLegerSelfId) {
        this.controllerDLegerSelfId = controllerDLegerSelfId;
    }

    /** 获取映射文件大小。 */
    public int getMappedFileSize() {
        return mappedFileSize;
    }

    /** 设置映射文件大小。 */
    public void setMappedFileSize(int mappedFileSize) {
        this.mappedFileSize = mappedFileSize;
    }

    /** 获取存储路径，空时按 controllerType 生成默认路径。 */
    public String getControllerStorePath() {
        if (controllerStorePath.isEmpty()) {
            controllerStorePath = System.getProperty("user.home") + File.separator + controllerType + "Controller";
        }
        return controllerStorePath;
    }

    /** 设置 Controller 存储路径。 */
    public void setControllerStorePath(String controllerStorePath) {
        this.controllerStorePath = controllerStorePath;
    }

    /** 是否允许非干净 Master 选举。 */
    public boolean isEnableElectUncleanMaster() {
        return enableElectUncleanMaster;
    }

    /** 设置是否允许非干净 Master 选举。 */
    public void setEnableElectUncleanMaster(boolean enableElectUncleanMaster) {
        this.enableElectUncleanMaster = enableElectUncleanMaster;
    }

    /** 是否处理读事件。 */
    public boolean isProcessReadEvent() {
        return isProcessReadEvent;
    }

    /** 设置是否处理读事件。 */
    public void setProcessReadEvent(boolean processReadEvent) {
        isProcessReadEvent = processReadEvent;
    }

    /** 角色变更时是否通知 Broker。 */
    public boolean isNotifyBrokerRoleChanged() {
        return notifyBrokerRoleChanged;
    }

    /** 设置角色变更通知开关。 */
    public void setNotifyBrokerRoleChanged(boolean notifyBrokerRoleChanged) {
        this.notifyBrokerRoleChanged = notifyBrokerRoleChanged;
    }

    /** 获取扫描非活跃 Master 间隔。 */
    public long getScanInactiveMasterInterval() {
        return scanInactiveMasterInterval;
    }

    /** 设置扫描非活跃 Master 间隔。 */
    public void setScanInactiveMasterInterval(long scanInactiveMasterInterval) {
        this.scanInactiveMasterInterval = scanInactiveMasterInterval;
    }

    /** 从 peers 解析本节点 DLedger 地址。 */
    public String getDLedgerAddress() {
        return Arrays.stream(this.controllerDLegerPeers.split(";"))
            .filter(x -> this.controllerDLegerSelfId.equals(x.split("-")[0]))
            .map(x -> x.split("-")[1]).findFirst().get();
    }

    /** 获取指标导出器类型。 */
    public MetricsExporterType getMetricsExporterType() {
        return metricsExporterType;
    }

    /** 设置指标导出器类型。 */
    public void setMetricsExporterType(MetricsExporterType metricsExporterType) {
        this.metricsExporterType = metricsExporterType;
    }

    /** 按整型设置指标导出器类型。 */
    public void setMetricsExporterType(int metricsExporterType) {
        this.metricsExporterType = MetricsExporterType.valueOf(metricsExporterType);
    }

    /** 按字符串设置指标导出器类型。 */
    public void setMetricsExporterType(String metricsExporterType) {
        this.metricsExporterType = MetricsExporterType.valueOf(metricsExporterType);
    }

    /** 获取 gRPC 导出目标。 */
    public String getMetricsGrpcExporterTarget() {
        return metricsGrpcExporterTarget;
    }

    /** 设置 gRPC 导出目标。 */
    public void setMetricsGrpcExporterTarget(String metricsGrpcExporterTarget) {
        this.metricsGrpcExporterTarget = metricsGrpcExporterTarget;
    }

    /** 获取 gRPC 导出请求头。 */
    public String getMetricsGrpcExporterHeader() {
        return metricsGrpcExporterHeader;
    }

    /** 设置 gRPC 导出请求头。 */
    public void setMetricsGrpcExporterHeader(String metricsGrpcExporterHeader) {
        this.metricsGrpcExporterHeader = metricsGrpcExporterHeader;
    }

    /** 获取 gRPC 导出超时。 */
    public long getMetricGrpcExporterTimeOutInMills() {
        return metricGrpcExporterTimeOutInMills;
    }

    /** 设置 gRPC 导出超时。 */
    public void setMetricGrpcExporterTimeOutInMills(long metricGrpcExporterTimeOutInMills) {
        this.metricGrpcExporterTimeOutInMills = metricGrpcExporterTimeOutInMills;
    }

    /** 获取 gRPC 导出间隔。 */
    public long getMetricGrpcExporterIntervalInMills() {
        return metricGrpcExporterIntervalInMills;
    }

    /** 设置 gRPC 导出间隔。 */
    public void setMetricGrpcExporterIntervalInMills(long metricGrpcExporterIntervalInMills) {
        this.metricGrpcExporterIntervalInMills = metricGrpcExporterIntervalInMills;
    }

    /** 获取日志导出间隔。 */
    public long getMetricLoggingExporterIntervalInMills() {
        return metricLoggingExporterIntervalInMills;
    }

    /** 设置日志导出间隔。 */
    public void setMetricLoggingExporterIntervalInMills(long metricLoggingExporterIntervalInMills) {
        this.metricLoggingExporterIntervalInMills = metricLoggingExporterIntervalInMills;
    }

    /** 获取 Prometheus 导出端口。 */
    public int getMetricsPromExporterPort() {
        return metricsPromExporterPort;
    }

    /** 设置 Prometheus 导出端口。 */
    public void setMetricsPromExporterPort(int metricsPromExporterPort) {
        this.metricsPromExporterPort = metricsPromExporterPort;
    }

    /** 获取 Prometheus 导出主机。 */
    public String getMetricsPromExporterHost() {
        return metricsPromExporterHost;
    }

    /** 设置 Prometheus 导出主机。 */
    public void setMetricsPromExporterHost(String metricsPromExporterHost) {
        this.metricsPromExporterHost = metricsPromExporterHost;
    }

    /** 获取指标标签 CSV。 */
    public String getMetricsLabel() {
        return metricsLabel;
    }

    /** 设置指标标签 CSV。 */
    public void setMetricsLabel(String metricsLabel) {
        this.metricsLabel = metricsLabel;
    }

    /** 是否增量导出指标。 */
    public boolean isMetricsInDelta() {
        return metricsInDelta;
    }

    /** 设置是否增量导出指标。 */
    public void setMetricsInDelta(boolean metricsInDelta) {
        this.metricsInDelta = metricsInDelta;
    }

    /** 获取 Controller 类型。 */
    public String getControllerType() {
        return controllerType;
    }

    /** 设置 Controller 类型。 */
    public void setControllerType(String controllerType) {
        this.controllerType = controllerType;
    }

    /** 获取 jRaft 子配置。 */
    public JraftConfig getJraftConfig() {
        return jraftConfig;
    }

    /** 设置 jRaft 子配置。 */
    public void setJraftConfig(JraftConfig jraftConfig) {
        this.jraftConfig = jraftConfig;
    }

    /** 获取 Master 选举最大重试次数。 */
    public int getElectMasterMaxRetryCount() {
        return this.electMasterMaxRetryCount;
    }

    /** 设置 Master 选举最大重试次数。 */
    public void setElectMasterMaxRetryCount(int electMasterMaxRetryCount) {
        this.electMasterMaxRetryCount = electMasterMaxRetryCount;
    }
}
