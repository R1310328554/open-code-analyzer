package org.keycloak.protocol.oid4vc.utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.keycloak.common.util.KeycloakUriBuilder;
import org.keycloak.common.util.Time;
import org.keycloak.models.ClientModel;
import org.keycloak.models.IssuedVerifiableCredentialModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserVerifiableCredentialModel;
import org.keycloak.models.oid4vci.CredentialScopeModel;
import org.keycloak.protocol.oid4vc.OID4VCLoginProtocolFactory;
import org.keycloak.protocol.oid4vc.issuance.OID4VCIssuerWellKnownProvider;

import static org.keycloak.protocol.oid4vc.issuance.OID4VCIssuerEndpoint.CREDENTIAL_OFFER_PATH;

/**
 * OpenID for Verifiable Credentials（OID4VC）通用工具。
 * <p>提供凭证发放 URI 构建、用户可验证凭证校验及已发放凭证查询。</p>
 */
public class OID4VCUtil {

    /** 工具类，私有构造器。 */
    private OID4VCUtil() {
    }

    /**
     * 构建可分享给钱包的凭证发放 URI（{@code openid-credential-offer://}）。
     * @param session Keycloak 会话
     * @param nonce 凭证发放 URI 中的 nonce
     * @return 编码后的凭证发放 URI
     */
    public static String getOfferAsUri(KeycloakSession session, String nonce) {
        String offerUri = KeycloakUriBuilder.fromUri(
                OID4VCIssuerWellKnownProvider.getIssuer(session.getContext()) + "/protocol/{protocol}/{credentialOfferPath}/{nonce}")
                .buildAsString(OID4VCLoginProtocolFactory.PROTOCOL_ID, CREDENTIAL_OFFER_PATH, nonce);
        return "openid-credential-offer://?credential_offer_uri=" + URLEncoder.encode(offerUri, StandardCharsets.UTF_8);
    }

    /**
     * 判断用户是否已持有指定凭证 scope 的可验证凭证。
     * @param session Keycloak 会话
     * @param user 用户
     * @param credentialScope 凭证 scope
     * @return 若用户账户上存在对应可验证凭证则为 true
     */
    public static boolean hasVerifiableCredential(KeycloakSession session, UserModel user, CredentialScopeModel credentialScope) {
        return session.users().getVerifiableCredentialsByUser(user.getId())
                .anyMatch(credential -> credential.getClientScopeId().equals(credentialScope.getId()));
    }

    /**
     * 校验用户已发放凭证是否存在且与预期客户端、scope 一致且未过期。
     * @param session Keycloak 会话
     * @param user 用户
     * @param issuedCredentialId 已发放凭证 ID
     * @param expectedCredentialScope 预期凭证 scope
     * @param expectedClient 预期客户端
     * @return 校验通过的已发放可验证凭证模型
     * @throws IllegalStateException 凭证缺失、不匹配或已过期
     */
    public static IssuedVerifiableCredentialModel checkIssuedVerifiableCredential(KeycloakSession session, UserModel user, String issuedCredentialId, CredentialScopeModel expectedCredentialScope, ClientModel expectedClient) {
        if (issuedCredentialId == null) {
            throw new IllegalStateException("Issued credential ID not present");
        }

        // TODO: 性能优化：宜按 ID 直接查询已发放凭证
        Optional<IssuedVerifiableCredentialModel> issuedCred = session.users().getIssuedVerifiableCredentialsStreamByUser(user.getId())
                .filter(issuedCredential -> issuedCredential.getId().equals(issuedCredentialId))
                .findFirst();
        if (issuedCred.isEmpty()) {
            throw new IllegalStateException("Verifiable credential not found");
        }
        if (!expectedClient.getId().equals(issuedCred.get().getClientId())) {
            throw new IllegalStateException("Different client sent credential request than client from issued-credential");
        }

        // 解析 verifiableCredentialId 以核对预期 scope
        UserVerifiableCredentialModel verifiableCredential = session.users()
                .getVerifiableCredentialById(issuedCred.get().getVerifiableCredentialId());
        if (verifiableCredential == null) {
            throw new IllegalStateException("User verifiable credential not found for issued credential");
        }
        if (!expectedCredentialScope.getId().equals(verifiableCredential.getClientScopeId())) {
            throw new IllegalStateException("Different client scope than client scope from issued-credential");
        }

        IssuedVerifiableCredentialModel issuedCredential = issuedCred.get();

        // 校验已发放凭证未过期
        long currentTimeMs = Time.currentTimeMillis();
        if (currentTimeMs > issuedCredential.getExpiresAt()) {
            throw new IllegalStateException("Issued credential is expired");
        }

        return issuedCredential;
    }

    /**
     * 列出指定用户在某客户端下已发放的可验证凭证。
     * @param session Keycloak 会话
     * @param user 用户
     * @param client 客户端
     * @return 已发放凭证列表
     */
    public static List<IssuedVerifiableCredentialModel> getIssuedVerifiableCredentialsByUserAndClient(KeycloakSession session, UserModel user, ClientModel client) {
        return session.users().getIssuedVerifiableCredentialsStreamByUser(user.getId())
                .filter(issuedCredential -> client.getId().equals(issuedCredential.getClientId()))
                .toList();
    }
}
