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

package org.apache.rocketmq.broker;

import java.io.File;

/**
 * Broker 持久化配置文件路径辅助类：topic、消费位点、订阅组等 JSON 路径。
 */
public class BrokerPathConfigHelper {
    /** Broker 主配置文件路径，可通过 {@link #setBrokerConfigPath} 覆盖。 */
    private static String brokerConfigPath = System.getProperty("user.home") + File.separator + "store"
        + File.separator + "config" + File.separator + "broker.properties";

    public static String getBrokerConfigPath() {
        return brokerConfigPath;
    }

    public static void setBrokerConfigPath(String path) {
        brokerConfigPath = path;
    }

    /** 返回 topic 配置 JSON 路径。 */
    public static String getTopicConfigPath(final String rootDir) {
        return getConfigDir(rootDir) + "topics.json";
    }

    public static String getTopicQueueMappingPath(final String rootDir) {
        return getConfigDir(rootDir) + "topicQueueMapping.json";
    }

    /** 返回消费位点持久化 JSON 路径。 */
    public static String getConsumerOffsetPath(final String rootDir) {
        return getConfigDir(rootDir) + "consumerOffset.json";
    }

    public static String getLmqConsumerOffsetPath(final String rootDir) {
        return getConfigDir(rootDir) + "lmqConsumerOffset.json";
    }

    public static String getConsumerOrderInfoPath(final String rootDir) {
        return getConfigDir(rootDir) + "consumerOrderInfo.json";
    }

    /** 返回订阅组配置 JSON 路径。 */
    public static String getSubscriptionGroupPath(final String rootDir) {
        return getConfigDir(rootDir) + "subscriptionGroup.json";
    }
    public static String getTimerCheckPath(final String rootDir) {
        return getConfigDir(rootDir) + "timercheck";
    }
    public static String getTimerMetricsPath(final String rootDir) {
        return getConfigDir(rootDir) + "timermetrics";
    }
    public static String getTransactionMetricsPath(final String rootDir) {
        return getConfigDir(rootDir) + "transactionMetrics";
    }

    public static String getConsumerFilterPath(final String rootDir) {
        return getConfigDir(rootDir) + "consumerFilter.json";
    }

    public static String getMessageRequestModePath(final String rootDir) {
        return getConfigDir(rootDir) + "messageRequestMode.json";
    }

    /** 返回 rootDir 下 config 子目录路径（含尾部分隔符）。 */
    private static String getConfigDir(final String rootDir) {
        return rootDir + File.separator + "config" + File.separator;
    }
}
