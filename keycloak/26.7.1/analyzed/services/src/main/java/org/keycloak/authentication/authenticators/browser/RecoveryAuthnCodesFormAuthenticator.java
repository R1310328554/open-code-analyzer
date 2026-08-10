package org.keycloak.authentication.authenticators.browser;

import java.util.Optional;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.CredentialValidator;
import org.keycloak.authentication.authenticators.util.AuthenticatorUtils;
import org.keycloak.common.util.ObjectUtil;
import org.keycloak.credential.CredentialModel;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.credential.RecoveryAuthnCodesCredentialProvider;
import org.keycloak.credential.RecoveryAuthnCodesCredentialProviderFactory;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.RecoveryAuthnCodesCredentialModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.models.utils.RecoveryAuthnCodesUtils;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.storage.ReadOnlyException;

import static org.keycloak.services.validation.Validation.FIELD_USERNAME;

/**
 * 恢复认证码表单认证器，在浏览器流程中展示恢复码输入页并校验用户提交的恢复认证码；校验成功后若所有码已用尽则添加重新配置恢复码的必需操作。
 */
public class RecoveryAuthnCodesFormAuthenticator implements Authenticator, CredentialValidator<RecoveryAuthnCodesCredentialProvider> {

    /** 认证会话 note：已生成的恢复认证码列表。 */
    public static final String GENERATED_RECOVERY_AUTHN_CODES_NOTE = "RecoveryAuthnCodes.generatedRecoveryAuthnCodes";
    /** 认证会话 note：恢复码生成时间戳。 */
    public static final String GENERATED_AT_NOTE = "RecoveryAuthnCodes.generatedAt";

    /** @param keycloakSession 当前 Keycloak 会话 */
    public RecoveryAuthnCodesFormAuthenticator(KeycloakSession keycloakSession) {
    }

    @Override
    /** 展示恢复认证码登录表单。 */
    public void authenticate(AuthenticationFlowContext context) {
        context.challenge(createLoginForm(context, false, null, null));
    }

    @Override
    /** 处理表单提交，校验恢复认证码并记录凭证类型事件。 */
    public void action(AuthenticationFlowContext context) {
        context.getEvent().detail(Details.CREDENTIAL_TYPE, RecoveryAuthnCodesCredentialModel.TYPE)
                .user(context.getUser());
        if (isRecoveryAuthnCodeInputValid(context)) {
            context.success(RecoveryAuthnCodesCredentialModel.TYPE);
        }
    }

    /** 校验用户输入的恢复认证码，处理暴力破解锁定与码用尽后的必需操作。 */
    private boolean isRecoveryAuthnCodeInputValid(AuthenticationFlowContext authnFlowContext) {
        boolean result = false;
        MultivaluedMap<String, String> formParamsMap = authnFlowContext.getHttpRequest().getDecodedFormParameters();
        String recoveryAuthnCodeUserInput = formParamsMap.getFirst(RecoveryAuthnCodesUtils.FIELD_RECOVERY_CODE_IN_BROWSER_FLOW);

        UserModel authenticatedUser = authnFlowContext.getUser();
        boolean disabledByBruteForce = isDisabledByBruteForce(authnFlowContext, authenticatedUser);
        if (ObjectUtil.isBlank(recoveryAuthnCodeUserInput)
                || "true".equals(authnFlowContext.getAuthenticationSession().getAuthNote(AbstractUsernameFormAuthenticator.SESSION_INVALID))) {
            // 暴力破解锁定可能已解除，需清除认证会话中的无效标记
            if (!disabledByBruteForce) {
                authnFlowContext.getAuthenticationSession().removeAuthNote(AbstractUsernameFormAuthenticator.SESSION_INVALID);
            } else {
                authnFlowContext.forceChallenge(createLoginForm(authnFlowContext, true,
                        RecoveryAuthnCodesUtils.RECOVERY_AUTHN_CODES_INPUT_DEFAULT_ERROR_MESSAGE,
                        RecoveryAuthnCodesUtils.FIELD_RECOVERY_CODE_IN_BROWSER_FLOW));
                return result;
            }
        }

        if (!disabledByBruteForce) {
            boolean isValid = authenticatedUser.credentialManager().isValid(
                    UserCredentialModel.buildFromBackupAuthnCode(recoveryAuthnCodeUserInput.replace("-", "")));
            if (!isValid) {
                Response responseChallenge = createLoginForm(authnFlowContext, true,
                        RecoveryAuthnCodesUtils.RECOVERY_AUTHN_CODES_INPUT_DEFAULT_ERROR_MESSAGE,
                        RecoveryAuthnCodesUtils.FIELD_RECOVERY_CODE_IN_BROWSER_FLOW);
                authnFlowContext.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, responseChallenge);
            } else {
                result = true;
                Optional<CredentialModel> optUserCredentialFound = RecoveryAuthnCodesUtils.getCredential(authenticatedUser);
                RecoveryAuthnCodesCredentialModel recoveryCodeCredentialModel = null;
                if (optUserCredentialFound.isPresent()) {
                    recoveryCodeCredentialModel = RecoveryAuthnCodesCredentialModel
                            .createFromCredentialModel(optUserCredentialFound.get());
                    if (recoveryCodeCredentialModel.allCodesUsed()) {
                        authenticatedUser.credentialManager().removeStoredCredentialById(
                                recoveryCodeCredentialModel.getId());
                    }
                }
                if (recoveryCodeCredentialModel == null || recoveryCodeCredentialModel.allCodesUsed()) {
                    addRequiredAction(authnFlowContext);
                }
            }
        }
        else {
            authnFlowContext.getAuthenticationSession().setAuthNote(AbstractUsernameFormAuthenticator.SESSION_INVALID, "true");
        }
        return result;
    }

    /** 添加 CONFIGURE_RECOVERY_AUTHN_CODES 必需操作（只读用户则写入认证会话）。 */
    protected void addRequiredAction(AuthenticationFlowContext authnFlowContext) {
        try {
            authnFlowContext.getUser().addRequiredAction(UserModel.RequiredAction.CONFIGURE_RECOVERY_AUTHN_CODES);
        } catch (ReadOnlyException e) {
            // 用户为只读，至少将必需操作写入认证会话
            authnFlowContext.getAuthenticationSession().addRequiredAction(UserModel.RequiredAction.CONFIGURE_RECOVERY_AUTHN_CODES);
        }
    }

    /** 检查用户是否因暴力破解被锁定，若锁定则强制展示错误表单。 */
    protected boolean isDisabledByBruteForce(AuthenticationFlowContext authnFlowContext, UserModel authenticatedUser) {
        String bruteForceError;
        Response challengeResponse;
        bruteForceError = getDisabledByBruteForceEventError(authnFlowContext, authenticatedUser);
        if (bruteForceError == null) {
            return false;
        }
        authnFlowContext.getEvent().user(authenticatedUser);
        authnFlowContext.getEvent().error(bruteForceError);
        challengeResponse = createLoginForm(authnFlowContext, false, Messages.INVALID_USER, FIELD_USERNAME);
        authnFlowContext.forceChallenge(challengeResponse);
        return true;
    }

    /** @return 暴力破解锁定对应的事件错误码，未锁定则返回 null */
    protected String getDisabledByBruteForceEventError(AuthenticationFlowContext authnFlowContext, UserModel authenticatedUser) {
        return AuthenticatorUtils.getDisabledByBruteForceEventError(authnFlowContext, authenticatedUser);
    }

    /** 构建恢复认证码登录表单响应，可选附加字段级或全局错误。 */
    private Response createLoginForm(AuthenticationFlowContext authnFlowContext, boolean withInvalidUserCredentialsError,
            String errorToRaise, String fieldError) {
        Response challengeResponse;
        LoginFormsProvider loginFormsProvider;
        if (withInvalidUserCredentialsError) {
            loginFormsProvider = authnFlowContext.form();
            authnFlowContext.getEvent().user(authnFlowContext.getUser());
            authnFlowContext.getEvent().error(Errors.INVALID_USER_CREDENTIALS);
            loginFormsProvider.addError(new FormMessage(fieldError, errorToRaise));
        } else {
            loginFormsProvider = authnFlowContext.form().setExecution(authnFlowContext.getExecution().getId());
            if (errorToRaise != null) {
                if (fieldError != null) {
                    loginFormsProvider.addError(new FormMessage(fieldError, errorToRaise));
                } else {
                    loginFormsProvider.setError(errorToRaise);
                }
            }
        }
        challengeResponse = loginFormsProvider.createLoginRecoveryAuthnCode();
        return challengeResponse;
    }

    @Override
    /** @return 本步骤要求上下文中已有用户 */
    public boolean requiresUser() {
        return true;
    }

    @Override
    /** @return 用户是否已配置恢复认证码凭证 */
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return user.credentialManager().isConfiguredFor(RecoveryAuthnCodesCredentialModel.TYPE);
    }

    @Override
    /** 向认证会话添加 CONFIGURE_RECOVERY_AUTHN_CODES 必需操作。 */
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        AuthenticationSessionModel authenticationSession = session.getContext().getAuthenticationSession();
        if (!authenticationSession.getRequiredActions().contains(UserModel.RequiredAction.CONFIGURE_RECOVERY_AUTHN_CODES.name())) {
            authenticationSession.addRequiredAction(UserModel.RequiredAction.CONFIGURE_RECOVERY_AUTHN_CODES.name());
        }
    }

    @Override
    public void close() {
    }

    @Override
    /** @return 恢复认证码凭证提供者 */
    public RecoveryAuthnCodesCredentialProvider getCredentialProvider(KeycloakSession session) {
        return (RecoveryAuthnCodesCredentialProvider)session.getProvider(CredentialProvider.class, RecoveryAuthnCodesCredentialProviderFactory.PROVIDER_ID);
    }

    @Override
    /** @return 恢复认证码凭证类型 */
    public String getType(KeycloakSession session) {
        return RecoveryAuthnCodesCredentialModel.TYPE;
    }
}
