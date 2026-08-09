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

package org.apache.rocketmq.store.ha;

import java.util.concurrent.CompletableFuture;

/**
 * HA 连接状态通知请求：等待指定远程地址达到期望状态。
 */
public class HAConnectionStateNotificationRequest {
    /** 异步完成 Future，true 表示达到期望状态。 */
    private final CompletableFuture<Boolean> requestFuture = new CompletableFuture<>();
    /** 期望达到的连接状态。 */
    private final HAConnectionState expectState;
    /** 目标远程地址（IP）。 */
    private final String remoteAddr;
    /** 连接关闭时是否以 false 完成 Future。 */
    private final boolean notifyWhenShutdown;

    /** 构造状态等待请求。 */
    public HAConnectionStateNotificationRequest(HAConnectionState expectState, String remoteAddr, boolean notifyWhenShutdown) {
        this.expectState = expectState;
        this.remoteAddr = remoteAddr;
        this.notifyWhenShutdown = notifyWhenShutdown;
    }

    /** 返回完成 Future。 */
    public CompletableFuture<Boolean> getRequestFuture() {
        return requestFuture;
    }

    /** 返回远程地址。 */
    public String getRemoteAddr() {
        return remoteAddr;
    }

    /** 是否在 SHUTDOWN 时通知。 */
    public boolean isNotifyWhenShutdown() {
        return notifyWhenShutdown;
    }

    /** 返回期望状态。 */
    public HAConnectionState getExpectState() {
        return expectState;
    }
}
