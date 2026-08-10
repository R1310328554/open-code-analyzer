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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.ws.rs.BadRequestException;
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
import jakarta.ws.rs.core.UriBuilder;

import org.keycloak.authentication.AuthenticationFlow;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.ClientAuthenticator;
import org.keycloak.authentication.ClientAuthenticatorFactory;
import org.keycloak.authentication.ConfigurableAuthenticatorFactory;
import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormAuthenticator;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.deployment.DeployedConfigurationsManager;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticationFlowModel;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RequiredActionConfigModel;
import org.keycloak.models.RequiredActionProviderModel;
import org.keycloak.models.utils.Base32;
import org.keycloak.models.utils.DefaultAuthenticationFlows;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.models.utils.RepresentationToModel;
import org.keycloak.provider.ConfiguredProvider;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.representations.idm.AuthenticationExecutionInfoRepresentation;
import org.keycloak.representations.idm.AuthenticationExecutionRepresentation;
import org.keycloak.representations.idm.AuthenticationFlowRepresentation;
import org.keycloak.representations.idm.AuthenticatorConfigInfoRepresentation;
import org.keycloak.representations.idm.AuthenticatorConfigRepresentation;
import org.keycloak.representations.idm.ConfigPropertyRepresentation;
import org.keycloak.representations.idm.ErrorRepresentation;
import org.keycloak.representations.idm.RequiredActionConfigInfoRepresentation;
import org.keycloak.representations.idm.RequiredActionConfigRepresentation;
import org.keycloak.representations.idm.RequiredActionProviderRepresentation;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.resources.KeycloakOpenAPI;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;
import org.keycloak.userprofile.ValidationException;
import org.keycloak.utils.CredentialHelper;
import org.keycloak.utils.RequiredActionHelper;
import org.keycloak.utils.ReservedCharValidator;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.NoCache;

/**
 * 认证管理 REST 资源。
 * <p>管理认证流、执行步骤、认证器/表单/客户端认证器提供者、必需操作（Required Actions）及其配置。</p>
 *
 * @resource Authentication Management
 * @author Bill Burke
 */
@Extension(name = KeycloakOpenAPI.Profiles.ADMIN, value = "")
public class AuthenticationManagementResource {

    /** 当前领域 */
    private final RealmModel realm;
    /** Keycloak 会话 */
    private final KeycloakSession session;
    /** 细粒度权限评估器 */
    private final AdminPermissionEvaluator auth;
    /** 管理事件构建器 */
    private final AdminEventBuilder adminEvent;

    /** 日志记录器 */
    protected static final Logger logger = Logger.getLogger(AuthenticationManagementResource.class);

    /** 构造认证管理资源并绑定 AUTH_FLOW 事件类型。 */
    public AuthenticationManagementResource(KeycloakSession session, AdminPermissionEvaluator auth, AdminEventBuilder adminEvent) {
        this.session = session;
        this.realm = session.getContext().getRealm();
        this.auth = auth;
        this.adminEvent = adminEvent.resource(ResourceType.AUTH_FLOW);
    }

    /**
     * 获取表单认证器（Form Authenticator）提供者列表。
     * @return 提供者元数据流
     */
    @Path("/form-providers")
    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.AUTHENTICATION_MANAGEMENT)
    @Operation(summary = "Get form providers Returns a stream of form providers.")
    public Stream<Map<String, Object>> getFormProviders() {
        auth.realm().requireViewRealm();

        return buildProviderMetadata(session.getKeycloakSessionFactory().getProviderFactoriesStream(FormAuthenticator.class));
    }

    /**
     * 获取认证器（Authenticator）提供者列表。
     * @return 提供者元数据流
     */
    @Path("/authenticator-providers")
    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.AUTHENTICATION_MANAGEMENT)
    @Operation( summary = "Get authenticator providers Returns a stream of authenticator providers.")
    public Stream<Map<String, Object>> getAuthenticatorProviders() {
        auth.realm().requireViewRealm();

        return buildProviderMetadata(session.getKeycloakSessionFactory().getProviderFactoriesStream(Authenticator.class));
    }

    /**
     * 获取客户端认证器提供者列表（含 supportsSecret 标志）。
     * @return 提供者元数据流
     */
    @Path("/client-authenticator-providers")
    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.AUTHENTICATION_MANAGEMENT)
    @Operation( summary = "Get client authenticator providers Returns a stream of client authenticator providers.")
    public Stream<Map<String, Object>> getClientAuthenticatorProviders() {
        auth.realm().requireViewClientAuthenticatorProviders();
        Stream<ProviderFactory> factories =  session.getKeycloakSessionFactory().getProviderFactoriesStream(ClientAuthenticator.class);

        return factories.map(factory -> {
            Map<String, Object> data = new HashMap<>();
            buildProviderMetadataHelper(data, factory);
            data.put("supportsSecret", ((ClientAuthenticatorFactory) factory).supportsSecret());
            return data;
        });
    }

    /** 填充提供者 ID、描述与显示名称。 */
    private void buildProviderMetadataHelper(Map<String, Object> data, ProviderFactory factory) {
        data.put("id", factory.getId());
        ConfigurableAuthenticatorFactory configured = (ConfigurableAuthenticatorFactory) factory;
        data.put("description", configured.getHelpText());
        data.put("displayName", configured.getDisplayType());
    }

    /** 将 ProviderFactory 流转换为 Admin UI 元数据 Map 流。 */
    public Stream<Map<String, Object>> buildProviderMetadata(Stream<ProviderFactory> factories) {
        return factories.map(factory -> {
            Map<String, Object> data = new HashMap<>();
            buildProviderMetadataHelper(data, factory);
            return data;
        });
    }

    /**
     * 获取表单动作（Form Action）提供者列表。
     * @return 提供者元数据流
     */
    @Path("/form-action-providers")
    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.AUTHENTICATION_MANAGEMENT)
    @Operation( summary = "Get form action providers Returns a stream of form action providers."
    )
    public Stream<Map<String, Object>> getFormActionProviders() {
        auth.realm().requireViewRealm();

        return buildProviderMetadata(session.getKeycloakSessionFactory().getProviderFactoriesStream(FormAction.class));
    }


    /**
     * 获取顶级认证流列表（排除 SAML ECP 内置流）。
     * @return 认证流表示流
     */
    @Path("/flows")
    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.AUTHENTICATION_MANAGEMENT)
    @Operation( summary = "Get authentication flows Returns a stream of authentication flows.")
    public Stream<AuthenticationFlowRepresentation> getFlows() {
        auth.realm().requireViewAuthenticationFlows();

        return realm.getAuthenticationFlowsStream()
                .filter(flow -> flow.isTopLevel() && !Objects.equals(flow.getAlias(), DefaultAuthenticationFlows.SAML_ECP_FLOW))
                .map(flow -> ModelToRepresentation.toRepresentation(session, realm, flow));
    }

    /**
     * 创建新认证流。
     *
     * @param flow 认证流表示
     * @return 201 Created，Location 指向新流
     */
    @Path("/flows")
    @POST
    @NoCache
    @Consumes(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.AUTHENTICATION_MANAGEMENT)
    @Operation( summary = "Create a new authentication flow")
    @APIResponses(value = {
        @APIResponse(responseCode = "201", description = "Created"),
        @APIResponse(responseCode = "409", description = "Conflict")
    })
    public Response createFlow(@Parameter( description = "Authentication flow representation") AuthenticationFlowRepresentation flow) {
        auth.realm().requireManageRealm();

        if (flow.getAlias() == null || flow.getAlias().isEmpty()) {
            throw ErrorResponse.exists("Failed to create flow with empty alias name");
        }

        if (realm.getFlowByAlias(flow.getAlias()) != null) {
            throw ErrorResponse.exists("Flow " + flow.getAlias() + " already exists");
        }

        // 空描述时用空字符串避免 NPE
        if(Objects.isNull(flow.getDescription())) {
            flow.setDescription("");
        }

        ReservedCharValidator.validate(flow.getAlias());

        AuthenticationFlowModel createdModel = realm.addAuthenticationFlow(RepresentationToModel.toModel(flow));

        flow.setId(createdModel.getId());
        adminEvent.operation(OperationType.CREATE).resourcePath(session.getContext().getUri(), createdModel.getId()).representation(flow).success();
        return Response.created(session.getContext().getUri().getAbsolutePathBuilder().path(flow.getId()).build()).build();
    }

    /**
     * 按 ID 获取认证流。
     *
     * @param id 流 ID
     * @return 认证流表示
     */
    @Path("/flows/{id}")
    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.AUTHENTICATION_MANAGEMENT)
    @Operation( summary = "Get authentication flow for id")
    public AuthenticationFlowRepresentation getFlow(@Parameter(description = "Flow id") @PathParam("id") String id) {
        auth.realm().requireViewRealm();

        AuthenticationFlowModel flow = realm.getAuthenticationFlowById(id);
        if (flow == null) {
            throw new NotFoundException("Could not find flow with id");
        }
        return ModelToRepresentation.toRepresentation(session, realm, flow);
    }

    /**
     * 更新认证流别名与描述。
     *
     * @param id 流 ID
     * @param flow 认证流表示
     */
    @Path("/flows/{id}")
    @PUT
    @NoCache
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.AUTHENTICATION_MANAGEMENT)
    @Operation( summary = "Update an authentication flow")
    @APIResponses(value = {
        @APIResponse(responseCode = "204", description = "No Content"),
        @APIResponse(responseCode = "409", description = "Conflict")
    })
    public void updateFlow(@PathParam("id") String id, AuthenticationFlowRepresentation flow) {
        auth.realm().requireManageRealm();

        AuthenticationFlowRepresentation existingFlow = getFlow(id);

        if (flow.getAlias() == null || flow.getAlias().isEmpty()) {
            throw ErrorResponse.exists("Failed to update flow with empty alias name");
        }

        ReservedCharValidator.validate(flow.getAlias());

        // 校验目标流存在
        AuthenticationFlowModel checkFlow = realm.getAuthenticationFlowById(id);
        if (checkFlow == null) {
            session.getTransactionManager().setRollbackOnly();
            throw new NotFoundException("Illegal execution");
        }

        // 别名冲突时抛出 409
        if (realm.getFlowByAlias(flow.getAlias()) != null && !checkFlow.getAlias().equals(flow.getAlias())) {
            throw ErrorResponse.exists("Flow alias name already exists");
        }

        // 别名变更时更新模型
        if (checkFlow.getAlias() != null && !checkFlow.getAlias().equals(flow.getAlias())) {
            checkFlow.setAlias(flow.getAlias());
        } else if (checkFlow.getAlias() == null && flow.getAlias() != null) {
            checkFlow.setAlias(flow.getAlias());
	}

        // 描述变更时更新模型
        if (checkFlow.getDescription() != null && !checkFlow.getDescription().equals(flow.getDescription())) {
            checkFlow.setDescription(flow.getDescription());
        } else if (checkFlow.getDescription() == null && flow.getDescription() != null) {
            checkFlow.setDescription(flow.getDescription());
	}

        // 持久化认证流更新
        flow.setId(existingFlow.getId());
        realm.updateAuthenticationFlow(RepresentationToModel.toModel(flow));
        adminEvent.operation(OperationType.UPDATE).resourcePath(session.getContext().getUri()).representation(flow).success();
    }

    /**
     * 删除认证流（含子流与执行步骤）。
     *
     * @param id 流 ID
     */
    @Path("/flows/{id}")
    @DELETE
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.AUTHENTICATION_MANAGEMENT)
    @Operation( summary = "Delete an authentication flow")
    @APIResponse(responseCode = "204", description = "No Content")
    public void deleteFlow(@Parameter(description = "Flow id") @PathParam("id") String id) {
        auth.realm().requireManageRealm();

        AuthenticationFlowModel flow = realm.getAuthenticationFlowById(id);
        if (flow == null) {
            throw new NotFoundException("Flow not found");
        }

        KeycloakModelUtils.deepDeleteAuthenticationFlow(session, realm, flow,
                () -> {}, // 允许删除即使存在缺失引用
                () -> {
                    throw new BadRequestException("Can't delete built in flow");
                },
                flow.isBuiltIn()
        );

        // 顶层流仅记录一条删除事件（深度 ≥2 时分开记录会有问题）
        adminEvent.operation(OperationType.DELETE).resourcePath(session.getContext().getUri()).success();
    }

    /**
     * 复制现有认证流并重命名。
     *
     * @param flowAlias 源流别名
     * @param data JSON，须含 {@code newName} 属性
     * @return 201 Created，Location 指向新流
     */
    @Path("/flows/{flowAlias}/copy")
    @POST
    @NoCache
    @Consumes(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.AUTHENTICATION_MANAGEMENT)
    @Operation( summary = "Copy existing authentication flow under a new name The new name is given as 'newName' attribute of the passed JSON object")
    @APIResponses(value = {
        @APIResponse(responseCode = "201", description = "Created"),
        @APIResponse(responseCode = "409", description = "Conflict")
    })
    public Response copy(@Parameter(description="name of the existing authentication flow") @PathParam("flowAlias") String flowAlias, Map<String, String> data) {
        auth.realm().requireManageRealm();

        String newName = data.get("newName");
        ReservedCharValidator.validate(newName);
        if (realm.getFlowByAlias(newName) != null) {
            throw ErrorResponse.exists("New flow alias name already exists");
        }

        AuthenticationFlowModel flow = realm.getFlowByAlias(flowAlias);
        if (flow == null) {
            logger.debugf("flow not found: %s", flowAlias);
            throw new NotFoundException("Flow not found");
        }

        AuthenticationFlowModel copy = copyFlow(session, realm, flow, newName);

        data.put("id", copy.getId());
        adminEvent.operation(OperationType.CREATE).resourcePath(session.getContext().getUri()).representation(data).success();

        return Response.created(session.getContext().getUri().getAbsolutePathBuilder().path(copy.getId()).build()).build();
    }

    /** 深拷贝认证流及其全部执行步骤与配置。 */
    public static AuthenticationFlowModel copyFlow(KeycloakSession session, RealmModel realm, AuthenticationFlowModel flow, String newName) {
        AuthenticationFlowModel copy = new AuthenticationFlowModel();
        copy.setAlias(newName);
        copy.setDescription(flow.getDescription());
        copy.setProviderId(flow.getProviderId());
        copy.setBuiltIn(false);
        copy.setTopLevel(flow.isTopLevel());
        copy = realm.addAuthenticationFlow(copy);
        copy(session, realm, newName, flow, copy);
        return copy;
    }

    /** 递归复制源流下的执行步骤、子流与认证器配置。 */
    public static void copy(KeycloakSession session, RealmModel realm, String newName, AuthenticationFlowModel from, AuthenticationFlowModel to) {
        realm.getAuthenticationExecutionsStream(from.getId()).forEachOrdered(execution -> {
            if (execution.isAuthenticatorFlow()) {
                AuthenticationFlowModel subFlow = realm.getAuthenticationFlowById(execution.getFlowId());
                AuthenticationFlowModel copy = new AuthenticationFlowModel();
                copy.setAlias(newName + " " + subFlow.getAlias());
                copy.setDescription(subFlow.getDescription());
                copy.setProviderId(subFlow.getProviderId());
                copy.setBuiltIn(false);
                copy.setTopLevel(false);
                copy = realm.addAuthenticationFlow(copy);
                execution.setFlowId(copy.getId());
                copy(session, realm, newName, subFlow, copy);
            }

            if (execution.getAuthenticatorConfig() != null) {
                DeployedConfigurationsManager configManager = new DeployedConfigurationsManager(session);
                AuthenticatorConfigModel config = configManager.getAuthenticatorConfig(realm, execution.getAuthenticatorConfig());

                if (config == null) {
                    logger.debugf("Authentication execution configuration with id [%s] not found", execution.getAuthenticatorConfig());
                    throw new IllegalStateException("Authentication execution configuration not found");
                }

                if (configManager.getDeployedAuthenticatorConfig(execution.getAuthenticatorConfig()) != null) {
                    // 已部署提供者的共享配置直接复用 ID
                    execution.setAuthenticatorConfig(config.getId());
                } else {
                    config.setId(null);

                    if (config.getAlias() != null) {
                        config.setAlias(newName + " " + config.getAlias());
                        if (configManager.getAuthenticatorConfigByAlias(realm, config.getAlias()) != null) {
                            logger.warnf("Authentication execution configuration [%s] already exists", config.getAlias());
                            throw new IllegalStateException("Authentication execution configuration " + config.getAlias() + " already exists.");
                        }
                    }

                    AuthenticatorConfigModel newConfig = realm.addAuthenticatorConfig(config);

                    execution.setAuthenticatorConfig(newConfig.getId());
                }
            }

            execution.setId(null);
            execution.setParentFlow(to.getId());
            realm.addAuthenticatorExecution(execution);
        });
    }

    /**
     * 向现有流添加子流及绑定执行步骤。
     * <p>同时创建子认证流实体与父流下的 execution 实体；Location 头指向新子流（非 execution）。</p>
     *
     * @param flowAlias 父流别名
     * @param data JSON，含 alias、type、provider、priority、description
     * @return 201 Created，Location 指向新子流
     */
    @Path("/flows/{flowAlias}/executions/flow")
    @POST
    @NoCache
    @Consumes(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.AUTHENTICATION_MANAGEMENT)
    @Operation( summary = "Add new flow with new execution to existing flow")
    @APIResponses(value = {
        @APIResponse(responseCode = "201", description = "Created"),
        @APIResponse(responseCode = "409", description = "Conflict")
    })
    public Response addExecutionFlow(@Parameter(description = "Alias of parent authentication flow") @PathParam("flowAlias") String flowAlias, @Parameter(description = "New authentication flow / execution JSON data containing 'alias', 'type', 'provider', 'priority', and 'description' attributes") Map<String, Object> data) {
        auth.realm().requireManageRealm();

        AuthenticationFlowModel parentFlow = realm.getFlowByAlias(flowAlias);
        if (parentFlow == null) {
            throw ErrorResponse.error("Parent flow doesn't exist", Response.Status.BAD_REQUEST);
        }
        if (parentFlow.isBuiltIn()) {
            throw new BadRequestException("It is illegal to add sub-flow to a built in flow");
        }
        String alias = (String) data.get("alias");
        String type = (String) data.get("type");
        String provider = (String) data.get("provider");
        int priority = data.containsKey("priority") ? (Integer) data.get("priority") : getNextPriority(parentFlow);

        // 描述为 null 时用空字符串避免 NPE
        String description = Objects.isNull(data.get("description")) ? "" : (String) data.get("description");


        AuthenticationFlowModel newFlow = realm.getFlowByAlias(alias);
        if (newFlow != null) {
            throw ErrorResponse.exists("New flow alias name already exists");
        }
        newFlow = new AuthenticationFlowModel();
        newFlow.setAlias(alias);
        newFlow.setDescription(description);
        newFlow.setProviderId(type);
        newFlow = realm.addAuthenticationFlow(newFlow);
        AuthenticationExecutionModel execution = new AuthenticationExecutionModel();
        execution.setParentFlow(parentFlow.getId());
        execution.setFlowId(newFlow.getId());
        execution.setRequirement(AuthenticationExecutionModel.Requirement.DISABLED);
        execution.setAuthenticatorFlow(true);
        if (type.equals("form-flow")) {
            execution.setAuthenticator(provider);
        }
        execution.setPriority(priority);
        execution = realm.addAuthenticatorExecution(execution);

        data.put("id", execution.getId());
        adminEvent.operation(OperationType.CREATE).resource(ResourceType.AUTH_EXECUTION_FLOW).resourcePath(session.getContext().getUri()).representation(data).success();

        String addExecutionPathSegment = UriBuilder.fromMethod(AuthenticationManagementResource.class, "addExecutionFlow").build(parentFlow.getAlias()).getPath();
        return Response.created(session.getContext().getUri().getBaseUriBuilder().path(session.getContext().getUri().getPath().replace(addExecutionPathSegment, "")).path("flows").path(newFlow.getId()).build()).build();
    }

    /** 计算父流下新 execution 的下一个 priority 值。 */
    private int getNextPriority(AuthenticationFlowModel parentFlow) {
        List<AuthenticationExecutionModel> executions = realm.getAuthenticationExecutionsStream(parentFlow.getId())
                .collect(Collectors.toList());
        return executions.isEmpty() ? 0 : executions.get(executions.size() - 1).getPriority() + 1;
    }

    /**
     * 向认证流添加新的认证器 execution。
     *
     * @param flowAlias 父流别名
     * @param data JSON，含 provider 与可选 priority
     * @return 201 Created，Location 指向新 execution
     */
    @Path("/flows/{flowAlias}/executions/execution")
    @POST
    @NoCache
    @Consumes(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.AUTHENTICATION_MANAGEMENT)
    @Operation( summary="Add new authentication execution to a flow")
    @APIResponse(responseCode = "201", description = "Created")
    public Response addExecutionToFlow(@Parameter(description = "Alias of parent flow") @PathParam("flowAlias") String flowAlias, @Parameter(description = "New execution JSON data containing 'provider' and 'priority' (optional) attribute") Map<String, Object> data) {
        auth.realm().requireManageRealm();

        AuthenticationFlowModel parentFlow = realm.getFlowByAlias(flowAlias);
        if (parentFlow == null) {
            throw new BadRequestException("Parent flow doesn't exist");
        }
        if (parentFlow.isBuiltIn()) {
            throw new BadRequestException("It is illegal to add execution to a built in flow");
        }
        String provider = (String) data.get("provider");
        int priority = data.containsKey("priority") ? (Integer) data.get("priority") : getNextPriority(parentFlow);

        // 校验 provider 为已注册的认证器工厂
        ProviderFactory f = getProviderFactory( parentFlow, provider);

        AuthenticationExecutionModel execution = new AuthenticationExecutionModel();
        execution.setParentFlow(parentFlow.getId());

        ConfigurableAuthenticatorFactory conf = (ConfigurableAuthenticatorFactory) f;
        if (conf.getRequirementChoices().length == 1)
            execution.setRequirement(conf.getRequirementChoices()[0]);
        else
            execution.setRequirement(AuthenticationExecutionModel.Requirement.DISABLED);

        execution.setAuthenticatorFlow(false);
        execution.setAuthenticator(provider);
        execution.setPriority(priority);

        execution = realm.addAuthenticatorExecution(execution);

        checkConfigForDeployedProvider(f, execution);

        data.put("id", execution.getId());
        adminEvent.operation(OperationType.CREATE).resource(ResourceType.AUTH_EXECUTION).resourcePath(session.getContext().getUri()).representation(data).success();

        String addExecutionPathSegment = UriBuilder.fromMethod(AuthenticationManagementResource.class, "addExecutionToFlow").build(parentFlow.getAlias()).getPath();
        return Response.created(session.getContext().getUri().getBaseUriBuilder().path(session.getContext().getUri().getPath().replace(addExecutionPathSegment, "")).path("executions").path(execution.getId()).build()).build();
    }

    /** 按父流类型解析 Authenticator/FormAction/ClientAuthenticator 工厂。 */
    private ProviderFactory getProviderFactory(AuthenticationFlowModel parentFlow, String provider) {
        ProviderFactory f = null;
        if (parentFlow.getProviderId().equals(AuthenticationFlow.CLIENT_FLOW)) {
            f = session.getKeycloakSessionFactory().getProviderFactory(ClientAuthenticator.class, provider);
        } else if (parentFlow.getProviderId().equals(AuthenticationFlow.FORM_FLOW)) {
            f = session.getKeycloakSessionFactory().getProviderFactory(FormAction.class, provider);
        } else {
            f = session.getKeycloakSessionFactory().getProviderFactory(Authenticator.class, provider);
        }
        if (f == null) {
            throw new BadRequestException("No authentication provider found for id: " + provider);
        }
        return f;
    }


    /** 若已部署提供者带有默认配置则自动绑定 execution 配置 ID。 */
    private void checkConfigForDeployedProvider(ProviderFactory f, AuthenticationExecutionModel execution) {
        if (f instanceof ConfiguredProvider) {
            ConfiguredProvider internalProviderFactory = (ConfiguredProvider) f;
            AuthenticatorConfigModel config = internalProviderFactory.getConfig();

            if (config != null) {
                // 工厂定义默认配置时自动关联
                // 假定配置已在 DeployedConfigurationsProvider 中注册
                // 适用于内置已部署配置的内部提供者
                logger.tracef("Updating execution of provider '%s' with shared configuration.", execution.getAuthenticator());
                execution.setAuthenticatorConfig(config.getId());
                realm.updateAuthenticatorExecution(execution);
            }
        }
    }

    /**
     * 获取认证流下全部 execution（含嵌套子流，递归展开）。
     *
     * @param flowAlias 流别名
     * @return execution 信息列表
     */
    @Path("/flows/{flowAlias}/executions")
    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.AUTHENTICATION_MANAGEMENT)
    @Operation( summary = "Get authentication executions for a flow")
    public List<AuthenticationExecutionInfoRepresentation> getExecutions(@Parameter(description = "Flow alias") @PathParam("flowAlias") String flowAlias) {
        auth.realm().requireViewRealm();

        AuthenticationFlowModel flow = realm.getFlowByAlias(flowAlias);
        if (flow == null) {
            logger.debugf("flow not found: %s", flowAlias);
            throw new NotFoundException("Flow not found");
        }
        List<AuthenticationExecutionInfoRepresentation> result = new LinkedList<>();

        int level = 0;

        recurseExecutions(flow, result, level);
        return result;
    }

    /** 解析 execution 关联的认证器配置 ID（缺失时记录警告）。 */
    private String getAuthenticationConfig(String flowAlias, AuthenticationExecutionModel model) {
        if (model.getAuthenticatorConfig() == null) {
            return null;
        }
        AuthenticatorConfigModel config = new DeployedConfigurationsManager(session).getAuthenticatorConfig(realm, model.getAuthenticatorConfig());
        if (config == null) {
            logger.warnf("Authenticator configuration '%s' is missing for execution '%s' (%s) in flow '%s'",
                    model.getAuthenticatorConfig(), model.getId(), model.getAuthenticator(), flowAlias);
            return null;
        }
        return config.getId();
    }

    /** 递归遍历流及其子流，构建带层级 index 的 execution 信息列表。 */
    public void recurseExecutions(AuthenticationFlowModel flow, List<AuthenticationExecutionInfoRepresentation> result, int level) {
        AtomicInteger index = new AtomicInteger(0);
        realm.getAuthenticationExecutionsStream(flow.getId()).forEachOrdered(execution -> {
            AuthenticationExecutionInfoRepresentation rep = new AuthenticationExecutionInfoRepresentation();
            rep.setLevel(level);
            rep.setIndex(index.getAndIncrement());
            rep.setRequirementChoices(new LinkedList<>());
            rep.setPriority(execution.getPriority());
            if (execution.isAuthenticatorFlow()) {
                AuthenticationFlowModel flowRef = realm.getAuthenticationFlowById(execution.getFlowId());
                if (AuthenticationFlow.BASIC_FLOW.equals(flowRef.getProviderId())) {
                    rep.getRequirementChoices().add(AuthenticationExecutionModel.Requirement.REQUIRED.name());
                    rep.getRequirementChoices().add(AuthenticationExecutionModel.Requirement.ALTERNATIVE.name());
                    rep.getRequirementChoices().add(AuthenticationExecutionModel.Requirement.DISABLED.name());
                    rep.getRequirementChoices().add(AuthenticationExecutionModel.Requirement.CONDITIONAL.name());
                } else if (AuthenticationFlow.FORM_FLOW.equals(flowRef.getProviderId())) {
                    rep.getRequirementChoices().add(AuthenticationExecutionModel.Requirement.REQUIRED.name());
                    rep.getRequirementChoices().add(AuthenticationExecutionModel.Requirement.DISABLED.name());
                    rep.setProviderId(execution.getAuthenticator());
                    rep.setAuthenticationConfig(getAuthenticationConfig(flow.getAlias(), execution));
                } else if (AuthenticationFlow.CLIENT_FLOW.equals(flowRef.getProviderId())) {
                    rep.getRequirementChoices().add(AuthenticationExecutionModel.Requirement.ALTERNATIVE.name());
                    rep.getRequirementChoices().add(AuthenticationExecutionModel.Requirement.REQUIRED.name());
                    rep.getRequirementChoices().add(AuthenticationExecutionModel.Requirement.DISABLED.name());
                }
                rep.setDisplayName(flowRef.getAlias());
                rep.setDescription(flowRef.getDescription());
                rep.setConfigurable(false);
                rep.setId(execution.getId());
                rep.setAuthenticationFlow(execution.isAuthenticatorFlow());
                rep.setRequirement(execution.getRequirement().name());
                rep.setFlowId(execution.getFlowId());
                result.add(rep);
                recurseExecutions(flowRef, result, level + 1);
            } else {
                String providerId = execution.getAuthenticator();
                ConfigurableAuthenticatorFactory factory = CredentialHelper.getConfigurableAuthenticatorFactory(session, providerId);
                if (factory == null) {
                    logger.warnf("Cannot find authentication provider implementation with provider ID '%s'", providerId);
                    throw new NotFoundException("Could not find authenticator provider");
                }
                rep.setDisplayName(factory.getDisplayType());
                rep.setConfigurable(factory.isConfigurable());
                for (AuthenticationExecutionModel.Requirement choice : factory.getRequirementChoices()) {
                    rep.getRequirementChoices().add(choice.name());
                }
                rep.setId(execution.getId());

                if (factory.isConfigurable()) {
                    String authenticatorConfigId = execution.getAuthenticatorConfig();
                    if(authenticatorConfigId != null) {
                        AuthenticatorConfigModel authenticatorConfig = new DeployedConfigurationsManager(session).getAuthenticatorConfig(realm, authenticatorConfigId);

                        if (authenticatorConfig != null) {
                            rep.setAlias(authenticatorConfig.getAlias());
                        }
                    }
                }

                rep.setRequirement(execution.getRequirement().name());

                providerId = execution.getAuthenticator();

                // script- 前缀提供者 ID 需 Base32 编码以便作为 URL 路径参数
                if (providerId.startsWith("script-")) {
                    providerId = Base32.encode(providerId.getBytes());
                }

                rep.setProviderId(providerId);
                rep.setAuthenticationConfig(getAuthenticationConfig(flow.getAlias(), execution));
                result.add(rep);
            }
        });
    }

    /**
     * 更新流的 execution 优先级、requirement 或子流别名/描述。
     * @param flowAlias 流别名
     * @param rep execution 信息表示
     */
    @Path("/flows/{flowAlias}/executions")
    @PUT
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.AUTHENTICATION_MANAGEMENT)
    @Operation( summary = "Update authentication executions of a Flow")
    @APIResponses(value = {
        @APIResponse(responseCode = "204", description = "No Content"),
        @APIResponse(responseCode = "409", description = "Conflict")
    })
    public void updateExecutions(@Parameter(description = "Flow alias") @PathParam("flowAlias") String flowAlias, @Parameter(description = "AuthenticationExecutionInfoRepresentation") AuthenticationExecutionInfoRepresentation rep) {
        auth.realm().requireManageRealm();

        AuthenticationFlowModel flow = realm.getFlowByAlias(flowAlias);
        if (flow == null) {
            logger.debugf("flow not found: %s", flowAlias);
            throw new NotFoundException("flow not found");
        }

        AuthenticationExecutionModel model = realm.getAuthenticationExecutionById(rep.getId());
        if (model == null) {
            session.getTransactionManager().setRollbackOnly();
            throw new NotFoundException("Illegal execution");

        }
        boolean updateExecution = false;
        if (model.getPriority() != rep.getPriority()) {
            model.setPriority(rep.getPriority());
            updateExecution = true;
        }
        if (!model.getRequirement().name().equals(rep.getRequirement())) {
            model.setRequirement(AuthenticationExecutionModel.Requirement.valueOf(rep.getRequirement()));
            updateExecution = true;
        }
        if (updateExecution) {
            realm.updateAuthenticatorExecution(model);
            adminEvent.operation(OperationType.UPDATE).resource(ResourceType.AUTH_EXECUTION).resourcePath(session.getContext().getUri()).representation(rep).success();
            return;
        }

        // 非子流 execution 不能更新名称与描述
        if (rep.getAuthenticationFlow() == null) {
            return;
        }

        // 校验子流存在
        AuthenticationFlowModel checkFlow = realm.getAuthenticationFlowById(rep.getFlowId());
        if (checkFlow == null) {
            session.getTransactionManager().setRollbackOnly();
            throw new NotFoundException("Illegal execution");
        }

        //if a different flow with the same name does already exist, throw an exception
        if (realm.getFlowByAlias(rep.getDisplayName()) != null && !checkFlow.getAlias().equals(rep.getDisplayName())) {
            throw ErrorResponse.exists("Flow alias name already exists");
        }

        //if the name changed
        if (!checkFlow.getAlias().equals(rep.getDisplayName())) {
            checkFlow.setAlias(rep.getDisplayName());
        }

        // 描述为 null 时设为空字符串
        if (Objects.isNull(checkFlow.getDescription())) {
            checkFlow.setDescription("");
        }

        // 描述变更时更新子流
        if (!checkFlow.getDescription().equals(rep.getDescription())) {
            checkFlow.setDescription(rep.getDescription());
        }

        // 持久化子流更新
        realm.updateAuthenticationFlow(checkFlow);
        adminEvent.operation(OperationType.UPDATE).resource(ResourceType.AUTH_EXECUTION).resourcePath(session.getContext().getUri()).representation(rep).success();
    }

    /**
     * 按 ID 获取单个 authentication execution。
     * @param executionId execution ID
     * @return execution 表示
     */
    @Path("/executions/{executionId}")
    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.AUTHENTICATION_MANAGEMENT)
    @Operation( summary = "Get Single Execution")
    public AuthenticationExecutionRepresentation getExecution(final @PathParam("executionId") String executionId) {
    	//http://localhost:8080/auth/admin/realms/master/authentication/executions/cf26211b-9e68-4788-b754-1afd02e59d7f
        auth.realm().requireViewRealm();

        final Optional<AuthenticationExecutionModel> model = Optional.ofNullable(realm.getAuthenticationExecutionById(executionId));
        if (!model.isPresent()) {
            logger.debugf("Could not find execution by Id: %s", executionId);
            throw new NotFoundException("Illegal execution");
        }

        return ModelToRepresentation.toRepresentation(model.get());
    }

    /**
     * 添加新的 authentication execution（通用 POST 入口）。
     *
     * @param execution execution JSON 模型
     * @return 201 Created
     */
    @Path("/executions")
    @POST
    @NoCache
    @Consumes(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.AUTHENTICATION_MANAGEMENT)
    @Operation( summary = "Add new authentication execution")
    @APIResponse(responseCode = "201", description = "Created")
    public Response addExecution(@Parameter(description = "JSON model describing authentication execution") AuthenticationExecutionRepresentation execution) {
        auth.realm().requireManageRealm();

        AuthenticationExecutionModel model = RepresentationToModel.toModel(session, realm, execution);
        AuthenticationFlowModel parentFlow = getParentFlow(model);
        if (parentFlow.isBuiltIn()) {
            throw new BadRequestException("It is illegal to add execution to a built in flow");
        }
        int priority = execution.getPriority() != null ? execution.getPriority() : getNextPriority(parentFlow);
        model.setPriority(priority);
        model = realm.addAuthenticatorExecution(model);

        if (!execution.isAuthenticatorFlow()) {
            ProviderFactory f = getProviderFactory(parentFlow, execution.getAuthenticator());
            checkConfigForDeployedProvider(f, model);
        }

        adminEvent.operation(OperationType.CREATE).resource(ResourceType.AUTH_EXECUTION).resourcePath(session.getContext().getUri(), model.getId()).representation(execution).success();
        return Response.created(session.getContext().getUri().getAbsolutePathBuilder().path(model.getId()).build()).build();
    }

    /** 解析 execution 的父认证流，缺失时抛出 BadRequestException。 */
    public AuthenticationFlowModel getParentFlow(AuthenticationExecutionModel model) {
        if (model.getParentFlow() == null) {
            throw new BadRequestException("parent flow not set on new execution");
        }
        AuthenticationFlowModel parentFlow = realm.getAuthenticationFlowById(model.getParentFlow());
        if (parentFlow == null) {
            throw new BadRequestException("execution parent flow does not exist");

        }
        return parentFlow;
    }


    /**
     * 提高 execution 优先级（与前一 execution 交换）。
     *
     * @param execution execution ID
     */
    @Path("/executions/{executionId}/raise-priority")
    @POST
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.AUTHENTICATION_MANAGEMENT)
    @Operation( summary = "Raise execution's priority")
    @APIResponse(responseCode = "204", description = "No Content")
    public void raisePriority(@Parameter(description = "Execution id") @PathParam("executionId") String execution) {
        auth.realm().requireManageRealm();

        AuthenticationExecutionModel model = realm.getAuthenticationExecutionById(execution);
        if (model == null) {
            session.getTransactionManager().setRollbackOnly();
            throw new NotFoundException("Illegal execution");

        }
        AuthenticationFlowModel parentFlow = getParentFlow(model);
        if (parentFlow.isBuiltIn()) {
            throw new BadRequestException("It is illegal to modify execution in a built in flow");
        }

        AuthenticationExecutionModel previous = null;
        for (AuthenticationExecutionModel exe : realm.getAuthenticationExecutionsStream(parentFlow.getId()).collect(Collectors.toList())) {
            if (exe.getId().equals(model.getId())) {
                break;
            }
            previous = exe;

        }
        if (previous == null) return;
        int tmp = previous.getPriority();
        previous.setPriority(model.getPriority());
        realm.updateAuthenticatorExecution(previous);
        model.setPriority(tmp);
        realm.updateAuthenticatorExecution(model);

        adminEvent.operation(OperationType.UPDATE).resource(ResourceType.AUTH_EXECUTION).resourcePath(session.getContext().getUri()).success();
    }

    /**
     * 降低 execution 优先级（与后一 execution 交换）。
     *
     * @param execution execution ID
     */
    @Path("/executions/{executionId}/lower-priority")
    @POST
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.AUTHENTICATION_MANAGEMENT)
    @Operation( summary = "Lower execution's priority")
    @APIResponse(responseCode = "204", description = "No Content")
    public void lowerPriority(@Parameter( description = "Execution id") @PathParam("executionId") String execution) {
        auth.realm().requireManageRealm();

        AuthenticationExecutionModel model = realm.getAuthenticationExecutionById(execution);
        if (model == null) {
            session.getTransactionManager().setRollbackOnly();
            throw new NotFoundException("Illegal execution");

        }
        AuthenticationFlowModel parentFlow = getParentFlow(model);
        if (parentFlow.isBuiltIn()) {
            throw new BadRequestException("It is illegal to modify execution in a built in flow");
        }
        List<AuthenticationExecutionModel> executions = realm.getAuthenticationExecutionsStream(parentFlow.getId()).collect(Collectors.toList());
        int i;
        for (i = 0; i < executions.size(); i++) {
            if (executions.get(i).getId().equals(model.getId())) {
                break;
            }
        }
        if (i + 1 >= executions.size()) return;
        AuthenticationExecutionModel next = executions.get(i + 1);
        int tmp = model.getPriority();
        model.setPriority(next.getPriority());
        realm.updateAuthenticatorExecution(model);
        next.setPriority(tmp);
        realm.updateAuthenticatorExecution(next);

        adminEvent.operation(OperationType.UPDATE).resource(ResourceType.AUTH_EXECUTION).resourcePath(session.getContext().getUri()).success();
    }


    /**
     * 删除 authentication execution。
     *
     * @param execution execution ID
     */
    @Path("/executions/{executionId}")
    @DELETE
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.AUTHENTICATION_MANAGEMENT)
    @Operation( summary = "Delete execution")
    @APIResponse(responseCode = "204", description = "No Content")
    public void removeExecution(@Parameter(description = "Execution id") @PathParam("executionId") String execution) {
        auth.realm().requireManageRealm();

        AuthenticationExecutionModel model = realm.getAuthenticationExecutionById(execution);
        if (model == null) {
            session.getTransactionManager().setRollbackOnly();
            throw new NotFoundException("Illegal execution");
        }

        AuthenticationFlowModel parentFlow = getParentFlow(model);
        if (parentFlow.isBuiltIn()) {
            throw new BadRequestException("It is illegal to remove execution from a built in flow");
        }

        KeycloakModelUtils.deepDeleteAuthenticationExecutor(session, realm, model,
                () -> {}, // allow deleting even with missing references
                () -> {
                    throw new BadRequestException("It is illegal to remove execution from a built in flow");
                },
                parentFlow.isBuiltIn()
        );

        adminEvent.operation(OperationType.DELETE).resource(ResourceType.AUTH_EXECUTION).resourcePath(session.getContext().getUri()).success();
    }


    /**
     * 为 execution 创建或替换认证器配置。
     *
     * @param execution execution ID
     * @param json 新配置 JSON
     * @return 201 Created，Location 指向配置
     */
    @Path("/executions/{executionId}/config")
    @POST
    @NoCache
    @Consumes(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.AUTHENTICATION_MANAGEMENT)
    @Operation( summary = "Update execution with new configuration")
    @APIResponses(value = {
        @APIResponse(responseCode = "201", description = "Created"),
        @APIResponse(responseCode = "409", description = "Conflict")
    })
    public Response newExecutionConfig(@Parameter(description = "Execution id") @PathParam("executionId") String execution, @Parameter(description = "JSON with new configuration") AuthenticatorConfigRepresentation json) {
        auth.realm().requireManageRealm();

        if (json.getAlias() == null || json.getAlias().isEmpty()) {
            throw ErrorResponse.exists("Failed to create authentication execution configuration with empty alias name");
        }

        ReservedCharValidator.validate(json.getAlias());

        AuthenticationExecutionModel model = realm.getAuthenticationExecutionById(execution);
        if (model == null) {
            session.getTransactionManager().setRollbackOnly();
            throw new NotFoundException("Illegal execution");
        }

        // 读取 execution 上已有的配置
        AuthenticatorConfigModel prevConfig = null;
        if (model.getAuthenticatorConfig() != null) {
            prevConfig = realm.getAuthenticatorConfigById(model.getAuthenticatorConfig());
        }

        AuthenticatorConfigModel otherConfig = realm.getAuthenticatorConfigByAlias(json.getAlias());
        if (otherConfig != null && (prevConfig == null || !prevConfig.getId().equals(otherConfig.getId()))) {
            throw ErrorResponse.exists("Authentication execution configuration " + json.getAlias() + " already exists");
        }

        // 移除旧配置后写入新配置
        if (prevConfig != null) {
            realm.removeAuthenticatorConfig(prevConfig);
        }

        AuthenticatorConfigModel config = RepresentationToModel.toModel(json);
        config = realm.addAuthenticatorConfig(config);
        model.setAuthenticatorConfig(config.getId());
        realm.updateAuthenticatorExecution(model);

        json.setId(config.getId());
        adminEvent.operation(OperationType.CREATE).resource(ResourceType.AUTHENTICATOR_CONFIG).resourcePath(session.getContext().getUri()).representation(json).success();
        return Response.created(session.getContext().getUri().getAbsolutePathBuilder().path(config.getId()).build()).build();
    }

    /**
     * 获取 execution 关联的认证器配置（已弃用）。
     *
     * @param execution execution ID
     * @param id 配置 ID
     * @return 配置表示
     * @deprecated 请改用 {@link #getAuthenticatorConfig(String)}
     */
    @Path("/executions/{executionId}/config/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.AUTHENTICATION_MANAGEMENT)
    @Operation( summary = "Get execution's configuration", deprecated = true)
    @Deprecated
    public AuthenticatorConfigRepresentation getAuthenticatorConfig(@Parameter(description = "Execution id") @PathParam("executionId") String execution, @Parameter(description = "Configuration id") @PathParam("id") String id) {
        auth.realm().requireViewRealm();

        AuthenticatorConfigModel config = new DeployedConfigurationsManager(session).getAuthenticatorConfig(realm, id);
        if (config == null) {
            throw new NotFoundException("Could not find authenticator config");

        }
        return ModelToRepresentation.toRepresentation(config);
    }

    /**
     * 获取尚未在领域中注册的 Required Action 提供者。
     * @return 未注册提供者信息流
     */
    @Path("unregistered-required-actions")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.AUTHENTICATION_MANAGEMENT)
    @Operation( summary = "Get unregistered required actions Returns a stream of unregistered required actions.")
    public Stream<Map<String, String>> getUnregisteredRequiredActions() {
        auth.realm().requireViewRealm();

        Set<String> providerIds = realm.getRequiredActionProvidersStream()
                .map(RequiredActionProviderModel::getProviderId).collect(Collectors.toSet());

        return session.getKeycloakSessionFactory().getProviderFactoriesStream(RequiredActionProvider.class)
                .filter(factory -> !providerIds.contains(factory.getId()))
                .map(factory -> {
                    RequiredActionFactory r = (RequiredActionFactory) factory;
                    Map<String, String> m = new HashMap<>();
                    m.put("name", r.getDisplayText());
                    m.put("providerId", r.getId());
                    return m;
                });
    }

    /**
     * 注册新的 Required Action 到当前领域。
     *
     * @param data JSON，含 providerId 与 name
     */
    @Path("register-required-action")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.AUTHENTICATION_MANAGEMENT)
    @Operation( summary = "Register a new required actions")
    @APIResponse(responseCode = "204", description = "No Content")
    public void registerRequiredAction(@Parameter(description = "JSON containing 'providerId', and 'name' attributes.") Map<String, String> data) {
        auth.realm().requireManageRealm();

        String providerId = data.get("providerId");

        if (providerId == null || session.getKeycloakSessionFactory().getProviderFactory(RequiredActionProvider.class, providerId) == null) {
            throw new BadRequestException("Required Action Provider with given providerId not found");
        }

        String name = data.get("name");
        RequiredActionProviderModel requiredAction = new RequiredActionProviderModel();
        requiredAction.setAlias(providerId);
        requiredAction.setName(name);
        requiredAction.setProviderId(providerId);
        requiredAction.setDefaultAction(false);
        requiredAction.setPriority(getNextRequiredActionPriority());
        requiredAction.setEnabled(true);
        requiredAction = realm.addRequiredActionProvider(requiredAction);

        data.put("id", requiredAction.getId());
        adminEvent.operation(OperationType.CREATE).resource(ResourceType.REQUIRED_ACTION).resourcePath(session.getContext().getUri()).representation(data).success();
    }

    /** 计算新 Required Action 的下一个 priority。 */
    private int getNextRequiredActionPriority() {
        List<RequiredActionProviderModel> actions = realm.getRequiredActionProvidersStream().collect(Collectors.toList());
        return actions.isEmpty() ? 0 : actions.get(actions.size() - 1).getPriority() + 1;
    }


    /**
     * 获取已注册的 Required Action 列表。
     * @return Required Action 表示流
     */
    @Path("required-actions")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.AUTHENTICATION_MANAGEMENT)
    @Operation( summary = "Get required actions Returns a stream of required actions.")
    public Stream<RequiredActionProviderRepresentation> getRequiredActions() {
        auth.realm().requireViewRequiredActions();

        return realm.getRequiredActionProvidersStream().map(AuthenticationManagementResource::toRepresentation);
    }

    /** 将 {@link RequiredActionProviderModel} 转换为 REST 表示。 */
    public static RequiredActionProviderRepresentation toRepresentation(RequiredActionProviderModel model) {
        RequiredActionProviderRepresentation rep = new RequiredActionProviderRepresentation();
        rep.setAlias(model.getAlias());
        rep.setProviderId(model.getProviderId());
        rep.setName(model.getName());
        rep.setDefaultAction(model.isDefaultAction());
        rep.setPriority(model.getPriority());
        rep.setEnabled(model.isEnabled());
        rep.setConfig(model.getConfig());
        return rep;
    }

    /**
     * 按别名获取 Required Action。
     * @param alias Required Action 别名
     * @return Required Action 表示
     */
    @Path("required-actions/{alias}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.AUTHENTICATION_MANAGEMENT)
    @Operation( summary = "Get required action for alias")
    public RequiredActionProviderRepresentation getRequiredAction(@Parameter(description = "Alias of required action") @PathParam("alias") String alias) {
        auth.realm().requireViewRealm();

        RequiredActionProviderModel model = realm.getRequiredActionProviderByAlias(alias);
        if (model == null) {
            throw new NotFoundException("Failed to find required action");
        }
        return toRepresentation(model);
    }


    /**
     * 更新 Required Action 配置（名称、默认、优先级、启用状态等）。
     *
     * @param alias Required Action 别名
     * @param rep 新状态 JSON
     */
    @Path("required-actions/{alias}")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.AUTHENTICATION_MANAGEMENT)
    @Operation( summary = "Update required action")
    @APIResponse(responseCode = "204", description = "No Content")
    public void updateRequiredAction(@Parameter(description = "Alias of required action") @PathParam("alias") String alias, @Parameter(description = "JSON describing new state of required action") RequiredActionProviderRepresentation rep) {
        auth.realm().requireManageRealm();

        RequiredActionProviderModel model = realm.getRequiredActionProviderByAlias(alias);
        if (model == null) {
            throw new NotFoundException("Failed to find required action");
        }
        RequiredActionProviderModel update = new RequiredActionProviderModel();
        update.setId(model.getId());
        update.setName(rep.getName());
        update.setAlias(rep.getAlias());
        update.setProviderId(model.getProviderId());
        update.setDefaultAction(rep.isDefaultAction());
        update.setPriority(rep.getPriority());
        update.setEnabled(rep.isEnabled());
        update.setConfig(rep.getConfig());
        realm.updateRequiredActionProvider(update);

        adminEvent.operation(OperationType.UPDATE).resource(ResourceType.REQUIRED_ACTION).resourcePath(session.getContext().getUri()).representation(rep).success();
    }

    /**
     * 从领域删除 Required Action。
     * @param alias Required Action 别名
     */
    @Path("required-actions/{alias}")
    @DELETE
    @Tag(name = KeycloakOpenAPI.Admin.Tags.AUTHENTICATION_MANAGEMENT)
    @Operation( summary = "Delete required action")
    @APIResponse(responseCode = "204", description = "No Content")
    public void removeRequiredAction(@Parameter(description = "Alias of required action") @PathParam("alias") String alias) {
        auth.realm().requireManageRealm();

        RequiredActionProviderModel model = realm.getRequiredActionProviderByAlias(alias);
        if (model == null) {
            throw new NotFoundException("Failed to find required action.");
        }
        realm.removeRequiredActionProvider(model);

        adminEvent.operation(OperationType.DELETE).resource(ResourceType.REQUIRED_ACTION).resourcePath(session.getContext().getUri()).success();
    }

    /**
     * 提高 Required Action 优先级。
     *
     * @param alias Required Action 别名
     */
    @Path("required-actions/{alias}/raise-priority")
    @POST
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.AUTHENTICATION_MANAGEMENT)
    @Operation( summary = "Raise required action's priority")
    @APIResponse(responseCode = "204", description = "No Content")
    public void raiseRequiredActionPriority(@Parameter(description = "Alias of required action") @PathParam("alias") String alias) {
        auth.realm().requireManageRealm();

        RequiredActionProviderModel model = realm.getRequiredActionProviderByAlias(alias);
        if (model == null) {
            throw new NotFoundException("Failed to find required action.");
        }

        RequiredActionProviderModel previous = null;
        for (RequiredActionProviderModel action : realm.getRequiredActionProvidersStream().collect(Collectors.toList())) {
            if (action.getId().equals(model.getId())) {
                break;
            }
            previous = action;
        }
        if (previous == null) return;
        int tmp = previous.getPriority();
        previous.setPriority(model.getPriority());
        realm.updateRequiredActionProvider(previous);
        model.setPriority(tmp);
        realm.updateRequiredActionProvider(model);

        adminEvent.operation(OperationType.UPDATE).resource(ResourceType.REQUIRED_ACTION).resourcePath(session.getContext().getUri()).success();
    }

    /**
     * 降低 Required Action 优先级。
     *
     * @param alias Required Action 别名
     */
    @Path("/required-actions/{alias}/lower-priority")
    @POST
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.AUTHENTICATION_MANAGEMENT)
    @Operation( summary = "Lower required action's priority")
    @APIResponse(responseCode = "204", description = "No Content")
    public void lowerRequiredActionPriority(@Parameter(description = "Alias of required action") @PathParam("alias") String alias) {
        auth.realm().requireManageRealm();

        RequiredActionProviderModel model = realm.getRequiredActionProviderByAlias(alias);
        if (model == null) {
            throw new NotFoundException("Failed to find required action.");
        }

        List<RequiredActionProviderModel> actions = realm.getRequiredActionProvidersStream().collect(Collectors.toList());
        int i;
        for (i = 0; i < actions.size(); i++) {
            if (actions.get(i).getId().equals(model.getId())) {
                break;
            }
        }
        if (i + 1 >= actions.size()) return;
        RequiredActionProviderModel next = actions.get(i + 1);
        int tmp = model.getPriority();
        model.setPriority(next.getPriority());
        realm.updateRequiredActionProvider(model);
        next.setPriority(tmp);
        realm.updateRequiredActionProvider(next);

        adminEvent.operation(OperationType.UPDATE).resource(ResourceType.REQUIRED_ACTION).resourcePath(session.getContext().getUri()).success();
    }

    /**
     * 获取 Required Action 提供者的配置项描述（元数据）。
     * @param alias Required Action 别名
     * @return 配置描述表示
     */
    @Path("required-actions/{alias}/config-description")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.AUTHENTICATION_MANAGEMENT)
    @Operation( summary = "Get RequiredAction provider configuration description")
    public RequiredActionConfigInfoRepresentation getRequiredActionConfigDescription(@Parameter(description = "Alias of required action")  @PathParam("alias") String alias) {
        auth.realm().requireViewRealm();

        RequiredActionFactory factory = RequiredActionHelper.lookupConfigurableRequiredActionFactory(session, alias);
        if (factory == null) {
            throw new NotFoundException("Could not find configurable RequiredAction provider");
        }

        RequiredActionConfigInfoRepresentation rep = new RequiredActionConfigInfoRepresentation();
        rep.setProperties(new LinkedList<>());
        List<ProviderConfigProperty> configProperties = Optional.ofNullable(factory.getConfigMetadata()).orElse(Collections.emptyList());
        for (ProviderConfigProperty prop : configProperties) {
            ConfigPropertyRepresentation propRep = getConfigPropertyRep(prop);
            rep.getProperties().add(propRep);
        }
        return rep;
    }

    /**
     * 获取当前领域中 Required Action 的实际配置值。
     * @param alias Required Action 别名
     * @return 配置表示
     */
    @Path("required-actions/{alias}/config")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.AUTHENTICATION_MANAGEMENT)
    @Operation( summary = "Get RequiredAction configuration")
    public RequiredActionConfigRepresentation getRequiredActionConfig(@Parameter(description = "Alias of required action")  @PathParam("alias") String alias) {
        auth.realm().requireViewRealm();

        RequiredActionFactory factory = RequiredActionHelper.lookupConfigurableRequiredActionFactory(session, alias);
        if (factory == null) {
            throw new BadRequestException("RequiredAction is not configurable");
        }

        RequiredActionConfigModel config = realm.getRequiredActionConfigByAlias(alias);
        if (config == null) {
            throw new NotFoundException("Could not find RequiredAction config");
        }

        return ModelToRepresentation.toRepresentation(config);
    }

    /**
     * 删除 Required Action 在领域中的配置。
     * @param alias Required Action 别名
     */
    @Path("required-actions/{alias}/config")
    @DELETE
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.AUTHENTICATION_MANAGEMENT)
    @Operation( summary = "Delete RequiredAction configuration")
    @APIResponse(responseCode = "204", description = "No Content")
    public void removeRequiredActionConfig(@Parameter(description = "Alias of required action")  @PathParam("alias") String alias) {
        auth.realm().requireManageRealm();

        RequiredActionFactory factory = RequiredActionHelper.lookupConfigurableRequiredActionFactory(session, alias);
        if (factory == null) {
            throw new BadRequestException("RequiredAction is not configurable");
        }

        RequiredActionConfigModel config = realm.getRequiredActionConfigByAlias(alias);
        if (config == null) {
            throw new NotFoundException("Could not find RequiredAction config");
        }
        realm.removeRequiredActionProviderConfig(config);

        adminEvent.operation(OperationType.DELETE) //
                .resource(ResourceType.REQUIRED_ACTION_CONFIG) //
                .resourcePath(session.getContext().getUri()) //
                .success();
    }

    /**
     * 更新 Required Action 配置（含 User Profile 校验）。
     * @param alias Required Action 别名
     * @param rep 新配置 JSON
     */
    @Path("required-actions/{alias}/config")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.AUTHENTICATION_MANAGEMENT)
    @Operation( summary = "Update RequiredAction configuration")
    @APIResponse(responseCode = "204", description = "No Content")
    public void updateRequiredActionConfig(@Parameter(description = "Alias of required action")  @PathParam("alias") String alias, @Parameter(description = "JSON describing new state of required action configuration") RequiredActionConfigRepresentation rep) {
        auth.realm().requireManageRealm();

        RequiredActionFactory factory = RequiredActionHelper.lookupConfigurableRequiredActionFactory(session, alias);
        if (factory == null) {
            throw new BadRequestException("RequiredAction is not configurable");
        }

        RequiredActionConfigModel exists = realm.getRequiredActionConfigByAlias(alias);
        if (exists == null) {
            throw new NotFoundException("Could not find RequiredAction config");
        }

        exists.setConfig(RepresentationToModel.removeEmptyString(rep.getConfig()));
        try {
            realm.updateRequiredActionConfig(exists);
            adminEvent.operation(OperationType.UPDATE) //
                    .resource(ResourceType.REQUIRED_ACTION_CONFIG) //
                    .resourcePath(session.getContext().getUri()) //
                    .representation(rep) //
                    .success();
        } catch (ValidationException ve) {
            List<ErrorRepresentation> errorReps = ve.getErrors().stream().map(err -> new ErrorRepresentation(err.getAttribute(), err.getMessage(), err.getMessageParameters())).toList();
            throw ErrorResponse.errors(errorReps, Response.Status.BAD_REQUEST);
        }
    }

    /**
     * 获取认证器提供者的配置项描述。
     * @param providerId 认证器 provider ID（script 提供者可为 Base32 编码）
     * @return 配置描述表示
     */
    @Path("config-description/{providerId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.AUTHENTICATION_MANAGEMENT)
    @Operation( summary = "Get authenticator provider's configuration description")
    public AuthenticatorConfigInfoRepresentation getAuthenticatorConfigDescription(@PathParam("providerId") String providerId) {
        auth.realm().requireViewRealm();

        ConfigurableAuthenticatorFactory factory = CredentialHelper.getConfigurableAuthenticatorFactory(session, providerId);

        if (factory == null) {
            providerId = new String(Base32.decode(providerId));
            factory = CredentialHelper.getConfigurableAuthenticatorFactory(session, providerId);
        }

        if (factory == null) {
            throw new NotFoundException("Could not find authenticator provider");
        }

        AuthenticatorConfigInfoRepresentation rep = new AuthenticatorConfigInfoRepresentation();
        rep.setProviderId(providerId);
        rep.setName(factory.getDisplayType());
        rep.setHelpText(factory.getHelpText());
        rep.setProperties(new LinkedList<>());
        List<ProviderConfigProperty> configProperties = Optional.ofNullable(factory.getConfigProperties()).orElse(Collections.emptyList());
        for (ProviderConfigProperty prop : configProperties) {
            ConfigPropertyRepresentation propRep = getConfigPropertyRep(prop);
            rep.getProperties().add(propRep);
        }
        return rep;
    }

    /** 将 {@link ProviderConfigProperty} 转换为 REST 表示。 */
    private ConfigPropertyRepresentation getConfigPropertyRep(ProviderConfigProperty prop) {
        return ModelToRepresentation.toRepresentation(prop);
    }

    /**
     * 获取所有客户端认证器的 per-client 配置项描述。
     * @return providerId → 配置属性列表
     */
    @Path("per-client-config-description")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.AUTHENTICATION_MANAGEMENT)
    @Operation( summary = "Get configuration descriptions for all clients")
    public Map<String, List<ConfigPropertyRepresentation>> getPerClientConfigDescription() {
        auth.realm().requireViewClientAuthenticatorProviders();

        return session.getKeycloakSessionFactory().getProviderFactoriesStream(ClientAuthenticator.class)
                .collect(Collectors.toMap(
                        ProviderFactory::getId,
                        factory -> {
                            ClientAuthenticatorFactory clientAuthFactory = (ClientAuthenticatorFactory)
                                    CredentialHelper.getConfigurableAuthenticatorFactory(session, factory.getId());
                            return clientAuthFactory.getConfigPropertiesPerClient().stream()
                                    .map(this::getConfigPropertyRep).collect(Collectors.toList());
                        }));
    }

    /**
     * 创建独立认证器配置（已弃用）。
     * @param rep 配置 JSON
     * @return 201 Created
     * @deprecated 请改用 {@link #newExecutionConfig(String, AuthenticatorConfigRepresentation)}
     */
    @Path("config")
    @POST
    @NoCache
    @Consumes(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.AUTHENTICATION_MANAGEMENT)
    @Operation( summary = "Create new authenticator configuration", deprecated = true)
    @APIResponses(value = {
        @APIResponse(responseCode = "201", description = "Created"),
        @APIResponse(responseCode = "409", description = "Conflict")
    })
    @Deprecated
    public Response createAuthenticatorConfig(@Parameter(description = "JSON describing new authenticator configuration") AuthenticatorConfigRepresentation rep) {
        auth.realm().requireManageRealm();

        if (rep.getAlias() == null || rep.getAlias().isEmpty()) {
            throw ErrorResponse.exists("Failed to create authentication execution configuration with empty alias name");
        }

        if (realm.getAuthenticatorConfigByAlias(rep.getAlias()) != null) {
            throw ErrorResponse.exists("Authentication execution configuration " + rep.getAlias() + " already exists");
        }

        ReservedCharValidator.validate(rep.getAlias());

        AuthenticatorConfigModel config = realm.addAuthenticatorConfig(RepresentationToModel.toModel(rep));
        adminEvent.operation(OperationType.CREATE).resource(ResourceType.AUTHENTICATOR_CONFIG).resourcePath(session.getContext().getUri(), config.getId()).representation(rep).success();
        return Response.created(session.getContext().getUri().getAbsolutePathBuilder().path(config.getId()).build()).build();
    }

    /**
     * 按 ID 获取认证器配置。
     * @param id 配置 ID
     * @return 配置表示
     */
    @Path("config/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.AUTHENTICATION_MANAGEMENT)
    @Operation( summary = "Get authenticator configuration")
    public AuthenticatorConfigRepresentation getAuthenticatorConfig(@Parameter(description = "Configuration id") @PathParam("id") String id) {
        auth.realm().requireViewRealm();

        AuthenticatorConfigModel config = new DeployedConfigurationsManager(session).getAuthenticatorConfig(realm, id);
        if (config == null) {
            throw new NotFoundException("Could not find authenticator config");

        }
        return ModelToRepresentation.toRepresentation(config);
    }

    /**
     * 删除认证器配置并解除所有 execution 引用。
     * @param id 配置 ID
     */
    @Path("config/{id}")
    @DELETE
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.AUTHENTICATION_MANAGEMENT)
    @Operation( summary = "Delete authenticator configuration")
    @APIResponse(responseCode = "204", description = "No Content")
    public void removeAuthenticatorConfig(@Parameter(description = "Configuration id") @PathParam("id") String id) {
        auth.realm().requireManageRealm();

        AuthenticatorConfigModel config = realm.getAuthenticatorConfigById(id);
        if (config == null) {
            throw new NotFoundException("Could not find authenticator config");

        }
        realm.getAuthenticationFlowsStream().forEach(flow -> realm.getAuthenticationExecutionsStream(flow.getId())
                .filter(exe -> Objects.equals(id, exe.getAuthenticatorConfig()))
                .forEachOrdered(exe -> {
                    exe.setAuthenticatorConfig(null);
                    realm.updateAuthenticatorExecution(exe);
                }));

        realm.removeAuthenticatorConfig(config);

        adminEvent.operation(OperationType.DELETE).resource(ResourceType.AUTHENTICATOR_CONFIG).resourcePath(session.getContext().getUri()).success();
    }

    /**
     * 更新认证器配置别名与键值（已部署只读配置不可修改）。
     * @param id 配置 ID
     * @param rep 新配置 JSON
     */
    @Path("config/{id}")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @NoCache
    @Tag(name = KeycloakOpenAPI.Admin.Tags.AUTHENTICATION_MANAGEMENT)
    @Operation( summary = "Update authenticator configuration")
    @APIResponse(responseCode = "204", description = "No Content")
    public void updateAuthenticatorConfig(@Parameter(description = "Configuration id") @PathParam("id") String id, @Parameter(description = "JSON describing new state of authenticator configuration") AuthenticatorConfigRepresentation rep) {
        auth.realm().requireManageRealm();

        ReservedCharValidator.validate(rep.getAlias());
        if (new DeployedConfigurationsManager(session).getDeployedAuthenticatorConfig(id) != null) {
            throw new BadRequestException("Authenticator config is read-only");
        }

        AuthenticatorConfigModel exists = realm.getAuthenticatorConfigById(id);
        if (exists == null) {
            throw new NotFoundException("Could not find authenticator config");
        }

        exists.setAlias(rep.getAlias());
        exists.setConfig(RepresentationToModel.removeEmptyString(rep.getConfig()));
        realm.updateAuthenticatorConfig(exists);
        adminEvent.operation(OperationType.UPDATE).resource(ResourceType.AUTHENTICATOR_CONFIG).resourcePath(session.getContext().getUri()).representation(rep).success();
    }
}
