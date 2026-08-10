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
package org.keycloak.services.resources.admin;

import java.util.Map;
import java.util.stream.Stream;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.authorization.admin.AuthorizationService;
import org.keycloak.authorization.fgap.AdminPermissionsSchema;
import org.keycloak.client.clienttype.ClientTypeException;
import org.keycloak.common.Profile;
import org.keycloak.events.Errors;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.ModelException;
import org.keycloak.models.ModelValidationException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.authorization.ResourceServerRepresentation;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.ErrorResponseException;
import org.keycloak.services.clientpolicy.ClientPolicyException;
import org.keycloak.services.clientpolicy.context.AdminClientRegisterContext;
import org.keycloak.services.clientpolicy.context.AdminClientRegisteredContext;
import org.keycloak.services.managers.ClientManager;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.services.resources.KeycloakOpenAPI;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;
import org.keycloak.utils.SearchQueryUtils;
import org.keycloak.validation.ValidationUtil;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.NoCache;

import static java.lang.Boolean.TRUE;

import static org.keycloak.utils.StreamsUtil.paginatedStream;

/**
 * 领域客户端集合 REST 资源。
 * <p>列出、搜索、创建客户端，并路由到单个客户端子资源。</p>
 *
 * @resource Clients
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@Extension(name = KeycloakOpenAPI.Profiles.ADMIN, value = "")
public class ClientsResource {
    /** 日志记录器 */
    protected static final Logger logger = Logger.getLogger(ClientsResource.class);
    /** 当前领域 */
    protected final RealmModel realm;
    /** 细粒度权限评估器 */
    private final AdminPermissionEvaluator auth;
    /** 管理事件构建器 */
    private final AdminEventBuilder adminEvent;

    /** Keycloak 会话 */
    protected final KeycloakSession session;

    /** 构造客户端集合资源。
     * @param session Keycloak 会话
     * @param auth 权限评估器
     * @param adminEvent 管理事件构建器
     */
    public ClientsResource(KeycloakSession session, AdminPermissionEvaluator auth, AdminEventBuilder adminEvent) {
        this.session = session;
        this.realm = session.getContext().getRealm();
        this.auth = auth;
        this.adminEvent = adminEvent.resource(ResourceType.CLIENT);

    }

    /**
     * 获取领域客户端列表（存储异常客户端静默过滤）。
     * @param clientId 按 clientId 过滤
     * @param viewableOnly 仅返回管理员可完整查看的客户端
     * @param search 是否为搜索模式
     * @param firstResult 分页偏移
     * @param maxResults 最大返回数
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.CLIENTS)
    @Operation( summary = "Get clients belonging to the realm.",
        description = "If a client can’t be retrieved from the storage due to a problem with the underlying storage, it is silently removed from the returned list. This ensures that concurrent modifications to the list don’t prevent callers from retrieving this list.")
    public Stream<ClientRepresentation> getClients(@Parameter(description = "filter by clientId") @QueryParam("clientId") String clientId,
                                                 @Parameter(description = "filter clients that cannot be viewed in full by admin") @QueryParam("viewableOnly") @DefaultValue("false") boolean viewableOnly,
                                                 @Parameter(description = "whether this is a search query or a getClientById query") @QueryParam("search") @DefaultValue("false") boolean search,
                                                 @QueryParam("q") String searchQuery,
                                                 @Parameter(description = "the first result") @QueryParam("first") Integer firstResult,
                                                 @Parameter(description = "the max results to return") @QueryParam("max") Integer maxResults) {
        return ModelToRepresentation.filterValidRepresentations(
                getClientModels(clientId, viewableOnly, search, searchQuery, firstResult, maxResults), c -> {
                    ClientRepresentation representation = ModelToRepresentation.toRepresentation(c, session);
                    representation.setAccess(auth.clients().getAccess(c));
                    return representation;
                });
    }

    /** 内部：按条件查询客户端模型流 */
    public Stream<ClientModel> getClientModels(String clientId,
            boolean viewableOnly,
            boolean search,
            String searchQuery,
            Integer firstResult,
            Integer maxResults) {
        auth.clients().requireList();

        boolean canView = AdminPermissionsSchema.SCHEMA.isAdminPermissionsEnabled(realm) || auth.clients().canView();
        Stream<ClientModel> clientModels = Stream.empty();
        try {
            if (searchQuery != null) {
                Map<String, String> attributes = SearchQueryUtils.getFields(searchQuery);
                clientModels = canView
                        ? realm.searchClientByAttributes(attributes, firstResult, maxResults)
                        : realm.searchClientByAttributes(attributes, -1, -1);
            } else if (clientId == null || clientId.trim().equals("")) {
                clientModels = canView
                        ? realm.getClientsStream(firstResult, maxResults)
                        : realm.getClientsStream();
            } else if (search) {
                clientModels = canView
                        ? realm.searchClientByClientIdStream(clientId, firstResult, maxResults)
                        : realm.searchClientByClientIdStream(clientId, -1, -1);
            } else {
                ClientModel client = realm.getClientByClientId(clientId);
                if (client != null) {
                    if (AdminPermissionsSchema.SCHEMA.isAdminPermissionsEnabled(realm)) {
                        clientModels = Stream.of(client).filter(auth.clients()::canView);
                    } else {
                        clientModels = Stream.of(client);
                    }
                }
            }
        }
        catch (ModelException e) {
            throw new ErrorResponseException(Errors.INVALID_REQUEST, e.getMessage(), Response.Status.BAD_REQUEST);
        }

        Stream<ClientModel> s = clientModels.filter(m -> canView || auth.clients().canView(m));

        if (!canView) {
            s = paginatedStream(s, firstResult, maxResults);
        }

        return s;
    }

    /** 获取客户端授权服务子资源 */
    private AuthorizationService getAuthorizationService(ClientModel clientModel) {
        return new AuthorizationService(session, clientModel, auth, adminEvent);
    }

    /**
     * 创建新客户端（client_id 须唯一）。
     * @param rep 客户端表示
     * @return 201 Created
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.CLIENTS)
    @Operation( summary = "Create a new client Client’s client_id must be unique!")
    @APIResponses(value = {
        @APIResponse(responseCode = "201", description = "Created"),
        @APIResponse(responseCode = "409", description = "Conflict")
    })
    public Response createClient(final ClientRepresentation rep) {
        var created = createClientModel(rep);
        return Response.created(session.getContext().getUri().getAbsolutePathBuilder().path(created.getId()).build()).build();
    }

    /** 创建客户端模型（含服务账号、授权服务、客户端策略与校验）。
     * @param rep 客户端表示
     * @return 已创建客户端模型
     */
    public ClientModel createClientModel(final ClientRepresentation rep) {
        auth.clients().requireManage();

        try {
            session.clientPolicy().triggerOnEvent(new AdminClientRegisterContext(rep, auth.adminAuth()));

            ClientModel clientModel = ClientManager.createClient(session, realm, rep);

            if (TRUE.equals(rep.isServiceAccountsEnabled())) {
                new ClientManager(new RealmManager(session)).enableServiceAccount(clientModel);
            }

            adminEvent.operation(OperationType.CREATE).resourcePath(session.getContext().getUri(), clientModel.getId()).representation(rep).success();

            if (Profile.isFeatureEnabled(Profile.Feature.AUTHORIZATION) && TRUE.equals(rep.getAuthorizationServicesEnabled())) {
                AuthorizationService authorizationService = getAuthorizationService(clientModel);

                authorizationService.enable(true);

                ResourceServerRepresentation authorizationSettings = rep.getAuthorizationSettings();

                if (authorizationSettings != null) {
                    authorizationService.getResourceServerService().importSettings(authorizationSettings);
                }
            }

            ValidationUtil.validateClient(session, clientModel, true, r -> {
                session.getTransactionManager().setRollbackOnly();
                throw new ErrorResponseException(
                        Errors.INVALID_INPUT,
                        r.getAllLocalizedErrorsAsString(AdminRoot.getMessages(session, realm, auth.adminAuth().getToken().getLocale())),
                        Response.Status.BAD_REQUEST);
            });

            session.getContext().setClient(clientModel);
            session.clientPolicy().triggerOnEvent(new AdminClientRegisteredContext(clientModel, auth.adminAuth()));

            return clientModel;
        } catch (ModelDuplicateException e) {
            throw ErrorResponse.exists("Client " + rep.getClientId() + " already exists");
        } catch (ClientPolicyException cpe) {
            throw new ErrorResponseException(cpe.getError(), cpe.getErrorDetail(), Response.Status.BAD_REQUEST);
        } catch (ModelValidationException e) {
            throw new ErrorResponseException(Errors.INVALID_INPUT, e.getMessage(), Response.Status.BAD_REQUEST);
        }
        catch (ClientTypeException cte) {
            throw ErrorResponse.error(cte.getMessage(), cte.getParameters(), Response.Status.BAD_REQUEST);
        }
    }

    /**
     * 获取单个客户端子资源。
     * @param id 客户端 UUID（非 client-id）
     * @return {@link ClientResource}
     */
    @Path("{client-uuid}")
    public ClientResource getClient(final @PathParam("client-uuid") @Parameter(description = "id of client (not client-id!)") String id) {

        ClientModel clientModel = realm.getClientById(id);
        if (clientModel == null) {
            // 防止通过 ID 探测客户端是否存在
            if (auth.clients().canList()) throw new NotFoundException("Could not find client");
            else throw new ForbiddenException();
        }

        session.getContext().setClient(clientModel);

        return new ClientResource(realm, auth, clientModel, session, adminEvent);
    }

}
