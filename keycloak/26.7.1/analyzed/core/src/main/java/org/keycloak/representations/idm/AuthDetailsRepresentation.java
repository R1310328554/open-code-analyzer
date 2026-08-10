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

/**
 * 管理事件或审计日志中记录的操作主体认证上下文。
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2016 Red Hat Inc.
 */
public class AuthDetailsRepresentation {

    /** 操作所在 realm 的内部 ID。 */
    private String realmId;
    /** 发起操作的客户端 ID。 */
    private String clientId;
    /** 执行操作的用户 ID。 */
    private String userId;
    /** 请求来源 IP 地址。 */
    private String ipAddress;

    /** @return realm ID */
    public String getRealmId() {
        return realmId;
    }

    /** @param realmId realm ID */
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

    /** @return 用户 ID */
    public String getUserId() {
        return userId;
    }

    /** @param userId 用户 ID */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * 注意：当反向代理未提供有效地址时，返回值可能不是真实 IP。
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

}
