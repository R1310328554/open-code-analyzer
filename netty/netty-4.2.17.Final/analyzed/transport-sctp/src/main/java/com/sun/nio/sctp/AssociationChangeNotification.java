/*
 * Copyright 2011 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.sun.nio.sctp;

/**
 * SCTP 关联生命周期变更通知（如 COMM_UP、COMM_LOST、RESTART、SHUTDOWN）。
 * <p>由 {@link SctpChannel#receive} 的 {@link NotificationHandler} 回调； stub 类在非 SCTP 平台不可用。</p>
 */
public abstract class AssociationChangeNotification implements Notification {
    /** 平台能力探测静态块 */
    static {
        UnsupportedOperatingSystemException.raise();
    }
}
