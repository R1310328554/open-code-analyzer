package org.keycloak.protocol.oauth2.cimd.provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.keycloak.OAuthErrorException;
import org.keycloak.common.util.Time;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.models.utils.RepresentationToModel;
import org.keycloak.protocol.oauth2.cimd.clientpolicy.context.CimdClientRegisterContext;
import org.keycloak.protocol.oauth2.cimd.clientpolicy.context.CimdClientRegisteredContext;
import org.keycloak.protocol.oauth2.cimd.clientpolicy.context.CimdClientUpdateContext;
import org.keycloak.protocol.oauth2.cimd.clientpolicy.context.CimdClientUpdatedContext;
import org.keycloak.protocol.oauth2.cimd.clientpolicy.executor.AbstractClientIdMetadataDocumentExecutor;
import org.keycloak.protocol.oidc.OIDCLoginProtocolFactory;
import org.keycloak.protocol.oidc.mappers.AbstractPairwiseSubMapper;
import org.keycloak.protocol.oidc.mappers.PairwiseSubMapperHelper;
import org.keycloak.protocol.oidc.mappers.SHA256PairwiseSubMapper;
import org.keycloak.protocol.oidc.utils.SubjectType;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.ProtocolMapperRepresentation;
import org.keycloak.representations.oidc.OIDCClientRepresentation;
import org.keycloak.services.clientpolicy.ClientPolicyException;
import org.keycloak.services.clientregistration.ErrorCodes;
import org.keycloak.services.clientregistration.oidc.DescriptionConverter;
import org.keycloak.services.managers.ClientManager;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.services.resources.admin.ClientResource;
import org.keycloak.validation.ClientValidationContext;
import org.keycloak.validation.ClientValidationProvider;
import org.keycloak.validation.ValidationContext;
import org.keycloak.validation.ValidationResult;

import org.jboss.logging.Logger;

/**
 * 持久化客户端元数据的抽象 CIMD Provider。
 * <p>创建与更新流程与动态客户端注册（DCR）中的 {@code OIDCClientRegistrationProvider} 基本一致；
 * 差异仅在于元数据获取方式：DCR 由客户端提交（RFC 7591），CIMD 由授权服务器拉取。</p>
 * <p>未直接复用 {@code OIDCClientRegistrationProvider} 的原因：</p>
 * <ul>
 *     <li>DCR 不允许 {@code client_id}，CIMD 则强制要求。</li>
 *     <li>DCR（RFC 7592）会签发注册访问令牌，CIMD 不需要。</li>
 * </ul>
 * <p>缓存过期时间保存在 {@link ClientRepresentation}/{@link ClientModel} 属性中；
 * 过期后当前不做删除，未来可结合客户端工作流处理。</p>
 * <p>抽象类负责持久化通用逻辑；具体子类可读取 {@link AbstractClientIdMetadataDocumentExecutor} 配置并增强元数据，便于自定义 Provider。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public abstract class AbstractPersistentClientIdMetadataDocumentProvider<CONFIG extends AbstractClientIdMetadataDocumentExecutor.Configuration> implements ClientIdMetadataDocumentProvider<CONFIG> {

    protected KeycloakSession session;
    protected CONFIG configuration;

    /** 客户端元数据缓存过期时间（Unix 秒）属性键。 */
    public static final String CIMD_CACHE_EXPIRY_TIME_IN_SEC = "cimd.cache.expiry.time.in.sec";

    /** @return 子类使用的日志记录器 */
    protected abstract Logger getLogger();

    /** @param session Keycloak 会话 */
    protected AbstractPersistentClientIdMetadataDocumentProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void setCacheExpiryTimeToClientMetadata(ClientRepresentation clientRep, int cacheExpiryTimeInSec) {
        clientRep.getAttributes().put(CIMD_CACHE_EXPIRY_TIME_IN_SEC, Integer.toString(cacheExpiryTimeInSec));
    }

    @Override
    public void setCacheExpiryTimeToClientMetadata(ClientModel clientModel, int cacheExpiryTimeInSec) {
        clientModel.setAttribute(CIMD_CACHE_EXPIRY_TIME_IN_SEC, Integer.toString(cacheExpiryTimeInSec));
    }

    @Override
    public AbstractClientIdMetadataDocumentExecutor.FetchOperation determineFetchOperation(String clientId) {
        RealmModel realm = session.getContext().getRealm();
        ClientModel existingClientModel = realm.getClientByClientId(clientId);
        if (existingClientModel != null) {
            getLogger().debugv("client already exist: clientId = {0}", clientId);
            // 客户端元数据缓存：仍有效则跳过；否则拉取、校验并覆盖持久化
            // TODO：出错时是否删除元数据？当前保留，未来可由工作流自动清理，仅返回错误
            if (existingClientModel.getAttribute(CIMD_CACHE_EXPIRY_TIME_IN_SEC) != null) {
                int i = Integer.parseInt(existingClientModel.getAttribute(CIMD_CACHE_EXPIRY_TIME_IN_SEC));
                if (Time.currentTime() > i) {
                    getLogger().debugv("client need to update: clientId = {0}", clientId);
                    return AbstractClientIdMetadataDocumentExecutor.FetchOperation.UPDATE;
                } else {
                    // 已持久化的元数据仍在缓存有效期内
                    getLogger().debugv("client no need to update: clientId = {0}", clientId);
                    return AbstractClientIdMetadataDocumentExecutor.FetchOperation.NO_UPDATE;
                }
            }
        }
        getLogger().debugv("client need to create: clientId = {0}", clientId);
        return AbstractClientIdMetadataDocumentExecutor.FetchOperation.CREATE;
    }

    @Override
    public ClientModel createClientMetadata(AbstractClientIdMetadataDocumentExecutor.OIDCClientRepresentationWithCacheControl clientOIDCWithCacheControl) throws ClientPolicyException {
        // 与动态客户端注册流程相同，但不设置注册访问令牌
        RealmModel realm = session.getContext().getRealm();
        try {
            OIDCClientRepresentation clientOIDC = clientOIDCWithCacheControl.getOidcClientRepresentation();
            ClientRepresentation clientRep = DescriptionConverter.toInternal(session, clientOIDC);

            // 写入缓存过期时间
            setCacheExpiryTimeToClientMetadata(clientRep, clientOIDCWithCacheControl.getClientMetadataCacheControl().getCacheExpiryTimeInSec());

            // 按 CIMD 执行器配置增强客户端元数据
            augmentClientMetadata(clientRep);

            if (clientRep.getOptionalClientScopes() != null && clientRep.getDefaultClientScopes() == null) {
                clientRep.setDefaultClientScopes(List.of(OIDCLoginProtocolFactory.BASIC_SCOPE));
            }

            EventBuilder event = new EventBuilder(realm, session, session.getContext().getConnection());
            event.event(EventType.CLIENT_REGISTER);
            session.clientPolicy().triggerOnEvent(new CimdClientRegisterContext(clientRep));
            ClientModel clientModel = ClientManager.createClient(session, realm, clientRep);

            if (clientRep.getDefaultRoles() != null) {
                for (String name : clientRep.getDefaultRoles()) {
                    addDefaultRole(clientModel, name);
                }
            }

            if (clientModel.isServiceAccountsEnabled()) {
                new ClientManager(new RealmManager(session)).enableServiceAccount(clientModel);
            }

            if (Boolean.TRUE.equals(clientRep.getAuthorizationServicesEnabled())) {
                RepresentationToModel.createResourceServer(clientModel, session, true);
            }

            session.getContext().setClient(clientModel);
            session.clientPolicy().triggerOnEvent(new CimdClientRegisteredContext(clientModel));

            clientRep = ModelToRepresentation.toRepresentation(clientModel, session);

            clientRep.setDirectAccessGrantsEnabled(clientModel.isDirectAccessGrantsEnabled());

            Stream<String> defaultRolesNames = getDefaultRolesStream(clientModel);
            if (defaultRolesNames != null) {
                clientRep.setDefaultRoles(defaultRolesNames.toArray(String[]::new));
            }

            event.client(clientRep.getClientId()).success();

            clientModel = realm.getClientByClientId(clientRep.getClientId());
            updatePairwiseSubMappers(clientModel, SubjectType.parse(clientOIDC.getSubjectType()), clientOIDC.getSectorIdentifierUri());
            updateClientRepWithProtocolMappers(clientModel, clientRep);

            validateClient(clientModel, clientOIDC, true);

            return clientModel;
        } catch (ModelDuplicateException e) {
            getLogger().warnv("ModelDuplicateException: {0}", e);
            throw new ClientPolicyException(ErrorCodes.INVALID_CLIENT_METADATA, "Client Identifier in use");
        } catch (ClientPolicyException e) {
            throw e; // intentionally
        } catch (Exception e) {
            getLogger().warnv("Exception: {0}", e);
            throw invalidClientMetadata("invalid request");
        }
    }

    @Override
    public ClientModel updateClientMetadata(AbstractClientIdMetadataDocumentExecutor.OIDCClientRepresentationWithCacheControl clientOIDCWithCacheControl) throws ClientPolicyException {
        // do the same thing as in dynamic client registration except for:
        //   - not set client registration token
        RealmModel realm = session.getContext().getRealm();

        try {
            OIDCClientRepresentation clientOIDC = clientOIDCWithCacheControl.getOidcClientRepresentation();
            ClientRepresentation clientRep = DescriptionConverter.toInternal(session, clientOIDC);
            String clientId = clientOIDC.getClientId();

            // set cache expiry time
            setCacheExpiryTimeToClientMetadata(clientRep, clientOIDCWithCacheControl.getClientMetadataCacheControl().getCacheExpiryTimeInSec());

            // 按配置增强客户端元数据
            augmentClientMetadata(clientRep);

            if (clientOIDC.getScope() != null) {
                ClientModel oldClient = realm.getClientByClientId(clientId);
                Collection<String> defaultClientScopes = oldClient.getClientScopes(true).keySet();
                clientRep.setDefaultClientScopes(new ArrayList<>(defaultClientScopes));
            }

            EventBuilder event = new EventBuilder(realm, session, session.getContext().getConnection());
            event.event(EventType.CLIENT_UPDATE).client(clientId);

            ClientModel clientModel = realm.getClientByClientId(clientId);

            if (!clientModel.getClientId().equals(clientRep.getClientId())) {
                throw invalidClientMetadata("Client Identifier modified");
            }

            session.clientPolicy().triggerOnEvent(new CimdClientUpdateContext(clientRep, clientModel));

            ClientResource.updateClientServiceAccount(session, clientModel, clientRep.isServiceAccountsEnabled());
            RepresentationToModel.updateClient(clientRep, clientModel, session);
            RepresentationToModel.updateClientProtocolMappers(clientRep, clientModel);
            RepresentationToModel.updateClientScopes(clientRep, clientModel);

            clientRep = ModelToRepresentation.toRepresentation(clientModel, session);

            Stream<String> defaultRolesNames = getDefaultRolesStream(clientModel);
            if (defaultRolesNames != null) {
                clientRep.setDefaultRoles(defaultRolesNames.toArray(String[]::new));
            }

            event.client(clientRep.getClientId()).success();

            session.getContext().setClient(clientModel);
            session.clientPolicy().triggerOnEvent(new CimdClientUpdatedContext(clientModel));

            clientModel = realm.getClientByClientId(clientRep.getClientId());
            updatePairwiseSubMappers(clientModel, SubjectType.parse(clientOIDC.getSubjectType()), clientOIDC.getSectorIdentifierUri());
            updateClientRepWithProtocolMappers(clientModel, clientRep);

            validateClient(clientModel, clientOIDC, false);

            return clientModel;
        } catch (ClientPolicyException e) {
            throw e; // intentionally
        } catch (Exception e) {
            getLogger().warnv("Exception: {0}", e);
            throw invalidClientMetadata("invalid request");
        }
    }

    private static ClientPolicyException invalidClientMetadata(String errorDetail) {
        return new ClientPolicyException(OAuthErrorException.INVALID_REQUEST, errorDetail);
    }

    // 与 AbstractClientRegistrationProvider.addDefaultRole 相同
    private void addDefaultRole(ClientModel client, String name) {
        client.getRealm().getDefaultRole().addCompositeRole(getOrAddRoleId(client, name));
    }

    // 与 AbstractClientRegistrationProvider.getOrAddRoleId 相同
    private RoleModel getOrAddRoleId(ClientModel client, String name) {
        RoleModel role = client.getRole(name);
        if (role == null) {
            role = client.addRole(name);
        }
        return role;
    }

    // 与 AbstractClientRegistrationProvider.getDefaultRolesStream 相同
    private Stream<String> getDefaultRolesStream(ClientModel client) {
        return client.getRealm().getDefaultRole().getCompositesStream()
                .filter(role -> role.isClientRole() && Objects.equals(role.getContainerId(), client.getId()))
                .map(RoleModel::getName);
    }

    // 与 OIDCClientRegistrationProvider.updatePairwiseSubMappers 相同
    private void updatePairwiseSubMappers(ClientModel clientModel, SubjectType subjectType, String sectorIdentifierUri) {
        if (subjectType == SubjectType.PAIRWISE) {

            // 更新已有 pairwise 映射器，不存在则新建
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

            // 无现有 pairwise 映射器时创建
            if (!foundPairwise.get()) {
                ProtocolMapperRepresentation newPairwise = SHA256PairwiseSubMapper.createPairwiseMapper(sectorIdentifierUri, null);
                clientModel.addProtocolMapper(RepresentationToModel.toModel(newPairwise));
            }

        } else {
            // 非 pairwise 主题类型时移除所有 pairwise 映射器
            clientModel.getProtocolMappersStream()
                    .filter(mapperRep -> mapperRep.getProtocolMapper().endsWith(AbstractPairwiseSubMapper.PROVIDER_ID_SUFFIX))
                    .toList()
                    .forEach(clientModel::removeProtocolMapper);
        }
    }

    // 与 OIDCClientRegistrationProvider.updateClientRepWithProtocolMappers 相同
    private void updateClientRepWithProtocolMappers(ClientModel clientModel, ClientRepresentation rep) {
        List<ProtocolMapperRepresentation> mappings =
                clientModel.getProtocolMappersStream().map(ModelToRepresentation::toRepresentation).collect(Collectors.toList());
        rep.setProtocolMappers(mappings);
    }

    // 与 AbstractClientRegistrationProvider 的校验逻辑相同，错误时抛出 ClientPolicyException
    private void validateClient(ClientModel client, OIDCClientRepresentation oidcClient, boolean create) throws ClientPolicyException{
        ClientValidationProvider provider = session.getProvider(ClientValidationProvider.class);
        if (provider != null) {
            ValidationContext.Event event = create ? ValidationContext.Event.CREATE : ValidationContext.Event.UPDATE;
            ValidationResult result;

            if (oidcClient != null) {
                result = provider.validate(new ClientValidationContext.OIDCContext(event, session, client, oidcClient));
            }
            else {
                result = provider.validate(new ClientValidationContext(event, session, client));
            }

            if (!result.isValid()) {
                getLogger().warnv("validateClient failed: {0}", result.getAllErrorsAsString());
                session.getTransactionManager().setRollbackOnly();
                throw new ClientPolicyException(OAuthErrorException.INVALID_REQUEST, "invalid request");
            }
        }
    }
}
