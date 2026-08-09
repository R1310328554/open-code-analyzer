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
package org.apache.rocketmq.proxy.config;

/**
 * 指标采集模式：控制 Proxy 是否从客户端收集指标及采集方式。
 */
public enum MetricCollectorMode {
    /** 关闭客户端指标采集。 */
    OFF("off"),
    /** 从客户端采集指标并上报至指定地址。 */
    ON("on"),
    /** 由 Proxy 自身采集并聚合指标。 */
    PROXY("proxy");

    /** 配置字符串值（off/on/proxy）。 */
    private final String modeString;

    /** 构造枚举项并绑定配置字符串。 */
    MetricCollectorMode(String modeString) {
        this.modeString = modeString;
    }

    /** 返回模式对应的配置字符串。 */
    public String getModeString() {
        return modeString;
    }

    /** 按配置字符串解析枚举，无法识别时默认 {@link #OFF}。 */
    public static MetricCollectorMode getEnumByString(String modeString) {
        for (MetricCollectorMode mode : MetricCollectorMode.values()) {
            if (mode.modeString.equals(modeString.toLowerCase())) {
                return mode;
            }
        }
        return OFF;
    }
}
