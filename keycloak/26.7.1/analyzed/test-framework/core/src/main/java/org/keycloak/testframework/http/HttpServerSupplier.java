package org.keycloak.testframework.http;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.keycloak.testframework.annotations.InjectHttpServer;
import org.keycloak.testframework.injection.InstanceContext;
import org.keycloak.testframework.injection.LifeCycle;
import org.keycloak.testframework.injection.RequestedInstance;
import org.keycloak.testframework.injection.Supplier;

import com.sun.net.httpserver.HttpServer;

/**
 * 为 {@link InjectHttpServer} 注入 JDK {@link HttpServer} 的供应器。
 * <p>
 * 在 {@code 127.0.0.1:8500} 启动简易 HTTP 服务器，供测试回调或 mock 端点使用。
 */
public class HttpServerSupplier implements Supplier<HttpServer, InjectHttpServer> {

    /** {@inheritDoc} 创建并启动 HttpServer。 */
    @Override
    public HttpServer getValue(InstanceContext<HttpServer, InjectHttpServer> instanceContext) {
        try {
            HttpServer httpServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 8500), 10);
            httpServer.start();
            return httpServer;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** {@inheritDoc} 立即停止 HttpServer。 */
    @Override
    public void close(InstanceContext<HttpServer, InjectHttpServer> instanceContext) {
        instanceContext.getValue().stop(0);
    }

    /** {@inheritDoc} 所有 HttpServer 实例互相兼容。 */
    @Override
    public boolean compatible(InstanceContext<HttpServer, InjectHttpServer> a, RequestedInstance<HttpServer, InjectHttpServer> b) {
        return true;
    }

    /** {@inheritDoc} 默认 {@link LifeCycle#GLOBAL} 生命周期。 */
    @Override
    public LifeCycle getDefaultLifecycle() {
        return LifeCycle.GLOBAL;
    }

}
