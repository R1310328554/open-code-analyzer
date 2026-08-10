package org.keycloak.config;

import java.util.List;

import org.keycloak.common.enums.HostnameVerificationPolicy;

/**
 * 系统信任库、Kubernetes CA 与 TLS 主机名校验相关配置选项。
 */
public class TruststoreOptions {

    /** 配置选项：truststore-paths，系统信任库文件或目录列表。 */
    public static final Option<List<String>> TRUSTSTORE_PATHS = OptionBuilder.listOptionBuilder("truststore-paths", String.class)
            .category(OptionCategory.TRUSTSTORE)
            .description("List of pkcs12 (p12, pfx, or pkcs12 file extensions), PEM files, or directories containing those files that will be used as a system truststore.")
            .build();

    /** 配置选项：truststore-kubernetes-enabled，是否自动加载 Kubernetes/OpenShift 服务 CA。 */
    public static final Option<Boolean> TRUSTSTORE_KUBERNETES_CA_ENABLED = new OptionBuilder<>("truststore-kubernetes-enabled", Boolean.class)
            .category(OptionCategory.TRUSTSTORE)
            .description("If enabled, the server will automatically include the default Kubernetes service account CA certificate from \"/var/run/secrets/kubernetes.io/serviceaccount/ca.crt\" and the OpenShift service CA certificate from \"/var/run/secrets/kubernetes.io/serviceaccount/service-ca.crt\" when running in a container environment.")
            .defaultValue(true)
            .build();

    /** 配置选项：tls-hostname-verifier，出站 HTTPS/SMTP 请求的 TLS 主机名校验策略。 */
    public static final Option<HostnameVerificationPolicy> HOSTNAME_VERIFICATION_POLICY = new OptionBuilder<>("tls-hostname-verifier", HostnameVerificationPolicy.class)
            .category(OptionCategory.TRUSTSTORE)
            .description("The TLS hostname verification policy for out-going HTTPS and SMTP requests. ANY should not be used in production.")
            .defaultValue(HostnameVerificationPolicy.DEFAULT)
            .deprecatedValues("STRICT and WILDCARD have been deprecated, use DEFAULT instead.", HostnameVerificationPolicy.STRICT, HostnameVerificationPolicy.WILDCARD)
            .build();

}
