package org.keycloak.representations.idm;

/**
 * 测试 LDAP 连接的 REST 请求表示，用于管理 API 验证 LDAP 用户联合组件的连接参数。
 */
public class TestLdapConnectionRepresentation {

    /** 测试动作类型（如 testConnection、testAuthentication）。 */
    private String action;
    /** LDAP 连接 URL。 */
    private String connectionUrl;
    /** 绑定 DN（服务账户标识）。 */
    private String bindDn;
    /** 绑定凭证（密码）。 */
    private String bindCredential;
    /** 是否使用 Truststore SPI（true/false 字符串）。 */
    private String useTruststoreSpi;
    /** 连接超时时间（毫秒，字符串形式）。 */
    private String connectionTimeout;
    /** 关联的组件 ID（可选，用于更新已有组件前测试）。 */
    private String componentId;
    /** 是否启用 StartTLS（true/false 字符串）。 */
    private String startTls;
    /** 认证类型（如 simple）。 */
    private String authType;

    /** 默认构造函数。 */
    public TestLdapConnectionRepresentation() {
    }

    /**
     * 构造不含 StartTLS、认证类型及组件 ID 的测试请求。
     *
     * @param action 测试动作
     * @param connectionUrl LDAP 连接 URL
     * @param bindDn 绑定 DN
     * @param bindCredential 绑定凭证
     * @param useTruststoreSpi 是否使用 Truststore SPI
     * @param connectionTimeout 连接超时
     */
    public TestLdapConnectionRepresentation(String action, String connectionUrl, String bindDn, String bindCredential, String useTruststoreSpi, String connectionTimeout) {
        this(action, connectionUrl, bindDn, bindCredential, useTruststoreSpi, connectionTimeout, null, null, null);
    }

    /**
     * 构造不含组件 ID 的测试请求。
     *
     * @param action 测试动作
     * @param connectionUrl LDAP 连接 URL
     * @param bindDn 绑定 DN
     * @param bindCredential 绑定凭证
     * @param useTruststoreSpi 是否使用 Truststore SPI
     * @param connectionTimeout 连接超时
     * @param startTls 是否启用 StartTLS
     * @param authType 认证类型
     */
    public TestLdapConnectionRepresentation(String action, String connectionUrl, String bindDn, String bindCredential, String useTruststoreSpi, String connectionTimeout, String startTls, String authType) {
        this(action, connectionUrl, bindDn, bindCredential, useTruststoreSpi, connectionTimeout, startTls, authType, null);
    }

    /**
     * 完整参数构造函数。
     *
     * @param action 测试动作
     * @param connectionUrl LDAP 连接 URL
     * @param bindDn 绑定 DN
     * @param bindCredential 绑定凭证
     * @param useTruststoreSpi 是否使用 Truststore SPI
     * @param connectionTimeout 连接超时
     * @param startTls 是否启用 StartTLS
     * @param authType 认证类型
     * @param componentId 组件 ID
     */
    public TestLdapConnectionRepresentation(String action, String connectionUrl, String bindDn, String bindCredential,
            String useTruststoreSpi, String connectionTimeout, String startTls, String authType, String componentId) {
        this.action = action;
        this.connectionUrl = connectionUrl;
        this.bindDn = bindDn;
        this.bindCredential = bindCredential;
        this.useTruststoreSpi = useTruststoreSpi;
        this.connectionTimeout = connectionTimeout;
        this.startTls = startTls;
        this.authType = authType;
        this.componentId = componentId;
    }

    /** @return 测试动作 */
    public String getAction() {
        return action;
    }

    /** @param action 测试动作 */
    public void setAction(String action) {
        this.action = action;
    }

    /** @return LDAP 连接 URL */
    public String getConnectionUrl() {
        return connectionUrl;
    }

    /** @param connectionUrl LDAP 连接 URL */
    public void setConnectionUrl(String connectionUrl) {
        this.connectionUrl = connectionUrl;
    }

    /** @return 认证类型 */
    public String getAuthType() {
        return authType;
    }

    /** @param authType 认证类型 */
    public void setAuthType(String authType) {
        this.authType = authType;
    }

    /** @return 绑定 DN */
    public String getBindDn() {
        return bindDn;
    }

    /** @param bindDn 绑定 DN */
    public void setBindDn(String bindDn) {
        this.bindDn = bindDn;
    }

    /** @return 绑定凭证 */
    public String getBindCredential() {
        return bindCredential;
    }

    /** @param bindCredential 绑定凭证 */
    public void setBindCredential(String bindCredential) {
        this.bindCredential = bindCredential;
    }

    /** @return 是否使用 Truststore SPI */
    public String getUseTruststoreSpi() {
        return useTruststoreSpi;
    }

    /** @param useTruststoreSpi 是否使用 Truststore SPI */
    public void setUseTruststoreSpi(String useTruststoreSpi) {
        this.useTruststoreSpi = useTruststoreSpi;
    }

    /** @return 连接超时 */
    public String getConnectionTimeout() {
        return connectionTimeout;
    }

    /** @param connectionTimeout 连接超时 */
    public void setConnectionTimeout(String connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    /** @return 组件 ID */
    public String getComponentId() {
        return componentId;
    }

    /** @param componentId 组件 ID */
    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    /** @return 是否启用 StartTLS */
    public String getStartTls() {
        return startTls;
    }

    /** @param startTls 是否启用 StartTLS */
    public void setStartTls(String startTls) {
        this.startTls = startTls;
    }

}
