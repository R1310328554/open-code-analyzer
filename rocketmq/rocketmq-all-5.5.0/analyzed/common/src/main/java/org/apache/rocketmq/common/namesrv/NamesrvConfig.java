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

/**
 * $Id: NamesrvConfig.java 1839 2013-05-16 02:12:02Z vintagewang@apache.org $
 */
package org.apache.rocketmq.common.namesrv;

import java.io.File;
import org.apache.rocketmq.common.MixAll;

/**
 * NameServer 进程配置项：线程池、路径、Topic 策略及 Controller 集成等。
 */
public class NamesrvConfig {

    /** RocketMQ 安装根目录。 */
    private String rocketmqHome = MixAll.ROCKETMQ_HOME_DIR;
    /** KV 配置持久化文件路径。 */
    private String kvConfigPath = System.getProperty("user.home") + File.separator + "namesrv" + File.separator + "kvConfig.json";
    /** NameServer 主配置文件存储路径。 */
    private String configStorePath = System.getProperty("user.home") + File.separator + "namesrv" + File.separator + "namesrv.properties";
    /** 生产环境标识名。 */
    private String productEnvName = "center";
    /** 是否为集群测试模式。 */
    private boolean clusterTest = false;
    /** 是否启用顺序消息相关能力。 */
    private boolean orderMessageEnable = false;
    /** 是否向 Broker 返回顺序 Topic 配置。 */
    private boolean returnOrderTopicConfigToBroker = true;

    /** 处理客户端请求（如 GET_ROUTEINTO_BY_TOPIC）的线程数。 */
    private int clientRequestThreadPoolNums = 8;
    /** 处理 Broker/运维请求（如 REGISTER_BROKER）的线程数。 */
    private int defaultThreadPoolNums = 16;
    /** 客户端请求队列容量。 */
    private int clientRequestThreadPoolQueueCapacity = 50000;
    /** Broker/运维请求队列容量。 */
    private int defaultThreadPoolQueueCapacity = 10000;
    /** 扫描非活跃 Broker 的周期间隔（毫秒）。 */
    private long scanNotActiveBrokerInterval = 5 * 1000;

    /** 待注销 Broker 请求队列容量。 */
    private int unRegisterBrokerQueueCapacity = 3000;

    /**
     * 是否支持 Acting Master：主节点宕机时从节点可临时承担主职责，支持：
     * 1. 消息队列 lock/unlock；
     * 2. searchOffset、maxOffset/minOffset 查询；
     * 3. 最早消息存储时间查询。
     */
    private boolean supportActingMaster = false;

    /** 是否启用全量 Topic 列表接口。 */
    private volatile boolean enableAllTopicList = true;


    /** 是否启用 Topic 列表相关能力。 */
    private volatile boolean enableTopicList = true;

    /** 最小 BrokerId 变更时是否通知客户端。 */
    private volatile boolean notifyMinBrokerIdChanged = false;

    /** 是否在本 NameServer 进程中启动 Controller。 */
    private boolean enableControllerInNamesrv = false;

    /** 启动时是否等待依赖服务就绪。 */
    private volatile boolean needWaitForService = false;

    /** 等待依赖服务的超时秒数。 */
    private int waitSecondsForService = 45;

    /**
     * 启用后，Broker 注册载荷中不存在的 Topic 将从 NameServer 路由中删除。
     *
     * 注意：
     * 1. 需与 Broker 的 enableSingleTopicRegister 同时启用，避免意外丢失路由；
     * 2. 暂不支持静态 Topic。
     */
    private boolean deleteTopicWithBrokerRegistration = false;
    /** 配置黑名单：名单内项不允许通过命令热更新，需重启进程修改。 */
    private String configBlackList = "configBlackList;configStorePath;kvConfigPath";

    public String getConfigBlackList() {
        return configBlackList;
    }

    public void setConfigBlackList(String configBlackList) {
        this.configBlackList = configBlackList;
    }

    public boolean isOrderMessageEnable() {
        return orderMessageEnable;
    }

    public void setOrderMessageEnable(boolean orderMessageEnable) {
        this.orderMessageEnable = orderMessageEnable;
    }

    public String getRocketmqHome() {
        return rocketmqHome;
    }

    public void setRocketmqHome(String rocketmqHome) {
        this.rocketmqHome = rocketmqHome;
    }

    public String getKvConfigPath() {
        return kvConfigPath;
    }

    public void setKvConfigPath(String kvConfigPath) {
        this.kvConfigPath = kvConfigPath;
    }

    public String getProductEnvName() {
        return productEnvName;
    }

    public void setProductEnvName(String productEnvName) {
        this.productEnvName = productEnvName;
    }

    public boolean isClusterTest() {
        return clusterTest;
    }

    public void setClusterTest(boolean clusterTest) {
        this.clusterTest = clusterTest;
    }

    public String getConfigStorePath() {
        return configStorePath;
    }

    public void setConfigStorePath(final String configStorePath) {
        this.configStorePath = configStorePath;
    }

    public boolean isReturnOrderTopicConfigToBroker() {
        return returnOrderTopicConfigToBroker;
    }

    public void setReturnOrderTopicConfigToBroker(boolean returnOrderTopicConfigToBroker) {
        this.returnOrderTopicConfigToBroker = returnOrderTopicConfigToBroker;
    }

    public int getClientRequestThreadPoolNums() {
        return clientRequestThreadPoolNums;
    }

    public void setClientRequestThreadPoolNums(final int clientRequestThreadPoolNums) {
        this.clientRequestThreadPoolNums = clientRequestThreadPoolNums;
    }

    public int getDefaultThreadPoolNums() {
        return defaultThreadPoolNums;
    }

    public void setDefaultThreadPoolNums(final int defaultThreadPoolNums) {
        this.defaultThreadPoolNums = defaultThreadPoolNums;
    }

    public int getClientRequestThreadPoolQueueCapacity() {
        return clientRequestThreadPoolQueueCapacity;
    }

    public void setClientRequestThreadPoolQueueCapacity(final int clientRequestThreadPoolQueueCapacity) {
        this.clientRequestThreadPoolQueueCapacity = clientRequestThreadPoolQueueCapacity;
    }

    public int getDefaultThreadPoolQueueCapacity() {
        return defaultThreadPoolQueueCapacity;
    }

    public void setDefaultThreadPoolQueueCapacity(final int defaultThreadPoolQueueCapacity) {
        this.defaultThreadPoolQueueCapacity = defaultThreadPoolQueueCapacity;
    }

    public long getScanNotActiveBrokerInterval() {
        return scanNotActiveBrokerInterval;
    }

    public void setScanNotActiveBrokerInterval(long scanNotActiveBrokerInterval) {
        this.scanNotActiveBrokerInterval = scanNotActiveBrokerInterval;
    }

    public int getUnRegisterBrokerQueueCapacity() {
        return unRegisterBrokerQueueCapacity;
    }

    public void setUnRegisterBrokerQueueCapacity(final int unRegisterBrokerQueueCapacity) {
        this.unRegisterBrokerQueueCapacity = unRegisterBrokerQueueCapacity;
    }

    public boolean isSupportActingMaster() {
        return supportActingMaster;
    }

    public void setSupportActingMaster(final boolean supportActingMaster) {
        this.supportActingMaster = supportActingMaster;
    }

    public boolean isEnableAllTopicList() {
        return enableAllTopicList;
    }

    public void setEnableAllTopicList(boolean enableAllTopicList) {
        this.enableAllTopicList = enableAllTopicList;
    }

    public boolean isEnableTopicList() {
        return enableTopicList;
    }

    public void setEnableTopicList(boolean enableTopicList) {
        this.enableTopicList = enableTopicList;
    }

    public boolean isNotifyMinBrokerIdChanged() {
        return notifyMinBrokerIdChanged;
    }

    public void setNotifyMinBrokerIdChanged(boolean notifyMinBrokerIdChanged) {
        this.notifyMinBrokerIdChanged = notifyMinBrokerIdChanged;
    }

    public boolean isEnableControllerInNamesrv() {
        return enableControllerInNamesrv;
    }

    public void setEnableControllerInNamesrv(boolean enableControllerInNamesrv) {
        this.enableControllerInNamesrv = enableControllerInNamesrv;
    }

    public boolean isNeedWaitForService() {
        return needWaitForService;
    }

    public void setNeedWaitForService(boolean needWaitForService) {
        this.needWaitForService = needWaitForService;
    }

    public int getWaitSecondsForService() {
        return waitSecondsForService;
    }

    public void setWaitSecondsForService(int waitSecondsForService) {
        this.waitSecondsForService = waitSecondsForService;
    }

    public boolean isDeleteTopicWithBrokerRegistration() {
        return deleteTopicWithBrokerRegistration;
    }

    public void setDeleteTopicWithBrokerRegistration(boolean deleteTopicWithBrokerRegistration) {
        this.deleteTopicWithBrokerRegistration = deleteTopicWithBrokerRegistration;
    }
}
