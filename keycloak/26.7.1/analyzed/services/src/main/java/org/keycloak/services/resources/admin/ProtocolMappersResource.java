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

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Stream;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.ProtocolMapperContainerModel;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.models.utils.RepresentationToModel;
import org.keycloak.protocol.ProtocolMapper;
import org.keycloak.protocol.ProtocolMapperConfigException;
import org.keycloak.representations.idm.ProtocolMapperRepresentation;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.ErrorResponseException;
import org.keycloak.services.resources.KeycloakOpenAPI;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.NoCache;

import static org.keycloak.protocol.ProtocolMapperUtils.isEnabled;

/**
 * 协议映射器（Protocol Mapper）管理 REST 资源基类。
 * <p>管理客户端或客户端范围上的 OIDC/SAML 协议映射器 CRUD。</p>
 *
 * @resource Protocol Mappers
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@Extension(name = KeycloakOpenAPI.Profiles.ADMIN, value = "")
public class ProtocolMappersResource {
    /** 日志记录器 */
    protected static final Logger logger = Logger.getLogger(ProtocolMappersResource.class);

    /** 当前领域 */
    protected final RealmModel realm;

    /** 协议映射器容器（客户端或客户端范围） */
    protected final ProtocolMapperContainerModel client;

    /** 细粒度权限评估器 */
    protected final AdminPermissionEvaluator auth;
    /** 管理权限检查回调 */
    protected final AdminPermissionEvaluator.RequirePermissionCheck managePermission;
    /** 查看权限检查回调 */
    protected final AdminPermissionEvaluator.RequirePermissionCheck viewPermission;

    /** 管理事件构建器 */
    protected final AdminEventBuilder adminEvent;

    /** Keycloak 会话 */
    protected final KeycloakSession session;

    /** 构造协议映射器资源。
     * @param session Keycloak 会话
     * @param client 映射器容器
     * @param auth 权限评估器
     * @param adminEvent 管理事件构建器
     * @param managePermission 管理权限检查
     * @param viewPermission 查看权限检查
     */
    public ProtocolMappersResource(KeycloakSession session, ProtocolMapperContainerModel client, AdminPermissionEvaluator auth,
                                   AdminEventBuilder adminEvent,
                                   AdminPermissionEvaluator.RequirePermissionCheck managePermission,
                                   AdminPermissionEvaluator.RequirePermissionCheck viewPermission) {
        this.session = session;
        this.realm = session.getContext().getRealm();
        this.auth = auth;
        this.client = client;
        this.adminEvent = adminEvent.resource(ResourceType.PROTOCOL_MAPPER);
        this.managePermission = managePermission;
        this.viewPermission = viewPermission;

    }

    /**
     * 按协议类型获取启用的协议映射器列表。
     * @param protocol 协议名称
     * @return 映射器表示流
     */
    @GET
    @NoCache
    @Path("protocol/{protocol}")
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.PROTOCOL_MAPPERS)
    @Operation(summary = "Get mappers by name for a specific protocol")
    public Stream<ProtocolMapperRepresentation> getMappersPerProtocol(@PathParam("protocol") String protocol) {
        viewPermission.require();

        return client.getProtocolMappersStream()
                .filter(mapper -> isEnabled(session, mapper) && Objects.equals(mapper.getProtocol(), protocol))
                .map(this::toEffectiveProtocolMapperRep);
    }

    /**
     * 创建单个协议映射器。
     * @param rep 映射器表示
     */
    @Path("models")
    @POST
    @NoCache
    @Consumes(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.PROTOCOL_MAPPERS)
    @Operation(summary = "Create a mapper")
    @APIResponses(value = {
        @APIResponse(responseCode = "201", description = "Created"),
        @APIResponse(responseCode = "409", description = "Conflict")
    })
    public Response createMapper(ProtocolMapperRepresentation rep) {
        managePermission.require();

        ProtocolMapperModel model = null;
        try {
            model = RepresentationToModel.toModel(rep);
            validateModel(model);
            model = client.addProtocolMapper(model);
            adminEvent.operation(OperationType.CREATE).resourcePath(session.getContext().getUri(), model.getId()).representation(rep).success();

        } catch (ModelDuplicateException e) {
            throw ErrorResponse.exists("Protocol mapper exists with same name");
        }

        return Response.created(session.getContext().getUri().getAbsolutePathBuilder().path(model.getId()).build()).build();
    }
    /** 批量创建协议映射器 */
    /**
    @Path("add-models")
    @POST
    @NoCache
    @Consumes(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.PROTOCOL_MAPPERS)
    @Operation(summary = "Create multiple mappers")
    @APIResponse(responseCode = "204", description = "No Content")
    public void createMapper(List<ProtocolMapperRepresentation> reps) {
        managePermission.require();

        ProtocolMapperModel model = null;
        for (ProtocolMapperRepresentation rep : reps) {
            model = RepresentationToModel.toModel(rep);
            validateModel(model);
            model = client.addProtocolMapper(model);
        }
        adminEvent.operation(OperationType.CREATE).resourcePath(session.getContext().getUri()).representation(reps).success();
    }

    /**
     * 获取所有启用的协议映射器。
     * @return 映射器表示流
     */
    @GET
    @NoCache
    @Path("models")
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.PROTOCOL_MAPPERS)
    @Operation(summary = "Get mappers")
    public Stream<ProtocolMapperRepresentation> getMappers() {
        viewPermission.require();

        return client.getProtocolMappersStream()
                .filter(mapper -> isEnabled(session, mapper))
                .map(this::toEffectiveProtocolMapperRep);
    }

    /**
     * 按 ID 获取协议映射器（含有效配置）。
     * @param id 映射器 ID
     * @return 映射器表示
     */
    @GET
    @NoCache
    @Path("models/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.PROTOCOL_MAPPERS)
    @Operation(summary = "Get mapper by id")
    public ProtocolMapperRepresentation getMapperById(@Parameter(description = "Mapper id") @PathParam("id") String id) {
        viewPermission.require();

        ProtocolMapperModel model = client.getProtocolMapperById(id);
        if (model == null) throw new NotFoundException("Model not found");
        return toEffectiveProtocolMapperRep(model);
    }

    /** 解析映射器提供者并返回有效表示 */
    private ProtocolMapperRepresentation toEffectiveProtocolMapperRep(ProtocolMapperModel model) {
        ProtocolMapper mapper = (ProtocolMapper) session.getKeycloakSessionFactory().getProviderFactory(ProtocolMapper.class, model.getProtocolMapper());
        if (mapper == null) {
            logger.warnf("Protocol mapper provider '%s' not found. Configured on mapper with ID '%s'", model.getProtocolMapper(), model.getId());
            throw new NotFoundException("Protocol mapper provider not found");
        }

        model = mapper.getEffectiveModel(session, realm, model);
        return ModelToRepresentation.toRepresentation(model);
    }

    /**
     * 更新协议映射器。
     * @param id 映射器 ID
     * @param rep 新配置
     */
    @PUT
    @NoCache
    @Path("models/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.PROTOCOL_MAPPERS)
    @Operation(summary = "Update the mapper")
    public void update(@Parameter(description = "Mapper id") @PathParam("id") String id, ProtocolMapperRepresentation rep) {
        managePermission.require();

        ProtocolMapperModel model = client.getProtocolMapperById(id);
        if (model == null) throw new NotFoundException("Model not found");
        model = RepresentationToModel.toModel(rep);

        validateModel(model);

        client.updateProtocolMapper(model);
        adminEvent.operation(OperationType.UPDATE).resourcePath(session.getContext().getUri()).representation(rep).success();
    }

    /**
     * 删除协议映射器。
     * @param id 映射器 ID
     */
    @DELETE
    @NoCache
    @Path("models/{id}")
    @Tag(name = KeycloakOpenAPI.Admin.Tags.PROTOCOL_MAPPERS)
    @Operation(summary = "Delete the mapper")
    public void delete(@Parameter(description = "Mapper id") @PathParam("id") String id) {
        managePermission.require();

        ProtocolMapperModel model = client.getProtocolMapperById(id);
        if (model == null) throw new NotFoundException("Model not found");
        client.removeProtocolMapper(model);
        adminEvent.operation(OperationType.DELETE).resourcePath(session.getContext().getUri()).success();

    }

    /** 调用映射器提供者校验配置 */
    private void validateModel(ProtocolMapperModel model) {
        try {
            ProtocolMapper mapper = (ProtocolMapper)session.getKeycloakSessionFactory().getProviderFactory(ProtocolMapper.class, model.getProtocolMapper());
            if (mapper != null) {
                mapper.validateConfig(session, realm, client, model);
            } else {
                throw new NotFoundException("ProtocolMapper provider not found");
            }
        } catch (ProtocolMapperConfigException ex) {
            logger.error(ex.getMessage());
            Properties messages = AdminRoot.getMessages(session, realm, auth.adminAuth().getToken().getLocale());
            throw new ErrorResponseException(ex.getMessage(), MessageFormat.format(messages.getProperty(ex.getMessageKey(), ex.getMessage()), ex.getParameters()),
                    Response.Status.BAD_REQUEST);
        }
    }

}
