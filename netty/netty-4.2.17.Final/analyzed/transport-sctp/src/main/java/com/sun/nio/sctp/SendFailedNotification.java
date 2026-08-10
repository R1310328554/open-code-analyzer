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
 * SCTP 发送失败通知：某条出站消息未能送达时由协议栈上报。
 * <p>通过 {@link NotificationHandler#handleNotification(SendFailedNotification, Object)}  处理；Netty 中 {@link io.netty.channel.sctp.SctpNotificationHandler} 将其转为 pipeline 用户事件。 非 SCTP 平台为编译占位 stub。</p>
 */
public abstract class SendFailedNotification implements Notification {
    /** 类加载时校验 OS/JDK 是否支持 SCTP */
    static {
        UnsupportedOperatingSystemException.raise();
    }
}
