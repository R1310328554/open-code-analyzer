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

package org.keycloak.events;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户操作审计事件数据模型。
 * <p>由 {@link EventBuilder} 组装并分发给 {@link EventStoreProvider} 与 {@link EventListenerProvider}。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class Event {

    /** 事件唯一标识（UUID）。 */
    private String id;

    /** 事件发生时间戳（毫秒）。 */
    private long time;

    /** 事件类型枚举。 */
    private EventType type;

    private String realmId;
    private String realmName;

    private String clientId;

    private String userId;

    private String sessionId;

    private String ipAddress;

    /** 失败时的错误码，见 {@link Errors}。 */
    private String error;

    /** 附加键值详情，键名见 {@link Details}。 */
    private Map<String, String> details;

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

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public String getRealmId() {
        return realmId;
    }

    public void setRealmId(String realmId) {
        this.realmId = maxLength(realmId, 255);
    }

    public String getRealmName() {
        return realmName;
    }

    public void setRealmName(String realmName) {
        this.realmName = realmName;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = maxLength(clientId, 255);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = maxLength(userId, 255);
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = maxLength(sessionId, 255);
    }

    /**
     * 客户端 IP 地址。
     * <p>反向代理未传递有效地址时可能为空或非真实 IP。</p>
     *
     * @return the ip address
     */
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

    public Map<String, String> getDetails() {
        return details;
    }

    public void setDetails(Map<String, String> details) {
        this.details = details;
    }

    /** 深拷贝事件对象（含 details 映射）。 */
    @Override
    public Event clone() {
        Event clone = new Event();
        clone.id = id;
        clone.time = time;
        clone.type = type;
        clone.realmId = realmId;
        clone.realmName = realmName;
        clone.clientId = clientId;
        clone.userId = userId;
        clone.sessionId = sessionId;
        clone.ipAddress = ipAddress;
        clone.error = error;
        clone.details = details != null ? new HashMap<>(details) : null;
        return clone;
    }

    /** 截断字符串至指定最大长度，防止持久化字段溢出。 */
    static String maxLength(String string, int length){
        if (string != null && string.length() > length) {
            return string.substring(0, length - 1);
        }
        return string;
    }

}
