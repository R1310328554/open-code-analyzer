package org.keycloak.protocol.oid4vc.issuance.requiredactions;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import jakarta.ws.rs.core.Response;

import org.keycloak.Config;
import org.keycloak.authentication.InitiatedActionSupport;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.events.Details;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.oid4vci.CredentialScopeModel;
import org.keycloak.protocol.oid4vc.OID4VCEnvironmentProviderFactory;
import org.keycloak.protocol.oid4vc.issuance.CredentialOfferException;
import org.keycloak.protocol.oid4vc.issuance.OffsetTimeProvider;
import org.keycloak.protocol.oid4vc.issuance.TimeProvider;
import org.keycloak.protocol.oid4vc.issuance.credentialoffer.CredentialOfferProvider;
import org.keycloak.protocol.oid4vc.issuance.credentialoffer.CredentialOfferState;
import org.keycloak.protocol.oid4vc.issuance.credentialoffer.CredentialOfferStorage;
import org.keycloak.protocol.oid4vc.utils.CredentialScopeUtils;
import org.keycloak.representations.idm.oid4vc.VerifiableCredentialOfferActionConfig;
import org.keycloak.sessions.AuthenticationSessionModel;

import com.google.zxing.WriterException;
import org.jboss.logging.Logger;

import static org.keycloak.constants.OID4VCIConstants.CREDENTIAL_OFFER_NONCE;
import static org.keycloak.constants.OID4VCIConstants.IS_ADMIN_INITIATED;
import static org.keycloak.constants.OID4VCIConstants.VERIFIABLE_CREDENTIAL_OFFER_PROVIDER_ID;
import static org.keycloak.events.Details.REASON;
import static org.keycloak.protocol.oid4vc.issuance.OID4VCIssuerEndpoint.CREDENTIAL_OFFER_LIFESPAN_REALM_ATTRIBUTE_KEY;
import static org.keycloak.protocol.oid4vc.issuance.OID4VCIssuerEndpoint.DEFAULT_CREDENTIAL_OFFER_LIFESPAN_S;
import static org.keycloak.protocol.oid4vc.model.AuthorizationCodeGrant.AUTH_CODE_GRANT_TYPE;
import static org.keycloak.protocol.oid4vc.model.ErrorType.INVALID_CREDENTIAL_OFFER_REQUEST;
import static org.keycloak.protocol.oid4vc.model.ErrorType.MISSING_CREDENTIAL_CONFIG;
import static org.keycloak.protocol.oid4vc.model.ErrorType.UNKNOWN_CREDENTIAL_CONFIGURATION;
import static org.keycloak.protocol.oid4vc.model.PreAuthorizedCodeGrant.PRE_AUTH_GRANT_TYPE;

/**
 * 可验证凭证发放（Credential Offer）必需操作提供方与工厂。
 * <p>创建并持久化凭证发放状态，向用户展示 QR 码与发放 URI；支持管理员发起的 AIA 流程。</p>
 */
public class VerifiableCredentialOfferAction implements RequiredActionProvider, RequiredActionFactory, OID4VCEnvironmentProviderFactory {

    private static final Logger logger = Logger.getLogger(VerifiableCredentialOfferAction.class);

    private final TimeProvider timeProvider;

    /** 使用 {@link OffsetTimeProvider} 作为默认时间源。 */
    public VerifiableCredentialOfferAction() {
        this.timeProvider = new OffsetTimeProvider();
    }

    /** @return 管理员界面显示的必需操作名称 */
    @Override
    public String getDisplayText() {
        return "Register Verifiable Credential Offer";
    }

    /** 本实现为单例，直接返回自身。 */
    @Override
    public RequiredActionProvider create(KeycloakSession session) {
        return this;
    }

    /** SPI 初始化；无额外配置。 */
    @Override
    public void init(Config.Scope config) {
    }

    /** 会话工厂就绪后的后置初始化；无操作。 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    /** @return 必需操作提供方 ID {@value org.keycloak.constants.OID4VCIConstants#VERIFIABLE_CREDENTIAL_OFFER_PROVIDER_ID} */
    @Override
    public String getId() {
        return VERIFIABLE_CREDENTIAL_OFFER_PROVIDER_ID;
    }

    /** 评估是否触发本必需操作；当前仅记录 trace 日志。 */
    @Override
    public void evaluateTriggers(RequiredActionContext context) {
        logger.tracef("Evaluate triggers invoked for '%s'", context.getAction());
    }

    /**
     * 展示凭证发放挑战页：解析 KC 动作参数、创建或复用发放状态，渲染 QR 码表单。
     *
     * @param context 必需操作上下文
     */
        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        KeycloakSession session = context.getSession();
        RealmModel realm = context.getRealm();
        UserModel user = context.getUser();
        EventBuilder event = context.getEvent();
        event.event(EventType.VERIFIABLE_CREDENTIAL_CREATE_OFFER);

        String credentialOfferConfig = authSession.getClientNote(Constants.KC_ACTION_PARAMETER);
        if (credentialOfferConfig == null) {
            event.error(MISSING_CREDENTIAL_CONFIG.getValue());
            context.ignore();
            return;
        }

        VerifiableCredentialOfferActionConfig actionConfig = getActionConfig(credentialOfferConfig);
        if (actionConfig == null) {
            event.detail(REASON, "Parameter of AIA in incorrect format. KC action parameter value was: " + credentialOfferConfig)
                    .error(INVALID_CREDENTIAL_OFFER_REQUEST.getValue());
            context.ignore();
            return;
        }
        if (actionConfig.getCredentialConfigurationId() == null) {
            event.detail(REASON, "Credential configuration ID was missing. KC action parameter value was: " + credentialOfferConfig)
                    .error(INVALID_CREDENTIAL_OFFER_REQUEST.getValue());
            context.ignore();
            return;
        }
        String credentialConfigId = actionConfig.getCredentialConfigurationId();

        CredentialScopeModel credScope = CredentialScopeUtils.findCredentialScopeModelByConfigurationId(
                realm, () -> session.clientScopes().getClientScopesStream(realm), credentialConfigId);
        if (credScope == null) {
            event.detail(Details.CREDENTIAL_TYPE, credentialConfigId);
            event.detail(REASON, "Client scope was not found for credential configuration ID: " + credentialConfigId)
                    .error(UNKNOWN_CREDENTIAL_CONFIGURATION.getValue());
            context.ignore();
            return;
        }

        logger.debugf("Required action challenge invoked for provider '%s' and config '%s'", context.getAction(), actionConfig);

        String nonce = context.getAuthenticationSession().getAuthNote(CREDENTIAL_OFFER_NONCE);
        if (nonce == null) {
            try {
                CredentialOfferState credOfferState = createCredentialsOffer(context.getSession(), realm, user, event, actionConfig);
                nonce = credOfferState.getNonce();
                context.getAuthenticationSession().setAuthNote(CREDENTIAL_OFFER_NONCE, credOfferState.getNonce());
            } catch (CredentialOfferException ex) {
                event.detail(Details.REASON, ex.getMessage()).error(ex.getErrorType());
                context.ignore();
                return;
            }
        }

        LoginFormsProvider form = context.form();
        try {
            String displayName = CredentialScopeUtils.getCredentialDisplayName(context.getSession(), context.getUser(), credScope);
            form.setAttribute("credentialOffer", new CredentialOfferBean(context.getSession(), nonce));
            form.setAttribute("credentialDisplayName", displayName);
            if ("true".equals(context.getAuthenticationSession().getAuthNote(IS_ADMIN_INITIATED))) {
                form.setAttribute("skipCancelButton", true);
            }
        } catch (WriterException | IOException ex) {
            String message = "Error when generating credential-offer QR code: " + ex.getMessage();
            event.detail(REASON, message)
                    .error(INVALID_CREDENTIAL_OFFER_REQUEST.getValue());
            context.ignore();
            return;
        }

        Response response = form.createForm("oid4vc-credential-offer.ftl");
        context.challenge(response);
    }


    /**
     * 创建凭证发放状态并写入存储。
     *
     * @param session      Keycloak 会话
     * @param realm        当前域
     * @param user         目标用户
     * @param event        事件构建器
     * @param actionConfig 发放动作配置
     * @return 新建的 {@link CredentialOfferState}
     * @throws CredentialOfferException 发放创建失败
     */
        boolean preAuthorized = actionConfig.getPreAuthorized() != null && actionConfig.getPreAuthorized();
        String grantType = preAuthorized ? PRE_AUTH_GRANT_TYPE : AUTH_CODE_GRANT_TYPE;
        int credentialOfferLifespan = Optional.ofNullable(realm.getAttribute(CREDENTIAL_OFFER_LIFESPAN_REALM_ATTRIBUTE_KEY))
                .map(Integer::valueOf)
                .orElse(DEFAULT_CREDENTIAL_OFFER_LIFESPAN_S);
        int expiresAt = timeProvider.currentTimeSeconds() + credentialOfferLifespan;

        String credentialConfigurationId = actionConfig.getCredentialConfigurationId();
        event = event.clone().detail(Details.CREDENTIAL_TYPE, credentialConfigurationId);

        String clientId = actionConfig.getClientId();
        CredentialOfferProvider offerProvider = session.getProvider(CredentialOfferProvider.class);
        CredentialOfferState offerState = offerProvider.createCredentialOffer(user, grantType,
                List.of(credentialConfigurationId), clientId, user.getUsername(), expiresAt);

        CredentialOfferStorage offerStorage = session.getProvider(CredentialOfferStorage.class);
        offerStorage.putOfferState(offerState);

        logger.debugf("Stored credential offer state: [credentialConfigId=%s, clientId=%s, username=%s, nonce=%s]",
                credentialConfigurationId, clientId, user.getUsername(), offerState.getNonce());

        // 记录预授权、目标用户/客户端等事件详情
        event.detail(Details.VERIFIABLE_CREDENTIAL_PRE_AUTHORIZED, String.valueOf(preAuthorized));
        event.detail(Details.VERIFIABLE_CREDENTIAL_TARGET_USER_ID, user.getId());
        if (clientId != null) {
            event.detail(Details.VERIFIABLE_CREDENTIAL_TARGET_CLIENT_ID, clientId);
        }
        event.success();

        return offerState;
    }

    /** 用户确认已消费凭证发放后继续登录流程。 */
    @Override
    public void processAction(RequiredActionContext context) {
        // 用户已消费发放后继续登录
        logger.tracef("Process action invoked for: " + context.getAction());
        context.success();
    }

    /** 关闭资源；无状态，无操作。 */
    @Override
    public void close() {
    }

    /** 支持管理员发起的 AIA（Application Initiated Action）。 */
    @Override
    public InitiatedActionSupport initiatedActionSupport() {
        return InitiatedActionSupport.SUPPORTED;
    }

    /** 用户取消 AIA 时移除已创建但未消费的发放状态。 */
    @Override
    public void initiatedActionCanceled(KeycloakSession session, AuthenticationSessionModel authSession) {
        // 用户拒绝发放，清理存储中的发放状态
        String nonce = authSession.getAuthNote(CREDENTIAL_OFFER_NONCE);
        if (nonce != null) {
            CredentialOfferStorage offerStore = session.getProvider(CredentialOfferStorage.class);
            CredentialOfferState state = offerStore.getOfferStateByNonce(nonce);
            if (state != null) {
                offerStore.removeOfferState(state);
            }
        }

        RequiredActionProvider.super.initiatedActionCanceled(session, authSession);
    }

    /** 解码 KC 动作参数字符串为 {@link VerifiableCredentialOfferActionConfig}。 */
    private VerifiableCredentialOfferActionConfig getActionConfig(String credentialOfferUserConfig) {
        try {
            return VerifiableCredentialOfferActionConfig.decodeConfig(credentialOfferUserConfig);
        } catch (IOException ioe) {
            logger.warnf("Parameter of %s AIA in incorrect format. Parameter value was: %s", VERIFIABLE_CREDENTIAL_OFFER_PROVIDER_ID, credentialOfferUserConfig);
            return null;
        }
    }

}
