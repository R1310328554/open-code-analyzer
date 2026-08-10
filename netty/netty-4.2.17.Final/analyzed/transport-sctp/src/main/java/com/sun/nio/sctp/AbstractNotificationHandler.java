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
 * SCTP 通知处理的空实现基类。
 * <p>实现 {@link NotificationHandler} 的全部 {@code handleNotification} 重载并默认返回  {@code null}；子类按需覆盖。非 SCTP 平台加载时静态块抛出  {@link UnsupportedOperatingSystemException}。</p>
 * @param <T> 与 {@link SctpChannel#receive} 传入的 attachment 类型一致
 */
@SuppressWarnings("all")
public class AbstractNotificationHandler<T> implements NotificationHandler<T> {
    /** 类加载时校验当前 OS/JDK 是否支持 SCTP */
    static {
        UnsupportedOperatingSystemException.raise();
    }

    /** 关联状态变更通知；默认忽略（返回 {@code null}） */
    public HandlerResult handleNotification(AssociationChangeNotification notification, Object o) {
        return null;
    }

    /** 通用通知入口；默认忽略 */
    public HandlerResult handleNotification(Notification notification, Object o) {
        return null;
    }

    /** 对端地址可达性变更；默认忽略 */
    public HandlerResult handleNotification(PeerAddressChangeNotification notification, Object o) {
        return null;
    }

    /** 发送失败通知；默认忽略 */
    public HandlerResult handleNotification(SendFailedNotification notification, Object o) {
        return null;
    }

    /** 关联关闭通知；默认忽略 */
    public HandlerResult handleNotification(ShutdownNotification notification, Object o) {
        return null;
    }
}
