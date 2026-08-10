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

package org.keycloak.representations.adapters.action;

import java.util.List;

/**
 * 管理服务器向 adapter 下发的登出动作，可指定 adapter 会话 ID、Keycloak 会话 ID 及 notBefore 边界。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class LogoutAction extends AdminAction {
    /** 登出动作类型常量。 */
    public static final String LOGOUT = "LOGOUT";
    /** 需登出的 adapter 侧会话 ID 列表。 */
    protected List<String> adapterSessionIds;
    /** notBefore 边界（秒）；早于该时间签发的令牌应视为无效。 */
    protected int notBefore;
    /** 需登出的 Keycloak 服务端会话 ID 列表。 */
    protected List<String> keycloakSessionIds;

    /** 默认无参构造器。 */
    public LogoutAction() {
    }

    /**
     * 构造完整登出动作。
     *
     * @param id 动作 ID
     * @param expiration 过期时间（秒）
     * @param resource 目标资源
     * @param adapterSessionIds adapter 会话 ID 列表
     * @param notBefore notBefore 边界
     * @param keycloakSessionIds Keycloak 会话 ID 列表
     */
    public LogoutAction(String id, int expiration, String resource, List<String> adapterSessionIds, int notBefore, List<String> keycloakSessionIds) {
        super(id, expiration, resource, LOGOUT);
        this.adapterSessionIds = adapterSessionIds;
        this.notBefore = notBefore;
        this.keycloakSessionIds = keycloakSessionIds;
    }


    /** @return notBefore 边界（秒） */
    public int getNotBefore() {
        return notBefore;
    }

    /** @param notBefore notBefore 边界（秒） */
    public void setNotBefore(int notBefore) {
        this.notBefore = notBefore;
    }

    /** @return adapter 会话 ID 列表 */
    public List<String> getAdapterSessionIds() {
        return adapterSessionIds;
    }

    /** @return Keycloak 会话 ID 列表 */
    public List<String> getKeycloakSessionIds() {
        return keycloakSessionIds;
    }

    /** @param keycloakSessionIds Keycloak 会话 ID 列表 */
    public void setKeycloakSessionIds(List<String> keycloakSessionIds) {
        this.keycloakSessionIds = keycloakSessionIds;
    }

    /** @return 动作类型是否为 {@link #LOGOUT} */
    @Override
    public boolean validate() {
        return LOGOUT.equals(action);
    }
}
