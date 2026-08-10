package org.keycloak.config;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.keycloak.common.crypto.FipsMode;

import static org.keycloak.config.OptionsUtil.DURATION_DESCRIPTION;


/**
 * HTTP/HTTPS 监听器、TLS 证书、线程池与优雅关闭相关配置选项。
 */
public class HttpOptions {

    /** 是否启用 HTTP 监听器 */
    public static final Option<Boolean> HTTP_ENABLED = new OptionBuilder<>("http-enabled", Boolean.class)
            .category(OptionCategory.HTTP)
            .description("Enables the HTTP listener. Enabled by default in development mode. Typically not enabled in production unless the server is fronted by a TLS termination proxy.")
            .defaultValue(Boolean.FALSE)
            .build();

    /** 配置选项：http host */
    public static final Option<String> HTTP_HOST = new OptionBuilder<>("http-host", String.class)
            .category(OptionCategory.HTTP)
            .description("The HTTP Host. In prod mode or when running on Windows Subsystem For Linux the default is to bind to all network addresses (0.0.0.0), which means the server may be accessible from other machines on your network. Otherwise defaults to localhost.")
            .build();

    /** 配置选项：http relative path */
    public static final Option<String> HTTP_RELATIVE_PATH = new OptionBuilder<>("http-relative-path", String.class)
            .category(OptionCategory.HTTP)
            .description("Set the path relative to '/' for serving resources. The path must start with a '/'.")
            .defaultValue("/")
            .buildTime(true)
            .build();

    /** 配置选项：http port */
    public static final Option<Integer> HTTP_PORT = new OptionBuilder<>("http-port", Integer.class)
            .category(OptionCategory.HTTP)
            .description("The used HTTP port.")
            .defaultValue(8080)
            .build();

    /** 配置选项：https port */
    public static final Option<Integer> HTTPS_PORT = new OptionBuilder<>("https-port", Integer.class)
            .category(OptionCategory.HTTP)
            .description("The used HTTPS port.")
            .defaultValue(8443)
            .build();

    /** HTTPS 客户端证书认证模式。 */

    public enum ClientAuth {
        /** 不要求客户端证书。 */
        none,
        /** 请求但不强制客户端证书。 */
        request,
        /** 强制要求客户端证书。 */
        required
    }

    /** 配置选项：https client auth */
    public static final Option<ClientAuth> HTTPS_CLIENT_AUTH = new OptionBuilder<>("https-client-auth", ClientAuth.class)
            .category(OptionCategory.HTTP)
            .description("Configures the server to require/request client authentication.")
            .defaultValue(ClientAuth.none)
            .buildTime(true)
            .build();

    /** 配置选项：https cipher suites */
    public static final Option<String> HTTPS_CIPHER_SUITES = new OptionBuilder<>("https-cipher-suites", String.class)
            .category(OptionCategory.HTTP)
            .description("The cipher suites to use. If none is given, a reasonable default is selected.")
            .build();

    public static final Option<List<String>> HTTPS_PROTOCOLS = OptionBuilder.listOptionBuilder("https-protocols", String.class)
            .category(OptionCategory.HTTP)
            .description("The list of protocols to explicitly enable. If a value is not supported by the JRE / security configuration, it will be silently ignored.")
            .expectedValues(Arrays.asList("TLSv1.3", "TLSv1.2"))
            .strictExpectedValues(false)
            .defaultValue(Arrays.asList("TLSv1.3", "TLSv1.2"))
            .build();

    /** 配置选项：https certificates reload period */
    public static final Option<String> HTTPS_CERTIFICATES_RELOAD_PERIOD = new OptionBuilder<>("https-certificates-reload-period", String.class)
            .category(OptionCategory.HTTP)
            .description("Interval on which to reload key store, trust store, and certificate files referenced by https-* options. " + DURATION_DESCRIPTION + " Must be greater than 30 seconds. Use -1 to disable.")
            .defaultValue("1h")
            .build();

    /** 配置选项：https certificate file */
    public static final Option<File> HTTPS_CERTIFICATE_FILE = new OptionBuilder<>("https-certificate-file", File.class)
            .category(OptionCategory.HTTP)
            .description("The file path to a server certificate or certificate chain in PEM format.")
            .build();

    /** 配置选项：https certificate key file */
    public static final Option<File> HTTPS_CERTIFICATE_KEY_FILE = new OptionBuilder<>("https-certificate-key-file", File.class)
            .category(OptionCategory.HTTP)
            .description("The file path to a private key in PEM format.")
            .build();

    /** 配置选项：https key store file */
    public static final Option<File> HTTPS_KEY_STORE_FILE = new OptionBuilder<>("https-key-store-file", File.class)
            .category(OptionCategory.HTTP)
            .description("The key store which holds the certificate information instead of specifying separate files.")
            .build();

    /** 配置选项：https key store password */
    public static final Option<String> HTTPS_KEY_STORE_PASSWORD = new OptionBuilder<>("https-key-store-password", String.class)
            .category(OptionCategory.HTTP)
            .description("The password of the key store file.")
            .defaultValue("password")
            .build();

    /** 配置选项：https key store type */
    public static final Option<String> HTTPS_KEY_STORE_TYPE = new OptionBuilder<>("https-key-store-type", String.class)
            .category(OptionCategory.HTTP)
            .description("The type of the key store file. " +
                    "If not given, the type is automatically detected based on the file extension. " +
                    "If '" + SecurityOptions.FIPS_MODE.getKey() + "' is set to '" + FipsMode.STRICT + "' and no value is set, it defaults to 'BCFKS'.")
            .build();

    /** 配置选项：https trust store file */
    public static final Option<File> HTTPS_TRUST_STORE_FILE = new OptionBuilder<>("https-trust-store-file", File.class)
            .category(OptionCategory.HTTP)
            .description("The trust store which holds the certificate information of the certificates to trust.")
            .build();

    /** 配置选项：https trust store password */
    public static final Option<String> HTTPS_TRUST_STORE_PASSWORD = new OptionBuilder<>("https-trust-store-password", String.class)
            .category(OptionCategory.HTTP)
            .description("The password of the trust store file.")
            .build();

    /** 配置选项：https trust store type */
    public static final Option<String> HTTPS_TRUST_STORE_TYPE = new OptionBuilder<>("https-trust-store-type", String.class)
            .category(OptionCategory.HTTP)
            .description("The type of the trust store file. " +
                    "If not given, the type is automatically detected based on the file extension. " +
                    "If '" + SecurityOptions.FIPS_MODE.getKey() + "' is set to '" + FipsMode.STRICT + "' and no value is set, it defaults to 'BCFKS'.")
            .build();

    /** 配置选项：http max queued requests */
    public static final Option<Integer> HTTP_MAX_QUEUED_REQUESTS = new OptionBuilder<>("http-max-queued-requests", Integer.class)
            .category(OptionCategory.HTTP)
            .description("Maximum number of queued HTTP requests. " +
                         "Use this to shed load in an overload situation. Excess requests will return a \"503 Server not Available\" response.")
            .build();

    /** 配置选项：http pool max threads */
    public static final Option<Integer> HTTP_POOL_MAX_THREADS = new OptionBuilder<>("http-pool-max-threads", Integer.class)
            .category(OptionCategory.HTTP)
            .description("The maximum number of threads. If this is not specified then it will be automatically sized " +
                         "to the greater of 4 * the number of available processors and 50. " +
                         "For example if there are 4 processors the max threads will be 50. " +
                         "If there are 48 processors it will be 192.")
            .build();

    /** 配置选项：http metrics histograms enabled */
    public static final Option<Boolean> HTTP_METRICS_HISTOGRAMS_ENABLED = new OptionBuilder<>("http-metrics-histograms-enabled", Boolean.class)
            .category(OptionCategory.HTTP)
            .description("Enables a histogram with default buckets for the duration of HTTP server requests.")
            .defaultValue(Boolean.FALSE)
            .build();

    /** 配置选项：http metrics slos */
    public static final Option<String> HTTP_METRICS_SLOS = new OptionBuilder<>("http-metrics-slos", String.class)
            .category(OptionCategory.HTTP)
            .description("Service level objectives for HTTP server requests. Use this instead of the default histogram, or use it in combination to add additional buckets. " +
                    "Specify a list of comma-separated values defined in milliseconds. Example with buckets from 5ms to 10s: 5,10,25,50,250,500,1000,2500,5000,10000")
            .build();

    /** 配置选项：http accept non normalized paths */
    public static final Option<Boolean> HTTP_ACCEPT_NON_NORMALIZED_PATHS = new OptionBuilder<>("http-accept-non-normalized-paths", Boolean.class)
            .category(OptionCategory.HTTP)
            .description("If the server should accept paths that are not normalized according to RFC3986 or that contain a double slash ('//') or semicolon (';'). While accepting those requests might be relevant for legacy applications, it is recommended to disable it to allow for more concise URL filtering.")
            .deprecated()
            .defaultValue(Boolean.FALSE)
            .build();

    /** 配置选项：shutdown timeout */
    public static final Option<String> SHUTDOWN_TIMEOUT = new OptionBuilder<>("shutdown-timeout", String.class)
            .category(OptionCategory.HTTP)
            .description("The shutdown period waiting for currently running HTTP requests to finish and distributed caches to settle. " + DURATION_DESCRIPTION)
            .defaultValue("10s")
            .build();

    /** 配置选项：shutdown delay */
    public static final Option<String> SHUTDOWN_DELAY = new OptionBuilder<>("shutdown-delay", String.class)
            .category(OptionCategory.HTTP)
            .description("Length of the pre-shutdown phase during which the server prepares for shutdown. " + DURATION_DESCRIPTION +
                    " This period allows for loadbalancer reconfiguration and draining of TLS/HTTP keepalive connections.")
            .defaultValue("1s")
            .build();
}
