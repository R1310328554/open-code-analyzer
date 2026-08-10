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

package org.keycloak.protocol;

import java.net.URI;

import jakarta.ws.rs.core.Response;

import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;

/**
 * 客户端安装配置提供者：生成适配器示例/模板文件。
 * <p>例如 OIDC 适配器的 {@code keycloak.json}、SAML 适配器的 {@code keycloak-saml.xml}。</p>
 * <p>Provides a template/sample client config adapter file.  For example keycloak.json for our OIDC adapter.  keycloak-saml.xml for our SAML client adapter</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface ClientInstallationProvider extends Provider, ProviderFactory<ClientInstallationProvider> {
    /** 生成客户端安装配置 HTTP 响应。 */
    Response generateInstallation(KeycloakSession session, RealmModel realm, ClientModel client, URI serverBaseUri);
    /** @return 关联的登录协议 ID */
    String getProtocol();
    /** @return 管理控制台显示类型名称 */
    String getDisplayType();
    /** @return 安装说明帮助文本 */
    String getHelpText();
    /** @return 下载文件名 */
    String getFilename();
    /** @return 响应 Content-Type */
    String getMediaType();
    /** @return 是否仅提供下载（不可在线预览） */
    boolean isDownloadOnly();
}
