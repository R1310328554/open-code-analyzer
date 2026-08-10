/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
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

import jakarta.ws.rs.core.MultivaluedMap;

import org.keycloak.saml.common.constants.GeneralConstants;

/**
 * SAML HTTP-POST 绑定表单 Bean：从 POST 表单数据提取 SAMLRequest/Response、RelayState 与目标 URL。
 * <p>供 FreeMarker 模板渲染自动提交 SAML 消息的隐藏表单字段。</p>
 */
public class SAMLPostFormBean {

    /** SAML 认证请求（AuthnRequest）Base64 编码内容。 */
    private final String samlRequest;
    /** SAML 响应（Response）Base64 编码内容。 */
    private final String samlResponse;
    /** 中继状态，用于关联 IdP/SP 会话。 */
    private final String relayState;
    /** SAML 消息 POST 提交目标 URL。 */
    private final String url;

    /** @param formData HTTP POST 表单参数映射 */
    public SAMLPostFormBean(MultivaluedMap<String, String> formData) {
        samlRequest = formData.getFirst(GeneralConstants.SAML_REQUEST_KEY);
        samlResponse = formData.getFirst(GeneralConstants.SAML_RESPONSE_KEY);
        relayState = formData.getFirst(GeneralConstants.RELAY_STATE);
        url = formData.getFirst(GeneralConstants.URL);
    }

    /** @return SAML 认证请求内容 */
    public String getSAMLRequest() {
        return samlRequest;
    }

    /** @return SAML 响应内容 */
    public String getSAMLResponse() {
        return samlResponse;
    }

    /** @return 中继状态值 */
    public String getRelayState() {
        return relayState;
    }

    /** @return POST 提交目标 URL */
    public String getUrl() {
        return url;
    }
}
