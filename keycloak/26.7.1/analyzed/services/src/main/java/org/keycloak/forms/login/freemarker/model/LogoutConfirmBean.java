/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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
 *
 */

package org.keycloak.forms.login.freemarker.model;

import org.keycloak.models.utils.SystemClientUtil;
import org.keycloak.sessions.AuthenticationSessionModel;

/**
 * 登出确认 FreeMarker Bean：携带登出确认码及是否跳过“返回应用”链接的判定。
 * <p>系统客户端发起的登出通常不展示返回链接。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class LogoutConfirmBean {

    /** 登出确认操作码。 */
    private final String code;
    /** 登出关联的认证会话（可为 null）。 */
    private final AuthenticationSessionModel logoutSession;

    /** @param code 确认码 @param logoutSession 登出认证会话 */
    public LogoutConfirmBean(String code, AuthenticationSessionModel logoutSession) {
        this.code = code;
        this.logoutSession = logoutSession;
    }

    /** @return 登出确认码 */
    public String getCode() {
        return code;
    }

    /** @return 为 true 时模板应跳过“返回应用”链接 */
    public boolean isSkipLink() {
        return logoutSession == null || logoutSession.getClient().equals(SystemClientUtil.getSystemClient(logoutSession.getRealm()));
    }
}
