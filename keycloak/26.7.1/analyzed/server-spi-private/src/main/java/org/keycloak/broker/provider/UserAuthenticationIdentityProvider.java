package org.keycloak.broker.provider;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.keycloak.events.EventBuilder;
import org.keycloak.models.FederatedIdentityModel;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.sessions.AuthenticationSessionModel;

/**
 * 用户认证型身份提供方：发起浏览器/OAuth 登录、处理回调并完成联邦用户生命周期。
 */
public interface UserAuthenticationIdentityProvider<C extends IdentityProviderModel> extends IdentityProvider<C> {

    /** 会话备注键：标记外部身份提供方上下文。 */
    String EXTERNAL_IDENTITY_PROVIDER = "EXTERNAL_IDENTITY_PROVIDER";
    /** 会话备注键：存储联邦访问令牌。 */
    String FEDERATED_ACCESS_TOKEN = "FEDERATED_ACCESS_TOKEN";

    /** IdP 回调端点与 Keycloak 认证流程之间的桥接回调。 */
    interface AuthenticationCallback {

        /**
         * 解码并校验认证会话，确保未过期。
     *
     * Common method to return current authenticationSession and verify if it is not expired
         *
         * @param encodedCode
         * @return see description
         */
        AuthenticationSessionModel getAndVerifyAuthenticationSession(String encodedCode);

        /**
         * 远程 IdP 认证成功后由提供者调用，完成本地登录响应。
     *
     * This method should be called by provider after the JAXRS callback endpoint has finished authentication
         * with the remote IDP. There is an assumption that authenticationSession is set in the context when this method is called
         *
         * @param context
         * @return see description
         */
        Response authenticated(BrokeredIdentityContext context);

        /**
         * 用户在 IdP 侧取消认证（如拒绝同意页）时调用。
     *
     * Called when user cancelled authentication on the IDP side - for example user didn't approve consent page on the IDP side.
         * Assumption is that authenticationSession is set in the {@link org.keycloak.models.KeycloakContext} when this method is called
         *
         * @param idpConfig identity provider config
         * @return see description
         */
        Response cancelled(IdentityProviderModel idpConfig);

        /**
         * 指示应使用指定 IdP 重试登录。
     *
     * Indicates that login with the particular IDP should be retried
         *
         * @param identityProvider provider to retry login
         * @param authSession authentication session
         * @return see description
         */
        Response retryLogin(UserAuthenticationIdentityProvider<?> identityProvider, AuthenticationSessionModel authSession);

        /**
         * IdP 侧发生错误时调用。
     *
     * Called when error happened on the IDP side.
         * Assumption is that authenticationSession is set in the {@link org.keycloak.models.KeycloakContext} when this method is called
         *
         * @return see description
         */
        Response error(IdentityProviderModel idpConfig, String message);
    }

    /** FirstBrokerLogin 前预处理联邦身份上下文。 */
    void preprocessFederatedIdentity(KeycloakSession session, RealmModel realm, BrokeredIdentityContext context);
    /** IdP 认证完成后的收尾处理。 */
    void authenticationFinished(AuthenticationSessionModel authSession, BrokeredIdentityContext context);
    /** 首次导入联邦用户时同步属性。 */
    void importNewUser(KeycloakSession session, RealmModel realm, UserModel user, BrokeredIdentityContext context);
    /** 已存在用户再次登录时更新联邦属性。 */
    void updateBrokeredUser(KeycloakSession session, RealmModel realm, UserModel user, BrokeredIdentityContext context);

    /**
     * 远程 IdP 回调 Keycloak 的 JAX-RS 端点实现。
     *
     * JAXRS callback endpoint for when the remote IDP wants to callback to keycloak.
     *
     * @return
     */
    Object callback(RealmModel realm, AuthenticationCallback callback, EventBuilder event);

    /**
     * <p>向身份提供方发起认证请求，整个认证流程仅调用一次。</p>
     *
     * <p>Initiates the authentication process by sending an authentication request to an identity provider. This method is called
     * only once during the authentication.</p>
     *
     * @param request The initial authentication request. Contains all the contextual information in order to build an authentication request to the
     *                    identity provider.
     * @return
     */
    Response performLogin(AuthenticationRequest request);

    /**
     * <p>返回认证过程中存储的联邦令牌（V1 API，仅从数据库读取，已弃用）。</p>
     *
     * <p>Returns a {@link jakarta.ws.rs.core.Response} containing the token previously stored during the authentication process for a
     * specific user. Deprecated method used for Identity Brokering API V1 that only uses the database.</p>
     *
     * @param session
     * @param identity
     * @return
     */
    @Deprecated
    Response retrieveToken(KeycloakSession session, FederatedIdentityModel identity);

    /**
     * <p>返回认证过程中存储的联邦令牌，优先查用户会话，否则回退数据库。</p>
     *
     * <p>Returns a {@link jakarta.ws.rs.core.Response} containing the token previously stored during the authentication process for a
     * specific user. This method looks in the user session and in the database if not present.</p>
     *
     * @param session
     * @param identity
     * @param userSession
     * @param user
     * @return
     */
    Response retrieveToken(KeycloakSession session, FederatedIdentityModel identity, UserSessionModel userSession, UserModel user);

    /** 执行 IdP 侧 backchannel 登出。 */
    void backchannelLogout(KeycloakSession session, UserSessionModel userSession, UriInfo uriInfo, RealmModel realm);

    /**
     * Keycloak 通过浏览器发起登出时调用，应向 IdP 发起联合登出；不支持则返回 null。
     *
     * Called when a Keycloak application initiates a logout through the browser.  This is expected to do a logout
     * with the IDP
     *
     * @param userSession
     * @param uriInfo
     * @param realm
     * @return null if this is not supported by this provider
     */
    Response keycloakInitiatedBrowserLogout(KeycloakSession session, UserSessionModel userSession, UriInfo uriInfo, RealmModel realm);

    /**
     * 序列化/反序列化附加数据的marshaller，供客户端会话持久化。
     *
     * Implementation of marshaller to serialize/deserialize attached data to Strings, which can be saved in clientSession
     * @return
     */
    IdentityProviderDataMarshaller getMarshaller();

    /**
     * 是否支持较长的 state/RelayState 参数以携带更多上下文。
     *
     * @return true if identity provider supports long value of "state" parameter (or "RelayState" parameter), which can hold relatively big amount of context data
     */
    default boolean supportsLongStateParameter() {
        return true;
    }
}
