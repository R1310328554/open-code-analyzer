package org.keycloak.storage.ldap.idm.store.ldap;

import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.StartTlsRequest;
import javax.naming.ldap.StartTlsResponse;
import javax.net.ssl.SSLSocketFactory;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.LDAPConstants;
import org.keycloak.storage.ldap.LDAPConfig;
import org.keycloak.tracing.TracingProvider;
import org.keycloak.truststore.TruststoreProvider;
import org.keycloak.vault.VaultStringSecret;

import org.jboss.logging.Logger;

import static javax.naming.Context.SECURITY_CREDENTIALS;

/**
 * LDAP 连接上下文管理器，负责创建、认证与关闭 {@link LdapContext}，支持 StartTLS 与连接池。
 *
 * @author mhajas
 */
public final class LDAPContextManager implements AutoCloseable {

    private static final Logger logger = Logger.getLogger(LDAPContextManager.class);

    private final KeycloakSession session;
    private final LDAPConfig ldapConfig;
    private StartTlsResponse tlsResponse;
    private LdapContext ldapContext;

    public LDAPContextManager(KeycloakSession session, LDAPConfig connectionProperties) {
        this.session = session;
        this.ldapConfig = connectionProperties;
    }

    public static LDAPContextManager create(KeycloakSession session, LDAPConfig connectionProperties) {
        return new LDAPContextManager(session, connectionProperties);
    }

    /** 以管理员身份创建并认证 LDAP 连接。 */
    private void createLdapContext() throws NamingException {
        var tracing = session.getProvider(TracingProvider.class);
        tracing.startSpan(LDAPContextManager.class, "createLdapContext");
        try {
            Hashtable<Object, Object> connProp = getNonAuthConnectionProperties(ldapConfig);

            // 非 StartTLS 时在初始环境中绑定，以便连接池复用；StartTLS 则推迟到协商完成后再绑定
            if (!ldapConfig.isStartTls()) {
                setAuthConnectionProperties(connProp, ldapConfig, getBindPassword());
            }

            if (ldapConfig.isConnectionTrace()) {
                connProp.put(LDAPConstants.CONNECTION_TRACE_BER, System.err);
            }

            ldapContext = new SessionBoundInitialLdapContext(session, connProp, null);

            // 按需发送 StartTLS 请求并配置 SSL 上下文
            if (ldapConfig.isStartTls()) {
                SSLSocketFactory sslSocketFactory = null;
                if (LDAPUtil.shouldUseTruststoreSpi(ldapConfig)) {
                    TruststoreProvider provider = session.getProvider(TruststoreProvider.class);
                    sslSocketFactory = provider.getSSLSocketFactory();
                }

                tlsResponse = startTLS(ldapContext, sslSocketFactory);

                // startTLS 失败时 LDAPContextManager.startTLS 应已抛异常，此处再做一次防御性检查
                if (tlsResponse == null) {
                    throw new NamingException("Wasn't able to establish LDAP connection through StartTLS");
                }

                // StartTLS 完成后才能进行认证绑定
                setAdminConnectionAuthProperties(ldapContext);
            }
        } catch (NamingException e) {
            tracing.error(e);
            throw e;
        } finally {
            tracing.endSpan();
        }

        // 绑定会在上下文上执行操作时自动触发，也可显式调用 reconnect()（如 LDAPServerCapabilitiesManager.testLDAP() 中的认证测试）
    }

    public LdapContext getLdapContext() throws NamingException {
        if (ldapContext == null) createLdapContext();

        return ldapContext;
    }

    /** 从 Vault 或配置读取 bind 密码，可能为 null。 */
    private String getBindPassword() {
        VaultStringSecret vaultSecret = session.vault().getStringSecret(ldapConfig.getBindCredential());
        return vaultSecret.get().orElse(ldapConfig.getBindCredential());
    }

    /**
     * 在已有 {@link LdapContext} 上发起 StartTLS 协商。
     *
     * @param ldapContext LDAP 上下文
     * @param sslSocketFactory SSL 套接字工厂，可为 null
     * @return StartTLS 响应
     */
    public static StartTlsResponse startTLS(LdapContext ldapContext, SSLSocketFactory sslSocketFactory) throws NamingException {
        StartTlsResponse tls = null;

        try {
            tls = (StartTlsResponse) ldapContext.extendedOperation(new StartTlsRequest());
            tls.negotiate(sslSocketFactory);
        } catch (Exception e) {
            logger.error("Could not negotiate TLS", e);
            NamingException ne = new AuthenticationException("Could not negotiate TLS");
            ne.setRootCause(e);
            throw ne;
        }

        return tls;
    }

    /** 将认证相关属性写入初始连接环境，使已绑定连接可被连接池复用。 */
    static void setAuthConnectionProperties(Hashtable<Object, Object> connProp, LDAPConfig ldapConfig, String bindPassword) {
        String authType = ldapConfig.getAuthType();
        if (authType != null) {
            connProp.put(Context.SECURITY_AUTHENTICATION, authType);
        }

        if (!LDAPConstants.AUTH_TYPE_NONE.equals(authType)) {
            String bindDN = ldapConfig.getBindDN();
            if (bindDN != null) {
                connProp.put(Context.SECURITY_PRINCIPAL, bindDN);
            }

            if (bindPassword != null) {
                connProp.put(SECURITY_CREDENTIALS, bindPassword);
            }
        }

        logConnectionProperties(connProp);
    }

    /** 在已有上下文上配置管理员认证属性。 */
    private void setAdminConnectionAuthProperties(LdapContext ldapContext) throws NamingException {
        String authType = ldapConfig.getAuthType();
        if (authType != null) {
            ldapContext.addToEnvironment(Context.SECURITY_AUTHENTICATION, authType);
        }

        if (!LDAPConstants.AUTH_TYPE_NONE.equals(authType)) {
            String bindDN = ldapConfig.getBindDN();
            if (bindDN != null) {
                ldapContext.addToEnvironment(Context.SECURITY_PRINCIPAL, bindDN);
            }

            String bindPassword = getBindPassword();
            if (bindPassword != null) {
                ldapContext.addToEnvironment(SECURITY_CREDENTIALS, bindPassword);
            }
        }

        logConnectionProperties(ldapContext.getEnvironment());
    }

    /** 记录连接环境（bind 凭证已脱敏）。 */
    private static void logConnectionProperties(Map<?, ?> env) {
        if (logger.isDebugEnabled()) {
            Map<Object, Object> copyEnv = new Hashtable<>(env);
            if (copyEnv.containsKey(Context.SECURITY_CREDENTIALS)) {
                copyEnv.put(Context.SECURITY_CREDENTIALS, "**************************************");
            }
            logger.debugf("Creating LdapContext using properties: [%s]", copyEnv);
        }
    }


    /**
     * 返回与认证无关的连接属性（不含 bindType、bindDn、bindPassword）。
     * 供管理员连接与用户认证共用；调用方需按场景自行填充认证属性。
     *
     * @param ldapConfig LDAP 配置
     * @return 非认证连接属性表
     */
    public static Hashtable<Object, Object> getNonAuthConnectionProperties(LDAPConfig ldapConfig) {
        HashMap<String, Object> env = new HashMap<>();

        env.put(Context.INITIAL_CONTEXT_FACTORY, ldapConfig.getFactoryName());

        String url = ldapConfig.getConnectionUrl();

        if (url != null) {
            if (url.contains(",")) {
                logger.warnf("LDAP connection URL contains commas, which are not supported as URL separators. "
                        + "Use spaces to separate multiple LDAP URLs for failover (e.g. \"ldap://host1:389 ldap://host2:389\"). "
                        + "Current URL: %s", url);
            }
            env.put(Context.PROVIDER_URL, url);
        } else {
            logger.warn("LDAP URL is null. LDAPOperationManager won't work correctly");
        }

        // StartTLS 时使用默认套接字工厂，TrustStore SSL 工厂在 StartTlsResponse.negotiate() 时传入
        if (!ldapConfig.isStartTls() && LDAPUtil.shouldUseTruststoreSpi(ldapConfig)) {
            env.put("java.naming.ldap.factory.socket", "org.keycloak.truststore.SSLSocketFactory");
        }

        String connectionPooling = ldapConfig.getConnectionPooling();
        if (connectionPooling != null) {
            env.put("com.sun.jndi.ldap.connect.pool", connectionPooling);
        }

        String connectionTimeout = ldapConfig.getConnectionTimeout();
        if (connectionTimeout != null && !connectionTimeout.isEmpty()) {
            env.put("com.sun.jndi.ldap.connect.timeout", connectionTimeout);
        }

        String readTimeout = ldapConfig.getReadTimeout();
        if (readTimeout != null && !readTimeout.isEmpty()) {
            env.put("com.sun.jndi.ldap.read.timeout", readTimeout);
        }

        // 合并额外连接属性
        Properties additionalProperties = ldapConfig.getAdditionalConnectionProperties();
        if (additionalProperties != null) {
            for (Object key : additionalProperties.keySet()) {
                env.put(key.toString(), additionalProperties.getProperty(key.toString()));
            }
        }

        StringBuilder binaryAttrsBuilder = new StringBuilder();
        if (ldapConfig.isObjectGUID()) {
            binaryAttrsBuilder.append(LDAPConstants.OBJECT_GUID).append(" ");
        }
        if (ldapConfig.isEdirectory()) {
            binaryAttrsBuilder.append(LDAPConstants.NOVELL_EDIRECTORY_GUID).append(" ");
        }
        for (String attrName : ldapConfig.getBinaryAttributeNames()) {
            binaryAttrsBuilder.append(attrName).append(" ");
        }

        String binaryAttrs = binaryAttrsBuilder.toString().trim();
        if (!binaryAttrs.isEmpty()) {
            env.put("java.naming.ldap.attributes.binary", binaryAttrs);
        }

        String referral = ldapConfig.getReferral();
        if (referral != null) {
            env.put(Context.REFERRAL, referral);
        }

        return new Hashtable<>(env);
    }

    /** {@inheritDoc} 关闭 StartTLS 响应与 LDAP 上下文。 */
    @Override
    public void close() {
        if (tlsResponse != null) {
            try {
                tlsResponse.close();
            } catch (IOException e) {
                logger.error("Could not close Ldap tlsResponse.", e);
            }
        }

        if (ldapContext != null) {
            try {
                ldapContext.close();
            } catch (NamingException e) {
                logger.error("Could not close Ldap context.", e);
            }
        }
    }
}
