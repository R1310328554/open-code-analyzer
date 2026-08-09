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

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * BrokerConfig 进程级单例持有者：全局仅允许初始化一次。
 */
public class BrokerConfigSingleton {
    /** 是否已完成初始化。 */
    private static AtomicBoolean isInit = new AtomicBoolean();
    /** 全局 Broker 配置实例。 */
    private static BrokerConfig brokerConfig;

    /** 获取 Broker 配置，未初始化时抛 IllegalArgumentException。 */
    public static BrokerConfig getBrokerConfig() {
        if (brokerConfig == null) {
            throw new IllegalArgumentException("brokerConfig Cannot be null !");
        }
        return brokerConfig;
    }

    /** 设置 Broker 配置，重复初始化抛 IllegalArgumentException。 */
    public static void setBrokerConfig(BrokerConfig brokerConfig) {
        if (!isInit.compareAndSet(false, true)) {
            throw new IllegalArgumentException("broker config have inited !");
        }
        BrokerConfigSingleton.brokerConfig = brokerConfig;
    }
}
