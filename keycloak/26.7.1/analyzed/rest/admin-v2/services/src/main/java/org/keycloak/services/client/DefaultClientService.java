package org.keycloak.services.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.annotation.Nonnull;
import jakarta.validation.groups.Default;
import jakarta.ws.rs.core.Response;

import org.keycloak.authorization.fgap.AdminPermissionsSchema;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.events.admin.v2.AdminEventV2Builder;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.mapper.ClientModelMapper;
import org.keycloak.models.mapper.ClientModelMappers;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.representations.admin.v2.BaseClientRepresentation;
import org.keycloak.representations.admin.v2.OIDCClientRepresentation;
import org.keycloak.representations.admin.v2.validation.CreateClient;
import org.keycloak.representations.admin.v2.validation.PatchClient;
import org.keycloak.representations.admin.v2.validation.PutClient;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.services.PatchType;
import org.keycloak.services.RolesService;
import org.keycloak.services.ServiceException;
import org.keycloak.services.client.query.ClientQueryEvaluator;
import org.keycloak.services.client.query.QueryParseUtils;
import org.keycloak.services.clientpolicy.ClientPolicyException;
import org.keycloak.services.clientpolicy.context.AdminClientRegisterContext;
import org.keycloak.services.clientpolicy.context.AdminClientRegisteredContext;
import org.keycloak.services.clientpolicy.context.AdminClientUnregisterContext;
import org.keycloak.services.clientpolicy.context.AdminClientUpdateContext;
import org.keycloak.services.clientpolicy.context.AdminClientUpdatedContext;
import org.keycloak.services.clientpolicy.context.AdminClientViewContext;
import org.keycloak.services.managers.ClientManager;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import org.keycloak.services.resources.admin.RoleContainerResource;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;
import org.keycloak.services.util.ObjectMapperResolver;
import org.keycloak.validation.ValidationUtil;
import org.keycloak.validation.jakarta.HibernateValidatorProvider;
import org.keycloak.validation.jakarta.JakartaValidatorProvider;
import org.keycloak.validation.jakarta.ValidationContext;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;

import static org.keycloak.representations.admin.v2.validators.ClientSecretNotBlankValidator.isClientSecret;
import static org.keycloak.utils.StreamsUtil.paginatedStream;
import static org.keycloak.utils.StringUtil.isBlank;

/**
 * Admin Client API v2 的默认客户端服务实现。
 */
public class DefaultClientService implements ClientService {
    private static final ObjectMapper MAPPER = new ObjectMapperResolver().getContext(null);
    private static final ClientModelMappers MAPPERS = new ClientModelMappers();

    private final KeycloakSession session;
    private final AdminPermissionEvaluator permissions;
    private final AdminEventBuilder adminEventBuilder;
    private final JakartaValidatorProvider validator;
    private final RolesService rolesService;

    public DefaultClientService(@Nonnull KeycloakSession session,
                                @Nonnull RealmModel realm,
                                @Nonnull AdminPermissionEvaluator permissions) {
        this.session = session;
        this.permissions = permissions;
        this.adminEventBuilder = new AdminEventV2Builder(realm, permissions.adminAuth(), session, session.getContext().getConnection()).resource(ResourceType.CLIENT);
        this.validator = new HibernateValidatorProvider(new ValidationContext(session, realm));
        this.rolesService = new RolesService(session, realm, permissions, adminEventBuilder);
    }

    @Override
    public Optional<BaseClientRepresentation> getClient(@Nonnull RealmModel realm,
                                                        @Nonnull String clientId) throws ServiceException {
        ClientModel client = realm.getClientByClientId(clientId);
        if (client == null) {
            return Optional.empty();
        }
        permissions.clients().requireView(client);
        
        try {
            session.clientPolicy().triggerOnEvent(new AdminClientViewContext(client, permissions.adminAuth()));
            return Optional.ofNullable(getMapper(client.getProtocol()).fromModel(client));
        } catch (ClientPolicyException e) {
            throw new ServiceException(e.getErrorDetail(), Response.Status.BAD_REQUEST);
        }
    }

    @Override
    public Stream<BaseClientRepresentation> getClients(@Nonnull RealmModel realm,
                                                       @Nonnull ClientProjectionOptions projectionOptions,
                                                       ClientSearchOptions searchOptions,
                                                       @Nonnull ClientSortAndSliceOptions sortAndSliceOptions) {
        permissions.clients().requireList();

        // TODO: 此检查较弱
        //  更严格的检查应确认剩余字段是否具有 repSetter
        //  多态下某字段在一子类型可投影、在另一子类型固定，此处可能暴露该问题
        //  be projectable in one subtype, but fixed in another

        projectionOptions.getFields().forEach(s -> {
            if (!MAPPERS.isKnownField(s)) {
                throw new ServiceException("%s is an unknown field".formatted(s), Response.Status.BAD_REQUEST);
            }
        });

        // 启用 FGAP 时，授权过滤在 JPA 层（PartialEvaluator 谓词）完成，可直接信任数据库结果。
        // 未启用时在内存中按 VIEW_CLIENTS 权限过滤。
        boolean canView = AdminPermissionsSchema.SCHEMA.isAdminPermissionsEnabled(realm) || permissions.clients().canView();
        boolean hasQuery = searchOptions != null && searchOptions.query() != null && !searchOptions.query().isBlank();
        boolean useJpaPagination = canView && !hasQuery;
        int offset = sortAndSliceOptions.offset();
        int limit = sortAndSliceOptions.limit();

        Comparator<BaseClientRepresentation> sortComparator = sortAndSliceOptions.getSortComparator();
        try {
            Stream<ClientModel> clientModels = useJpaPagination
                    ? realm.getClientsStream(offset, limit)
                    : realm.getClientsStream();

            Stream<BaseClientRepresentation> stream = clientModels
                    .filter(client -> canView || permissions.clients().canView(client))
                    .filter(client -> client.getProtocol() != null)
                    .map(client -> getMapper(client.getProtocol()).fromModel(client))
                    .filter(Objects::nonNull);

            stream = applySearchFilter(stream, searchOptions).sorted(sortComparator);
            if (!useJpaPagination) {
                stream = paginatedStream(stream, offset, limit);
            }
            return applyProjection(stream, projectionOptions);

        } catch (ModelException e) {
            throw new ServiceException(e.getMessage(), Response.Status.BAD_REQUEST);
        }
    }

    protected Stream<BaseClientRepresentation> applySearchFilter(Stream<BaseClientRepresentation> stream, ClientSearchOptions searchOptions) {
        if (searchOptions != null && searchOptions.query() != null && !searchOptions.query().isBlank()) {
            var queryCtx = QueryParseUtils.parse(searchOptions.query());
            QueryParseUtils.validate(queryCtx);
            return stream.filter(client -> ClientQueryEvaluator.matches(queryCtx, client));
        }
        return stream;
    }

    protected Stream<BaseClientRepresentation> applyProjection(Stream<BaseClientRepresentation> stream, ClientProjectionOptions projectionOptions) {
        if (projectionOptions.getFields().isEmpty()) return stream;
        return stream.map(rep -> {
            MAPPERS.applyProjection(rep, projectionOptions.getFields());
            return rep;
        });
    }

    @Override
    public BaseClientRepresentation createClient(RealmModel realm, BaseClientRepresentation client) throws ServiceException {
        return createOrUpdate(realm, null, client, CreateOrUpdateStrategy.ONLY_CREATE, false).representation();
    }

    @Override
    public CreateOrUpdateResult createOrUpdateClient(RealmModel realm, String clientId, BaseClientRepresentation client) throws ServiceException {
        return createOrUpdate(realm, clientId, client, CreateOrUpdateStrategy.PUT, false);
    }

    @Override
    public void deleteClient(RealmModel realm, String clientId) throws ServiceException {
        ClientModel client = realm.getClientByClientId(clientId);
        if (client == null) {
            throw new ServiceException("Could not find client", Response.Status.NOT_FOUND);
        }

        permissions.clients().requireManage(client);
        try {
            session.clientPolicy().triggerOnEvent(new AdminClientUnregisterContext(client, permissions.adminAuth()));
        } catch (ClientPolicyException e) {
            throw new ServiceException(e.getErrorDetail(), Response.Status.BAD_REQUEST);
        }

        var clientRepresentation = Optional.ofNullable(getMapper(client.getProtocol()).fromModel(client))
                .orElseThrow(() -> new ServiceException("Cannot map client model", Response.Status.BAD_REQUEST));

        if (new ClientManager(new RealmManager(session)).removeClient(realm, client)) {
            fireAdminEvent(OperationType.DELETE, clientRepresentation);
        } else {
            throw new ServiceException("Could not delete client", Response.Status.BAD_REQUEST);
        }
    }

    @Override
    public BaseClientRepresentation patchClient(RealmModel realm, String clientId, PatchType patchType, InputStream patch) throws ServiceException {
        Supplier<BaseClientRepresentation> getOriginalClient = () -> getClient(realm, clientId)
                .orElseThrow(() -> new ServiceException("Cannot find the specified client", Response.Status.NOT_FOUND));

        BaseClientRepresentation updated;
        boolean patchExplicitNullSecret = false;
        switch (patchType) {
            case JSON_MERGE -> {
                final byte[] patchData;
                try {
                    patchData = patch.readAllBytes();
                } catch (IOException e) {
                    throw new ServiceException("Unknown Error Occurred", Response.Status.INTERNAL_SERVER_ERROR);
                }
                try {
                    JsonNode root = MAPPER.readTree(patchData);
                    JsonNode authNode = root.get("auth");
                    if (authNode != null && authNode.has("secret") && authNode.get("secret").isNull()) {
                        patchExplicitNullSecret = true;
                    }
                } catch (IOException e) {
                    throw new ServiceException(e.getMessage(), Response.Status.BAD_REQUEST);
                }
                try (JsonParser parser = MAPPER.getFactory().createParser(patchData)) {
                    final ObjectReader objectReader = MAPPER.readerForUpdating(getOriginalClient.get());
                    JsonToken nextToken = parser.nextToken();
                    if (nextToken != JsonToken.START_OBJECT) {
                        throw new ServiceException("Cannot replace client resource with non-object", Response.Status.BAD_REQUEST);
                    }
                    updated = objectReader.readValue(parser);
                    if (parser.nextToken() != null) {
                        throw new ServiceException("Patch contains additional content", Response.Status.BAD_REQUEST);
                    }
                } catch (JsonMappingException e) {
                    var invalidFields = e.getPath().stream().map(JsonMappingException.Reference::getFieldName).collect(Collectors.joining(", "));
                    throw new ServiceException("Invalid values for these fields: %s".formatted((invalidFields)));
                } catch (JsonProcessingException e) {
                    throw new ServiceException(e.getMessage(), Response.Status.BAD_REQUEST);
                } catch (IOException e) {
                    throw new ServiceException("Unknown Error Occurred", Response.Status.INTERNAL_SERVER_ERROR);
                }
            }
            default -> throw new ServiceException("Invalid patch type", Response.Status.UNSUPPORTED_MEDIA_TYPE);
        }

        return createOrUpdate(realm, clientId, updated, CreateOrUpdateStrategy.PATCH, patchExplicitNullSecret).representation();
    }

    @Override
    public Stream<BaseClientRepresentation> deleteClients(RealmModel realm, ClientSearchOptions searchOptions) {
        // TODO Auto-generated method stub
        return null;
    }

    protected enum CreateOrUpdateStrategy {
        ONLY_CREATE(CreateClient.class),
        PUT(PutClient.class),
        PATCH(PatchClient.class);

        private final Class<?> validationGroup;

        CreateOrUpdateStrategy(Class<?> validationGroup) {
            this.validationGroup = validationGroup;
        }

        public Class<?> getValidationGroup() {
            return validationGroup;
        }
    }

    private CreateOrUpdateResult createOrUpdate(RealmModel realm, String clientId, BaseClientRepresentation client, CreateOrUpdateStrategy strategy, boolean patchExplicitNullSecret) throws ServiceException {
        validateUnknownFields(client);
        ClientModel model = null;
        if (!strategy.equals(CreateOrUpdateStrategy.ONLY_CREATE)) {
            assertSameClientIds(clientId, client.getClientId());
            model = realm.getClientByClientId(clientId);
        }
        boolean alreadyExists = model != null;
        if (isBlank(client.getProtocol())) {
            throw new ServiceException("protocol is required", Response.Status.BAD_REQUEST);
        }
        ClientModelMapper mapper = getMapper(client.getProtocol());

        try {
            if (alreadyExists) {
                switch (strategy) {
                    case ONLY_CREATE -> throw new ServiceException("Client already exists", Response.Status.CONFLICT);
                    case PUT, PATCH -> {
                        // Check permissions, execute validations and trigger client policies
                        permissions.clients().requireConfigure(model);
                        // Must run before bean validation: PutClient requires a non-blank secret for client-secret methods
                        generateClientSecretIfNeeded(client, model, strategy, patchExplicitNullSecret);
                        validator.validate(client, strategy.getValidationGroup(), Default.class);
                        var proposedRepresentation = getProposedOldRepresentation(realm, client, mapper);
                        session.clientPolicy().triggerOnEvent(new AdminClientUpdateContext(proposedRepresentation, model, permissions.adminAuth()));

                        // Update model
                        mapper.toModel(client, model);

                        // Validate the fully populated model
                        ValidationUtil.validateClient(session, model, false, r -> {
                            session.getTransactionManager().setRollbackOnly();
                            throw new ServiceException(r.getAllErrorsAsString(), Response.Status.BAD_REQUEST);
                        });

                        session.clientPolicy().triggerOnEvent(new AdminClientUpdatedContext(proposedRepresentation, model, permissions.adminAuth()));
                    }
                }
            } else {
                // Check permissions, execute validations and trigger client policies
                permissions.clients().requireManage();
                validator.validate(client, strategy.getValidationGroup(), Default.class);
                var proposedRepresentation = getProposedOldRepresentation(realm, client, mapper);
                session.clientPolicy().triggerOnEvent(new AdminClientRegisterContext(proposedRepresentation, permissions.adminAuth()));

                // Add basic attributes
                model = realm.addClient(clientId);
                model.setProtocol(client.getProtocol());

                // Generate random secret if applicable
                generateClientSecretIfNeeded(client, model, strategy, patchExplicitNullSecret);
                mapper.toModel(client, model);

                // Validate the fully populated model
                ValidationUtil.validateClient(session, model, true, r -> {
                    session.getTransactionManager().setRollbackOnly();
                    throw new ServiceException(r.getAllErrorsAsString(), Response.Status.BAD_REQUEST);
                });
                session.clientPolicy().triggerOnEvent(new AdminClientRegisteredContext(model, permissions.adminAuth()));
            }
        } catch (ClientPolicyException e) {
            throw new ServiceException(e.getErrorDetail(), Response.Status.BAD_REQUEST);
        }

        // Setup roles
        var clientRoles = rolesService.resource(model);
        handleRoles(clientRoles, client.getRoles());

        // OIDC specific
        if (client instanceof OIDCClientRepresentation oidcClient) {
            handleServiceAccount(model, oidcClient);
        }

        fireAdminEvent(alreadyExists ? OperationType.UPDATE : OperationType.CREATE, mapper.fromModel(model));
        return new CreateOrUpdateResult(mapper.fromModel(model), !alreadyExists);
    }

    /**
     * 触发客户端操作的 v2 管理事件（默认关闭，避免与 v1 重复）。
     *
     * @param operationType  操作类型（CREATE、UPDATE、DELETE）
     * @param representation 客户端 v2 表示
     */
    protected void fireAdminEvent(OperationType operationType, BaseClientRepresentation representation) {
        if (Boolean.parseBoolean(System.getProperty("kc.admin-v2.client-service.events.enabled", "false"))) {
            adminEventBuilder
                    .operation(operationType)
                    .resourcePath(session.getContext().getUri())
                    .representation(representation)
                    .success();
        }
    }

    /**
     * 创建临时客户端，将 {@link BaseClientRepresentation} 转为 v1 {@link ClientRepresentation}。
     * 客户端策略上下文需要 v1 表示，而 v2 API 无直接转换器；临时客户端随即删除。
     * <p>
     * 详见 <a href="https://github.com/keycloak/keycloak/issues/47576">keycloak#47576</a>。
     */
    private ClientRepresentation getProposedOldRepresentation(RealmModel realm, BaseClientRepresentation client, ClientModelMapper mapper) {
        String tempId = "__temp__" + client.getClientId() + "__" + System.nanoTime();
        ClientModel tempModel = realm.addClient(tempId);
        String clientId = client.getClientId();
        mapper.toModel(client, tempModel);
        try {
            var proposedRepresentation = ModelToRepresentation.toRepresentation(tempModel, session);
            proposedRepresentation.setClientId(clientId);
            return proposedRepresentation;
        } finally {
            realm.removeClient(tempModel.getId());
        }
    }

    private void generateClientSecretIfNeeded(BaseClientRepresentation client, ClientModel model, CreateOrUpdateStrategy strategy, boolean patchExplicitNullSecret) {
        if (client instanceof OIDCClientRepresentation oidcClient
                && OIDCClientRepresentation.PROTOCOL.equals(client.getProtocol())) {
            var auth = oidcClient.getAuth();
            if (auth != null && isClientSecret(auth.getMethod()) && isBlank(auth.getSecret())) {
                if (strategy == CreateOrUpdateStrategy.PATCH && patchExplicitNullSecret) {
                    auth.setSecret(KeycloakModelUtils.generateSecret(model));
                } else {
                    // PUT 时常省略 secret；Bean 校验（PutClient）前复用已持久化的 secret。
                    // PATCH 未显式 JSON null secret 时保持相同语义（不轮换）。
                    if (!isBlank(model.getSecret())) {
                        auth.setSecret(model.getSecret());
                    } else {
                        auth.setSecret(KeycloakModelUtils.generateSecret(model));
                    }
                }
            }
        }
    }

    protected void assertSameClientIds(String pathId, String payloadId) {
        if (payloadId == null) {
            // payload 中 clientId 为 null 表示未包含该字段，由 @NotBlank 校验
            return;
        }
        if (!Objects.equals(pathId, payloadId)) {
            throw new ServiceException("Field 'clientId' in payload does not match the provided 'clientId'", Response.Status.BAD_REQUEST);
        }
    }

    /**
     * 声明式管理客户端角色，使客户端角色集与表示中的 {@code roles} 完全一致。
     * <p>
     * 复用 API v1 逻辑。
     */
    protected void handleRoles(RoleContainerResource clientRoles, Set<String> rolesFromRep) {
        Set<String> desiredRoleNames = Optional.ofNullable(rolesFromRep)
                .orElse(Collections.emptySet());

        Set<String> currentRoleNames = clientRoles.getRoles(null, null, null, false)
                .map(RoleRepresentation::getName)
                .collect(Collectors.toSet());

        // 添加缺失角色（期望有而当前无）
        desiredRoleNames.stream()
                .filter(roleName -> !currentRoleNames.contains(roleName))
                .forEach(roleName -> {
                    try (var response = clientRoles.createRole(new RoleRepresentation(roleName, "", false))) {
                        // 关闭响应并消费 payload，避免性能问题
                        EntityUtils.consumeQuietly((HttpEntity) response.getEntity());
                    }
                });

        // 移除多余角色（当前有而期望无）
        currentRoleNames.stream()
                .filter(role -> !desiredRoleNames.contains(role))
                .forEach(clientRoles::deleteRole);
    }

    /**
     * 声明式管理服务账户：启用/禁用并同步领域与客户端角色映射。
     * <p>
     * 在 {@link UserModel} 上应用与 Admin REST 角色映射资源相同的权限检查，但不经过嵌套 JAX-RS 资源（不适合进程内服务调用）。
     */
    protected void handleServiceAccount(ClientModel model, OIDCClientRepresentation rep) {
        boolean serviceAccountEnabled = rep.getLoginFlows().contains(OIDCClientRepresentation.Flow.SERVICE_ACCOUNT);

        ClientManager.updateClientServiceAccount(session, model, serviceAccountEnabled);

        if (!serviceAccountEnabled) {
            return;
        }

        UserModel serviceAccountUser = new ClientManager(new RealmManager(session)).getServiceAccountUser(model)
                .orElseThrow(() -> new ServiceException("Cannot find service account user", Response.Status.BAD_REQUEST));

        RealmModel realm = model.getRealm();
        Set<String> desiredRoleNames = Optional.ofNullable(rep.getServiceAccountRoles()).orElse(Collections.emptySet());
        Set<RoleModel> currentRoles = serviceAccountUser.getRoleMappingsStream().collect(Collectors.toSet());
        Set<String> currentRoleNames = currentRoles.stream().map(RoleModel::getName).collect(Collectors.toSet());

        // serviceAccountRoles 为纯名称；同名时优先解析本客户端角色，再解析领域角色。
        List<RoleModel> rolesToAdd = new ArrayList<>();
        for (String roleName : desiredRoleNames) {
            if (currentRoleNames.contains(roleName)) {
                continue;
            }
            RoleModel clientRole = model.getRole(roleName);
            RoleModel resolved = clientRole != null ? clientRole : realm.getRole(roleName);
            if (resolved == null) {
                throw new ServiceException("Cannot assign role to the service account (field 'serviceAccount.roles') as it does not exist", Response.Status.BAD_REQUEST);
            }
            rolesToAdd.add(resolved);
        }

        List<RoleModel> rolesToRemove = new ArrayList<>();
        for (RoleModel role : currentRoles) {
            if (!desiredRoleNames.contains(role.getName())) {
                rolesToRemove.add(role);
            }
        }

        if (rolesToAdd.isEmpty() && rolesToRemove.isEmpty()) {
            return;
        }

        permissions.users().requireMapRoles(serviceAccountUser);
        for (RoleModel role : rolesToAdd) {
            permissions.roles().requireMapRole(role);
            serviceAccountUser.grantRole(role);
        }
        for (RoleModel role : rolesToRemove) {
            permissions.roles().requireMapRole(role);
            serviceAccountUser.deleteRoleMapping(role);
        }
    }

    protected void validateUnknownFields(BaseClientRepresentation rep) {
        if (!rep.getAdditionalFields().isEmpty()) {
            throw new ServiceException("Payload contains unknown fields: " + rep.getAdditionalFields().keySet(), Response.Status.BAD_REQUEST);
        }
    }

    public ClientModelMapper getMapper(String protocol) {
        return MAPPERS.getMapper(protocol).orElseThrow(() -> new ServiceException("Mapper not found, unsupported client protocol: " + protocol,
                Response.Status.BAD_REQUEST));
    }
}
