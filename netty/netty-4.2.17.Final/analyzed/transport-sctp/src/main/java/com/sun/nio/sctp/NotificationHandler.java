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
 * 处理 {@link SctpChannel#receive} 路径上到达的 SCTP 通知。
 * <p>实现类可继承 {@link AbstractNotificationHandler}； 各 {@code handleNotification} 重载返回 {@link HandlerResult} 控制是否继续。</p>
 * @param <T> 与 receive 调用方传入的 attachment 类型相同
 */
public interface NotificationHandler<T> {
}
