/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.events.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.keycloak.connections.jpa.AsynchronousCommitAllowed;

/**
 * 用户/客户端事件 JPA 实体，映射表 {@code EVENT_ENTITY}。
 * <p>记录登录、令牌、错误等业务事件；详情 JSON 支持长文本列迁移。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
@Entity
@Table(name="EVENT_ENTITY")
public class EventEntity implements AsynchronousCommitAllowed {

    /** 事件 UUID 主键。 */
    @Id
    @Column(name="ID", length = 36)
    private String id;

    /** 事件发生时间戳（毫秒）。 */
    @Column(name="EVENT_TIME")
    private long time;

    /** 事件类型（LOGIN/LOGOUT/REGISTER_ERROR 等）。 */
    @Column(name="TYPE")
    private String type;

    /** 所属 realm ID。 */
    @Column(name="REALM_ID")
    private String realmId;

    /** 关联客户端 ID。 */
    @Column(name="CLIENT_ID")
    private String clientId;

    /** 关联用户 ID。 */
    @Column(name="USER_ID")
    private String userId;

    /** 关联用户会话 ID。 */
    @Column(name="SESSION_ID")
    private String sessionId;

    /** 客户端 IP 地址。 */
    @Column(name="IP_ADDRESS")
    private String ipAddress;

    /** 错误信息（失败类事件）。 */
    @Column(name="ERROR")
    private String error;

    // 遗留短列，保留以读取未迁移的旧事件数据
    @Column(name="DETAILS_JSON", length = 2550)
    private String detailsJson;

    /** 长文本详情 JSON 列（新写入均走此列）。 */
    @Column(name="DETAILS_JSON_LONG_VALUE")
    private String detailsJsonLongValue;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRealmId() {
        return realmId;
    }

    public void setRealmId(String realmId) {
        this.realmId = realmId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    /** 优先返回长列详情，兼容旧版短列数据。 */
    public String getDetailsJson() {
        return detailsJsonLongValue != null ? detailsJsonLongValue : detailsJson;
    }

    public void setDetailsJson(String detailsJson) {
        this.detailsJsonLongValue = detailsJson;
    }

}
