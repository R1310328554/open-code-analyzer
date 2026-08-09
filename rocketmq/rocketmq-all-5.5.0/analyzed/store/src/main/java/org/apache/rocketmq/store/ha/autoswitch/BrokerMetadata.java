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

package org.apache.rocketmq.store.ha.autoswitch;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * Broker 元数据文件：持久化 clusterName、brokerName、brokerId。
 */
public class BrokerMetadata extends MetadataFile {

    /** 集群名称。 */
    protected String clusterName;

    /** Broker 名称。 */
    protected String brokerName;

    /** Broker ID（0 为主）。 */
    protected Long brokerId;

    /** 指定元数据文件路径。 */
    public BrokerMetadata(String filePath) {
        this.filePath = filePath;
    }

    /** 更新内存字段并写盘。 */
    public void updateAndPersist(String clusterName, String brokerName, Long brokerId) throws Exception {
        this.clusterName = clusterName;
        this.brokerName = brokerName;
        this.brokerId = brokerId;
        writeToFile();
    }

    /** 序列化为 cluster#broker#id 字符串。 */
    @Override
    public String encodeToStr() {
        StringBuilder sb = new StringBuilder();
        sb.append(clusterName).append("#");
        sb.append(brokerName).append("#");
        sb.append(brokerId);
        return sb.toString();
    }

    /** 从 # 分隔字符串反序列化。 */
    @Override
    public void decodeFromStr(String dataStr) {
        if (dataStr == null) return;
        String[] dataArr = dataStr.split("#");
        this.clusterName = dataArr[0];
        this.brokerName = dataArr[1];
        this.brokerId = Long.valueOf(dataArr[2]);
    }

    /** 三个字段均已加载则返回 true。 */
    @Override
    public boolean isLoaded() {
        return StringUtils.isNotEmpty(this.clusterName) && StringUtils.isNotEmpty(this.brokerName) && brokerId != null;
    }

    /** 清空内存中的元数据。 */
    @Override
    public void clearInMem() {
        this.clusterName = null;
        this.brokerName = null;
        this.brokerId = null;
    }

    /** 返回 brokerName。 */
    public String getBrokerName() {
        return brokerName;
    }

    /** 返回 brokerId。 */
    public Long getBrokerId() {
        return brokerId;
    }

    /** 返回 clusterName。 */
    public String getClusterName() {
        return clusterName;
    }

    /** 按 cluster、broker、id 比较相等。 */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BrokerMetadata that = (BrokerMetadata) o;
        return Objects.equals(clusterName, that.clusterName) && Objects.equals(brokerName, that.brokerName) && Objects.equals(brokerId, that.brokerId);
    }

    /** 计算哈希码。 */
    @Override
    public int hashCode() {
        return Objects.hash(clusterName, brokerName, brokerId);
    }

    /** 返回调试字符串。 */
    @Override
    public String toString() {
        return "BrokerMetadata{" +
                "clusterName='" + clusterName + '\'' +
                ", brokerName='" + brokerName + '\'' +
                ", brokerId=" + brokerId +
                ", filePath='" + filePath + '\'' +
                '}';
    }
}
