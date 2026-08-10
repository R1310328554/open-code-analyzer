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
package org.keycloak.services.clientregistration.oidc;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.common.util.Time;
import org.keycloak.events.EventType;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientSecretConstants;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.models.utils.RepresentationToModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.mappers.AbstractPairwiseSubMapper;
import org.keycloak.protocol.oidc.mappers.PairwiseSubMapperHelper;
import org.keycloak.protocol.oidc.mappers.SHA256PairwiseSubMapper;
import org.keycloak.protocol.oidc.utils.SubjectType;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.ProtocolMapperRepresentation;
import org.keycloak.representations.oidc.OIDCClientRepresentation;
import org.keycloak.services.ErrorResponseException;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.Urls;
import org.keycloak.services.clientregistration.AbstractClientRegistrationProvider;
import org.keycloak.services.clientregistration.ClientRegistrationException;
import org.keycloak.services.clientregistration.ErrorCodes;
import org.keycloak.services.cors.Cors;
import org.keycloak.urls.UrlType;

/**
 * OpenID Connect 动态客户端注册 REST 端点。
 * <p>接受 {@link OIDCClientRepresentation} JSON，实现 OIDC 规范的客户端注册、查询、更新与删除，并处理 CORS、成对 subject mapper 及协议映射器同步。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class OIDCClientRegistrationProvider extends AbstractClientRegistrationProvider {

    /** @param session Keycloak 会话 */
    public OIDCClientRegistrationProvider(KeycloakSession session) {
        super(session);
    }

    @OPTIONS
    /** CORS 预检请求处理（集合端点） */
    public Response preflight() {
        return Cors.builder().auth().preflight().allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS").add(Response.ok());
    }

    @OPTIONS
    @Path("{clientId}")
    /** CORS 预检请求处理（单客户端端点） */
    public Response preflightClient() {
        return preflight();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    /** 注册新 OIDC 客户端并返回含注册访问令牌的 201 响应 */
    public Response createOIDC(OIDCClientRepresentation clientOIDC) {
        event.event(EventType.CLIENT_REGISTER);
        Cors cors = cors();
        if (clientOIDC.getClientId() != null) {
            throw new ErrorResponseException(ErrorCodes.INVALID_CLIENT_METADATA, "Client Identifier included", Response.Status.BAD_REQUEST);
        }

        try {
            ClientRepresentation client = DescriptionConverter.toInternal(session, clientOIDC);

            OIDCClientRegistrationContext oidcContext = new OIDCClientRegistrationContext(session, client, this, clientOIDC);
            client = create(oidcContext);

            ClientModel clientModel = session.getContext().getRealm().getClientByClientId(client.getClientId());
            updatePairwiseSubMappers(clientModel, SubjectType.parse(clientOIDC.getSubjectType()), clientOIDC.getSectorIdentifierUri());
            updateClientRepWithProtocolMappers(clientModel, client);

            validateClient(clientModel, clientOIDC, true);

            URI uri = getRegistrationClientUri(clientModel);
            clientOIDC = DescriptionConverter.toExternalResponse(session, client, uri);
            clientOIDC.setClientIdIssuedAt(Time.currentTime());
            return cors.add(Response.created(uri).entity(clientOIDC));
        } catch (ClientRegistrationException cre) {
            ServicesLogger.LOGGER.clientRegistrationException(cre.getMessage());
            throw new ErrorResponseException(ErrorCodes.INVALID_CLIENT_METADATA, "Client metadata invalid", Response.Status.BAD_REQUEST);
        }
    }

    @GET
    @Path("{clientId}")
    @Produces(MediaType.APPLICATION_JSON)
    /** 查询指定 OIDC 客户端元数据 */
    public Response getOIDC(@PathParam("clientId") String clientId) {
        event.event(EventType.CLIENT_INFO);
        Cors cors = cors();
        ClientModel client = session.getContext().getRealm().getClientByClientId(clientId);

        ClientRepresentation clientRepresentation = get(client);

        OIDCClientRepresentation clientOIDC = DescriptionConverter.toExternalResponse(session, clientRepresentation, getRegistrationClientUri(client));
        return cors.add(Response.ok(clientOIDC));
    }

    @PUT
    @Path("{clientId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    /** 更新指定 OIDC 客户端配置 */
    public Response updateOIDC(@PathParam("clientId") String clientId, OIDCClientRepresentation clientOIDC) {
        event.event(EventType.CLIENT_UPDATE);
        Cors cors = cors();
        try {
            ClientRepresentation client = DescriptionConverter.toInternal(session, clientOIDC);

            if (clientOIDC.getScope() != null) {
                ClientModel oldClient = session.getContext().getRealm().getClientById(clientOIDC.getClientId());
                Collection<String> defaultClientScopes = oldClient.getClientScopes(true).keySet();
                client.setDefaultClientScopes(new ArrayList<>(defaultClientScopes));
            }

            OIDCClientRegistrationContext oidcContext = new OIDCClientRegistrationContext(session, client, this, clientOIDC);
            client = update(clientId, oidcContext);

            ClientModel clientModel = session.getContext().getRealm().getClientByClientId(client.getClientId());
            updatePairwiseSubMappers(clientModel, SubjectType.parse(clientOIDC.getSubjectType()), clientOIDC.getSectorIdentifierUri());
            updateClientRepWithProtocolMappers(clientModel, client);

            client.setSecret(clientModel.getSecret());
            client.getAttributes().put(ClientSecretConstants.CLIENT_SECRET_EXPIRATION,clientModel.getAttribute(ClientSecretConstants.CLIENT_SECRET_EXPIRATION));
            client.getAttributes().put(ClientSecretConstants.CLIENT_SECRET_CREATION_TIME,clientModel.getAttribute(ClientSecretConstants.CLIENT_SECRET_CREATION_TIME));

            validateClient(clientModel, clientOIDC, false);

            URI uri = getRegistrationClientUri(clientModel);
            clientOIDC = DescriptionConverter.toExternalResponse(session, client, uri);
            return cors.add(Response.ok(clientOIDC));
        } catch (ClientRegistrationException cre) {
            ServicesLogger.LOGGER.clientRegistrationException(cre.getMessage());
            throw new ErrorResponseException(ErrorCodes.INVALID_CLIENT_METADATA, "Client metadata invalid", Response.Status.BAD_REQUEST);
        }
    }

    @DELETE
    @Path("{clientId}")
    /** 删除指定 OIDC 客户端 */
    public Response deleteOIDC(@PathParam("clientId") String clientId) {
        event.event(EventType.CLIENT_DELETE);
        Cors cors = cors();
        delete(clientId);
        return cors.add(Response.noContent());
    }

    /** 构建带认证与来源校验的 CORS 处理器 */
    private Cors cors() {
        return Cors.builder().auth().checkAllowedOrigins(getAllowedOrigins());
    }

    /** 根据 subject_type 创建、更新或移除成对 subject 协议映射器 */
    private void updatePairwiseSubMappers(ClientModel clientModel, SubjectType subjectType, String sectorIdentifierUri) {
        if (subjectType == SubjectType.PAIRWISE) {

            // 查找已有成对 mapper 并更新，否则新建
            AtomicBoolean foundPairwise = new AtomicBoolean(false);

            clientModel.getProtocolMappersStream().filter((ProtocolMapperModel mapping) -> {
                if (mapping.getProtocolMapper().endsWith(AbstractPairwiseSubMapper.PROVIDER_ID_SUFFIX)) {
                    foundPairwise.set(true);
                    return true;
                } else {
                    return false;
                }
            }).toList().forEach((ProtocolMapperModel mapping) -> {
                PairwiseSubMapperHelper.setSectorIdentifierUri(mapping, sectorIdentifierUri);
                clientModel.updateProtocolMapper(mapping);
            });

            // 无现有成对 mapper 时创建新的
            if (!foundPairwise.get()) {
                ProtocolMapperRepresentation newPairwise = SHA256PairwiseSubMapper.createPairwiseMapper(sectorIdentifierUri, null);
                clientModel.addProtocolMapper(RepresentationToModel.toModel(newPairwise));
            }

        } else {
            // 非成对模式：移除所有成对 subject mapper
            clientModel.getProtocolMappersStream()
                    .filter(mapperRep -> mapperRep.getProtocolMapper().endsWith(AbstractPairwiseSubMapper.PROVIDER_ID_SUFFIX))
                    .toList()
                    .forEach(clientModel::removeProtocolMapper);
        }
    }

    /** 将客户端协议映射器同步到表示对象 */
    private void updateClientRepWithProtocolMappers(ClientModel clientModel, ClientRepresentation rep) {
        List<ProtocolMapperRepresentation> mappings =
                clientModel.getProtocolMappersStream().map(ModelToRepresentation::toRepresentation).collect(Collectors.toList());
        rep.setProtocolMappers(mappings);
    }

    /** @return 该客户端的 registration_client_uri */
    private URI getRegistrationClientUri(ClientModel client) {
        KeycloakContext context = session.getContext();
        RealmModel realm = context.getRealm();
        URI backendUri = context.getUri(UrlType.BACKEND).getBaseUri();
        return Urls.clientRegistration(backendUri, realm.getName(), OIDCLoginProtocol.LOGIN_PROTOCOL, client.getClientId());
    }
}
