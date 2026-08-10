package org.keycloak.authentication;

import org.keycloak.credential.CredentialProvider;
import org.keycloak.credential.CredentialTypeMetadata;
import org.keycloak.credential.CredentialTypeMetadataContext;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;

/**
 * 认证方式选择项：封装执行步骤与凭证类型的展示名、帮助文本及图标 CSS。
 * <p>用于让用户在多种 WebAuthn/OTP 等认证器间选择。</p>
 */
public class AuthenticationSelectionOption {

    /** 对应的认证执行模型。 */
    private final AuthenticationExecutionModel authExec;
    /** 凭证类型元数据（若非 CredentialValidator 则为 null）。 */
    private final CredentialTypeMetadata credentialTypeMetadata;

    /** 根据执行步骤解析认证器并加载凭证展示元数据。 */
    public AuthenticationSelectionOption(KeycloakSession session, AuthenticationExecutionModel authExec) {
        this.authExec = authExec;
        Authenticator authenticator = session.getProvider(Authenticator.class, authExec.getAuthenticator());
        if (authenticator instanceof CredentialValidator) {
            CredentialProvider credentialProvider = ((CredentialValidator) authenticator).getCredentialProvider(session);

            CredentialTypeMetadataContext ctx = CredentialTypeMetadataContext.builder()
                    .build(session);
            credentialTypeMetadata = credentialProvider.getCredentialTypeMetadata(ctx);
        } else {
            credentialTypeMetadata = null;
        }
    }


    /** 返回认证执行模型。 */
    public AuthenticationExecutionModel getAuthenticationExecution() {
        return authExec;
    }

    /** 返回执行步骤 ID。 */
    public String getAuthExecId(){
        return authExec.getId();
    }

    /** 展示名称：优先凭证元数据，否则回退为 authenticator-display-name。 */
    public String getDisplayName() {
        return credentialTypeMetadata == null ? authExec.getAuthenticator() + "-display-name" : credentialTypeMetadata.getDisplayName();
    }

    /** 帮助文本：优先凭证元数据，否则回退为 authenticator-help-text。 */
    public String getHelpText() {
        return credentialTypeMetadata == null ? authExec.getAuthenticator() + "-help-text" : credentialTypeMetadata.getHelpText();
    }

    /** 图标 CSS 类名。 */
    public String getIconCssClass() {
        // 暂不从 AuthenticatorFactory 读取 iconCssClass，未来可按需扩展
        // this capability for authenticator factories, which authenticators don't implement credentialProvider
        return credentialTypeMetadata == null ? CredentialTypeMetadata.DEFAULT_ICON_CSS_CLASS : credentialTypeMetadata.getIconCssClass();
    }


    /** 调试字符串，包含认证器 ID。 */
    @Override
    public String toString() {
        return " authSelection - " + authExec.getAuthenticator();
    }
}
