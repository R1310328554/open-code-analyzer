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
package org.apache.rocketmq.proxy;

/**
 * Proxy 命令行参数 POJO，由 Commons CLI 解析后填充。
 */
public class CommandLineArgument {
    /** NameServer 地址列表。 */
    private String namesrvAddr;
    /** 本地模式下 Broker 配置文件路径。 */
    private String brokerConfigPath;
    /** Proxy 配置文件路径。 */
    private String proxyConfigPath;
    /** 运行模式：LOCAL 或 CLUSTER。 */
    private String proxyMode;

    /** 获取 NameServer 地址。 */
    public String getNamesrvAddr() {
        return namesrvAddr;
    }

    /** 设置 NameServer 地址。 */
    public void setNamesrvAddr(String namesrvAddr) {
        this.namesrvAddr = namesrvAddr;
    }

    /** 获取 Broker 配置路径。 */
    public String getBrokerConfigPath() {
        return brokerConfigPath;
    }

    public void setBrokerConfigPath(String brokerConfigPath) {
        this.brokerConfigPath = brokerConfigPath;
    }

    /** 获取 Proxy 配置路径。 */
    public String getProxyConfigPath() {
        return proxyConfigPath;
    }

    public void setProxyConfigPath(String proxyConfigPath) {
        this.proxyConfigPath = proxyConfigPath;
    }

    /** 获取 Proxy 运行模式字符串。 */
    public String getProxyMode() {
        return proxyMode;
    }

    public void setProxyMode(String proxyMode) {
        this.proxyMode = proxyMode;
    }
}
