package org.keycloak.urls;

/** URL 类型：区分前端、后端与管理端请求所使用的主机名配置。 */
public enum UrlType {

    /** 面向浏览器/客户端的前端 URL。 */
    FRONTEND,
    /** 集群内部或后端通信 URL。 */
    BACKEND,
    /** 管理控制台 URL。 */
    ADMIN,
    /**
     * 本地管理 URL（已弃用，将移除）。
     * @deprecated to be removed
     */
    @Deprecated
    LOCAL_ADMIN

}
