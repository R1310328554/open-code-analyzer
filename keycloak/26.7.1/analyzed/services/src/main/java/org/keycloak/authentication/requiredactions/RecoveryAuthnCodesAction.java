package org.keycloak.authentication.requiredactions;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.keycloak.Config;
import org.keycloak.authentication.AuthenticatorUtil;
import org.keycloak.authentication.CredentialRegistrator;
import org.keycloak.authentication.InitiatedActionSupport;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.authentication.authenticators.browser.RecoveryAuthnCodesFormAuthenticator;
import org.keycloak.common.Profile;
import org.keycloak.events.Details;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RequiredActionConfigModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.RecoveryAuthnCodesCredentialModel;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.userprofile.ValidationException;
import org.keycloak.validate.ValidationError;

import static org.keycloak.utils.CredentialHelper.createRecoveryCodesCredential;

/**
 * 恢复认证码必需操作：生成并保存一组一次性恢复码凭证。
 * <p>需启用 {@link Profile.Feature#RECOVERY_CODES} 特性；支持剩余数量警告阈值配置。</p>
 */
public class RecoveryAuthnCodesAction implements RequiredActionProvider, RequiredActionFactory, EnvironmentDependentProviderFactory, CredentialRegistrator {

    /** 表单隐藏字段：生成的恢复码列表。 */
    private static final String FIELD_GENERATED_RECOVERY_AUTHN_CODES_HIDDEN = "generatedRecoveryAuthnCodes";
    private static final String FIELD_GENERATED_AT_HIDDEN = "generatedAt";
    private static final String FIELD_USER_LABEL_HIDDEN = "userLabel";
    /** 提供者标识符，与 {@link UserModel.RequiredAction#CONFIGURE_RECOVERY_AUTHN_CODES} 一致。 */
    public static final String PROVIDER_ID = UserModel.RequiredAction.CONFIGURE_RECOVERY_AUTHN_CODES.name();
    private static final RecoveryAuthnCodesAction INSTANCE = new RecoveryAuthnCodesAction();

    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES;

    /** 配置键：剩余恢复码数量警告阈值。 */
    public static final String WARNING_THRESHOLD = "warning_threshold";

    public static final int RECOVERY_CODES_WARNING_THRESHOLD_DEFAULT = 4;

    static {
        List<ProviderConfigProperty> properties = ProviderConfigurationBuilder.create() //
                .property() //
                .name(WARNING_THRESHOLD) //
                .label("Warning Threshold") //
                .helpText("When user has smaller amount of remaining recovery codes on his account than the value configured here, account console will show warning to the user, which will recommend him to setup new set of recovery codes.")
                .type(ProviderConfigProperty.INTEGER_TYPE) //
                .defaultValue(RECOVERY_CODES_WARNING_THRESHOLD_DEFAULT) //
                .add() //
                .build();

        CONFIG_PROPERTIES = properties;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getCredentialType(KeycloakSession session, AuthenticationSessionModel authenticationSession) {
        return RecoveryAuthnCodesCredentialModel.TYPE;
    }

    @Override
    public String getDisplayText() {
        return "Recovery Authentication Codes";
    }

    @Override
    public RequiredActionProvider create(KeycloakSession session) {
        return INSTANCE;
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public InitiatedActionSupport initiatedActionSupport() {
        return InitiatedActionSupport.SUPPORTED;
    }

    @Override
    public void evaluateTriggers(RequiredActionContext context) {
    }

    /** 展示恢复码配置页面。 */
    @Override
    public void requiredActionChallenge(RequiredActionContext context) {
        Response challenge = context.form().createResponse(UserModel.RequiredAction.CONFIGURE_RECOVERY_AUTHN_CODES);
        context.challenge(challenge);
    }

    /** 校验 auth note 防篡改后创建恢复码凭证。 */
    @Override
    public void processAction(RequiredActionContext reqActionContext) {
        EventBuilder event = reqActionContext.getEvent();
        event.event(EventType.UPDATE_CREDENTIAL);
        MultivaluedMap<String, String> httpReqParamsMap;

        event.detail(Details.CREDENTIAL_TYPE, RecoveryAuthnCodesCredentialModel.TYPE);

        httpReqParamsMap = reqActionContext.getHttpRequest().getDecodedFormParameters();
        final String generatedCodesString = httpReqParamsMap.getFirst(FIELD_GENERATED_RECOVERY_AUTHN_CODES_HIDDEN);
        final String generatedAtTimeString = httpReqParamsMap.getFirst(FIELD_GENERATED_AT_HIDDEN);
        final String generatedUserLabel = httpReqParamsMap.getFirst(FIELD_USER_LABEL_HIDDEN);

        if (!generatedAtTimeString.equals(reqActionContext.getAuthenticationSession().getAuthNote(RecoveryAuthnCodesFormAuthenticator.GENERATED_AT_NOTE))
                || !generatedCodesString.equals(reqActionContext.getAuthenticationSession().getAuthNote(RecoveryAuthnCodesFormAuthenticator.GENERATED_RECOVERY_AUTHN_CODES_NOTE))) {
            // 恢复码被篡改，重新展示配置页
            requiredActionChallenge(reqActionContext);
            return;
        }

        reqActionContext.getAuthenticationSession().removeAuthNote(RecoveryAuthnCodesFormAuthenticator.GENERATED_AT_NOTE);
        reqActionContext.getAuthenticationSession().removeAuthNote(RecoveryAuthnCodesFormAuthenticator.GENERATED_RECOVERY_AUTHN_CODES_NOTE);

        List<String> generatedCodes = Arrays.asList(generatedCodesString.split(","));
        RecoveryAuthnCodesCredentialModel credentialModel = createFromValues(generatedCodes, Long.valueOf(generatedAtTimeString), generatedUserLabel);

        if ("on".equals(httpReqParamsMap.getFirst("logout-sessions"))) {
            AuthenticatorUtil.logoutOtherSessions(reqActionContext);
        }

        createRecoveryCodesCredential(reqActionContext.getSession(), reqActionContext.getRealm(), reqActionContext.getUser(), credentialModel, generatedCodes);

        reqActionContext.success();
    }

    /** 由生成参数构建 {@link RecoveryAuthnCodesCredentialModel}。 */
    protected RecoveryAuthnCodesCredentialModel createFromValues(List<String> generatedCodes, Long generatedAtTime, String generatedUserLabel) {
        return RecoveryAuthnCodesCredentialModel.createFromValues(generatedCodes,
                generatedAtTime, generatedUserLabel);
    }

    @Override
    public void close() {
    }

    /** @return 是否启用恢复码特性 */
    @Override
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.RECOVERY_CODES);
    }

    @Override
    public List<ProviderConfigProperty> getConfigMetadata() {
        return Stream.concat(
                List.copyOf(CONFIG_PROPERTIES).stream(),
                RequiredActionFactory.super.getConfigMetadata().stream()
        ).toList();
    }

    /** 校验警告阈值配置为非负整数。 */
    @Override
    public void validateConfig(KeycloakSession session, RealmModel realm, RequiredActionConfigModel model) {
        RequiredActionFactory.super.validateConfig(session, realm, model);

        int parsedMaxAuthAge;
        try {
            parsedMaxAuthAge = parseWarningThreshold(model);
        } catch (Exception ex) {
            throw new ValidationException(new ValidationError(getId(), WARNING_THRESHOLD, "error-invalid-value"));
        }

        if (parsedMaxAuthAge < 0) {
            throw new ValidationException(new ValidationError(getId(), WARNING_THRESHOLD, "error-number-out-of-range-too-small", 0));
        }
    }

    private int parseWarningThreshold(RequiredActionConfigModel model) throws NumberFormatException {
        return Integer.parseInt(model.getConfigValue(WARNING_THRESHOLD));
    }
}
