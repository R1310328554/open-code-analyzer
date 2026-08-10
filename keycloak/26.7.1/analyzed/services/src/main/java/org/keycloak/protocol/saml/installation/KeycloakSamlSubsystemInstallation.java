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

package org.keycloak.protocol.saml.installation;

import java.net.URI;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.Config;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.protocol.ClientInstallationProvider;
import org.keycloak.protocol.saml.SamlClient;
import org.keycloak.protocol.saml.SamlProtocol;

/**
 * Keycloak SAML JBoss 子系统 XML 安装提供者：生成 standalone.xml 中 keycloak-saml 子系统片段。
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class KeycloakSamlSubsystemInstallation implements ClientInstallationProvider {

    /** {@inheritDoc} 生成 secure-deployment XML 片段 */
    @Override
    public Response generateInstallation(KeycloakSession session, RealmModel realm, ClientModel client, URI baseUri) {
        SamlClient samlClient = new SamlClient(client);
        StringBuilder buffer = new StringBuilder();
        buffer.append("<secure-deployment name=\"YOUR-WAR.war\">\n");
        KeycloakSamlClientInstallation.baseXml(session, realm, client, baseUri, samlClient, buffer);
        buffer.append("</secure-deployment>\n");
        return Response.ok(buffer.toString(), MediaType.TEXT_PLAIN_TYPE).build();
    }

    @Override
    public String getProtocol() {
        return SamlProtocol.LOGIN_PROTOCOL;
    }

    /** {@inheritDoc} 控制台显示名：Keycloak SAML JBoss Subsystem XML */
    @Override
    public String getDisplayType() {
        return "Keycloak SAML JBoss Subsystem XML";
    }

    /** {@inheritDoc} 说明需合并到 standalone.xml 的 keycloak-saml 子系统 */
    @Override
    public String getHelpText() {
        return "Keycloak SAML adapter JBoss subsystem xml you must edit. Put this into <subsystem xmlns=\"urn:jboss:domain:keycloak-saml:1.2\"> element of your standalone.xml file.";
    }

    /** {@inheritDoc} 输出文件名：keycloak-saml-subsystem.xml */
    @Override
    public String getFilename() {
        return "keycloak-saml-subsystem.xml";
    }

    @Override
    public String getMediaType() {
        return MediaType.APPLICATION_XML;
    }

    @Override
    public boolean isDownloadOnly() {
        return false;
    }

    @Override
    public void close() {

    }

    @Override
    public ClientInstallationProvider create(KeycloakSession session) {
        return this;
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    /** {@inheritDoc} SPI ID：keycloak-saml-subsystem */
    @Override
    public String getId() {
        return "keycloak-saml-subsystem";
    }
}
