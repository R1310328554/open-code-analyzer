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

package org.keycloak.testsuite.adapter.servlet;


import java.io.IOException;
import java.io.StringWriter;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.adapters.saml.SamlAuthenticationError;
import org.keycloak.adapters.saml.SamlPrincipal;
import org.keycloak.adapters.saml.SamlSession;
import org.keycloak.adapters.spi.AuthenticationError;

import org.jboss.resteasy.reactive.NoCache;
import org.w3c.dom.Document;

/**
 * 返回当前认证主体信息的 REST 端点，用于 SAML/OIDC 适配器集成测试。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @author mhajas
 * @version $Revision: 1 $
 */
@Path("/")
public class SendUsernameServlet {

    /** 是否在校验用户角色。 */
    private static boolean checkRoles = false;
    /** 最近一次捕获的 SAML 认证错误。 */
    private static SamlAuthenticationError authError;
    /** 最近一次发送的主体对象。 */
    private static Principal sentPrincipal;
    /** 需要校验的角色列表，默认为 manager。 */
    private static List<String> checkRolesList = Collections.singletonList("manager");

    @Context
    private HttpServletRequest httpServletRequest;

    /** 返回请求路径、主体名、会话与角色信息。 */
    @GET
    @NoCache
    public Response doGet(@QueryParam("checkRoles") boolean checkRolesFlag) throws IOException {
        System.out.println("In SendUsername Servlet doGet() check roles is " + (checkRolesFlag || checkRoles));
        if (httpServletRequest.getUserPrincipal() != null && (checkRolesFlag || checkRoles) && !checkRoles()) {
            return Response.status(Response.Status.FORBIDDEN).entity("Forbidden").build();
        }

        return Response.ok(getOutput()).header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_TYPE + ";charset=UTF-8").build();
    }

    /** POST 版本的主体信息端点，含角色校验。 */
    @POST
    @NoCache
    public Response doPost(@QueryParam("checkRoles") boolean checkRolesFlag) {
        System.out.println("In SendUsername Servlet doPost() check roles is " + (checkRolesFlag || checkRoles));

        if (httpServletRequest.getUserPrincipal() != null && (checkRolesFlag || checkRoles) && !checkRoles()) {
            throw new RuntimeException("User: " + httpServletRequest.getUserPrincipal() + " do not have required role");
        }

        return Response.ok(getOutput()).header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_TYPE + ";charset=UTF-8").build();

    }

    /** 保存并返回当前请求主体的 SAML 属性。 */
    @GET
    @Path("getAttributes")
    public Response getSentPrincipal() throws IOException {
        System.out.println("In SendUsername Servlet getSentPrincipal()");
        sentPrincipal = httpServletRequest.getUserPrincipal();

        return Response.ok(getAttributes()).header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_TYPE + ";charset=UTF-8").build();

    }

    /** 调用 {@link HttpServletRequest#changeSessionId()} 并返回新会话 ID。 */
    @GET
    @Path("change-session-id")
    public Response changeSessionId() throws IOException {
        System.out.println("In SendUsername Servlet changeSessionId()");
        final String sessionId = httpServletRequest.changeSessionId();

        return Response.ok(sessionId).header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_TYPE + ";charset=UTF-8").build();
    }

    /** 将 SAML 断言文档序列化为 XML 字符串返回。 */
    @GET
    @Path("getAssertionFromDocument")
    public Response getAssertionFromDocument() throws IOException, TransformerException {
        sentPrincipal = httpServletRequest.getUserPrincipal();
        Document doc = ((SamlPrincipal) sentPrincipal).getAssertionDocument();
        String xml = "";
        if (doc != null) {
            DOMSource domSource = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.transform(domSource, result);
            xml = writer.toString();
        }
        return Response.ok(xml).header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_TYPE + ";charset=UTF-8").build();
    }

    /** 子路径 GET 请求转发至根路径处理逻辑。 */
    @GET
    @Path("{path}")
    public Response doGetElseWhere(@PathParam("path") String path, @QueryParam("checkRoles") boolean checkRolesFlag) throws IOException {
        System.out.println("In SendUsername Servlet doGetElseWhere() - path: " + path);
        return doGet(checkRolesFlag);
    }

    /** 子路径 POST 请求转发至根路径处理逻辑。 */
    @POST
    @Path("{path}")
    public Response doPostElseWhere(@PathParam("path") String path, @QueryParam("checkRoles") boolean checkRolesFlag) throws IOException {
        System.out.println("In SendUsername Servlet doPostElseWhere() - path: " + path);
        return doPost(checkRolesFlag);
    }

    /** 渲染 SAML 认证错误页面（POST）。 */
    @POST
    @Path("error.html")
    public Response errorPagePost() {
        authError = (SamlAuthenticationError) httpServletRequest.getAttribute(AuthenticationError.class.getName());
        Integer statusCode = (Integer) httpServletRequest.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        System.out.println("In SendUsername Servlet errorPage() status code: " + statusCode);

        return Response.ok(getErrorOutput(statusCode)).header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_TYPE + ";charset=UTF-8").build();

    }

    /** 渲染 SAML 认证错误页面（GET）。 */
    @GET
    @Path("error.html")
    public Response errorPageGet() {
        return errorPagePost();
    }


    /** 启用后续请求的角色校验。 */
    @GET
    @Path("checkRoles")
    public String checkRolesEndPoint() {
        checkRoles = true;
        System.out.println("Setting checkRoles to true");
        return "Roles will be checked";
    }

    /** 禁用角色校验并重置默认角色列表。 */
    @GET
    @Path("uncheckRoles")
    public String uncheckRolesEndPoint() {
        checkRoles = false;
        System.out.println("Setting checkRoles to false");
        checkRolesList = Collections.singletonList("manager");
        return "Roles will not be checked";
    }

    /** 设置需要校验的角色列表（逗号分隔）。 */
    @GET
    @Path("setCheckRoles")
    public String setCheckRoles(@QueryParam("roles") String roles) {
        checkRolesList = Arrays.asList(roles.split(","));
        checkRoles = true;
        System.out.println("Setting checkRolesList to " + checkRolesList.toString());
        return "These roles will be checked: " + checkRolesList.toString();
    }

    /** 校验当前用户是否拥有 {@link #checkRolesList} 中的全部角色。 */
    private boolean checkRoles() {
        for (String role : checkRolesList) {
            System.out.println("In checkRoles() checking role " + role + " for user " + httpServletRequest.getUserPrincipal().getName());
            if (!httpServletRequest.isUserInRole(role)) {
                System.out.println("User is not in role " + role);
                return false;
            }
        }

        return true;
    }

    /** 组装主体、会话与角色信息的文本输出。 */
    private String getOutput() {
        String output = "request-path: ";
        output += httpServletRequest.getServletPath();
        output += "\n";
        output += "principal=";
        Principal principal = httpServletRequest.getUserPrincipal();

        if (principal == null) {
            return output + "null";
        }

        sentPrincipal = principal;

        output += principal.getName() + "\n";
        output += getSessionInfo() + "\n";
        output += getRoles() + "\n";

        return output;
    }

    /** 提取 SAML 会话索引与 NotOnOrAfter 时间。 */
    private String getSessionInfo() {
        HttpSession session = httpServletRequest.getSession(false);

        if (session != null) {
            final SamlSession samlSession = (SamlSession) httpServletRequest.getSession(false).getAttribute(SamlSession.class.getName());

            if (samlSession != null) {
                String output = "Session ID: " + samlSession.getSessionIndex() + "\n";
                XMLGregorianCalendar sessionNotOnOrAfter = samlSession.getSessionNotOnOrAfter();
                output += "SessionNotOnOrAfter: " + (sessionNotOnOrAfter == null ? "null" : sessionNotOnOrAfter.toString());
                return output;
            }

            return "SamlSession doesn't exist";
        }

        return "Session doesn't exist";
    }

    /** 列出 SAML 主体 Roles 属性中的角色名。 */
    private String getRoles() {
        StringBuilder output = new StringBuilder("Roles: ");
        for (String role : ((SamlPrincipal) httpServletRequest.getUserPrincipal()).getAttributes("Roles")) {
            output.append(role).append(",");
        }

        return output.toString();
    }

    /** 生成包含 HTTP 状态码与认证错误的 HTML 片段。 */
    private String getErrorOutput(Integer statusCode) {
        String output = "<html><head><title>Error Page</title></head><body><h1>There was an error</h1>";
        if (statusCode != null)
            output += "<br/>HTTP status code: " + statusCode;
        if (authError != null)
            output += "<br/>Error info: " + authError.toString();
        return output + "</body></html>";
    }

    /** 使用指定分隔符连接字符串列表。 */
    private static String joinList(String delimeter, List<String> list) {
        if (list == null || list.size() <= 0) return "";

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < list.size(); i++) {

            sb.append(list.get(i));

            // 非最后一项时追加分隔符
            if (i != list.size() - 1) {
                sb.append(delimeter);
            }

        }

        return sb.toString();
    }

    /** 格式化 SAML 主体属性及友好名称属性为 HTML。 */
    private String getAttributes() {
        SamlPrincipal principal = (SamlPrincipal) sentPrincipal;

        StringBuilder b = new StringBuilder();
        for (Entry<String, List<String>> e : principal.getAttributes().entrySet()) {
            b.append(e.getKey()).append(": ").append(joinList(",", e.getValue())).append("<br />");
        }

        for (String friendlyAttributeName : principal.getFriendlyNames()) {
            b.append("friendly ")
                    .append(friendlyAttributeName)
                    .append(": ")
                    .append(joinList(",", principal.getFriendlyAttributes(friendlyAttributeName)))
                    .append("<br />");
        }

        return b.toString();
    }

    /** 返回当前 SAML 断言 Issuer 值。 */
    @GET
    @Path("getAssertionIssuer")
    public Response getAssertionIssuer() throws IOException {
        sentPrincipal = httpServletRequest.getUserPrincipal();
        SamlPrincipal principal = (SamlPrincipal) sentPrincipal;
        return Response.ok(principal.getAssertion().getIssuer().getValue())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_TYPE + ";charset=UTF-8").build();
    }

}
