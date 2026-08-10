package org.keycloak.services.resources.account;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.authentication.AuthenticatorUtil;
import org.keycloak.authentication.requiredactions.util.CredentialDeleteHelper;
import org.keycloak.credential.CredentialMetadata;
import org.keycloak.credential.CredentialModel;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.credential.CredentialTypeMetadata;
import org.keycloak.credential.CredentialTypeMetadataContext;
import org.keycloak.events.Details;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.models.AccountRoles;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticationFlowModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.SubjectCredentialManager;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.OTPCredentialModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.protocol.oidc.utils.AcrUtils;
import org.keycloak.representations.account.CredentialMetadataRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.managers.Auth;
import org.keycloak.services.messages.Messages;
import org.keycloak.util.JsonSerialization;
import org.keycloak.utils.MediaType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.NoCache;

import static org.keycloak.models.AuthenticationExecutionModel.Requirement.DISABLED;
import static org.keycloak.utils.CredentialHelper.createUserStorageCredentialRepresentation;

/**
 * 账户 REST API 凭证管理子资源。
 * <p>列出可用凭证类型、删除凭证及更新用户标签。</p>
 */
public class AccountCredentialResource {

    /** 查询参数：按凭证类型过滤 */
    public static final String TYPE = "type";
    /** 查询参数：是否返回用户已有凭证 */
    public static final String USER_CREDENTIALS = "user-credentials";

    /** 日志记录器 */
    private static final Logger logger = Logger.getLogger(AccountCredentialResource.class);


    /** Keycloak 会话 */
    private final KeycloakSession session;
    /** 当前登录用户 */
    private final UserModel user;
    /** 当前领域 */
    private final RealmModel realm;
    /** 认证上下文 */
    private Auth auth;
    /** 事件构建器 */
    private final EventBuilder event;

    /** 构造凭证资源 */
    public AccountCredentialResource(KeycloakSession session, UserModel user, Auth auth, EventBuilder event) {
        this.session = session;
        this.user = user;
        this.auth = auth;
        this.event = event;
        realm = session.getContext().getRealm();
    }


    /** 凭证类型及其元数据与用户凭证列表的容器 */
    public static class CredentialContainer {
        // ** category、displayName、helptext 可为普通 UI 文本或本地化消息键
        //    a localized message bundle.  Typically, it will be a key, but
        //    the UI will work just fine if you don't care about localization
        //    and you want to just send UI text.
        //
        //    Also, the ${} shown in Apicurio is not needed.
        private String type;
        private String category; // **
        private String displayName;
        private String helptext;  // **
        private String iconCssClass;
        private String createAction;
        private String updateAction;
        private boolean removeable;
        private List<CredentialMetadataRepresentation> userCredentialMetadatas;
        private CredentialTypeMetadata metadata;

        public CredentialContainer() {
        }

        public CredentialContainer(CredentialTypeMetadata metadata, List<CredentialMetadataRepresentation> userCredentialMetadatas) {
            this.metadata = metadata;
            this.type = metadata.getType();
            this.category = metadata.getCategory().toString();
            this.displayName = metadata.getDisplayName();
            this.helptext = metadata.getHelpText();
            this.iconCssClass = metadata.getIconCssClass();
            this.createAction = metadata.getCreateAction();
            this.updateAction = metadata.getUpdateAction();
            this.removeable = metadata.isRemoveable();
            this.userCredentialMetadatas = userCredentialMetadatas;
        }

        public String getCategory() {
            return category;
        }

        public String getType() {
            return type;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getHelptext() {
            return helptext;
        }

        public String getIconCssClass() {
            return iconCssClass;
        }

        public String getCreateAction() {
            return createAction;
        }

        public String getUpdateAction() {
            return updateAction;
        }

        public boolean isRemoveable() {
            return removeable;
        }

        public List<CredentialMetadataRepresentation> getUserCredentialMetadatas() {
            return userCredentialMetadatas;
        }

        @JsonIgnore
        public CredentialTypeMetadata getMetadata() {
            return metadata;
        }
    }


    /**
     * 获取当前用户可用的凭证类型流。
     * <p>仅返回认证流中已启用且用户可用于登录的凭证类型。</p>
     *
     * @param type 按凭证类型过滤；为 null 时返回全部类型
     * @param userCredentials 是否在各类型的 userCredentialMetadatas 中返回用户凭证；默认 true
     * @return 凭证容器流
     */
    @GET
    @NoCache
    @Produces(jakarta.ws.rs.core.MediaType.APPLICATION_JSON)
    public Stream<CredentialContainer> credentialTypes(@QueryParam(TYPE) String type,
                                                     @QueryParam(USER_CREDENTIALS) Boolean userCredentials) {
        auth.requireOneOf(AccountRoles.MANAGE_ACCOUNT, AccountRoles.VIEW_PROFILE);

        boolean includeUserCredentials = userCredentials == null || userCredentials;

        Set<String> enabledCredentialTypes = getEnabledCredentialTypes();

        SubjectCredentialManager credentialManager = user.credentialManager();
        Stream<CredentialModel> modelsStream = includeUserCredentials ? credentialManager.getCredentials() : Stream.empty();
        List<CredentialModel> models = modelsStream.toList();

        Function<CredentialProvider, CredentialContainer> toCredentialContainer = (credentialProvider) -> {
            CredentialTypeMetadataContext ctx = CredentialTypeMetadataContext.builder()
                    .user(user)
                    .build(session);
            CredentialTypeMetadata metadata = credentialProvider.getCredentialTypeMetadata(ctx);

            List<CredentialMetadataRepresentation> userCredentialMetadataModels = null;

            if (includeUserCredentials) {
                List<CredentialModel> modelsOfType = models.stream()
                        .filter(credentialProvider::supportsCredentialType)
                        .toList();


                List<CredentialMetadata> credentialMetadataList = modelsOfType.stream()
                        .map(m -> {
                            return credentialProvider.getCredentialMetadata(
                                    credentialProvider.getCredentialFromModel(m), metadata
                            );
                        }).collect(Collectors.toList());

                // REST 端点不返回密钥数据
                credentialMetadataList.stream().forEach(md -> md.getCredentialModel().setSecretData(null));
                userCredentialMetadataModels = credentialMetadataList.stream().map(ModelToRepresentation::toRepresentation).collect(Collectors.toList());

                if (userCredentialMetadataModels.isEmpty() &&
                        user.credentialManager().isConfiguredFor(credentialProvider.getType())) {
                    // 联邦用户在 userStorage 侧可能已配置凭证，此处创建占位表示
                    // creating "dummy" credential representing the credential provided by userStorage
                    CredentialMetadataRepresentation metadataRepresentation = new CredentialMetadataRepresentation();
                    CredentialRepresentation credential = createUserStorageCredentialRepresentation(credentialProvider.getType());
                    metadataRepresentation.setCredential(credential);
                    userCredentialMetadataModels = Collections.singletonList(metadataRepresentation);
                }

                // 无用户凭证且无创建/更新必需动作时，
                // 不包含该凭证类型（用户无法操作）
                if (userCredentialMetadataModels.isEmpty() && metadata.getCreateAction() == null && metadata.getUpdateAction() == null) {
                    return null;
                }
            }

            return new CredentialContainer(metadata, userCredentialMetadataModels);
        };

        return AuthenticatorUtil.getCredentialProviders(session)
                .filter(p -> type == null || p.supportsCredentialType(type))
                .filter(p -> enabledCredentialTypes.contains(p.getType()))
                .map(toCredentialContainer)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(CredentialContainer::getMetadata));
    }

    // 遍历认证流及执行项，收集对应凭证类型的已启用认证器
    // credential type.
    private Set<String> getEnabledCredentialTypes() {
        return realm.getAuthenticationFlowsStream()
                .filter(((Predicate<AuthenticationFlowModel>) this::isFlowEffectivelyDisabled).negate())
                .flatMap(flow ->
                        realm.getAuthenticationExecutionsStream(flow.getId())
                                .filter(exe -> Objects.nonNull(exe.getAuthenticator()) && exe.getRequirement() != DISABLED)
                                .map(exe -> (AuthenticatorFactory) session.getKeycloakSessionFactory()
                                        .getProviderFactory(Authenticator.class, exe.getAuthenticator()))
                                .filter(Objects::nonNull)
                                .flatMap(authFact -> Stream.concat(Stream.of(authFact.getReferenceCategory()), authFact.getOptionalReferenceCategories(session).stream()))
                                .filter(Objects::nonNull)
                ).collect(Collectors.toSet());
    }

    /** 判断认证流是否因自身或父执行项被禁用而 effectively 不可用 */
    // Returns true if flow is effectively disabled
    private boolean isFlowEffectivelyDisabled(AuthenticationFlowModel flow) {
        while (!flow.isTopLevel()) {
            AuthenticationExecutionModel flowExecution = realm.getAuthenticationExecutionByFlowId(flow.getId());
            if (flowExecution == null) return false; // Can happen under some corner cases
            if (DISABLED == flowExecution.getRequirement()) return true;
            if (flowExecution.getParentFlow() == null) return false;

            // Check parent flow
            flow = realm.getAuthenticationFlowById(flowExecution.getParentFlow());
            if (flow == null) return false;
        }

        return false;
    }

    /** 从令牌 acr 解析当前 LoA 等级，用于删除凭证时的权限校验 */
    private Integer getCurrentAuthenticatedLevel() {
        ClientModel client = realm.getClientByClientId(auth.getToken().getIssuedFor());
        Map<String, Integer> acrLoaMap = AcrUtils.getAcrLoaMap(client);
        String tokenAcr = auth.getToken().getAcr();
        if (tokenAcr == null) {
            logger.warnf("Not able to remove credential of user '%s' as no acr claim on the token", user.getUsername());
            throw new ForbiddenException("No LoA on the token");
        }
        Integer currentAuthenticatedLevel = acrLoaMap.get(tokenAcr);
        if (currentAuthenticatedLevel != null) {
            return currentAuthenticatedLevel;
        } else {
            try {
                return Integer.parseInt(tokenAcr);
            } catch (NumberFormatException nfe) {
                logger.warnf("Token acr '%s' not found in acrLoaMap of client '%s' or realm '%s'. Not able to remove credential of user '%s'",
                        tokenAcr, client.getClientId(), realm.getName(), user.getUsername());
                throw new ForbiddenException("Unsupported acr on the token");
            }
        }
    }

    /**
     * 删除当前用户的指定凭证。
     *
     * @param credentialId 待删除凭证 ID
     * @deprecated 建议使用 {@code delete_credential} kc_action，例如在登录 URL 添加 {@code kc_action=delete_credential:123}
     */
    @Path("{credentialId}")
    @DELETE
    @NoCache
    @Deprecated
    public void removeCredential(final @PathParam("credentialId") String credentialId) {
        auth.require(AccountRoles.MANAGE_ACCOUNT);
        logger.warnf("Using deprecated endpoint of Account REST service for removing credential of user '%s' in the realm '%s'. It is recommended to use application initiated actions (AIA) for removing credentials",
                user.getUsername(),
                realm.getName());
        CredentialModel credential = CredentialDeleteHelper.removeCredential(session, user, credentialId, this::getCurrentAuthenticatedLevel);

        if (credential != null) {
            event.event(EventType.REMOVE_CREDENTIAL)
                    .detail(Details.CREDENTIAL_TYPE, credential.getType())
                    .detail(Details.SELECTED_CREDENTIAL_ID, credentialId)
                    .detail(Details.CREDENTIAL_USER_LABEL, credential.getUserLabel());
            if (OTPCredentialModel.TYPE.equals(credential.getType())) {
                event.clone().event(EventType.REMOVE_TOTP).success();
            }
            event.success();
        }
    }


    /**
     * 更新当前用户指定凭证的用户标签。
     *
     * @param credentialId 待更新凭证 ID
     * @param userLabel 新标签（JSON 字符串）
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{credentialId}/label")
    @NoCache
    public void setLabel(final @PathParam("credentialId") String credentialId, String userLabel) {
        auth.require(AccountRoles.MANAGE_ACCOUNT);
        CredentialModel credential = user.credentialManager().getStoredCredentialById(credentialId);
        if (credential == null) {
            throw new NotFoundException("Credential not found");
        }

        try {
            String label = JsonSerialization.readValue(userLabel, String.class);
            user.credentialManager().updateCredentialLabel(credentialId, label);
        } catch (IOException ioe) {
            throw ErrorResponse.error(Messages.INVALID_REQUEST, Response.Status.BAD_REQUEST);
        }
    }

    // TODO：凭证排序 API 暂保留注释
//    /**
//     * Move a credential to a position behind another credential
//     * @param credentialId The credential to move
//     */
//    @Path("{credentialId}/moveToFirst")
//    @POST
//    public void moveToFirst(final @PathParam("credentialId") String credentialId){
//        moveCredentialAfter(credentialId, null);
//    }
//
//    /**
//     * Move a credential to a position behind another credential
//     * @param credentialId The credential to move
//     * @param newPreviousCredentialId The credential that will be the previous element in the list. If set to null, the moved credential will be the first element in the list.
//     */
//    @Path("{credentialId}/moveAfter/{newPreviousCredentialId}")
//    @POST
//    public void moveCredentialAfter(final @PathParam("credentialId") String credentialId, final @PathParam("newPreviousCredentialId") String newPreviousCredentialId){
//        auth.require(AccountRoles.MANAGE_ACCOUNT);
//        session.userCredentialManager().moveCredentialTo(realm, user, credentialId, newPreviousCredentialId);
//    }

}
