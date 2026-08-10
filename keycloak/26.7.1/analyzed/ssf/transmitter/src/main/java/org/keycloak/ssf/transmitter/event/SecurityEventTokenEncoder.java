package org.keycloak.ssf.transmitter.event;

import java.util.Map;

import org.keycloak.crypto.SignatureProvider;
import org.keycloak.crypto.SignatureSignerContext;
import org.keycloak.jose.jws.JWSBuilder;
import org.keycloak.models.KeycloakSession;
import org.keycloak.ssf.Ssf;
import org.keycloak.ssf.SsfException;
import org.keycloak.ssf.event.token.SecurityEventToken;
import org.keycloak.ssf.event.token.SsfSecurityEventToken;

/**
 * 为给定 SSF 安全事件令牌（SET）生成已签名、base64url 编码的 JWS。
 * 刻意保持为薄 JWS 包装层：算法选择与允许列表校验由 {@link SsfSignatureAlgorithms} 负责，
 * 并在调用 {@link #encode(SecurityEventToken, String)} 前由派发器解析。
 * 这使编码器与发送方配置及接收方流状态解耦。
 */
public class SecurityEventTokenEncoder {

    private final KeycloakSession session;

    public SecurityEventTokenEncoder(KeycloakSession session) {
        this.session = session;
    }

    /**
     * 使用请求的 JWS 算法对给定令牌签名并编码。
     *
     * @throws SsfException 若 realm 未为 {@code signatureAlgorithm} 注册 {@link SignatureProvider}。
     *         对 FIPS 受限或配置错误的 realm 给出明确错误，而非在 {@link JWSBuilder} 深处 NPE。
     */
    public String encode(SecurityEventToken token, String signatureAlgorithm) {

        // CAEP Interop Profile 1.0 §2.8.1 MUST: a SET carries exactly
        // one event. Our generators always produce a single entry, but
        // a defensive check here fails loud if a future refactor
        // accidentally ships a multi-event SET rather than silently
        // emitting a spec-violating token.
        if (token instanceof SsfSecurityEventToken ssfToken) {
            Map<String, Object> events = ssfToken.getEvents();
            if (events == null || events.size() != 1) {
                throw new SsfException("SSF SET must carry exactly one event (events map size="
                        + (events == null ? 0 : events.size()) + ")");
            }
        }

        SignatureProvider signatureProvider = session.getProvider(SignatureProvider.class, signatureAlgorithm);
        if (signatureProvider == null) {
            throw new SsfException("No signer available for SSF SET signature algorithm " + signatureAlgorithm
                    + " — check the realm's active keys and FIPS/BCFIPS configuration.");
        }

        SignatureSignerContext signer = signatureProvider.signer();
        return newJwsBuilder().jsonContent(token).sign(signer);
    }

    protected JWSBuilder newJwsBuilder() {
        return new JWSBuilder().type(Ssf.SECEVENT_JWT_TYPE);
    }
}
