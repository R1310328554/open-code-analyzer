package org.keycloak.models.credential.dto;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * WebAuthn 凭据展示数据 DTO：在 {@link WebAuthnCredentialData} 基础上增加 UI 展示字段。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class WebAuthnCredentialPresentationData extends WebAuthnCredentialData {

    private final String authenticatorProvider;
    private final String iconLight;
    private final String iconDark;

    /** Jackson 反序列化构造器（含展示字段）。 */
    @JsonCreator
    public WebAuthnCredentialPresentationData(@JsonProperty("aaguid") String aaguid,
                                              @JsonProperty("credentialId") String credentialId,
                                              @JsonProperty("counter") long counter,
                                              @JsonProperty("attestationStatement") String attestationStatement,
                                              @JsonProperty("credentialPublicKey") String credentialPublicKey,
                                              @JsonProperty("attestationStatementFormat") String attestationStatementFormat,
                                              @JsonProperty("transports") Set<String> transports,
                                              @JsonProperty("authenticatorProvider") String authenticatorProvider,
                                              @JsonProperty("iconLight") String iconLight,
                                              @JsonProperty("iconDark") String iconDark) {
        super(aaguid, credentialId, counter, attestationStatement, credentialPublicKey, attestationStatementFormat, transports);
        this.authenticatorProvider = authenticatorProvider;
        this.iconLight = iconLight;
        this.iconDark = iconDark;
    }

    /** @return 认证器 Provider 标识 */
    public String getAuthenticatorProvider() {
        return authenticatorProvider;
    }

    /** @return 浅色主题图标 URL */
    public String getIconLight() {
        return iconLight;
    }

    /** @return 深色主题图标 URL */
    public String getIconDark() {
        return iconDark;
    }
}
