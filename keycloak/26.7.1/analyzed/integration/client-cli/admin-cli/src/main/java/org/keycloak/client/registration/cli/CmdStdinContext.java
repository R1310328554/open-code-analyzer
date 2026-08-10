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

package org.keycloak.client.registration.cli;

import java.io.IOException;
import java.util.List;

import org.keycloak.client.cli.common.AttributeOperation;
import org.keycloak.client.cli.util.AttributeException;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.oidc.OIDCClientRepresentation;
import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

import static org.keycloak.client.cli.util.IoUtil.readFileOrStdin;
import static org.keycloak.client.registration.cli.ReflectionUtil.setAttributes;

/**
 * 从文件或标准输入解析得到的注册 CLI 上下文。
 * <p>
 * 封装端点类型、原始内容、{@link ClientRepresentation} 或 {@link OIDCClientRepresentation} 对象，
 * 并提供属性合并与注册访问令牌读取。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class CmdStdinContext {

    /** 推断或指定的注册端点类型。 */
    private EndpointType regType;
    /** Keycloak 默认格式的客户端表示（若适用）。 */
    private ClientRepresentation client;
    /** OIDC 动态注册格式的客户端表示（若适用）。 */
    private OIDCClientRepresentation oidcClient;
    /** 原始文档内容（JSON 或 SAML XML）。 */
    private String content;
    /** 参数形似 CLI 选项时的客户端 ID 警告模板。 */
    public static final String CLIENT_OPTION_WARN = "You're using what looks like an OPTION as CLIENT: %s";
    /** 参数形似 CLI 选项时的令牌警告模板。 */
    public static final String TOKEN_OPTION_WARN = "You're using what looks like an OPTION as TOKEN: %s";

    /** 构造空上下文。 */
    public CmdStdinContext() {}

    /** 返回端点类型。 */
    public EndpointType getEndpointType() {
        return regType;
    }

    /** 设置端点类型。 */
    public void setEndpointType(EndpointType regType) {
        this.regType = regType;
    }

    /** 返回 Keycloak 默认格式客户端对象。 */
    public ClientRepresentation getClient() {
        return client;
    }

    /** 设置 Keycloak 默认格式客户端对象。 */
    public void setClient(ClientRepresentation client) {
        this.client = client;
    }

    /** 返回 OIDC 格式客户端对象。 */
    public OIDCClientRepresentation getOidcClient() {
        return oidcClient;
    }

    /** 设置 OIDC 格式客户端对象。 */
    public void setOidcClient(OIDCClientRepresentation oidcClient) {
        this.oidcClient = oidcClient;
    }

    /** 返回原始文档内容。 */
    public String getContent() {
        return content;
    }

    /** 设置原始文档内容。 */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 从已解析的客户端对象中提取注册访问令牌。
     *
     * @return 注册访问令牌，未解析客户端时返回 {@code null}
     */
    public String getRegistrationAccessToken() {
        if (client != null) {
            return client.getRegistrationAccessToken();
        } else if (oidcClient != null) {
            return oidcClient.getRegistrationAccessToken();
        }
        return null;
    }

    /**
     * 从文件或标准输入读取并解析客户端配置文档。
     * <p>
     * 未指定 {@code type} 时根据内容前缀自动推断：{@code <} 为 SAML XML，{@code {} 为 JSON。
     *
     * @param file 文件路径，{@code -} 表示标准输入
     * @param type 端点类型，可为 {@code null} 以自动检测
     * @return 填充完毕的上下文
     */
    public static CmdStdinContext parseFileOrStdin(String file, EndpointType type) {
    
        String content = readFileOrStdin(file).trim();
        ClientRepresentation client = null;
        OIDCClientRepresentation oidcClient = null;
    
        if (type == null) {
            // 根据文件内容推断正确的端点类型
            if (content.startsWith("<")) {
                // 形如 XML
                type = EndpointType.SAML2;
            } else if (content.startsWith("{")) {
                // 形如 JSON：先尝试 ClientRepresentation
                try {
                    client = JsonSerialization.readValue(content, ClientRepresentation.class);
                    type = EndpointType.DEFAULT;
    
                } catch (JsonParseException e) {
                    throw new RuntimeException("Failed to read the input document as JSON: " + e.getMessage(), e);
                } catch (Exception ignored) {
                    // deliberately not logged
                }
    
                if (client == null) {
                    // 再尝试 OIDCClientRepresentation
                    try {
                        oidcClient = JsonSerialization.readValue(content, OIDCClientRepresentation.class);
                        type = EndpointType.OIDC;
                    } catch (IOException ne) {
                        throw new RuntimeException("Unable to determine input document type. Use -e TYPE to specify the registration endpoint to use");
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to read the input document as JSON", e);
                    }
                }
    
            } else if (content.length() == 0) {
                throw new RuntimeException("Document provided by --file option is empty");
            } else {
                throw new RuntimeException("Unable to determine input document type. Use -e TYPE to specify the registration endpoint to use");
            }
        }
    
        // 校验内容类型；非 SAML XML 须能解析为 JSON
        if (content != null) {
            try {
                if (type == EndpointType.DEFAULT && client == null) {
                    client = JsonSerialization.readValue(content, ClientRepresentation.class);
                } else if (type == EndpointType.OIDC && oidcClient == null) {
                    oidcClient = JsonSerialization.readValue(content, OIDCClientRepresentation.class);
                }
            } catch (JsonParseException e) {
                throw new RuntimeException("Not a valid JSON document - " + e.getMessage(), e);
            } catch (UnrecognizedPropertyException e) {
                throw new RuntimeException("Attribute '" + e.getPropertyName() + "' not supported on document type '" + type.getName() + "'", e);
            } catch (IOException e) {
                throw new RuntimeException("Not a valid JSON document", e);
            }
        }
    
        CmdStdinContext ctx = new CmdStdinContext();
        ctx.setEndpointType(type);
        ctx.setContent(content);
        ctx.setClient(client);
        ctx.setOidcClient(oidcClient);
        return ctx;
    }

    /**
     * 将 {@code --set} 属性操作合并到上下文中的客户端表示。
     *
     * @param ctx 现有上下文
     * @param attrs 属性设置/追加/删除操作列表
     * @return 更新后的上下文（含新 JSON 内容与对象）
     */
    public static CmdStdinContext mergeAttributes(CmdStdinContext ctx, List<AttributeOperation> attrs) {
        String content = ctx.getContent();
        ClientRepresentation client = ctx.getClient();
        OIDCClientRepresentation oidcClient = ctx.getOidcClient();
        EndpointType type = ctx.getEndpointType();
        try {
            if (content == null) {
                if (type == EndpointType.DEFAULT) {
                    client = new ClientRepresentation();
                } else if (type == EndpointType.OIDC) {
                    oidcClient = new OIDCClientRepresentation();
                }
            }
            Object rep = client != null ? client : oidcClient;
            if (rep != null) {
                try {
                    setAttributes(rep, attrs);
                } catch (AttributeException e) {
                    throw new RuntimeException("Failed to set attribute '" + e.getAttributeName() + "' on document type '" + type.getName() + "'", e);
                }
                content = JsonSerialization.writeValueAsString(rep);
            } else {
                throw new RuntimeException("Setting attributes is not supported for type: " + type.getName());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to merge set attributes with configuration from file", e);
        }
    
        ctx.setContent(content);
        ctx.setClient(client);
        ctx.setOidcClient(oidcClient);
        return ctx;
    }
}
