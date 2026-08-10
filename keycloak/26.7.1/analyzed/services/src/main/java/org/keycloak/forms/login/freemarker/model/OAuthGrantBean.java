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
package org.keycloak.forms.login.freemarker.model;

import java.util.ArrayList;
import java.util.List;

import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.OrderedModel;
import org.keycloak.rar.AuthorizationDetails;

/**
 * OAuth 授权同意 FreeMarker Bean：展示客户端请求的 scope 与授权码。
 * <p>将 {@link ClientScopeModel} 转为模板可读的 {@link ClientScopeEntry}，规避 FreeMarker 无法调用接口默认方法 {@code getConsentScreenText} 的限制。</p>
 *
 * @author <a href="mailto:vrockai@redhat.com">Viliam Rockai</a>
 */
public class OAuthGrantBean {

    private static OrderedModel.OrderedModelComparator<ClientScopeEntry> COMPARATOR_INSTANCE = new OrderedModel.OrderedModelComparator<>();

    /** 按 guiOrder 排序的待授权客户端 scope 列表。 */
    private List<ClientScopeEntry> clientScopesRequested = new ArrayList<>();
    /** OAuth 授权码。 */
    private String code;
    /** 请求授权的客户端。 */
    private ClientModel client;

    /** @param code 授权码 @param client 客户端 @param clientScopesRequested 请求的 scope 详情列表 */
    public OAuthGrantBean(String code, ClientModel client, List<AuthorizationDetails> clientScopesRequested) {
        this.code = code;
        this.client = client;

        for (AuthorizationDetails authDetails : clientScopesRequested) {
            ClientScopeModel clientScope = authDetails.getClientScope();
            this.clientScopesRequested.add(new ClientScopeEntry(clientScope.getConsentScreenText(), clientScope.getGuiOrder(), authDetails));
        }
        this.clientScopesRequested.sort(COMPARATOR_INSTANCE);
    }

    /** @return 授权码 */
    public String getCode() {
        return code;
    }


    /** @return 客户端 ID */
    public String getClient() {
        return client.getClientId();
    }


    /** @return 待同意的客户端 scope 条目列表 */
    public List<ClientScopeEntry> getClientScopesRequested() {
        return clientScopesRequested;
    }


    // 因 FreeMarker 无法读取接口默认方法 getConsentScreenText，将 ClientScopeModel 包装为独立条目类
    /** 授权同意页上的单个客户端 scope 展示条目。 */
    public static class ClientScopeEntry implements OrderedModel {

        private final String consentScreenText;
        private final String guiOrder;
        private final String parameterizedScopeParameter;

        public ClientScopeEntry(String consentScreenText, String guiOrder, AuthorizationDetails authorizationDetails) {
            this.consentScreenText = consentScreenText;
            this.guiOrder = guiOrder;
            this.parameterizedScopeParameter = authorizationDetails.getParameterizedScopeParam();
        }

        /** @return 同意屏幕展示文案 */
        public String getConsentScreenText() {
            return consentScreenText;
        }

        @Override
        public String getGuiOrder() {
            return guiOrder;
        }

        /** @return 参数化 scope 的参数值（若有） */
        public String getParameterizedScopeParameter() {
            return parameterizedScopeParameter;
        }
    }
}
