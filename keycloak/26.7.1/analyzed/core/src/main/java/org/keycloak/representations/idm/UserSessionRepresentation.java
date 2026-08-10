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

package org.keycloak.representations.idm;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户会话的 Admin REST API 表示，用于查询与管理 realm 中的在线会话。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class UserSessionRepresentation {
    /** 会话唯一 ID。 */
    private String id;
    /** 会话所属用户名。 */
    private String username;
    /** 会话所属用户 ID。 */
    private String userId;
    /** 客户端 IP 地址。 */
    private String ipAddress;
    /** 会话开始时间（Unix 毫秒时间戳）。 */
    private long start;
    /** 最后一次访问时间（Unix 毫秒时间戳）。 */
    private long lastAccess;
    /** 是否为“记住我”会话。 */
    private boolean rememberMe;
    /** 会话关联的客户端 ID 到客户端名称的映射。 */
    private Map<String, String> clients = new HashMap<>();
    /** 是否为临时（transient）用户会话。 */
    private boolean transientUser;

    /** @return 会话 ID */
    public String getId() {
        return id;
    }

    /** @param id 会话 ID */
    public void setId(String id) {
        this.id = id;
    }

    /** @return 用户名 */
    public String getUsername() {
        return username;
    }

    /** @param username 用户名 */
    public void setUsername(String username) {
        this.username = username;
    }

    /** @return 用户 ID */
    public String getUserId() {
        return userId;
    }

    /** @param userId 用户 ID */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * 注意：若代理未提供有效地址，返回值可能不是真实 IP。
     *
     * @return IP 地址
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /** @param ipAddress IP 地址 */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /** @return 会话开始时间戳 */
    public long getStart() {
        return start;
    }

    /** @param start 会话开始时间戳 */
    public void setStart(long start) {
        this.start = start;
    }

    /** @return 最后访问时间戳 */
    public long getLastAccess() {
        return lastAccess;
    }

    /** @param lastAccess 最后访问时间戳 */
    public void setLastAccess(long lastAccess) {
        this.lastAccess = lastAccess;
    }

    /** @return 是否为“记住我”会话 */
    public boolean isRememberMe() {
        return rememberMe;
    }

    /** @param rememberMe 是否“记住我” */
    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    /** @return 关联客户端映射 */
    public Map<String, String> getClients() {
        return clients;
    }

    /** @param clients 关联客户端映射 */
    public void setClients(Map<String, String> clients) {
        this.clients = clients;
    }

    /** @return 是否为临时用户会话 */
    public boolean isTransientUser() {
        return transientUser;
    }

    /** @param transientUser 是否为临时用户 */
    public void setTransientUser(boolean transientUser) {
        this.transientUser = transientUser;
    }
}
