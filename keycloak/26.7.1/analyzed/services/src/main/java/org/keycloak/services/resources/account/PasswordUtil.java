package org.keycloak.services.resources.account;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;

/**
 * 账户密码配置状态工具（部分 API 已废弃）。
 */
public class PasswordUtil {

    /** 目标用户 */
    private final UserModel user;

    @Deprecated
    public PasswordUtil(KeycloakSession session, UserModel user) {
        this.user = user;
    }

    public PasswordUtil(UserModel user) {
        this.user = user;
    }

    /**
     * @deprecated 请改用 {@link #isConfigured()}
     */
    @Deprecated
    public boolean isConfigured(KeycloakSession session, RealmModel realm, UserModel user) {
        return user.credentialManager().isConfiguredFor(PasswordCredentialModel.TYPE);
    }

    /** 用户是否已配置密码凭证类型 */
    public boolean isConfigured() {
        return user.credentialManager().isConfiguredFor(PasswordCredentialModel.TYPE);
    }

    /** 占位更新方法（当前无实现） */
    public void update() {

    }

}
