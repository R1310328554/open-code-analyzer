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

package org.keycloak.events.admin;

/**
 * 管理事件操作者的认证上下文：realm、客户端、用户与请求 IP。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class AuthDetails {

    private String realmId;
    private String realmName;

    private String clientId;

    private String userId;

    private String ipAddress;
    
    /** 无参构造，供序列化使用。 */
    public AuthDetails() {}
    /** 拷贝构造，复制各认证字段。 */
    public AuthDetails(AuthDetails toCopy) {
        this.realmId = toCopy.getRealmId();
        this.realmName = toCopy.getRealmName();
        this.clientId = toCopy.getClientId();
        this.userId = toCopy.getUserId();
        this.ipAddress = toCopy.getIpAddress();
    }

    /** @return 操作者所在 realm 的 ID */
    public String getRealmId() {
        return realmId;
    }

    public void setRealmId(String realmId) {
        this.realmId = realmId;
    }

    /** @return 操作者所在 realm 的名称 */
    public String getRealmName() {
        return realmName;
    }

    public void setRealmName(String realmName) {
        this.realmName = realmName;
    }

    /** @return 已认证客户端 ID（服务账号场景） */
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /** @return 已认证用户 ID */
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    /** @return 发起管理请求的客户端 IP 地址 */
    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

}
