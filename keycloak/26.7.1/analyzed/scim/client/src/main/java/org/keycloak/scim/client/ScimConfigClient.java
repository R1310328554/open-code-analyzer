package org.keycloak.scim.client;


import org.keycloak.scim.resource.config.ServiceProviderConfig;

/**
 * SCIM ServiceProviderConfig 资源客户端，用于读取服务端能力配置。
 */
public class ScimConfigClient {

    /** 底层 SCIM 客户端。 */
    private final ScimClient client;

    public ScimConfigClient(ScimClient client) {
        this.client = client;
    }

    /** 获取 ServiceProviderConfig 资源。 */
    public ServiceProviderConfig get() {
        return client.execute(client.doGet(ServiceProviderConfig.class), ServiceProviderConfig.class);
    }
}
