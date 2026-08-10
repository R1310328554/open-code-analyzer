package org.keycloak.protocol.oid4vc.model;

import org.keycloak.representations.JsonWebToken;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * OpenID4VCI 预授权码（pre-authorized code）JWT 载荷。
 * <p>嵌入凭证发放状态的公开、非敏感子集，供 {@link JwtPreAuthCode} 序列化与验证。</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JwtPreAuthCode extends JsonWebToken {

    /** 预授权码上下文（凭证发放公开视图）。 */
    @JsonProperty("context")
    private PreAuthCodeCtx context;

    /** 随机盐，增强预授权码不可预测性。 */
    @JsonProperty("salt")
    private String salt;

    /** @return 预授权码上下文 */
    public PreAuthCodeCtx getContext() {
        return context;
    }

    /** @param context 预授权码上下文 @return 当前实例 */
    public JwtPreAuthCode context(PreAuthCodeCtx context) {
        this.context = context;
        return this;
    }

    /** @return 盐值 */
    public String getSalt() {
        return salt;
    }

    /** @param salt 盐值 @return 当前实例 */
    public JwtPreAuthCode salt(String salt) {
        this.salt = salt;
        return this;
    }
}
