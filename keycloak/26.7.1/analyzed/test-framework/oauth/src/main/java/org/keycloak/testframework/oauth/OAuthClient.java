package org.keycloak.testframework.oauth;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.client.registration.ClientRegistration;
import org.keycloak.protocol.oidc.utils.OIDCResponseMode;
import org.keycloak.testframework.ui.page.LoginPage;
import org.keycloak.testframework.ui.webdriver.ManagedWebDriver;
import org.keycloak.testsuite.util.oauth.AbstractOAuthClient;
import org.keycloak.testsuite.util.oauth.AuthorizationEndpointResponse;
import org.keycloak.testsuite.util.oauth.OAuthClientConfig;

import org.apache.http.impl.client.CloseableHttpClient;
import org.openqa.selenium.By;
import org.openqa.selenium.support.PageFactory;

/**
 * 测试框架 OAuth 客户端，用于向 Keycloak 发送 OAuth/OIDC 请求并处理回调。
 * <p>
 * 扩展 {@link AbstractOAuthClient}，集成 {@link ManagedWebDriver} 登录表单填充与授权响应解析。
 */
public class OAuthClient extends AbstractOAuthClient<OAuthClient> {

    private final ManagedWebDriver managedWebDriver;
    private final ClientResource clientResource;

    /**
     * 创建完整配置的 OAuth 测试客户端。
     *
     * @param baseUrl Keycloak 基础 URL
     * @param httpClient HTTP 客户端
     * @param managedWebDriver 托管 WebDriver，用于浏览器交互
     * @param clientResource 已创建客户端的管理资源，关闭时可删除客户端
     */
    public OAuthClient(String baseUrl, CloseableHttpClient httpClient, ManagedWebDriver managedWebDriver, ClientResource clientResource) {
        super(baseUrl, httpClient, managedWebDriver.driver());
        this.managedWebDriver = managedWebDriver;
        this.clientResource = clientResource;

        config = new OAuthClientConfig()
                .responseType(OAuth2Constants.CODE);
    }

    /**
     * 创建不绑定 {@link ClientResource} 的 OAuth 测试客户端。
     *
     * @param baseUrl Keycloak 基础 URL
     * @param httpClient HTTP 客户端
     * @param managedWebDriver 托管 WebDriver
     */
    public OAuthClient(String baseUrl, CloseableHttpClient httpClient, ManagedWebDriver managedWebDriver) {
        this(baseUrl, httpClient, managedWebDriver, null);
    }

    /** {@inheritDoc} 通过 {@link LoginPage} 填写并提交登录表单。 */
    @Override
    public void fillLoginForm(String username, String password) {
        LoginPage loginPage = new LoginPage(managedWebDriver);
        PageFactory.initElements(driver, loginPage);
        loginPage.fillLogin(username, password);
        loginPage.submit();
    }

    /** {@inheritDoc} 按 response_mode 等待 WebDriver 回调元素后解析授权响应。 */
    @Override
    public AuthorizationEndpointResponse parseLoginResponse() {
        if (config.getResponseMode() != null && config.getResponseMode().equals(OIDCResponseMode.FORM_POST.value())) {
            managedWebDriver.waiting().waitForOAuthCallback(webdriver1 -> webdriver1.findElement(By.id(OAuth2Constants.CODE)).isDisplayed() || webdriver1.findElement(By.id(OAuth2Constants.ERROR)).isDisplayed());
        } else if (config.getResponseMode() != null && config.getResponseMode().equals(OIDCResponseMode.FORM_POST_JWT.value())) {
            managedWebDriver.waiting().waitForOAuthCallback(webdriver1 -> webdriver1.findElement(By.id(OAuth2Constants.RESPONSE)).isDisplayed());
        } else {
            managedWebDriver.waiting().waitForOAuthCallback();
        }
        return super.parseLoginResponse();
    }

    /** @return 指向当前 realm 的 {@link ClientRegistration} 构建器 */
    public ClientRegistration clientRegistration() {
        return ClientRegistration.create().httpClient(httpClient().get()).url(baseUrl, config.getRealm()).build();
    }

    /** @return 关联的 Admin REST 客户端资源，可能为 {@code null} */
    public ClientResource clientResource() {
        return clientResource;
    }

    /** 若已绑定 {@link ClientResource}，则通过 Admin API 删除测试客户端。 */
    public void close() {
        if (clientResource != null) {
            clientResource.remove();
        }
    }

}
