package org.keycloak.services.x509;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

import org.jboss.logging.Logger;

/**
 * RFC 9440 客户端证书查找工厂。
 *
 * <p>工厂及其对应提供者从符合 RFC 9440 的反向代理转发的 HTTP 头中提取客户端证书。</p>
 *
 * @author <a href="mailto:seiferma.dev+kc@gmail.com">Stephan Seifermann</a>
 * @version $Revision: 1 $
 * @since 12/30/2024
 */
public class Rfc9440ClientCertificateLookupFactory implements X509ClientCertificateLookupFactory {

    private final static Logger logger = Logger.getLogger(Rfc9440ClientCertificateLookupFactory.class);
    /** 提供者标识符：{@code rfc9440}。 */
    private final static String PROVIDER = "rfc9440";

    /** 客户端证书 HTTP 头配置键。 */
    protected final static String HTTP_HEADER_CLIENT_CERT = "sslClientCert";
    /** 客户端证书 HTTP 头默认值。 */
    protected final static String HTTP_HEADER_CLIENT_CERT_DEFAULT = "Client-Cert";
    /** 证书链 HTTP 头配置键。 */
    protected final static String HTTP_HEADER_CERT_CHAIN = "sslCertChain";
    /** 证书链 HTTP 头默认值。 */
    protected final static String HTTP_HEADER_CERT_CHAIN_DEFAULT = "Client-Cert-Chain";
    /** 链长度上限配置键。 */
    protected final static String HTTP_HEADER_CERT_CHAIN_LENGTH = "certificateChainLength";
    /** 链长度上限默认值。 */
    protected final static int HTTP_HEADER_CERT_CHAIN_LENGTH_DEFAULT = 1;

    /** 配置的客户端证书 HTTP 头名。 */
    protected String sslClientCertHttpHeader;
    /** 配置的证书链 HTTP 头名。 */
    protected String sslChainHttpHeader;
    /** 配置的链长度上限。 */
    protected int certificateChainLength;

    /** {@inheritDoc} 读取头名与链长度配置。 */
    @Override
    public void init(Config.Scope config) {
        certificateChainLength = config.getInt(HTTP_HEADER_CERT_CHAIN_LENGTH, HTTP_HEADER_CERT_CHAIN_LENGTH_DEFAULT);
        if (certificateChainLength < 0) {
            throw new IllegalArgumentException(HTTP_HEADER_CERT_CHAIN_LENGTH + " must be >= 0, but was " + certificateChainLength);
        }
        sslClientCertHttpHeader = config.get(HTTP_HEADER_CLIENT_CERT, HTTP_HEADER_CLIENT_CERT_DEFAULT);
        sslChainHttpHeader = config.get(HTTP_HEADER_CERT_CHAIN, HTTP_HEADER_CERT_CHAIN_DEFAULT);

        logger.tracev("{0}:   ''{1}''", HTTP_HEADER_CLIENT_CERT, sslClientCertHttpHeader);
        logger.tracev("{0}:   ''{1}''", HTTP_HEADER_CERT_CHAIN, sslChainHttpHeader);
        logger.tracev("{0}:   ''{1}''", HTTP_HEADER_CERT_CHAIN_LENGTH, certificateChainLength);
    }

    /** {@inheritDoc} 创建 RFC 9440 查找实例。 */
    @Override
    public X509ClientCertificateLookup create(KeycloakSession session) {
        return new Rfc9440ClientCertificateLookup(sslClientCertHttpHeader, sslChainHttpHeader, certificateChainLength);
    }

    /** {@inheritDoc} 无后置初始化逻辑。 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // intentionally left blank
    }

    /** {@inheritDoc} 无资源需释放。 */
    @Override
    public void close() {
        // intentionally left blank
    }

    /** {@inheritDoc} 返回 {@link #PROVIDER}。 */
    @Override
    public String getId() {
        return PROVIDER;
    }
}
