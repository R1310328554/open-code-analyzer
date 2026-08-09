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

package org.apache.rocketmq.container;

import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.annotation.ImportantField;
import org.apache.rocketmq.common.utils.NetworkUtil;

/**
 * Broker 容器进程级配置：NameServer 地址拉取、容器 IP、配置黑名单等。
 */
public class BrokerContainerConfig {

    private String rocketmqHome = MixAll.ROCKETMQ_HOME_DIR;

    @ImportantField
    private String namesrvAddr = System.getProperty(MixAll.NAMESRV_ADDR_PROPERTY, System.getenv(MixAll.NAMESRV_ADDR_ENV));

    @ImportantField
    private boolean fetchNameSrvAddrByDnsLookup = false;

    @ImportantField
    private boolean fetchNamesrvAddrByAddressServer = false;

    @ImportantField
    private String brokerContainerIP = NetworkUtil.getLocalAddress();

    private String brokerConfigPaths = null;
    
    /**
     * 拉取 NameServer 地址的间隔，默认 10 秒。
     */
    private long fetchNamesrvAddrInterval = 10 * 1000;

    /**
     * 更新 NameServer 地址的间隔，默认 120 秒。
     */
    private long updateNamesrvAddrInterval = 60 * 2 * 1000;


    /**
     * 禁止通过命令行动态修改的配置项黑名单。
     * 修改黑名单本身或黑名单内配置需重启进程生效。
     */
    private String configBlackList = "configBlackList;brokerConfigPaths";

    public String getRocketmqHome() {
        return rocketmqHome;
    }

    public void setRocketmqHome(String rocketmqHome) {
        this.rocketmqHome = rocketmqHome;
    }

    public String getNamesrvAddr() {
        return namesrvAddr;
    }

    public void setNamesrvAddr(String namesrvAddr) {
        this.namesrvAddr = namesrvAddr;
    }

    public boolean isFetchNameSrvAddrByDnsLookup() {
        return fetchNameSrvAddrByDnsLookup;
    }

    public void setFetchNameSrvAddrByDnsLookup(boolean fetchNameSrvAddrByDnsLookup) {
        this.fetchNameSrvAddrByDnsLookup = fetchNameSrvAddrByDnsLookup;
    }

    public boolean isFetchNamesrvAddrByAddressServer() {
        return fetchNamesrvAddrByAddressServer;
    }

    public void setFetchNamesrvAddrByAddressServer(boolean fetchNamesrvAddrByAddressServer) {
        this.fetchNamesrvAddrByAddressServer = fetchNamesrvAddrByAddressServer;
    }

    public String getBrokerContainerIP() {
        return brokerContainerIP;
    }

    public String getBrokerConfigPaths() {
        return brokerConfigPaths;
    }

    public void setBrokerConfigPaths(String brokerConfigPaths) {
        this.brokerConfigPaths = brokerConfigPaths;
    }
    
    public long getFetchNamesrvAddrInterval() {
        return fetchNamesrvAddrInterval;
    }
    
    public void setFetchNamesrvAddrInterval(final long fetchNamesrvAddrInterval) {
        this.fetchNamesrvAddrInterval = fetchNamesrvAddrInterval;
    }

    public long getUpdateNamesrvAddrInterval() {
        return updateNamesrvAddrInterval;
    }

    public void setUpdateNamesrvAddrInterval(long updateNamesrvAddrInterval) {
        this.updateNamesrvAddrInterval = updateNamesrvAddrInterval;
    }

    public String getConfigBlackList() {
        return configBlackList;
    }

    public void setConfigBlackList(String configBlackList) {
        this.configBlackList = configBlackList;
    }
}
