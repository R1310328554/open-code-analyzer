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
 * 管理事件（Admin Event）JPA 实体，映射表 {@code ADMIN_EVENT_ENTITY}。
 * <p>记录 realm 内管理 API 操作审计：操作类型、资源路径、认证上下文及可选 representation。</p>
 *
 * @author <a href="mailto:giriraj.sharma27@gmail.com">Giriraj Sharma</a>
 */
@Entity
@Table(name="ADMIN_EVENT_ENTITY")
public class AdminEventEntity implements AsynchronousCommitAllowed {

    /** 事件 UUID 主键。 */
    @Id
    @Column(name="ID", length = 36)
    private String id;

    /** 事件发生时间戳（毫秒）。 */
    @Column(name="ADMIN_EVENT_TIME")
    private long time;

    /** 被操作资源所属 realm ID。 */
    @Column(name="REALM_ID")
    private String realmId;

    /** 操作类型（CREATE/UPDATE/DELETE/ACTION 等）。 */
    @Column(name="OPERATION_TYPE")
    private String operationType;

    /** 资源类型（USER/CLIENT/REALM 等）。 */
    @Column(name="RESOURCE_TYPE", length = 64)
    private String resourceType;

    /** 执行操作的认证 realm ID。 */
    @Column(name="AUTH_REALM_ID")
    private String authRealmId;

    /** 执行操作的客户端 ID。 */
    @Column(name="AUTH_CLIENT_ID")
    private String authClientId;

    /** 执行操作的用户 ID。 */
    @Column(name="AUTH_USER_ID")
    private String authUserId;

    /** 操作来源 IP 地址。 */
    @Column(name="IP_ADDRESS")
    private String authIpAddress;

    /** 被操作资源的 REST 路径。 */
    @Column(name="RESOURCE_PATH")
    private String resourcePath;

    /** 可选的资源 JSON 表示（写入时由 includeRepresentation 控制）。 */
    @Column(name="REPRESENTATION")
    private String representation;

    /** 操作失败时的错误信息。 */
    @Column(name="ERROR")
    private String error;

    /** 附加详情 JSON（键值对序列化）。 */
    @Column(name="DETAILS_JSON")
    private String detailsJson;

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

    public String getRealmId() {
        return realmId;
    }

    public void setRealmId(String realmId) {
        this.realmId = realmId;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getAuthRealmId() {
        return authRealmId;
    }

    public void setAuthRealmId(String authRealmId) {
        this.authRealmId = authRealmId;
    }

    public String getAuthClientId() {
        return authClientId;
    }

    public void setAuthClientId(String authClientId) {
        this.authClientId = authClientId;
    }

    public String getAuthUserId() {
        return authUserId;
    }

    public void setAuthUserId(String authUserId) {
        this.authUserId = authUserId;
    }

    public String getAuthIpAddress() {
        return authIpAddress;
    }

    public void setAuthIpAddress(String authIpAddress) {
        this.authIpAddress = authIpAddress;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public String getRepresentation() {
        return representation;
    }

    public void setRepresentation(String representation) {
        this.representation = representation;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getDetailsJson() {
        return detailsJson;
    }

    public void setDetailsJson(String detailsJson) {
        this.detailsJson = detailsJson;
    }
}
