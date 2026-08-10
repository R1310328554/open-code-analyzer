/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.services.resources.account;

import java.util.List;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.common.Profile;
import org.keycloak.models.AccountRoles;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.representations.idm.oid4vc.UserVerifiableCredentialRepresentation;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.cors.Cors;
import org.keycloak.services.managers.Auth;

import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.NoCache;

/**
 * 账户 REST API：可验证凭证（VC client scope）管理。
 * <p>列出与撤销用户关联的可验证凭证 scope。</p>
 */
public class AccountVerifiableCredentialResource {

    private static final Logger logger = Logger.getLogger(AccountVerifiableCredentialResource.class);

    private final KeycloakSession session;
    private final Auth auth;
    private final UserModel user;
    private final RealmModel realm;

    public AccountVerifiableCredentialResource(KeycloakSession session, Auth auth, UserModel user) {
        this.session = session;
        this.auth = auth;
        this.user = user;
        this.realm = auth.getRealm();
    }

    /**
     * 获取已认证用户的可验证凭证列表。
     *
     * @return 用户 VC 列表
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    public Response getCredentials() {
        auth.requireOneOf(AccountRoles.MANAGE_ACCOUNT, AccountRoles.VIEW_VERIFIABLE_CREDENTIALS, AccountRoles.MANAGE_VERIFIABLE_CREDENTIALS);
        checkOid4VCIEnabled();

        List<UserVerifiableCredentialRepresentation> credentials = session.users()
                .getVerifiableCredentialsByUser(user.getId())
                .map(model -> ModelToRepresentation.toRepresentation(model, realm))
                .peek(rep -> rep.setUserAttributes(null))  // 不向用户暴露属性快照
                .toList();

        return Cors.builder()
                .auth()
                .checkAllowedOrigins(auth.getToken())
                .add(Response.ok(credentials));
    }

    /**
     * 撤销已认证用户的指定可验证凭证。
     *
     * @param credentialScopeName 凭证 scope 名称
     * @return 成功时 204 No Content
     */
    @DELETE
    @Path("/{credentialScopeName}")
    public Response revokeCredential(@PathParam("credentialScopeName") String credentialScopeName) {
        auth.requireOneOf(AccountRoles.MANAGE_ACCOUNT, AccountRoles.MANAGE_VERIFIABLE_CREDENTIALS);
        checkOid4VCIEnabled();

        // 将 scope 名称解析为 ID
        ClientScopeModel clientScope = KeycloakModelUtils.getClientScopeByName(realm, credentialScopeName);
        if (clientScope == null) {
            throw new NotFoundException("Client scope not found: " + credentialScopeName);
        }

        boolean removed = session.users().removeVerifiableCredential(user.getId(), clientScope.getId());
        if (!removed) {
            logger.warn(String.format("Verifiable credential '%s' not found for user '%s' in the realm '%s'.",
                    credentialScopeName, user.getUsername(), realm.getName()));
            throw new NotFoundException("Verifiable credential not found");
        }

        return Cors.builder().auth().checkAllowedOrigins(auth.getToken()).add(Response.noContent());
    }

    /** 校验 OID4VC VCI 与领域 VC 是否启用 */
    private void checkOid4VCIEnabled() {
        if (!Profile.isFeatureEnabled(Profile.Feature.OID4VC_VCI)) {
            throw ErrorResponse.error("Feature " + Profile.Feature.OID4VC_VCI.getKey() + " not enabled", Response.Status.BAD_REQUEST);
        }
        if (!realm.isVerifiableCredentialsEnabled()) {
            throw ErrorResponse.error("Verifiable credentials not enabled for the realm", Response.Status.BAD_REQUEST);
        }
    }

}
