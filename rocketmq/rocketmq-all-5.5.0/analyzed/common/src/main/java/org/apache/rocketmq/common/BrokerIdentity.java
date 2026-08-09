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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.rocketmq.common.annotation.ImportantField;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Broker 身份标识：集群名、Broker 名、BrokerId 及容器模式标志。
 */
public class BrokerIdentity {
    /** 默认集群名称。 */
    private static final String DEFAULT_CLUSTER_NAME = "DefaultCluster";

    /** 公共模块日志器。 */
    protected static final Logger LOGGER = LoggerFactory.getLogger(LoggerName.COMMON_LOGGER_NAME);

    /** 本机主机名，静态块中初始化。 */
    private static String localHostName;

    static {
        try {
            localHostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            LOGGER.error("Failed to obtain the host name", e);
        }
    }

    /** Broker 容器模式的预置身份（在 localHostName 初始化后加载）。 */
    public static final BrokerIdentity BROKER_CONTAINER_IDENTITY = new BrokerIdentity(true);

    /** Broker 名称（重要字段）。 */
    @ImportantField
    private String brokerName = defaultBrokerName();
    /** 所属集群名称（重要字段）。 */
    @ImportantField
    private String brokerClusterName = DEFAULT_CLUSTER_NAME;
    /** Broker 角色 ID，默认 Master（重要字段）。 */
    @ImportantField
    private volatile long brokerId = MixAll.MASTER_ID;

    /** 是否为 Broker 容器实体本身。 */
    private boolean isBrokerContainer = false;

    /** 是否运行在 BrokerContainer 内（由启动方式决定，勿手动设置）。 */
    private boolean isInBrokerContainer = false;

    /** 默认构造，使用主机名等默认值。 */
    public BrokerIdentity() {
    }

    /** 指定是否为 Broker 容器身份。 */
    public BrokerIdentity(boolean isBrokerContainer) {
        this.isBrokerContainer = isBrokerContainer;
    }

    /** 按集群、名称与 ID 构造身份。 */
    public BrokerIdentity(String brokerClusterName, String brokerName, long brokerId) {
        this.brokerName = brokerName;
        this.brokerClusterName = brokerClusterName;
        this.brokerId = brokerId;
    }

    /** 完整构造，含容器内运行标志。 */
    public BrokerIdentity(String brokerClusterName, String brokerName, long brokerId, boolean isInBrokerContainer) {
        this.brokerName = brokerName;
        this.brokerClusterName = brokerClusterName;
        this.brokerId = brokerId;
        this.isInBrokerContainer = isInBrokerContainer;
    }

    /** 获取 Broker 名称。 */
    public String getBrokerName() {
        return brokerName;
    }

    /** 设置 Broker 名称。 */
    public void setBrokerName(final String brokerName) {
        this.brokerName = brokerName;
    }

    /** 获取集群名称。 */
    public String getBrokerClusterName() {
        return brokerClusterName;
    }

    /** 设置集群名称。 */
    public void setBrokerClusterName(final String brokerClusterName) {
        this.brokerClusterName = brokerClusterName;
    }

    /** 获取 Broker ID。 */
    public long getBrokerId() {
        return brokerId;
    }

    /** 设置 Broker ID。 */
    public void setBrokerId(final long brokerId) {
        this.brokerId = brokerId;
    }

    /** 是否在 BrokerContainer 内运行。 */
    public boolean isInBrokerContainer() {
        return isInBrokerContainer;
    }

    /** 设置是否在 BrokerContainer 内运行。 */
    public void setInBrokerContainer(boolean inBrokerContainer) {
        isInBrokerContainer = inBrokerContainer;
    }

    /** 默认 Broker 名：主机名或 DEFAULT_BROKER。 */
    private String defaultBrokerName() {
        return StringUtils.isEmpty(localHostName) ? "DEFAULT_BROKER" : localHostName;
    }

    /** 规范名称：容器为 BrokerContainer，否则 cluster_name_id。 */
    public String getCanonicalName() {
        return isBrokerContainer ? "BrokerContainer" : String.format("%s_%s_%d", brokerClusterName, brokerName,
            brokerId);
    }

    /** 带 # 分隔符的标识字符串。 */
    public String getIdentifier() {
        return "#" + getCanonicalName() + "#";
    }

    /** 按 cluster、name、id 比较相等性。 */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final BrokerIdentity identity = (BrokerIdentity) o;

        return new EqualsBuilder()
            .append(brokerId, identity.brokerId)
            .append(brokerName, identity.brokerName)
            .append(brokerClusterName, identity.brokerClusterName)
            .isEquals();
    }

    /** 基于 name、cluster、id 的哈希码。 */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(brokerName)
            .append(brokerClusterName)
            .append(brokerId)
            .toHashCode();
    }
}
