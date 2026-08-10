package org.keycloak.services.x509;

import java.security.cert.X509Certificate;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.truststore.TruststoreProvider;
import org.keycloak.truststore.TruststoreProviderFactory;

import org.jboss.logging.Logger;

/**
 * NGINX 反向代理 X.509 客户端证书查找工厂。
 *
 * <p>工厂及其对应提供者从 NGINX（TLS 终结）转发的 HTTP 头中提取客户端证书。
 * 根据 {@code trust-proxy-verification} 配置，可选择信任库重建链或依赖 NGINX 验证结果。</p>
 *
 * @author <a href="mailto:arnault.michel@toad-consulting.com">Arnault MICHEL</a>
 * @version $Revision: 1 $
 * @since 10/09/2018
 */

public class NginxProxySslClientCertificateLookupFactory extends AbstractClientCertificateFromHttpHeadersLookupFactory {

    private static final Logger logger = Logger.getLogger(NginxProxySslClientCertificateLookupFactory.class);

    /** 提供者标识符：{@code nginx}。 */
    private static final String PROVIDER = "nginx";

    /** 是否信任 NGINX 的 {@code ssl-client-verify} 验证结果而非自行重建链。 */
    protected static final String TRUST_PROXY_VERIFICATION = "trust-proxy-verification";

    /** 证书 PEM 是否经 URL 编码。 */
    protected static final String CERT_IS_URL_ENCODED = "cert-is-url-encoded";

    /** 是否启用代理验证模式。 */
    protected boolean trustProxyVerification;

    /** 证书是否 URL 编码。 */
    protected boolean certIsUrlEncoded;

    /** 信任库是否已加载（volatile 保证可见性）。 */
    private volatile boolean isTruststoreLoaded;

    /** 根 CA 证书集合。 */
    private Set<X509Certificate> trustedRootCerts;

    /** 中间 CA 证书集合。 */
    private Set<X509Certificate> intermediateCerts;

    /** {@inheritDoc} 读取代理验证与 URL 编码配置，初始化证书集合。 */
    @Override
    public void init(Config.Scope config) {
        super.init(config);
        this.trustProxyVerification = config.getBoolean(TRUST_PROXY_VERIFICATION, false);
        logger.tracev("{0}: ''{1}''", TRUST_PROXY_VERIFICATION, trustProxyVerification);
        this.certIsUrlEncoded = config.getBoolean(CERT_IS_URL_ENCODED, true);
        logger.tracev("{0}: ''{1}''", CERT_IS_URL_ENCODED, certIsUrlEncoded);
        this.isTruststoreLoaded = false;
        this.trustedRootCerts = ConcurrentHashMap.newKeySet();
        this.intermediateCerts = ConcurrentHashMap.newKeySet();

    }

    /** {@inheritDoc} 懒加载信任库后，按配置返回链重建或代理验证查找器。 */
    @Override
    public X509ClientCertificateLookup create(KeycloakSession session) {
        loadKeycloakTrustStore(session);
        if (trustProxyVerification) {
            return new NginxProxyTrustedClientCertificateLookup(sslClientCertHttpHeader,
                    sslChainHttpHeaderPrefix, certificateChainLength, certIsUrlEncoded);
        } else {
            return new NginxProxySslClientCertificateLookup(sslClientCertHttpHeader,
                    sslChainHttpHeaderPrefix, certificateChainLength, intermediateCerts, trustedRootCerts, isTruststoreLoaded, certIsUrlEncoded);
        }
    }

    /** {@inheritDoc} 返回 {@link #PROVIDER}。 */
    @Override
    public String getId() {
        return PROVIDER;
    }

    /**
     * 首次登录时懒加载 Keycloak 信任库（双重检查锁定）。
     *
     * @param kcSession Keycloak 会话
     */
    private void loadKeycloakTrustStore(KeycloakSession kcSession) {

        if (isTruststoreLoaded){
            return;
        }

        synchronized (this) {
            if (isTruststoreLoaded) {
                return;
            }
            logger.debug(" Loading Keycloak truststore ...");
            KeycloakSessionFactory factory = kcSession.getKeycloakSessionFactory();
            TruststoreProviderFactory truststoreFactory = (TruststoreProviderFactory) factory.getProviderFactory(TruststoreProvider.class);
            TruststoreProvider provider = truststoreFactory.create(kcSession);

            if (provider != null && provider.getTruststore() != null) {
                Set<X509Certificate> rootCertificates = provider.getRootCertificates().entrySet().stream().flatMap(t -> t.getValue().stream()).collect(Collectors.toSet());
                Set<X509Certificate> intermediateCertficiates = provider.getIntermediateCertificates().entrySet().stream().flatMap(t -> t.getValue().stream()).collect(Collectors.toSet());

                trustedRootCerts.addAll(rootCertificates);
                intermediateCerts.addAll(intermediateCertficiates);
                logger.debug("Keycloak truststore loaded for NGINX x509cert-lookup provider.");

                isTruststoreLoaded = true;
            }
        }
    }
}
