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
package org.keycloak.subsystem.adapter.saml.extension;

import java.util.HashMap;

import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.dmr.ModelType;

/**
 * IdP 单点登出（SLO）嵌套配置的属性定义基类。
 *
 * <p>集中声明 SLO 请求的签名验证、出站签名、绑定方式及端点 URL 等
 * {@link SimpleAttributeDefinition}，供 {@link IdentityProviderDefinition} 引用。</p>
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
abstract class SingleLogoutDefinition {

    /** 是否验证入站 SLO 请求的 XML 签名。 */
    static final SimpleAttributeDefinition VALIDATE_REQUEST_SIGNATURE =
            new SimpleAttributeDefinitionBuilder(Constants.Model.VALIDATE_REQUEST_SIGNATURE, ModelType.BOOLEAN, true)
                    .setXmlName(Constants.XML.VALIDATE_REQUEST_SIGNATURE)
                    .build();

    /** 是否验证 IdP 返回的 SLO 响应签名。 */
    static final SimpleAttributeDefinition VALIDATE_RESPONSE_SIGNATURE =
            new SimpleAttributeDefinitionBuilder(Constants.Model.VALIDATE_RESPONSE_SIGNATURE, ModelType.BOOLEAN, true)
                    .setXmlName(Constants.XML.VALIDATE_RESPONSE_SIGNATURE)
                    .build();

    /** 出站 SLO 请求是否签名。 */
    static final SimpleAttributeDefinition SIGN_REQUEST =
            new SimpleAttributeDefinitionBuilder(Constants.Model.SIGN_REQUEST, ModelType.BOOLEAN, true)
                    .setXmlName(Constants.XML.SIGN_REQUEST)
                    .build();

    /** 出站 SLO 响应是否签名。 */
    static final SimpleAttributeDefinition SIGN_RESPONSE =
            new SimpleAttributeDefinitionBuilder(Constants.Model.SIGN_RESPONSE, ModelType.BOOLEAN, true)
                    .setXmlName(Constants.XML.SIGN_RESPONSE)
                    .build();

    /** SLO 请求使用的 SAML 绑定（如 POST、REDIRECT）。 */
    static final SimpleAttributeDefinition REQUEST_BINDING =
            new SimpleAttributeDefinitionBuilder(Constants.Model.REQUEST_BINDING, ModelType.STRING, true)
                    .setXmlName(Constants.XML.REQUEST_BINDING)
                    .build();

    /** SLO 响应使用的 SAML 绑定。 */
    static final SimpleAttributeDefinition RESPONSE_BINDING =
            new SimpleAttributeDefinitionBuilder(Constants.Model.RESPONSE_BINDING, ModelType.STRING, true)
                    .setXmlName(Constants.XML.RESPONSE_BINDING)
                    .build();

    /** HTTP-POST 绑定的 SLO 端点 URL。 */
    static final SimpleAttributeDefinition POST_BINDING_URL =
            new SimpleAttributeDefinitionBuilder(Constants.Model.POST_BINDING_URL, ModelType.STRING, true)
                    .setXmlName(Constants.XML.POST_BINDING_URL)
                    .build();

    /** HTTP-Redirect 绑定的 SLO 端点 URL。 */
    static final SimpleAttributeDefinition REDIRECT_BINDING_URL =
            new SimpleAttributeDefinitionBuilder(Constants.Model.REDIRECT_BINDING_URL, ModelType.STRING, true)
                    .setXmlName(Constants.XML.REDIRECT_BINDING_URL)
                    .build();

    /** 全部 SLO 属性定义数组。 */
    static final SimpleAttributeDefinition[] ATTRIBUTES = {VALIDATE_REQUEST_SIGNATURE, VALIDATE_RESPONSE_SIGNATURE,
            SIGN_REQUEST, SIGN_RESPONSE, REQUEST_BINDING, RESPONSE_BINDING, POST_BINDING_URL, REDIRECT_BINDING_URL};

    /** XML 名称到属性定义的查找表。 */
    static final HashMap<String, SimpleAttributeDefinition> ATTRIBUTE_MAP = new HashMap<>();

    static {
        for (SimpleAttributeDefinition def : ATTRIBUTES) {
            ATTRIBUTE_MAP.put(def.getXmlName(), def);
        }
    }

    /** 按 XML 属性名查找 SLO 属性定义。 */
    static SimpleAttributeDefinition lookup(String xmlName) {
        return ATTRIBUTE_MAP.get(xmlName);
    }
}
