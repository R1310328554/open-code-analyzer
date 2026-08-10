/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.models.utils;

/**
 * 多站点/跨数据中心会话空闲超时校验的容差常量。
 * <p>补偿 lastSessionRefresh 同步延迟，避免误删仍有效的会话。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class SessionTimeoutHelper {


    /**
     * 周期性任务间隔（秒）：{@code lastSessionRefresh} 可能含陈旧值的最大时长。
     * <p>例如 DC1 刷新后会批量同步至 DC2，存在约 60 秒延迟。</p>
     */
    public static final int PERIODIC_TASK_INTERVAL_SECONDS = 60;


    /**
     * 校验用户会话空闲超时时仍允许的最大时间差（秒）。
     * <p>多数据中心下 DC2 可能尚未收到 DC1 的 lastSessionRefresh 更新。</p>
     * 参见 {@link #PERIODIC_TASK_INTERVAL_SECONDS}
     */
    public static final int IDLE_TIMEOUT_WINDOW_SECONDS = 120;


    /**
     * 周期性清理线程判定会话真正超时、可 GC 的最大容差（秒）。
     * <p>仅超出此窗口的会话视为已超时（考虑多站点同步延迟）。</p>
     * 参见 {@link #PERIODIC_TASK_INTERVAL_SECONDS} 与 {@link #IDLE_TIMEOUT_WINDOW_SECONDS}
     */
    public static final int PERIODIC_CLEANER_IDLE_TIMEOUT_WINDOW_SECONDS = 180;
}
