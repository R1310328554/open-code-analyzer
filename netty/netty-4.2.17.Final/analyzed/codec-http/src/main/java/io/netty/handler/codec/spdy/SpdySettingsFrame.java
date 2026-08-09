/*
 * Copyright 2013 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.codec.spdy;

import java.util.Set;

/**
 * SPDY 协议的 SETTINGS 帧：协商会话参数（并发流上限、初始窗口大小等）。
 * <p>各 {@code SETTINGS_*} 常量对应协议规定的设置项 ID；部分项支持持久化标志。
 */
public interface SpdySettingsFrame extends SpdyFrame {

    int SETTINGS_MINOR_VERSION                  = 0;
    int SETTINGS_UPLOAD_BANDWIDTH               = 1;
    int SETTINGS_DOWNLOAD_BANDWIDTH             = 2;
    int SETTINGS_ROUND_TRIP_TIME                = 3;
    int SETTINGS_MAX_CONCURRENT_STREAMS         = 4;
    int SETTINGS_CURRENT_CWND                   = 5;
    int SETTINGS_DOWNLOAD_RETRANS_RATE          = 6;
    int SETTINGS_INITIAL_WINDOW_SIZE            = 7;
    int SETTINGS_CLIENT_CERTIFICATE_VECTOR_SIZE = 8;

    /**
     * 返回已设置的 setting ID 集合，迭代器按 ID 升序遍历。
     */
    Set<Integer> ids();

    /**
     * 若指定 ID 已有值，返回 {@code true}。
     */
    boolean isSet(int id);

    /**
     * 返回 setting ID 的值；未设置时返回 -1。
     */
    int getValue(int id);

    /**
     * 设置 setting ID 的值（ID 非负且不超过 16777215）。
     */
    SpdySettingsFrame setValue(int id, int value);

    /**
     * 设置值及持久化标志：{@code persistVal} 表示是否要求对端持久化（仅服务器可设），
     * {@code persisted} 表示发送方声明该值已持久化（仅客户端可设）。
     */
    SpdySettingsFrame setValue(int id, int value, boolean persistVal, boolean persisted);

    /**
     * 移除指定 ID 的值及其持久化元数据。
     */
    SpdySettingsFrame removeValue(int id);

    /**
     * 若该 setting 应被对端持久化，返回 {@code true}；无值或未要求持久化则 {@code false}。
     */
    boolean isPersistValue(int id);

    /**
     * 设置是否要求对端持久化该 setting（仅当 ID 已有值时生效）。
     */
    SpdySettingsFrame setPersistValue(int id, boolean persistValue);

    /**
     * 若发送方声明该 setting 已持久化，返回 {@code true}。
     */
    boolean isPersisted(int id);

    /**
     * 标记该 setting 是否已持久化（仅当 ID 已有值时生效）。
     */
    SpdySettingsFrame setPersisted(int id, boolean persisted);

    /**
     * 是否应清除对端此前持久化的 settings。
     */
    boolean clearPreviouslyPersistedSettings();

    /**
     * 设置是否在应用本帧前清除对端已持久化的 settings。
     */
    SpdySettingsFrame setClearPreviouslyPersistedSettings(boolean clear);
}
