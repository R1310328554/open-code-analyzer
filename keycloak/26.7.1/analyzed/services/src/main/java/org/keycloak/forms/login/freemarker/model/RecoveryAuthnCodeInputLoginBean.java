package org.keycloak.forms.login.freemarker.model;

import java.util.Optional;

import org.keycloak.credential.CredentialModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.RecoveryAuthnCodesCredentialModel;
import org.keycloak.models.utils.RecoveryAuthnCodesUtils;

/**
 * 恢复认证码登录输入 FreeMarker Bean：告知模板当前应输入第几个恢复码。
 * <p>从用户 {@link RecoveryAuthnCodesCredentialModel} 读取下一个待使用的码序号。</p>
 */
public class RecoveryAuthnCodeInputLoginBean {

    /** 本次登录应输入的恢复码序号（从 1 起）。 */
    private final int codeNumber;

    /** @param session Keycloak 会话 @param realm Realm @param user 目标用户 */
    public RecoveryAuthnCodeInputLoginBean(KeycloakSession session, RealmModel realm, UserModel user) {
        Optional<CredentialModel> credentialModelOpt = RecoveryAuthnCodesUtils.getCredential(user);

        RecoveryAuthnCodesCredentialModel recoveryCodeCredentialModel = RecoveryAuthnCodesCredentialModel.createFromCredentialModel(credentialModelOpt.get());

        this.codeNumber = recoveryCodeCredentialModel.getNextRecoveryAuthnCode().get().getNumber();
    }

    /** @return 待输入恢复码的序号 */
    public int getCodeNumber() {
        return this.codeNumber;
    }

}
