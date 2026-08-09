/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.transport;

/**
 * 心跳发送 SPI：按 {@link #intervalMs()} 周期向 Sentinel Dashboard 上报机器存活信息。
 *
 * @author leyou
 * @author Eric Zhao
 */
public interface HeartbeatSender {

    /**
     * 向 Dashboard 发送一次心跳；核心模块按 {@link #intervalMs()} 间隔调用。
     *
     * @return whether heartbeat is successfully send.
     * @throws Exception if error occurs
     */
    boolean sendHeartbeat() throws Exception;

    /**
     * 默认心跳间隔（毫秒）；仅当配置项未指定 {@code csp.sentinel.heartbeat.interval.ms} 时生效。
     *
     * @return default interval of the sender in milliseconds
     */
    long intervalMs();
}
