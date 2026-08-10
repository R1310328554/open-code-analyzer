package org.keycloak.services.util;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.keycloak.common.util.Base64Url;
import org.keycloak.http.HttpRequest;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.ProtocolMapper;
import org.keycloak.protocol.oidc.mappers.AbstractOIDCProtocolMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAccessTokenMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAccessTokenResponseMapper;
import org.keycloak.protocol.oidc.mappers.OIDCIDTokenMapper;
import org.keycloak.protocol.oidc.mappers.TokenIntrospectionTokenMapper;
import org.keycloak.protocol.oidc.mappers.UserInfoTokenMapper;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.AccessToken;
import org.keycloak.services.x509.X509ClientCertificateLookup;

import org.jboss.logging.Logger;

/**
 * mTLS 持有者密钥（Holder-of-Key）令牌工具类。
 * <p>实现 OAuth mTLS 草案中的证书绑定令牌：在 cnf 声明中写入 x5t#S256 证书指纹，
 * 并在资源访问时校验请求客户端证书与令牌指纹一致。</p>
 *
 * @see <a href="https://tools.ietf.org/html/draft-ietf-oauth-mtls-08#section-3.1">OAuth mTLS</a>
 */
public class MtlsHoKTokenUtil {
    // KEYCLOAK-6771 Certificate Bound Token

    protected static final Logger logger = Logger.getLogger(MtlsHoKTokenUtil.class);

    /** 证书指纹哈希算法 */
    private static final String DIGEST_ALG = "SHA-256";

    /** 证书校验失败时的 OAuth 错误描述 */
    public static final String CERT_VERIFY_ERROR_DESC = "Client certificate missing, or its thumbprint and one in the token did NOT match";

    /** 返回用于临时注入的 mTLS HoK 协议映射器实例。 */
    public static Stream<Map.Entry<ProtocolMapperModel, ProtocolMapper>> getTransientProtocolMapper() {
        ProtocolMapperModel protocolMapperModel = new ProtocolMapperModel();
        protocolMapperModel.setId(MtlsHoKProtocolMapper.PROVIDER_ID);
        protocolMapperModel.setName("mtls-hok");
        protocolMapperModel.setProtocolMapper(MtlsHoKProtocolMapper.PROVIDER_ID);
        protocolMapperModel.setProtocol("openid-connect");
        protocolMapperModel.setConfig(Map.of());
        return Stream.of(Map.entry(protocolMapperModel, new MtlsHoKProtocolMapper()));
    }

    /**
     * 将访问令牌与当前请求的客户端证书绑定，写入 cnf.x5t#S256 指纹。
     *
     * @return 含证书指纹的 Confirmation，无证书或计算失败时返回 null
     */
    public static AccessToken.Confirmation bindTokenWithClientCertificate(HttpRequest request, KeycloakSession session) {
        X509Certificate[] certs = getCertificateChain(request, session);

        if (certs == null || certs.length < 1) {
            logger.warnf("no client certificate available.");
            return null;
        }

        String DERX509Base64UrlEncoded = null;
        try {
            // 证书链首项为客户端叶子证书
            DERX509Base64UrlEncoded = getCertificateThumbprintInSHA256DERX509Base64UrlEncoded(certs[0]);
            if (logger.isTraceEnabled()) dumpCertInfo(certs);
        } catch (NoSuchAlgorithmException | CertificateEncodingException e) {
            // 无法计算指纹则放弃签发 HoK 令牌
            logger.warnf("give up issuing hok token. %s", e);
            return null;
        }

        AccessToken.Confirmation confirmation = new AccessToken.Confirmation();
        confirmation.setCertThumbprint(DERX509Base64UrlEncoded);
        return confirmation;
    }

    /**
     * 校验 HoK 令牌 cnf 指纹与请求客户端证书是否一致。
     *
     * @return 绑定有效返回 true
     */
    public static boolean verifyTokenBindingWithClientCertificate(AccessToken token, HttpRequest request, KeycloakSession session) {
        if (token == null) {
            logger.warnf("token is null");
            return false;
        }

        // Bearer 令牌无 cnf 声明
        if (token.getConfirmation() == null) {
            logger.warnf("bearer token received instead of hok token.");
            return false;
        }

        X509Certificate[] certs = getCertificateChain(request, session);

        // HoK 令牌但请求未携带客户端证书
        if (certs == null || certs.length < 1) {
            logger.warnf("missing client certificate.");
            return false;
        }

        String DERX509Base64UrlEncoded = null;
        String x5ts256 = token.getConfirmation().getCertThumbprint();
        logger.tracef("hok token cnf-x5t#s256 = %s", x5ts256);

        try {
            // 证书链首项为客户端叶子证书
            DERX509Base64UrlEncoded = getCertificateThumbprintInSHA256DERX509Base64UrlEncoded(certs[0]);
            if (logger.isTraceEnabled()) dumpCertInfo(certs);
        } catch (NoSuchAlgorithmException | CertificateEncodingException e) {
            logger.warnf("client certificate exception. %s", e);
            return false;
        }

        if (!MessageDigest.isEqual(x5ts256.getBytes(), DERX509Base64UrlEncoded.getBytes())) {
            logger.warnf("certificate's thumbprint and one in the token did not match.");
            return false;
        }

        return true;
    }

    /** 通过 {@link X509ClientCertificateLookup} SPI 获取客户端证书链。 */
    private static X509Certificate[] getCertificateChain(HttpRequest request, KeycloakSession session) {
        try {
               // 从 SPI 获取 x509 客户端证书
            X509ClientCertificateLookup provider = session.getProvider(X509ClientCertificateLookup.class);
            if (provider == null) {
                logger.errorv("\"{0}\" Spi is not available, did you forget to update the configuration?", X509ClientCertificateLookup.class);
            return null;
            }
            X509Certificate[] certs = provider.getCertificateChain(request);
            return certs;
        } catch (GeneralSecurityException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 计算 X.509 证书 DER 编码的 SHA-256 指纹，Base64Url 编码。
     * <p>符合 OAuth mTLS cnf.x5t#S256 规范。</p>
     */
    private static String getCertificateThumbprintInSHA256DERX509Base64UrlEncoded (X509Certificate cert) throws NoSuchAlgorithmException, CertificateEncodingException {
        // 对 X.509 证书的 DER 编码计算 SHA-256
        byte[] DERX509Hash = cert.getEncoded();
        MessageDigest md = MessageDigest.getInstance(DIGEST_ALG);
        md.update(DERX509Hash);
        String DERX509Base64UrlEncoded = Base64Url.encode(md.digest());
        return DERX509Base64UrlEncoded;
    }

    /** trace 级别输出证书链调试信息。 */
    private static void dumpCertInfo(X509Certificate[] certs) throws CertificateEncodingException  {
        logger.tracef(":: Try Holder of Key Token");
        logger.tracef(":: # of x509 Client Certificate in Certificate Chain = %d", certs.length);
        for (int i = 0; i < certs.length; i++) {
            logger.tracef(":: certs[%d] Raw Bytes Counts of first x509 Client Certificate in Certificate Chain = %d", i, certs[i].toString().length());
            logger.tracef(":: certs[%d] Raw Bytes String of first x509 Client Certificate in Certificate Chain = %s", i, certs[i].toString());
            logger.tracef(":: certs[%d] DER Dump Bytes of first x509 Client Certificate in Certificate Chain = %d", i, certs[i].getEncoded().length);
            String DERX509Base64UrlEncoded = null;
            try {
                DERX509Base64UrlEncoded = getCertificateThumbprintInSHA256DERX509Base64UrlEncoded(certs[i]);
            } catch (Exception e) {}
            logger.tracef(":: certs[%d] Base64URL Encoded SHA-256 Hash of DER formatted first x509 Client Certificate in Certificate Chain = %s", i, DERX509Base64UrlEncoded);
            logger.tracef(":: certs[%d] DER Dump Bytes of first x509 Client Certificate TBScertificate in Certificate Chain = %d", i, certs[i].getTBSCertificate().length);
            logger.tracef(":: certs[%d] Signature Algorithm of first x509 Client Certificate in Certificate Chain = %s", i, certs[i].getSigAlgName());
            logger.tracef(":: certs[%d] Certfication Type of first x509 Client Certificate in Certificate Chain = %s", i, certs[i].getType());
            logger.tracef(":: certs[%d] Issuer DN of first x509 Client Certificate in Certificate Chain = %s", i, certs[i].getIssuerDN().getName());
            logger.tracef(":: certs[%d] Subject DN of first x509 Client Certificate in Certificate Chain = %s", i, certs[i].getSubjectDN().getName());
        }
    }

    /**
     * 将访问令牌绑定到客户端 mTLS 证书的协议映射器。
     * <p>在 cnf 声明中写入 x5t#S256 证书指纹，实现发送方约束令牌（含令牌交换场景）。</p>
     */
    public static class MtlsHoKProtocolMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper,
          OIDCIDTokenMapper, UserInfoTokenMapper, TokenIntrospectionTokenMapper, OIDCAccessTokenResponseMapper {

        /** 映射器 Provider ID */
        public static final String PROVIDER_ID = "mtls-hok-protocol-mapper";

        @Override
        public String getId() {
            return PROVIDER_ID;
        }

        @Override
        public String getDisplayCategory() {
            return TOKEN_MAPPER_CATEGORY;
        }

        @Override
        public String getDisplayType() {
            return "mtls-hok";
        }

        @Override
        public String getHelpText() {
            return "Binds access tokens to the client's mTLS certificate by adding the cnf claim with x5t#S256 thumbprint.";
        }

        @Override
        public List<ProviderConfigProperty> getConfigProperties() {
            return Collections.emptyList();
        }

        @Override
        public AccessToken transformAccessToken(AccessToken token, ProtocolMapperModel mappingModel,
                                                KeycloakSession session, UserSessionModel userSession, ClientSessionContext clientSessionCtx) {
            AccessToken.Confirmation confirmation = bindTokenWithClientCertificate(session.getContext().getHttpRequest(), session);
            if (confirmation != null) {
                token.setConfirmation(confirmation);
            }
            return token;
        }
    }
}
