/*
 * Copyright 2016 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.keycloak.representations.idm;

import java.util.Map;

/**
 * 用户事件（User Event）的 REST 表示，用于 Events API 查询与审计导出。
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2016 Red Hat Inc.
 */
public class EventRepresentation {

    /** 事件唯一标识。 */
    private String id;
    /** 事件发生时间（Unix 毫秒时间戳）。 */
    private long time;
    /** 事件类型（如 LOGIN、LOGOUT 等）。 */
    private String type;
    /** 所属 realm 的内部 ID。 */
    private String realmId;
    /** 触发事件的客户端 ID。 */
    private String clientId;
    /** 关联用户的内部 ID。 */
    private String userId;
    /** 关联用户会话 ID。 */
    private String sessionId;
    /** 客户端 IP 地址。 */
    private String ipAddress;
    /** 事件失败时的错误信息。 */
    private String error;
    /** 附加键值对详情。 */
    private Map<String, String> details;

    /** @return 事件 ID */
    public String getId() {
        return id;
    }

    /** @param id 事件 ID */
    public void setId(String id) {
        this.id = id;
    }

    /** @return 事件发生时间（毫秒） */
    public long getTime() {
        return time;
    }

    /** @param time 事件发生时间（毫秒） */
    public void setTime(long time) {
        this.time = time;
    }

    /** @return 事件类型 */
    public String getType() {
        return type;
    }

    /** @param type 事件类型 */
    public void setType(String type) {
        this.type = type;
    }

    /** @return realm 内部 ID */
    public String getRealmId() {
        return realmId;
    }

    /** @param realmId realm 内部 ID */
    public void setRealmId(String realmId) {
        this.realmId = realmId;
    }

    /** @return 客户端 ID */
    public String getClientId() {
        return clientId;
    }

    /** @param clientId 客户端 ID */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /** @return 用户内部 ID */
    public String getUserId() {
        return userId;
    }

    /** @param userId 用户内部 ID */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /** @return 用户会话 ID */
    public String getSessionId() {
        return sessionId;
    }

    /** @param sessionId 用户会话 ID */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * 注意：当代理未提供有效地址时，返回值可能不是真实 IP。
     *
     * @return 客户端 IP 地址
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /** @param ipAddress 客户端 IP 地址 */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /** @return 错误信息 */
    public String getError() {
        return error;
    }

    /** @param error 错误信息 */
    public void setError(String error) {
        this.error = error;
    }

    /** @return 附加详情 */
    public Map<String, String> getDetails() {
        return details;
    }

    /** @param details 附加详情 */
    public void setDetails(Map<String, String> details) {
        this.details = details;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EventRepresentation that = (EventRepresentation) o;

        if (time != that.time) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (realmId != null ? !realmId.equals(that.realmId) : that.realmId != null) return false;
        if (clientId != null ? !clientId.equals(that.clientId) : that.clientId != null) return false;
        if (userId != null ? !userId.equals(that.userId) : that.userId != null) return false;
        if (sessionId != null ? !sessionId.equals(that.sessionId) : that.sessionId != null) return false;
        if (ipAddress != null ? !ipAddress.equals(that.ipAddress) : that.ipAddress != null) return false;
        if (error != null ? !error.equals(that.error) : that.error != null) return false;
        return !(details != null ? !details.equals(that.details) : that.details != null);

    }

    @Override
    public int hashCode() {
        int result = (int) (time ^ (time >>> 32));
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (realmId != null ? realmId.hashCode() : 0);
        result = 31 * result + (clientId != null ? clientId.hashCode() : 0);
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        result = 31 * result + (sessionId != null ? sessionId.hashCode() : 0);
        result = 31 * result + (ipAddress != null ? ipAddress.hashCode() : 0);
        result = 31 * result + (error != null ? error.hashCode() : 0);
        result = 31 * result + (details != null ? details.hashCode() : 0);
        return result;
    }
}
