package org.keycloak.authentication.authenticators.client;

import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.security.auth.x500.X500Principal;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.keycloak.OAuth2Constants;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.ClientAuthenticationFlowContext;
import org.keycloak.authentication.authenticators.x509.CertificateValidator;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oidc.OIDCAdvancedConfigWrapper;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.services.x509.X509ClientCertificateLookup;
import org.keycloak.utils.StringUtil;

/**
 * X.509 客户端证书认证器：通过双向 TLS 提供的客户端证书校验客户端身份。
 * <p>匹配证书 Subject DN 与客户端配置，并可选校验 CA 证书链；支持 Open Banking Brasil 等自定义 OID 属性。</p>
 */
public class X509ClientAuthenticator extends AbstractClientAuthenticator {

    /** 提供者标识符。 */
    public static final String PROVIDER_ID = "client-x509";
    /** 客户端 X509 属性前缀。 */
    public static final String ATTR_PREFIX = "x509";
    /** 客户端属性键：期望的证书 Subject DN。 */
    public static final String ATTR_SUBJECT_DN = ATTR_PREFIX + ".subjectdn";
    /** 客户端属性键：签发 CA 的 Subject DN。 */
    public static final String ATTR_CA_SUBJECT_DN = ATTR_PREFIX + ".casubjectdn";

    /** 客户端属性键：是否允许正则匹配 Subject DN（已弃用）。 */
    public static final String ATTR_ALLOW_REGEX_PATTERN_COMPARISON = ATTR_PREFIX + ".allow.regex.pattern.comparison";

    // Open Banking Brasil 规范定义的自定义 OID（RFC1779/RFC2253 默认不识别） - https://openbanking-brasil.github.io/specs-seguranca/open-banking-brasil-certificate-standards-1_ID1.html#name-client-certificate
    // These are not recognized by default in RFC1779 or RFC2253 and hence not read in the java by default
    private static final Map<String, String> CUSTOM_OIDS = new HashMap<>();
    private static final Map<String, String> CUSTOM_OIDS_REVERSED = new HashMap<>();

    static {
        CUSTOM_OIDS.put("2.5.4.5", "serialNumber".toUpperCase());
        CUSTOM_OIDS.put("2.5.4.15", "businessCategory".toUpperCase());
        CUSTOM_OIDS.put("1.3.6.1.4.1.311.60.2.1.3", "jurisdictionCountryName".toUpperCase());
        CUSTOM_OIDS.put("1.2.840.113549.1.9.1", "emailAddress".toUpperCase());

        // 反向映射：属性名 -> OID
        for (Map.Entry<String, String> entry : CUSTOM_OIDS.entrySet()) {
            CUSTOM_OIDS_REVERSED.put(entry.getValue(), entry.getKey());
        }
        CUSTOM_OIDS_REVERSED.put("E", "1.2.840.113549.1.9.1"); // Another synonym for "EMAILADDRESS"
    }

    /** 使用自定义 OID 映射构造 {@link X500Principal}。 */
    public static X500Principal constructX500Principal(String subjectDN) {
        return new X500Principal(subjectDN, CUSTOM_OIDS_REVERSED);
    }

    /** 从请求提取客户端证书，校验 Subject DN 与 CA 链后认证客户端。 */
    @Override
    public void authenticateClient(ClientAuthenticationFlowContext context) {

        X509ClientCertificateLookup provider = context.getSession().getProvider(X509ClientCertificateLookup.class);
        if (provider == null) {
            logger.errorv("\"{0}\" Spi is not available, did you forget to update the configuration?",
                    X509ClientCertificateLookup.class);
            return;
        }

        X509Certificate[] certs = null;
        ClientModel client = null;
        try {
            certs = provider.getCertificateChain(context.getHttpRequest());
            String client_id = null;
            MediaType mediaType = context.getHttpRequest().getHttpHeaders().getMediaType();
            boolean hasFormData = mediaType != null && mediaType.isCompatible(MediaType.APPLICATION_FORM_URLENCODED_TYPE);

            MultivaluedMap<String, String> formData = hasFormData ? context.getHttpRequest().getDecodedFormParameters() : null;
            MultivaluedMap<String, String> queryParams = context.getSession().getContext().getUri().getQueryParameters();

            if (formData != null) {
                client_id = formData.getFirst(OAuth2Constants.CLIENT_ID);
            }

            if (client_id == null && queryParams != null) {
                client_id = queryParams.getFirst(OAuth2Constants.CLIENT_ID);
            }

            if (client_id == null) {
                client_id = context.getSession().getAttribute("client_id", String.class);
            }

            if (client_id == null) {
                Response challengeResponse = ClientAuthUtil.errorResponse(Response.Status.BAD_REQUEST.getStatusCode(), "invalid_client", "Missing client_id parameter");
                context.challenge(challengeResponse);
                return;
            }

            client = context.getRealm().getClientByClientId(client_id);
            if (client == null) {
                context.failure(AuthenticationFlowError.CLIENT_NOT_FOUND, null);
                return;
            }
            context.getEvent().client(client_id);
            context.setClient(client);

            if (!client.isEnabled()) {
                context.failure(AuthenticationFlowError.CLIENT_DISABLED, null);
                return;
            }
        } catch (GeneralSecurityException e) {
            logger.errorf("[X509ClientCertificateAuthenticator:authenticate] Exception: %s", e.getMessage());
            context.attempted();
            return;
        }

        if (certs == null || certs.length == 0) {
            // No x509 client cert, fall through and
            // continue processing the rest of the authentication flow
            logger.debug("[X509ClientCertificateAuthenticator:authenticate] x509 client certificate is not available for mutual SSL.");
            context.attempted();
            return;
        }

        OIDCAdvancedConfigWrapper clientCfg = OIDCAdvancedConfigWrapper.fromClientModel(client);
        String subjectDNRegexp = client.getAttribute(ATTR_SUBJECT_DN);
        if (StringUtil.isBlank(subjectDNRegexp)) {
            logger.errorf("[X509ClientCertificateAuthenticator:authenticate] %s is null or empty", ATTR_SUBJECT_DN);
            context.attempted();
            return;
        }

        // 仅校验证书链中第一张证书的 Subject DN
        X509Certificate certificate = certs[0];
        boolean matchedCertificate = checkSubjectDN(context, certificate, subjectDNRegexp, clientCfg.getAllowRegexPatternComparison());

        if (!matchedCertificate) {
            // We do quite expensive operation here, so better check the logging level beforehand.
            if (logger.isDebugEnabled()) {
                logger.debugf("[X509ClientCertificateAuthenticator:authenticate] Couldn't match any certificate for expected Subject DN '%s' with allow regex pattern '%s'.", subjectDNRegexp, clientCfg.getAllowRegexPatternComparison());
                logger.debugf("[X509ClientCertificateAuthenticator:authenticate] Checked Subject DN: %s", certificate.getSubjectDN().getName());
                logger.debugf("[X509ClientCertificateAuthenticator:authenticate] All SubjectDNs from the certificate chain: %s",
                        Arrays.stream(certs)
                                .map(cert -> cert.getSubjectDN().getName())
                                .collect(Collectors.toList()));
            }
            context.attempted();
            return;
        }

        // get the name of the CA to check
        String caSubjectDN = client.getAttribute(ATTR_CA_SUBJECT_DN);
        if (StringUtil.isBlank(caSubjectDN)) {
            // TODO：Keycloak 27.0 将强制要求配置 CA Subject DN
            logger.warnf("[X509ClientCertificateAuthenticator:authenticate] option '%s' is null or empty, this configuration is deprecated, please configure it for better security for client '%s' in realm '%s'",
                    ATTR_CA_SUBJECT_DN, client.getClientId(), context.getRealm().getName());
            // 未配置 CA DN 时向后兼容直接成功
            context.success();
            return;
        }

        // 校验证书链是否由受信 CA 签发
        X509Certificate ca = validateCertificateChain(context.getSession(), certs);
        if (ca == null) {
            logger.debugf("[X509ClientCertificateAuthenticator:authenticate] Cert '%s' is not trusted by keycloak.", certificate.getSubjectDN().getName());
            context.attempted();
            return;
        }

        // 校验 CA Subject DN 是否与配置一致
        if (!checkSubjectDNExact(ca, caSubjectDN)) {
            if (logger.isDebugEnabled()) {
                logger.debugf("[X509ClientCertificateAuthenticator:authenticate] Couldn't match CA certificate for expected Subject DNx %s with allow regex pattern '%s'.", caSubjectDN, clientCfg.getAllowRegexPatternComparison());
                logger.debugf("[X509ClientCertificateAuthenticator:authenticate] CA Subject DN: %s", ca.getSubjectDN().getName());
            }
            context.attempted();
            return;
        }

        logger.debugf("[X509ClientCertificateAuthenticator:authenticate] Matched %s certificate.", certificate.getSubjectDN().getName());
        context.success();
    }

    /** 按精确或正则方式匹配证书 Subject DN。 */
    private boolean checkSubjectDN(ClientAuthenticationFlowContext context, X509Certificate certificate, String subjectDN, boolean isRegExp){
        if (isRegExp) {
            return checkSubjectDNRegex(context, certificate, subjectDN);
        } else {
            return checkSubjectDNExact(certificate, subjectDN);
        }
    }

    private boolean checkSubjectDNRegex(ClientAuthenticationFlowContext context, X509Certificate certificate, String subjectDN) {
        Pattern subjectDNPattern = Pattern.compile(subjectDN);

        // getSubjectDN is deprecated and says should not be relied upon by portable code, we are deprecating regex comparison
        // TODO: Remove this option in keycloak 27.0
        logger.warnf("Regex comparison is deprecated. Please configure the X.509 client authenticator to use exact Subject DN for client '%s' in realm '%s'.",
                context.getRealm().getName(), context.getClient().getClientId());
        String subjectdn = certificate.getSubjectDN().getName();
        return subjectDNPattern.matcher(subjectdn).matches();
    }

    /** 按 RFC4514 格式精确比较 Subject DN（支持自定义 OID 展开）。 */
    private boolean checkSubjectDNExact(X509Certificate certificate, String subjectDN) {
        // OIDC/OAuth2 要求 RFC4514 精确 DN 匹配，参见 RFC8705
        // We allow custom OIDs attributes to be "expanded" or not expanded in the given Subject DN
        X500Principal expectedDNPrincipal = constructX500Principal(subjectDN);

        return (expectedDNPrincipal.getName(X500Principal.RFC2253, CUSTOM_OIDS).equals(certificate.getSubjectX500Principal().getName(X500Principal.RFC2253, CUSTOM_OIDS)));
    }

    /** 校验证书链信任与时戳，返回信任锚 CA 证书。 */
    private X509Certificate validateCertificateChain(KeycloakSession session, X509Certificate[] certs) {
        try {
            CertificateValidator validator = new CertificateValidator.CertificateValidatorBuilder()
                    .session(session)
                    .trustValidation()
                        .enabled(true)
                    .timestampValidation()
                        .enabled(true)
                    .build(certs);
            validator.checkRevocationStatus()
                    .validateTimestamps()
                    .validateTrust();
            return validator.getCertPathBuilderResult().getTrustAnchor().getTrustedCert();
        } catch (GeneralSecurityException e) {
            logger.warnf(e, "Invalid certificate %s", certs[0].getSubjectX500Principal().getName(X500Principal.RFC2253, CUSTOM_OIDS));
            return null;
        }
    }

    public String getDisplayType() {
        return "X509 Certificate";
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public List<ProviderConfigProperty> getConfigPropertiesPerClient() {
        return Collections.emptyList();
    }

    @Override
    public Map<String, Object> getAdapterConfiguration(KeycloakSession session, ClientModel client) {
        return Collections.emptyMap();
    }

    /** @return OIDC 协议下支持的认证方法（tls_client_auth） */
   @Override
    public Set<String> getProtocolAuthenticatorMethods(String loginProtocol) {
        if (loginProtocol.equals(OIDCLoginProtocol.LOGIN_PROTOCOL)) {
            Set<String> results = new HashSet<>();
            results.add(OIDCLoginProtocol.TLS_CLIENT_AUTH);
            return results;
        } else {
            return Collections.emptySet();
        }
    }

    @Override
    public String getHelpText() {
        return "Validates client based on a X509 Certificate";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return Collections.emptyList();
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

}
