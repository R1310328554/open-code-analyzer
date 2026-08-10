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
 * 对端传输地址可达性变更通知（如 ADDR_AVAILABLE、ADDR_UNREACHABLE）。
 * <p>多宿主 SCTP 关联中某 IP 路径失效或恢复时触发； 由 {@link NotificationHandler} 处理。</p>
 */
public abstract class PeerAddressChangeNotification implements Notification {
    /** stub：非 SCTP 平台拒绝加载 */
    static {
        UnsupportedOperatingSystemException.raise();
    }
}
