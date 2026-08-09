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

package org.apache.rocketmq.tools.monitor;

import org.apache.rocketmq.common.MixAll;

/**
 * 监控服务运行配置。
 * <p>默认从系统属性或环境变量读取 NameServer 地址，轮询间隔默认 60 秒。
 */
public class MonitorConfig {
    /** NameServer 地址，优先 JVM 属性 {@link MixAll#NAMESRV_ADDR_PROPERTY}。 */
    private String namesrvAddr = System.getProperty(MixAll.NAMESRV_ADDR_PROPERTY,
        System.getenv(MixAll.NAMESRV_ADDR_ENV));

    /** 每轮监控任务间隔（毫秒），默认 60 秒。 */
    private int roundInterval = 1000 * 60;

    /** @return NameServer 地址 */
    public String getNamesrvAddr() {
        return namesrvAddr;
    }

    /** @param namesrvAddr NameServer 地址 */
    public void setNamesrvAddr(String namesrvAddr) {
        this.namesrvAddr = namesrvAddr;
    }

    /** @return 监控轮询间隔（毫秒） */
    public int getRoundInterval() {
        return roundInterval;
    }

    /** @param roundInterval 监控轮询间隔（毫秒） */
    public void setRoundInterval(int roundInterval) {
        this.roundInterval = roundInterval;
    }
}
