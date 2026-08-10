package org.keycloak.testframework.oauth;

import java.util.List;

import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.testframework.injection.DependenciesBuilder;
import org.keycloak.testframework.injection.Dependency;
import org.keycloak.testframework.injection.InstanceContext;
import org.keycloak.testframework.injection.RequestedInstance;
import org.keycloak.testframework.injection.Supplier;
import org.keycloak.testframework.injection.SupplierHelpers;
import org.keycloak.testframework.oauth.annotations.InjectOAuthClient;
import org.keycloak.testframework.realm.ClientBuilder;
import org.keycloak.testframework.realm.ClientConfig;
import org.keycloak.testframework.realm.ManagedRealm;
import org.keycloak.testframework.server.KeycloakUrls;
import org.keycloak.testframework.ui.webdriver.ManagedWebDriver;
import org.keycloak.testframework.util.ApiUtil;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;

/**
 * 为 {@link InjectOAuthClient} 注解提供 {@link OAuthClient} 实例的供应器。
 * <p>
 * 在指定 realm 中创建测试客户端，关联 WebDriver、HTTP 客户端与 {@link TestApp} 重定向 URI。
 */
public class OAuthClientSupplier implements Supplier<OAuthClient, InjectOAuthClient> {

    /** {@inheritDoc} 声明对 URL、HTTP 客户端、WebDriver、测试应用与 realm 的依赖。 */
    @Override
    public List<Dependency> getDependencies(RequestedInstance<OAuthClient, InjectOAuthClient> instanceContext) {
        return DependenciesBuilder.create(KeycloakUrls.class)
                .add(HttpClient.class)
                .add(ManagedWebDriver.class, instanceContext.getAnnotation().webDriverRef())
                .add(TestApp.class)
                .add(ManagedRealm.class, instanceContext.getAnnotation().realmRef()).build();
    }

    /** {@inheritDoc} 创建 realm 客户端并初始化 {@link OAuthClient} 配置。 */
    @Override
    public OAuthClient getValue(InstanceContext<OAuthClient, InjectOAuthClient> instanceContext) {
        InjectOAuthClient annotation = instanceContext.getAnnotation();

        KeycloakUrls keycloakUrls = instanceContext.getDependency(KeycloakUrls.class);
        CloseableHttpClient httpClient = (CloseableHttpClient) instanceContext.getDependency(HttpClient.class);
        ManagedWebDriver webDriver = instanceContext.getDependency(ManagedWebDriver.class, annotation.webDriverRef());
        TestApp testApp = instanceContext.getDependency(TestApp.class);

        ManagedRealm realm = instanceContext.getDependency(ManagedRealm.class, annotation.realmRef());

        String redirectUri = testApp.getRedirectionUri();

        ClientConfig clientConfig = SupplierHelpers.getInstance(annotation.config());
        ClientRepresentation testAppClient = clientConfig.configure(ClientBuilder.create())
                .redirectUris(redirectUri)
                .build();

        if (annotation.kcAdmin()) {
            testAppClient.setAdminUrl(testApp.getAdminUri());
        }

        String clientId = testAppClient.getClientId();;
        if (!annotation.ref().isEmpty()) {
            clientId = clientId + "-" + annotation.ref();
            testAppClient.setClientId(clientId);
        }
        String clientSecret = testAppClient.getSecret();

        String id = ApiUtil.getCreatedId(realm.admin().clients().create(testAppClient));
        ClientResource clientResource = realm.admin().clients().get(id);
        OAuthClient oAuthClient = new OAuthClient(keycloakUrls.getBase(), httpClient, webDriver, clientResource);
        oAuthClient.config().realm(realm.getName()).client(clientId, clientSecret).redirectUri(redirectUri);
        return oAuthClient;
    }

    /** {@inheritDoc} 仅当 {@link InjectOAuthClient#ref()} 相同时视为兼容。 */
    @Override
    public boolean compatible(InstanceContext<OAuthClient, InjectOAuthClient> a, RequestedInstance<OAuthClient, InjectOAuthClient> b) {
        return a.getAnnotation().ref().equals(b.getAnnotation().ref());
    }

    /** {@inheritDoc} 关闭 OAuth 客户端并清理 realm 中的测试客户端。 */
    @Override
    public void close(InstanceContext<OAuthClient, InjectOAuthClient> instanceContext) {
        instanceContext.getValue().close();
    }
}
