package org.keycloak.broker.kubernetes;

/**
 * Kubernetes 客户端断言身份提供者相关常量。
 */
public interface KubernetesConstants {

    /** 集群内默认 Kubernetes OIDC issuer URL。 */
    String DEFAULT_KUBERNETES_ISSUER_URL = "https://kubernetes.default.svc.cluster.local";
    /** 环境变量：Kubernetes API 服务主机。 */
    String KUBERNETES_SERVICE_HOST_KEY = "KUBERNETES_SERVICE_HOST";
    /** 环境变量：Kubernetes API HTTPS 端口。 */
    String KUBERNETES_SERVICE_PORT_HTTPS_KEY = "KUBERNETES_SERVICE_PORT_HTTPS";
    /** Pod 内 ServiceAccount 投影令牌文件路径。 */
    String SERVICE_ACCOUNT_TOKEN_PATH = "/var/run/secrets/kubernetes.io/serviceaccount/token";

}
