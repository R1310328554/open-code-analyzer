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
package org.keycloak.models.utils;

import org.keycloak.models.AuthenticationFlowBindings;
import org.keycloak.models.AuthenticationFlowModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.Constants;
import org.keycloak.models.ModelException;
import org.keycloak.sessions.AuthenticationSessionModel;

import org.jboss.logging.Logger;

/**
 * 认证流程解析工具类。
 * <p>根据认证会话、客户端绑定覆盖及领域默认配置，解析浏览器与 Direct Grant 流程。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class AuthenticationFlowResolver {

    private static final Logger logger = Logger.getLogger(AuthenticationFlowResolver.class);

    /** 解析浏览器登录流程：优先请求别名，其次客户端绑定，最后领域默认。 */
    public static AuthenticationFlowModel resolveBrowserFlow(AuthenticationSessionModel authSession) {
        AuthenticationFlowModel flow = null;
        ClientModel client = authSession.getClient();

        // 检查是否通过 auth note 指定了特定流程别名
        String requestedFlowAlias = authSession.getAuthNote(Constants.REQUESTED_AUTHENTICATION_FLOW);
        if (requestedFlowAlias != null){
            flow = authSession.getRealm().getFlowByAlias(requestedFlowAlias);
            // 校验指定流程是否存在
            if (flow == null){
                throw new ModelException("Client " + client.getClientId() + " has requested browser flow " + requestedFlowAlias + ", but this flow does not exist.");
            } else {
                return flow;
            }
        }

        flow = resolveBindingOverrideFlowForClient(client, AuthenticationFlowBindings.BROWSER_BINDING);
        if (flow != null) {
            return flow;
        }
        return authSession.getRealm().getBrowserFlow();
    }
    /** 解析 Direct Grant（资源所有者密码）流程。 */
    public static AuthenticationFlowModel resolveDirectGrantFlow(AuthenticationSessionModel authSession) {
        AuthenticationFlowModel flow = null;
        ClientModel client = authSession.getClient();

        // 检查是否通过 auth note 指定了特定流程别名
        String requestedFlowAlias = authSession.getAuthNote(Constants.REQUESTED_AUTHENTICATION_FLOW);
        if (requestedFlowAlias != null){
            flow = authSession.getRealm().getFlowByAlias(requestedFlowAlias);
            // 校验指定流程是否存在
            if (flow == null){
                throw new ModelException("Client " + client.getClientId() + " has requested browser flow " + requestedFlowAlias + ", but this flow does not exist.");
            } else {
                return flow;
            }
        }

        flow = resolveBindingOverrideFlowForClient(client, AuthenticationFlowBindings.DIRECT_GRANT_BINDING);
        if (flow != null) {
            return flow;
        }
        return authSession.getRealm().getDirectGrantFlow();
    }

    /** 读取客户端对指定绑定类型（browser/direct grant）的流程覆盖；无效时记录警告并返回 null。 */
    public static AuthenticationFlowModel resolveBindingOverrideFlowForClient(ClientModel client, String flowBindingType) {
        String clientFlow = client.getAuthenticationFlowBindingOverride(flowBindingType);
        if (clientFlow != null) {
            AuthenticationFlowModel flow = client.getRealm().getAuthenticationFlowById(clientFlow);
            if (flow != null) {
                return flow;
            }
            logger.warnf("Client %s has %s flow override, but configured override flow '%s' does not exist, " +
                    "fallback to realm %s flow", client.getClientId(), flowBindingType, clientFlow, flowBindingType);
        }
        return null;
    }
}
