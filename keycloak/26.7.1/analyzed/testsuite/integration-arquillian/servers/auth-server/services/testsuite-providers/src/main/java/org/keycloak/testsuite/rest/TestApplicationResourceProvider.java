/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.testsuite.rest;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import org.keycloak.OAuth2Constants;
import org.keycloak.common.util.HtmlUtils;
import org.keycloak.http.HttpRequest;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.jose.jws.JWSInputException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oidc.grants.ciba.endpoints.ClientNotificationEndpointRequest;
import org.keycloak.representations.LogoutToken;
import org.keycloak.representations.adapters.action.LogoutAction;
import org.keycloak.representations.adapters.action.PushNotBeforeAction;
import org.keycloak.representations.adapters.action.TestAvailabilityAction;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resources.RealmsResource;
import org.keycloak.testsuite.rest.representation.TestAuthenticationChannelRequest;
import org.keycloak.testsuite.rest.resource.TestingOIDCEndpointsApplicationResource;
import org.keycloak.utils.MediaType;

import org.jboss.resteasy.reactive.NoCache;

/**
 * 测试应用 REST 资源提供者：接收适配器推送、登出通知及 OIDC 测试端点。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 * @author Stan Silvert ssilvert@redhat.com (C) 2016 Red Hat Inc.
 */
public class TestApplicationResourceProvider implements RealmResourceProvider {

    /** 当前 Keycloak 会话。 */
    private KeycloakSession session;

    /** 管理端登出动作队列。 */
    private final BlockingQueue<LogoutAction> adminLogoutActions;
    /** 前端通道登出令牌队列。 */
    private final BlockingQueue<LogoutToken> frontChannelLogoutTokens;
    /** 后端通道登出令牌（原始字符串）队列。 */
    private final BlockingQueue<String> backChannelLogoutTokens;
    /** Push-not-before 动作队列。 */
    private final BlockingQueue<PushNotBeforeAction> adminPushNotBeforeActions;
    /** 可用性测试动作队列。 */
    private final BlockingQueue<TestAvailabilityAction> adminTestAvailabilityAction;
    /** OIDC 客户端测试数据。 */
    private final TestApplicationResourceProviderFactory.OIDCClientData oidcClientData;

    /** 认证通道请求映射（按 ID）。 */
    private final ConcurrentMap<String, TestAuthenticationChannelRequest> authenticationChannelRequests;
    /** CIBA 客户端通知映射。 */
    private final ConcurrentMap<String, ClientNotificationEndpointRequest> cibaClientNotifications;
    /** Intent 客户端绑定映射。 */
    private final ConcurrentMap<String, String> intentClientBindings;

    /** 当前 HTTP 请求。 */
    private final HttpRequest request;

    /**
     * @param session Keycloak 会话
     * @param adminLogoutActions 管理登出动作队列
     * @param backChannelLogoutTokens 后端通道登出令牌队列
     * @param frontChannelLogoutTokens 前端通道登出令牌队列
     * @param adminPushNotBeforeActions Push-not-before 队列
     * @param adminTestAvailabilityAction 可用性测试队列
     * @param oidcClientData OIDC 客户端测试数据
     * @param authenticationChannelRequests 认证通道请求映射
     * @param cibaClientNotifications CIBA 通知映射
     * @param intentClientBindings Intent 绑定映射
     */
    public TestApplicationResourceProvider(KeycloakSession session, BlockingQueue<LogoutAction> adminLogoutActions,
            BlockingQueue<String> backChannelLogoutTokens,
            BlockingQueue<LogoutToken> frontChannelLogoutTokens,
            BlockingQueue<PushNotBeforeAction> adminPushNotBeforeActions,
            BlockingQueue<TestAvailabilityAction> adminTestAvailabilityAction,
            TestApplicationResourceProviderFactory.OIDCClientData oidcClientData,
            ConcurrentMap<String, TestAuthenticationChannelRequest> authenticationChannelRequests,
            ConcurrentMap<String, ClientNotificationEndpointRequest> cibaClientNotifications,
            ConcurrentMap<String, String> intentClientBindings) {
        this.session = session;
        this.adminLogoutActions = adminLogoutActions;
        this.backChannelLogoutTokens = backChannelLogoutTokens;
        this.frontChannelLogoutTokens = frontChannelLogoutTokens;
        this.adminPushNotBeforeActions = adminPushNotBeforeActions;
        this.adminTestAvailabilityAction = adminTestAvailabilityAction;
        this.oidcClientData = oidcClientData;
        this.authenticationChannelRequests = authenticationChannelRequests;
        this.cibaClientNotifications = cibaClientNotifications;
        this.intentClientBindings = intentClientBindings;
        this.request = session.getContext().getHttpRequest();
    }

    /** 接收 JWS 编码的管理端登出动作并入队。 */
    @POST
    @Consumes(MediaType.TEXT_PLAIN_UTF_8)
    @Path("/admin/k_logout")
    public void adminLogout(String data) throws JWSInputException {
        adminLogoutActions.add(new JWSInput(data).readJsonContent(LogoutAction.class));
    }

    /** 接收后端通道登出表单并保存 logout_token。 */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("/admin/backchannelLogout")
    public void backchannelLogout() {
        backChannelLogoutTokens.add(request.getDecodedFormParameters().getFirst(OAuth2Constants.LOGOUT_TOKEN));
    }

    /** 接收 JWS 编码的 push-not-before 动作并入队。 */
    @POST
    @Consumes(MediaType.TEXT_PLAIN_UTF_8)
    @Path("/admin/k_push_not_before")
    public void adminPushNotBefore(String data) throws JWSInputException {
        adminPushNotBeforeActions.add(new JWSInput(data).readJsonContent(PushNotBeforeAction.class));
    }

    /** 接收 JWS 编码的可用性测试动作并入队。 */
    @POST
    @Consumes(MediaType.TEXT_PLAIN_UTF_8)
    @Path("/admin/k_test_available")
    public void testAvailable(String data) throws JWSInputException {
        adminTestAvailabilityAction.add(new JWSInput(data).readJsonContent(TestAvailabilityAction.class));
    }

    /** 轮询管理端登出动作（最多等待 10 秒）。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/poll-admin-logout")
    public LogoutAction getAdminLogoutAction() throws InterruptedException {
        return adminLogoutActions.poll(10, TimeUnit.SECONDS);
    }

    /** 轮询后端通道登出令牌并解析为 {@link LogoutToken}。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/poll-backchannel-logout")
    public LogoutToken getBackChannelLogoutAction() throws InterruptedException, JWSInputException {
        return new JWSInput(backChannelLogoutTokens.poll(20, TimeUnit.SECONDS)).readJsonContent(LogoutToken.class);
    }

    /** 轮询原始后端通道登出令牌字符串。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/poll-backchannel-raw-logout")
    public String getBackChanneRawlLogoutAction() throws InterruptedException {
        return backChannelLogoutTokens.poll(20, TimeUnit.SECONDS);
    }

    /** 轮询 push-not-before 动作。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/poll-admin-not-before")
    public PushNotBeforeAction getAdminPushNotBefore() throws InterruptedException {
        return adminPushNotBeforeActions.poll(10, TimeUnit.SECONDS);
    }

    /** 轮询可用性测试动作。 */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/poll-test-available")
    public TestAvailabilityAction getTestAvailable() throws InterruptedException {
        return adminTestAvailabilityAction.poll(10, TimeUnit.SECONDS);
    }

    /** 清空管理端动作队列。 */
    @POST
    @Path("/clear-admin-actions")
    public Response clearAdminActions() {
        adminLogoutActions.clear();
        adminPushNotBeforeActions.clear();
        return Response.noContent().build();
    }

    /** 处理 POST 表单请求，渲染参数 HTML 或执行 clear-admin-actions。 */
    @POST
    @Consumes(jakarta.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML_UTF_8)
    @Path("/{action}")
    public Response post(@PathParam("action") String action) {
        if ("clear-admin-actions".equals(action)) {
            return clearAdminActions();
        }
        MultivaluedMap<String, String> formParams = request.getDecodedFormParameters();
        String title = "APP_REQUEST";
        if (action.equals("auth")) {
            title = "AUTH_RESPONSE";
        } else if (action.equals("logout")) {
            title = "LOGOUT_REQUEST";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><title>" + title + "</title></head><body>");

        sb.append("<b>Form parameters: </b><br>");
        for (String paramName : formParams.keySet()) {
            sb.append(paramName).append(": ").append("<span id=\"")
                    .append(paramName).append("\">")
                    .append(HtmlUtils.escapeAttribute(formParams.getFirst(paramName)))
                    .append("</span><br>");
        }
        sb.append("<br>");

        UriBuilder base = UriBuilder.fromUri("/auth");
        sb.append("<a href=\"" + RealmsResource.accountUrl(base).build("test").toString() + "\" id=\"account\">account</a>");

        sb.append("</body></html>");
        return Response.ok(sb.toString()).build();
    }

    /** 处理 GET 请求，返回带账户链接的简单 HTML 页。 */
    @GET
    @Produces(MediaType.TEXT_HTML_UTF_8)
    @Path("/{action}")
    public String get(@PathParam("action") String action) {
        //String requestUri = session.getContext().getUri().getRequestUri().toString();

        String title = "APP_REQUEST";
         if (action.equals("auth")) {
            title = "AUTH_RESPONSE";
        } else if (action.equals("logout")) {
            title = "LOGOUT_REQUEST";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><title>" + title + "</title></head><body>");
        UriBuilder base = UriBuilder.fromUri("/auth");
        sb.append("<a href=\"" + RealmsResource.accountUrl(base).build("test").toString() + "\" id=\"account\">account</a>");

        sb.append("</body></html>");
        return sb.toString();
    }

    /** 返回用于 AJAX 拉取账户资料的 HTML/JS 测试页。 */
    @GET
    @NoCache
    @Produces(MediaType.TEXT_HTML_UTF_8)
    @Path("/get-account-profile")
    public String getAccountProfile(@QueryParam("token") String token, @QueryParam("account-uri") String accountUri) {
        StringBuilder sb = new StringBuilder();
        sb.append("function getProfile() {\n");
        sb.append(" var req = new XMLHttpRequest();\n");
        sb.append(" req.open('GET', '" + accountUri + "', false);\n");
        if (token != null) {
            sb.append(" req.setRequestHeader('Authorization', 'Bearer " + token + "');\n");
        }
        sb.append(" req.setRequestHeader('Accept', 'application/json');\n");
        sb.append(" req.send(null);\n");
        sb.append(" document.getElementById('profileOutput').innerHTML=\"<span id='innerOutput'>\" + req.status + '///' + req.responseText; + \"</span>\"\n");
        sb.append("}");
        String jsScript = sb.toString();

        sb = new StringBuilder();
        sb.append("<html><head><title>Account Profile JS Test</title><script>\n")
                .append(jsScript)
                .append( "</script></head>\n")
                .append("<body onload='getProfile()'><div id='profileOutput'></div></body>")
                .append("</html>");
        return sb.toString();
    }

    /** 暴露 OIDC 客户端测试子资源。 */
    @Path("/oidc-client-endpoints")
    public TestingOIDCEndpointsApplicationResource getTestingOIDCClientEndpoints() {
        return new TestingOIDCEndpointsApplicationResource(oidcClientData, authenticationChannelRequests, cibaClientNotifications, intentClientBindings);
    }

    @Override
    public Object getResource() {
        return this;
    }

    @Override
    public void close() {

    }
}
