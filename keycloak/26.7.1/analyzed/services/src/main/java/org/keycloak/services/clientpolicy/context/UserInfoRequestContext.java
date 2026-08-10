/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.services.clientpolicy.context;

import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.protocol.oidc.endpoints.UserInfoEndpoint;
import org.keycloak.services.clientpolicy.ClientPolicyContext;
import org.keycloak.services.clientpolicy.ClientPolicyEvent;

/**
 * UserInfo 请求上下文：在 {@link ClientPolicyEvent#USERINFO_REQUEST} 事件上携带客户端会话与 access token 解析结果。
 * <p>OIDC UserInfo 端点处理 Bearer access token 前触发，供策略限制 claims 暴露或校验 token 类型。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class UserInfoRequestContext implements ClientPolicyContext, ClientPolicyClientSessionContext {

    /** 与 UserInfo 请求关联的客户端会话。 */
    private AuthenticatedClientSessionModel clientSession;
    /** 从 access token 解析出的 UserInfo 令牌信息。 */
    private UserInfoEndpoint.TokenForUserInfo tokenForUserInfo;

    /**
     * @param clientSession 客户端会话
     * @param tokenForUserInfo UserInfo 令牌解析结果
     */
        this.clientSession = clientSession;
        this.tokenForUserInfo = tokenForUserInfo;
    }

    /** {@inheritDoc} @return {@link ClientPolicyEvent#USERINFO_REQUEST} */
    @Override
    public ClientPolicyEvent getEvent() {
        return ClientPolicyEvent.USERINFO_REQUEST;
    }

    /** @return UserInfo 令牌解析结果 */
    public UserInfoEndpoint.TokenForUserInfo getTokenForUserInfo() {
        return tokenForUserInfo;
    }

    /** {@inheritDoc} @return 客户端会话 */
    @Override
    public AuthenticatedClientSessionModel getClientSession() {
        return clientSession;
    }
}
