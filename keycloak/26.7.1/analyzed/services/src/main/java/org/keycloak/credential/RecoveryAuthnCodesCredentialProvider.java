package org.keycloak.credential;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import org.keycloak.authentication.requiredactions.RecoveryAuthnCodesAction;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RequiredActionProviderModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.RecoveryAuthnCodesCredentialModel;
import org.keycloak.models.credential.dto.RecoveryAuthnCodeRepresentation;
import org.keycloak.models.credential.dto.RecoveryAuthnCodesCredentialData;
import org.keycloak.models.utils.RecoveryAuthnCodesUtils;
import org.keycloak.util.JsonSerialization;
import org.keycloak.utils.RequiredActionHelper;

import org.jboss.logging.Logger;

import static org.keycloak.models.credential.RecoveryAuthnCodesCredentialModel.RECOVERY_CODES_GENERATE_NEW_CODES;
import static org.keycloak.models.credential.RecoveryAuthnCodesCredentialModel.RECOVERY_CODES_NUMBER_REMAINING;
import static org.keycloak.models.credential.RecoveryAuthnCodesCredentialModel.RECOVERY_CODES_NUMBER_USED;

/**
 * 恢复认证码（Recovery Authentication Codes）凭证提供者。
 * <p>管理一次性备份码的创建、顺序消费、元数据展示与校验；每用户仅保留一份有效凭证。</p>
 */
public class RecoveryAuthnCodesCredentialProvider
        implements CredentialProvider<RecoveryAuthnCodesCredentialModel>, CredentialInputValidator {

    private static final Logger logger = Logger.getLogger(RecoveryAuthnCodesCredentialProvider.class);

    /** 当前 Keycloak 会话，用于读取 Realm 策略与必需动作配置。 */
    private final KeycloakSession session;

    /** @param session 当前 Keycloak 会话 */
    public RecoveryAuthnCodesCredentialProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public String getType() {
        return RecoveryAuthnCodesCredentialModel.TYPE;
    }

    @Override
    /** 创建恢复码凭证；若已存在同类型凭证则先删除再写入。 */
    public CredentialModel createCredential(RealmModel realm, UserModel user,
            RecoveryAuthnCodesCredentialModel credentialModel) {

        user.credentialManager().getStoredCredentialsByTypeStream(getType()).findFirst()
                .ifPresent(model -> deleteCredential(realm, user, model.getId()));

        return user.credentialManager().createStoredCredential(credentialModel);
    }

    @Override
    public boolean deleteCredential(RealmModel realm, UserModel user, String credentialId) {
        return user.credentialManager().removeStoredCredentialById(credentialId);
    }

    @Override
    public RecoveryAuthnCodesCredentialModel getCredentialFromModel(CredentialModel model) {
        return RecoveryAuthnCodesCredentialModel.createFromCredentialModel(model);
    }

    @Override
    /** 返回恢复码在凭据 UI 中的分类（双因素）、文案与注册必需动作。 */
    public CredentialTypeMetadata getCredentialTypeMetadata(CredentialTypeMetadataContext metadataContext) {
        CredentialTypeMetadata.CredentialTypeMetadataBuilder builder = CredentialTypeMetadata.builder().type(getType())
                .category(CredentialTypeMetadata.Category.TWO_FACTOR).displayName("recovery-authn-codes-display-name")
                .helpText("recovery-authn-codes-help-text").iconCssClass("kcAuthenticatorRecoveryAuthnCodesClass")
                .removeable(true);
        builder.createAction(UserModel.RequiredAction.CONFIGURE_RECOVERY_AUTHN_CODES.name());
        return builder.build(session);
    }

    @Override
    /** 根据剩余码数量生成警告/信息消息（低于阈值时提示重新生成）。 */
    public CredentialMetadata getCredentialMetadata(RecoveryAuthnCodesCredentialModel credentialModel, CredentialTypeMetadata credentialTypeMetadata) {

        CredentialMetadata credentialMetadata = new CredentialMetadata();
        try {
            RecoveryAuthnCodesCredentialData credentialData = JsonSerialization.readValue(credentialModel.getCredentialData(), RecoveryAuthnCodesCredentialData.class);
            if (credentialData.getRemainingCodes() < getWarningThreshold()) {
                credentialMetadata.setWarningMessageTitle(RECOVERY_CODES_NUMBER_REMAINING, String.valueOf(credentialData.getRemainingCodes()));
                credentialMetadata.setWarningMessageDescription(RECOVERY_CODES_GENERATE_NEW_CODES);
            }

            int codesUsed = credentialData.getTotalCodes() - credentialData.getRemainingCodes();
            String codesUsedMessage = codesUsed + "/" + credentialData.getTotalCodes();
            credentialMetadata.setInfoMessage(RECOVERY_CODES_NUMBER_USED, codesUsedMessage);
        } catch (IOException e) {
            logger.warn("unable to deserialize model information, skipping messages", e);
        }
        credentialMetadata.setCredentialModel(credentialModel);

        return credentialMetadata;
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return getType().equals(credentialType);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        return user.credentialManager().getStoredCredentialsByTypeStream(credentialType).anyMatch(Objects::nonNull);
    }

    @Override
    /** 校验用户提交的下一个未使用恢复码；成功则移除该码并持久化更新。 */
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput credentialInput) {
        String rawInputRecoveryAuthnCode = credentialInput.getChallengeResponse();
        Optional<CredentialModel> credential = user.credentialManager().getStoredCredentialsByTypeStream(getType()).findFirst();
        if (credential.isPresent()) {
            RecoveryAuthnCodesCredentialModel credentialModel = RecoveryAuthnCodesCredentialModel
                    .createFromCredentialModel(credential.get());
            if (!credentialModel.allCodesUsed()) {
                Optional<RecoveryAuthnCodeRepresentation> nextRecoveryAuthnCode = credentialModel.getNextRecoveryAuthnCode();
                if (nextRecoveryAuthnCode.isPresent()) {
                    String nextRecoveryCode = nextRecoveryAuthnCode.get().getEncodedHashedValue();
                    if (RecoveryAuthnCodesUtils.verifyRecoveryCodeInput(rawInputRecoveryAuthnCode, nextRecoveryCode)) {
                        credentialModel.removeRecoveryAuthnCode();
                        user.credentialManager().updateStoredCredential(credentialModel);
                        return true;
                    }

                }
            }
        }
        return false;
    }

    /** 剩余恢复码低于该阈值时在 UI 显示警告；优先读必需动作配置，否则用 Realm 密码策略。 */
    protected int getWarningThreshold() {
        RealmModel realm = session.getContext().getRealm();
        RequiredActionProviderModel requiredAction = RequiredActionHelper.getRequiredActionByProviderId(realm, RecoveryAuthnCodesAction.PROVIDER_ID);
        if (requiredAction != null && requiredAction.getConfig().containsKey(RecoveryAuthnCodesAction.WARNING_THRESHOLD)) {
            return Integer.parseInt(requiredAction.getConfig().get(RecoveryAuthnCodesAction.WARNING_THRESHOLD));
        } else {
            return session.getContext().getRealm().getPasswordPolicy().getRecoveryCodesWarningThreshold();
        }
    }
}
