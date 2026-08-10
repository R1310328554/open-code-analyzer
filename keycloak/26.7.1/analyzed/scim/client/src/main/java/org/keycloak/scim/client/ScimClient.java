package org.keycloak.scim.client;


import java.io.IOException;

import jakarta.ws.rs.core.Response.Status;

import org.keycloak.http.simple.SimpleHttp;
import org.keycloak.http.simple.SimpleHttpRequest;
import org.keycloak.http.simple.SimpleHttpResponse;
import org.keycloak.scim.client.authorization.AuthorizationMethod;
import org.keycloak.scim.protocol.response.ErrorResponse;
import org.keycloak.scim.resource.ResourceTypeRepresentation;
import org.keycloak.scim.resource.config.ServiceProviderConfig;

import org.apache.http.HttpHeaders;
import org.apache.http.client.HttpClient;

/**
 * <p>与 SCIM 2.0 兼容服务器交互的客户端，支持用户、组等资源的 CRUD 及服务端配置查询。</p>
 *
 * <p>本客户端供 Keycloak 内部测试与集成使用，非独立 SCIM 库；基于 {@link SimpleHttp} 实现。</p>
 *
 * <p>创建实例需 {@link HttpClient}，运行时可通过 {@link org.keycloak.connections.httpclient.HttpClientProvider} 获取。</p>
 *
 * <p>用法示例：
 * <pre>
 * try (ScimClient scimClient = ScimClient.create(httpClient)
 *         .withBaseUrl("https://scim.example.com")
 *         .withAuthorization(new ScimClient.Builder.OAuth2Bearer("https://auth.examplecom/realms/master/protocol/openid-connect/token", "client-id", "client-secret"))
 *         .build()) {
 *     ScimUser user = scimClient.users().get("user-id");
 * }
 * </pre>
 */
public final class ScimClient implements AutoCloseable {

    /** SCIM JSON 媒体类型。 */
    private static final String APPLICATION_SCIM_JSON = "application/scim+json";

    private final SimpleHttp http;
    private String baseUrl;
    private AuthorizationMethod authorizationMethod;

    private ScimClient(HttpClient http) {
        this.http = SimpleHttp.create(http);
    }

    public static Builder create(HttpClient httpClient) {
        return new Builder(httpClient);
    }

    /** 返回 Users 资源客户端。 */
    public ScimUsersClient users() {
        return new ScimUsersClient(this);
    }

    /** 返回 Groups 资源客户端。 */
    public ScimGroupsClient groups() {
        return new ScimGroupsClient(this);
    }

    /** 返回 ServiceProviderConfig 客户端。 */
    public ScimConfigClient config() {
        return new ScimConfigClient(this);
    }

    public ScimResourceTypesClient resourceTypes() {
        return new ScimResourceTypesClient(this);
    }

    public ScimSchemasClient schemas() {
        return new ScimSchemasClient(this);
    }

    /**
     * 对 SCIM 基址下的相对路径执行原始 GET 请求。
     * 非成功状态码时抛出 {@link ScimClientException}。
     *
     * @param path 相对路径（如 "InvalidType"）
     */
    public void get(String path) {
        execute(doGet(path));
    }

    @Override
    public void close() {
        // no-op for now
    }

    SimpleHttpResponse execute(SimpleHttpRequest request) throws ScimClientException {
        try {
            SimpleHttpResponse response = request.asResponse();

            if (!Status.Family.familyOf(response.getStatus()).equals(Status.Family.SUCCESSFUL)) {
                String payload = response.asString();

                try (response) {
                    ErrorResponse error = response.asJson(ErrorResponse.class);
                    throw new ScimClientException("Error response from SCIM server", error);
                } catch (ScimClientException sce) {
                    throw sce;
                } catch (Exception e) {
                    throw new ScimClientException("Unexpected error response from SCIM server", new ErrorResponse(payload, response.getStatus()));
                }
            }

            return response;
        } catch (ScimClientException sce) {
            throw sce;
        } catch (Exception e) {
            throw new ScimClientException("Unexpected response from SCIM server", e);
        }
    }

    SimpleHttpRequest doGet(String path) {
        return beforeRequest(http.doGet(baseUrl + path))
                .header(HttpHeaders.ACCEPT, APPLICATION_SCIM_JSON);
    }

    SimpleHttpRequest doGet(Class<? extends ResourceTypeRepresentation> resourceType, String path) {
        return beforeRequest(http.doGet(baseUrl + getResourceTypePath(resourceType) + path))
                .header(HttpHeaders.ACCEPT, APPLICATION_SCIM_JSON);
    }

    SimpleHttpRequest doGet(Class<? extends ResourceTypeRepresentation> resourceType) {
        return beforeRequest(http.doGet(baseUrl + getResourceTypePath(resourceType)))
                .header(HttpHeaders.ACCEPT, APPLICATION_SCIM_JSON);
    }

    SimpleHttpRequest doPost(Class<? extends ResourceTypeRepresentation> resourceType) {
        return beforeRequest(http.doPost(baseUrl + getResourceTypePath(resourceType)))
                .header(HttpHeaders.CONTENT_TYPE, APPLICATION_SCIM_JSON);
    }

    SimpleHttpRequest doPost(Class<? extends ResourceTypeRepresentation> resourceType, String path) {
        return beforeRequest(http.doPost(baseUrl + getResourceTypePath(resourceType) + path))
                .header(HttpHeaders.CONTENT_TYPE, APPLICATION_SCIM_JSON);
    }

    private String getResourceTypePath(Class<? extends ResourceTypeRepresentation> resourceType) {
        String path = "/" + resourceType.getSimpleName();

        if (resourceType.equals(ServiceProviderConfig.class)) {
            return path;
        }

        return path + "s";
    }

    SimpleHttpRequest doDelete(Class<? extends ResourceTypeRepresentation> resourceType, String id) {
        return beforeRequest(http.doDelete(baseUrl + getResourceTypePath(resourceType) + "/" + id));
    }

    SimpleHttpRequest doPut(Class<? extends ResourceTypeRepresentation> resourceType, String id) {
        return beforeRequest(http.doPut(baseUrl + getResourceTypePath(resourceType) + "/" + id))
                .header(HttpHeaders.CONTENT_TYPE, APPLICATION_SCIM_JSON);
    }

    SimpleHttpRequest doPatch(Class<? extends ResourceTypeRepresentation> resourceType, String id) {
        return beforeRequest(http.doPatch(baseUrl + getResourceTypePath(resourceType) + "/" + id))
                .header(HttpHeaders.CONTENT_TYPE, APPLICATION_SCIM_JSON);
    }

    <T> T execute(SimpleHttpRequest request, Class<T> responseType) {
        try (SimpleHttpResponse response = execute(request)) {
            if (responseType == null) {
                return null;
            }
            return response.asJson(responseType);
        } catch (IOException e) {
            throw new ScimClientException("Error executing request", e);
        }
    }

    private SimpleHttpRequest beforeRequest(SimpleHttpRequest request) {
        authorizationMethod.onBefore(http, request);
        return request;
    }

    private void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public static class Builder {

        /** 默认 SCIM API 路径前缀。 */
        private static final String DEFAULT_API_PATH = "/scim/v2/";

        private final ScimClient client;

        private Builder(HttpClient baseUrl) {
            client = new ScimClient(baseUrl);
        }

        /**
         * 设置 SCIM 服务器基址（如 https://scim.example.com），自动追加默认路径 /scim/v2/。
         *
         * @param baseUrl SCIM 服务器基址
         * @return 当前构建器
         */
        public Builder withBaseUrl(String baseUrl) {
            client.setBaseUrl(baseUrl + DEFAULT_API_PATH);
            return this;
        }

        /**
         * Configure the authorization method to use for requests. This method will be called before each request is sent,
         * allowing you to set the appropriate headers for authentication.
         *
         * @param method the authorization method to use for requests
         * @return this builder for chaining
         */
        public Builder withAuthorization(AuthorizationMethod method) {
            client.setAuthorizationMethod(method);
            return this;
        }

        /**
         * 构建并返回配置完成的 {@link ScimClient} 实例。
         *
         * @return 可用的 ScimClient
         */
        public ScimClient build() {
            return client.connect();
        }

    }

    private void setAuthorizationMethod(AuthorizationMethod method) {
        this.authorizationMethod = method;
    }

    private ScimClient connect() {
        return this;
    }

}
