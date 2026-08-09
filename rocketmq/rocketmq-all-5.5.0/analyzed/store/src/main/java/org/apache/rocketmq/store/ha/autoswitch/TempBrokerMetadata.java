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

/**
 * 临时 Broker 元数据：注册阶段持久化集群名、Broker 名、ID 及校验码。
 */
public class TempBrokerMetadata extends BrokerMetadata {

    /** 注册校验码，用于主从自动切换时的身份验证。 */
    private String registerCheckCode;

    /** 仅指定文件路径的构造，其余字段为空。 */
    public TempBrokerMetadata(String filePath) {
        this(filePath, null, null, null, null);
    }

    /** 以完整字段初始化临时 Broker 元数据。 */
    public TempBrokerMetadata(String filePath, String clusterName, String brokerName, Long brokerId, String registerCheckCode) {
        super(filePath);
        super.clusterName = clusterName;
        super.brokerId = brokerId;
        super.brokerName = brokerName;
        this.registerCheckCode = registerCheckCode;
    }

    /** 更新内存字段并立即持久化到文件。 */
    public void updateAndPersist(String clusterName, String brokerName, Long brokerId, String registerCheckCode) throws Exception {
        super.clusterName = clusterName;
        super.brokerName = brokerName;
        super.brokerId = brokerId;
        this.registerCheckCode = registerCheckCode;
        writeToFile();
    }

    /** 以 # 分隔符拼接各字段编码为字符串。 */
    @Override
    public String encodeToStr() {
        StringBuilder sb = new StringBuilder();
        sb.append(clusterName).append("#");
        sb.append(brokerName).append("#");
        sb.append(brokerId).append("#");
        sb.append(registerCheckCode);
        return sb.toString();
    }

    /** 从 # 分隔的字符串解析各字段。 */
    @Override
    public void decodeFromStr(String dataStr) {
        if (dataStr == null) return;
        String[] dataArr = dataStr.split("#");
        this.clusterName = dataArr[0];
        this.brokerName = dataArr[1];
        this.brokerId = Long.valueOf(dataArr[2]);
        this.registerCheckCode = dataArr[3];
    }

    /** 判断基础字段与注册校验码均已加载。 */
    @Override
    public boolean isLoaded() {
        return super.isLoaded() && StringUtils.isNotEmpty(this.registerCheckCode);
    }

    /** 清空内存中的注册校验码及父类字段。 */
    @Override
    public void clearInMem() {
        super.clearInMem();
        this.registerCheckCode = null;
    }

    /** 返回 Broker ID。 */
    public Long getBrokerId() {
        return brokerId;
    }

    /** 返回注册校验码。 */
    public String getRegisterCheckCode() {
        return registerCheckCode;
    }

    /** 返回包含各字段的可读字符串。 */
    @Override
    public String toString() {
        return "TempBrokerMetadata{" +
                "registerCheckCode='" + registerCheckCode + '\'' +
                ", clusterName='" + clusterName + '\'' +
                ", brokerName='" + brokerName + '\'' +
                ", brokerId=" + brokerId +
                ", filePath='" + filePath + '\'' +
                '}';
    }
}
