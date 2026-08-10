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
package org.keycloak.subsystem.adapter.saml.extension;

import java.util.HashMap;

import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.dmr.ModelType;

/**
 * HTTP 客户端（{@code HttpClient}）属性的 WildFly 管理模型定义。
 *
 * <p>对应 schema 中 {@code http-client-type} 复杂类型的全部可配置项，
 * 包括 TLS、代理、连接池与超时等。</p>
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
abstract class HttpClientDefinition {

    /** 是否允许任意主机名（跳过主机名校验）。 */
    private static final SimpleAttributeDefinition ALLOW_ANY_HOSTNAME =
            new SimpleAttributeDefinitionBuilder(Constants.Model.ALLOW_ANY_HOSTNAME, ModelType.BOOLEAN, true)
                    .setXmlName(Constants.XML.ALLOW_ANY_HOSTNAME)
                    .setAllowExpression(true)
                    .build();

    /** 客户端密钥库路径。 */
    private static final SimpleAttributeDefinition CLIENT_KEYSTORE =
            new SimpleAttributeDefinitionBuilder(Constants.Model.CLIENT_KEYSTORE, ModelType.STRING, true)
                    .setXmlName(Constants.XML.CLIENT_KEYSTORE)
                    .setAllowExpression(true)
                    .build();

    /** 客户端密钥库密码。 */
    private static final SimpleAttributeDefinition CLIENT_KEYSTORE_PASSWORD =
            new SimpleAttributeDefinitionBuilder(Constants.Model.CLIENT_KEYSTORE_PASSWORD, ModelType.STRING, true)
                    .setXmlName(Constants.XML.CLIENT_KEYSTORE_PASSWORD)
                    .setAllowExpression(true)
                    .build();

    /** HTTP 连接池大小。 */
    private static final SimpleAttributeDefinition CONNECTION_POOL_SIZE =
            new SimpleAttributeDefinitionBuilder(Constants.Model.CONNECTION_POOL_SIZE, ModelType.INT, true)
                    .setXmlName(Constants.XML.CONNECTION_POOL_SIZE)
                    .setAllowExpression(true)
                    .build();

    /** 是否禁用信任管理器（不校验服务端证书）。 */
    private static final SimpleAttributeDefinition DISABLE_TRUST_MANAGER =
            new SimpleAttributeDefinitionBuilder(Constants.Model.DISABLE_TRUST_MANAGER, ModelType.BOOLEAN, true)
                    .setXmlName(Constants.XML.DISABLE_TRUST_MANAGER)
                    .setAllowExpression(true)
                    .build();

    /** HTTP 代理 URL。 */
    private static final SimpleAttributeDefinition PROXY_URL =
            new SimpleAttributeDefinitionBuilder(Constants.Model.PROXY_URL, ModelType.STRING, true)
                    .setXmlName(Constants.XML.PROXY_URL)
                    .setAllowExpression(true)
                    .build();

    /** 信任库路径。 */
    private static final SimpleAttributeDefinition TRUSTSTORE =
            new SimpleAttributeDefinitionBuilder(Constants.Model.TRUSTSTORE, ModelType.STRING, true)
                    .setXmlName(Constants.XML.TRUSTSTORE)
                    .setAllowExpression(true)
                    .build();

    /** 信任库密码。 */
    private static final SimpleAttributeDefinition TRUSTSTORE_PASSWORD =
            new SimpleAttributeDefinitionBuilder(Constants.Model.TRUSTSTORE_PASSWORD, ModelType.STRING, true)
                    .setXmlName(Constants.XML.TRUSTSTORE_PASSWORD)
                    .setAllowExpression(true)
                    .build();

    /** Socket 读取超时（毫秒）。 */
    private static final SimpleAttributeDefinition SOCKET_TIMEOUT =
            new SimpleAttributeDefinitionBuilder(Constants.Model.SOCKET_TIMEOUT, ModelType.LONG, true)
                    .setXmlName(Constants.XML.SOCKET_TIMEOUT)
                    .setAllowExpression(true)
                    .build();

    /** 建立连接超时（毫秒）。 */
    private static final SimpleAttributeDefinition CONNECTION_TIMEOUT =
            new SimpleAttributeDefinitionBuilder(Constants.Model.CONNECTION_TIMEOUT, ModelType.LONG, true)
                    .setXmlName(Constants.XML.CONNECTION_TIMEOUT)
                    .setAllowExpression(true)
                    .build();

    /** 连接存活时间 TTL（毫秒）。 */
    private static final SimpleAttributeDefinition CONNECTION_TTL =
            new SimpleAttributeDefinitionBuilder(Constants.Model.CONNECTION_TTL, ModelType.LONG, true)
                    .setXmlName(Constants.XML.CONNECTION_TTL)
                    .setAllowExpression(true)
                    .build();

    /** HttpClient 全部属性定义数组。 */
    static final SimpleAttributeDefinition[] ATTRIBUTES = {ALLOW_ANY_HOSTNAME, CLIENT_KEYSTORE, CLIENT_KEYSTORE_PASSWORD,
            CONNECTION_POOL_SIZE, DISABLE_TRUST_MANAGER, PROXY_URL, TRUSTSTORE, TRUSTSTORE_PASSWORD, SOCKET_TIMEOUT, CONNECTION_TIMEOUT, CONNECTION_TTL};

    /** XML 元素名到属性定义的索引。 */
    private static final HashMap<String, SimpleAttributeDefinition> ATTRIBUTE_MAP = new HashMap<>();

    static {
        for (SimpleAttributeDefinition def : ATTRIBUTES) {
            ATTRIBUTE_MAP.put(def.getXmlName(), def);
        }
    }

    /** 按 XML 元素名查找对应的属性定义。 */
    static SimpleAttributeDefinition lookup(String xmlName) {
        return ATTRIBUTE_MAP.get(xmlName);
    }
}
