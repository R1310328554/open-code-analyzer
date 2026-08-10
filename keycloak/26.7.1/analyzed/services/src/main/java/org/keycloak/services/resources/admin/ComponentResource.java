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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Stream;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.common.ClientConnection;
import org.keycloak.component.ComponentFactory;
import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.component.SubComponentFactory;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.models.utils.RepresentationToModel;
import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.representations.idm.ComponentRepresentation;
import org.keycloak.representations.idm.ComponentTypeRepresentation;
import org.keycloak.representations.idm.ConfigPropertyRepresentation;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.resources.KeycloakOpenAPI;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.NoCache;

/**
 * 领域组件（Component）管理 REST 资源。
 * <p>管理用户联邦、密钥提供者等 SPI 组件的 CRUD 及子组件类型查询。</p>
 *
 * @resource Component
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@Extension(name = KeycloakOpenAPI.Profiles.ADMIN, value = "")
public class ComponentResource {
    /** 日志记录器 */
    protected static final Logger logger = Logger.getLogger(ComponentResource.class);

    /** 当前领域 */
    protected final RealmModel realm;

    /** 细粒度权限评估器 */
    private final AdminPermissionEvaluator auth;

    /** 管理事件构建器 */
    private final AdminEventBuilder adminEvent;

    /** 客户端连接信息 */
    protected final ClientConnection clientConnection;

    /** Keycloak 会话 */
    protected final KeycloakSession session;

    /** HTTP 请求头 */
    protected final HttpHeaders headers;

    /** 构造组件资源。
     * @param session Keycloak 会话
     * @param auth 权限评估器
     * @param adminEvent 管理事件构建器
     */
    public ComponentResource(KeycloakSession session, AdminPermissionEvaluator auth, AdminEventBuilder adminEvent) {
        this.session = session;
        this.auth = auth;
        this.realm = session.getContext().getRealm();
        this.adminEvent = adminEvent.resource(ResourceType.COMPONENT);
        this.clientConnection = session.getContext().getConnection();
        this.headers = session.getContext().getRequestHeaders();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.COMPONENT)
    @Operation()
    /**
     * 按父 ID、类型、名称或 providerId 过滤列出组件。
     * @param parent 父组件 ID
     * @param type 提供者类型
     * @param name 组件名称
     * @param providerId 提供者 ID
     * @return 组件表示流
     */
    public Stream<ComponentRepresentation> getComponents(@QueryParam("parent") String parent,
                                                       @QueryParam("type") String type,
                                                       @QueryParam("name") String name,
                                                       @QueryParam("providerId") String providerId) {
        auth.realm().requireViewRealm();
        Stream<ComponentModel> components;
        if (parent == null && type == null) {
            components = realm.getComponentsStream();

        } else if (type == null) {
            components = realm.getComponentsStream(parent);
        } else if (parent == null) {
            components = realm.getComponentsStream(realm.getId(), type);
        } else {
            components = realm.getComponentsStream(parent, type);
        }

        return components
                .filter(component -> !isInternalComponent(component.getProviderType(), component.getProviderId()))
                .filter(component -> Objects.isNull(name) || Objects.equals(component.getName(), name))
                .filter(component -> Objects.isNull(providerId) || Objects.equals(component.getProviderId(), providerId))
                .map(component -> {
                    try {
                        return ModelToRepresentation.toRepresentation(session, component, false);
                    } catch (Exception e) {
                        logger.error("Failed to get component list for component model " + component.getName() + " of realm " + realm.getName());
                        return ModelToRepresentation.toRepresentationWithoutConfig(component);
                    }
                });
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.COMPONENT)
    @Operation()
    /** 创建新组件。
     * @param rep 组件表示
     * @return 201 Created
     */
    public Response create(ComponentRepresentation rep) {
        auth.realm().requireManageRealm();
        try {
            rejectInternalComponent(rep.getProviderType(), rep.getProviderId());
            ComponentModel model = RepresentationToModel.toModel(session, rep);
            if (model.getParentId() == null) model.setParentId(realm.getId());

            model = realm.addComponentModel(model);

            adminEvent.operation(OperationType.CREATE).resourcePath(session.getContext().getUri(), model.getId()).representation(rep).success();
            return Response.created(session.getContext().getUri().getAbsolutePathBuilder().path(model.getId()).build()).build();
        } catch (ComponentValidationException e) {
            return localizedErrorResponse(e);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid provider type or no such provider", e);
        }
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.COMPONENT)
    @Operation()
    /** 按 ID 获取组件详情。
     * @param id 组件 ID
     * @return 组件表示
     */
    public ComponentRepresentation getComponent(@PathParam("id") String id) {
        auth.realm().requireViewRealm();
        ComponentModel model = realm.getComponent(id);
        if (model == null || isInternalComponent(model.getProviderType(), model.getProviderId())) {
            throw new NotFoundException("Could not find component");
        }
        ComponentRepresentation rep = ModelToRepresentation.toRepresentation(session, model, false);
        return rep;
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.COMPONENT)
    @Operation()
    /** 更新组件配置。
     * @param id 组件 ID
     * @param rep 新配置
     * @return 204 No Content
     */
    public Response updateComponent(@PathParam("id") String id, ComponentRepresentation rep) {
        auth.realm().requireManageRealm();
        try {
            ComponentModel model = realm.getComponent(id);
            if (model == null) {
                throw new NotFoundException("Could not find component");
            }
            rejectInternalComponent(model.getProviderType(), model.getProviderId());
            RepresentationToModel.updateComponent(session, rep, model, false);
            adminEvent.operation(OperationType.UPDATE).resourcePath(session.getContext().getUri()).representation(rep).success();
            realm.updateComponent(model);
            return Response.noContent().build();
        } catch (ComponentValidationException e) {
            return localizedErrorResponse(e);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid provider type or no such provider", e);
        }
    }
    @DELETE
    @Path("{id}")
    @Tag(name = KeycloakOpenAPI.Admin.Tags.COMPONENT)
    @Operation()
    /** 删除组件。
     * @param id 组件 ID
     */
    public void removeComponent(@PathParam("id") String id) {
        auth.realm().requireManageRealm();
        ComponentModel model = realm.getComponent(id);
        if (model == null) {
            throw new NotFoundException("Could not find component");
        }
        rejectInternalComponent(model.getProviderType(), model.getProviderId());
        adminEvent.operation(OperationType.DELETE).resourcePath(session.getContext().getUri()).success();
        realm.removeComponent(model);
    }

    /** 将组件校验异常本地化为 BAD_REQUEST 错误响应 */
    private Response localizedErrorResponse(ComponentValidationException cve) {
        Properties messages = AdminRoot.getMessages(session, realm, auth.adminAuth().getToken().getLocale(), "admin-messages", "messages");

        Object[] localizedParameters = cve.getParameters()==null ? null : Arrays.asList(cve.getParameters()).stream().map((Object parameter) -> {

            if (parameter instanceof String) {
                String paramStr = (String) parameter;
                return messages.getProperty(paramStr, paramStr);
            } else {
                return parameter;
            }

        }).toArray();

        String message = MessageFormat.format(messages.getProperty(cve.getMessage(), cve.getMessage()), localizedParameters);
        throw ErrorResponse.error(message, Response.Status.BAD_REQUEST);
    }

    /**
     * 列出指定父组件下可配置的子组件类型。
     * @param parentId 父组件 ID
     * @param subtype 子组件提供者类名
     * @return 组件类型表示流
     */
    @GET
    @Path("{id}/sub-component-types")
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.COMPONENT)
    @Operation( summary = "List of subcomponent types that are available to configure for a particular parent component.")
    public Stream<ComponentTypeRepresentation> getSubcomponentConfig(@PathParam("id") String parentId, @QueryParam("type") String subtype) {
        auth.realm().requireViewRealm();
        ComponentModel parent = realm.getComponent(parentId);
        if (parent == null || isInternalComponent(parent.getProviderType(), parent.getProviderId())) {
            throw new NotFoundException("Could not find parent component");
        }
        if (subtype == null) {
            throw new BadRequestException("must specify a subtype");
        }
        Class<? extends Provider> providerClass;
        try {
            providerClass = (Class<? extends Provider>)Class.forName(subtype);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return session.getKeycloakSessionFactory().getProviderFactoriesStream(providerClass)
            .filter(ComponentFactory.class::isInstance)
            .filter(factory -> !((ComponentFactory<?, ?>) factory).isInternal())
            .map(factory -> toComponentTypeRepresentation(factory, parent));
    }

    /** 判断组件是否由内部 API 管理（不可通过此端点操作） */
    private boolean isInternalComponent(String providerType, String providerId) {
        try {
            Class<? extends Provider> providerClass = session.getProviderClass(providerType);
            if (providerClass == null) return false;
            ProviderFactory<?> factory = session.getKeycloakSessionFactory().getProviderFactory(providerClass, providerId);
            return factory instanceof ComponentFactory<?, ?> cf && cf.isInternal();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /** 拒绝操作内部组件 */
    private void rejectInternalComponent(String providerType, String providerId) {
        if (isInternalComponent(providerType, providerId)) {
            throw new ForbiddenException("Components managed through internal APIs cannot be managed through the component endpoint");
        }
    }

    /** 将提供者工厂转换为组件类型表示（含配置属性） */
    private ComponentTypeRepresentation toComponentTypeRepresentation(ProviderFactory factory, ComponentModel parent) {
        ComponentTypeRepresentation rep = new ComponentTypeRepresentation();
        rep.setId(factory.getId());

        ComponentFactory componentFactory = (ComponentFactory)factory;

        rep.setHelpText(componentFactory.getHelpText());
        List<ProviderConfigProperty> props;
        Map<String, Object> metadata;
        if (factory instanceof SubComponentFactory) {
            props = ((SubComponentFactory)factory).getConfigProperties(realm, parent);
            metadata = ((SubComponentFactory)factory).getTypeMetadata(realm, parent);

        } else {
            props = componentFactory.getConfigProperties();
            metadata = componentFactory.getTypeMetadata();
        }

        List<ConfigPropertyRepresentation> propReps =  ModelToRepresentation.toRepresentation(props);
        rep.setProperties(propReps);
        rep.setMetadata(metadata);
        return rep;
    }
}
