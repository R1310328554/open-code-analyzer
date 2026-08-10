package org.keycloak.testframework.http;

import java.io.IOException;
import java.util.List;
import javax.net.ssl.SSLContext;

import org.keycloak.testframework.annotations.InjectHttpClient;
import org.keycloak.testframework.https.ManagedCertificates;
import org.keycloak.testframework.injection.DependenciesBuilder;
import org.keycloak.testframework.injection.Dependency;
import org.keycloak.testframework.injection.InstanceContext;
import org.keycloak.testframework.injection.LifeCycle;
import org.keycloak.testframework.injection.RequestedInstance;
import org.keycloak.testframework.injection.Supplier;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 * 为 {@link InjectHttpClient} 注入 Apache {@link HttpClient} 的供应器。
 * <p>
 * 在启用 TLS 时使用 {@link ManagedCertificates} 提供的客户端 SSL 上下文。
 */
public class HttpClientSupplier implements Supplier<HttpClient, InjectHttpClient> {

    /** {@inheritDoc} 声明对 {@link ManagedCertificates} 的依赖。 */
    @Override
    public List<Dependency> getDependencies(RequestedInstance<HttpClient, InjectHttpClient> instanceContext) {
        return DependenciesBuilder.create(ManagedCertificates.class).build();
    }

    /** {@inheritDoc} 构建 HttpClient，可选 TLS 与重定向策略。 */
    @Override
    public HttpClient getValue(InstanceContext<HttpClient, InjectHttpClient> instanceContext) {
        HttpClientBuilder builder = HttpClientBuilder.create();

        ManagedCertificates managedCerts = instanceContext.getDependency(ManagedCertificates.class);

        if (managedCerts.isTlsEnabled()) {
            SSLContext sslContext = managedCerts.getClientSSLContext();
            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                    sslContext,
                    SSLConnectionSocketFactory.getDefaultHostnameVerifier()
            );

            builder.setSSLSocketFactory(sslSocketFactory);
        }

        if (!instanceContext.getAnnotation().followRedirects()) {
            builder.disableRedirectHandling();
        }

        return builder.build();
    }

    /** {@inheritDoc} 关闭 {@link CloseableHttpClient}。 */
    @Override
    public void close(InstanceContext<HttpClient, InjectHttpClient> instanceContext) {
        try {
            ((CloseableHttpClient) instanceContext.getValue()).close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** {@inheritDoc} 默认 {@link LifeCycle#GLOBAL} 生命周期。 */
    @Override
    public LifeCycle getDefaultLifecycle() {
        return LifeCycle.GLOBAL;
    }

    /** {@inheritDoc} 仅当 {@code followRedirects} 注解值相同时兼容。 */
    @Override
    public boolean compatible(InstanceContext<HttpClient, InjectHttpClient> a, RequestedInstance<HttpClient, InjectHttpClient> b) {
        return a.getAnnotation().followRedirects() == b.getAnnotation().followRedirects();
    }

}
