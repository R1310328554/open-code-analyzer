package org.keycloak.http.simple;

/** HTTP 方法枚举，供 {@link SimpleHttp} 与 {@link SimpleHttpRequest} 使用。 */
public enum SimpleHttpMethod {

    /** GET 请求。 */ GET,
    /** DELETE 请求。 */ DELETE,
    /** HEAD 请求。 */ HEAD,
    /** PUT 请求。 */ PUT,
    /** PATCH 请求。 */ PATCH,
    /** POST 请求。 */ POST,
    /** OPTIONS 请求。 */ OPTIONS

}
