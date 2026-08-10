package org.keycloak.testframework.events;

import java.io.IOException;

import org.keycloak.testframework.annotations.InjectSysLogServer;
import org.keycloak.testframework.injection.InstanceContext;
import org.keycloak.testframework.injection.LifeCycle;
import org.keycloak.testframework.injection.RequestedInstance;
import org.keycloak.testframework.injection.Supplier;
import org.keycloak.testframework.injection.SupplierOrder;
import org.keycloak.testframework.server.KeycloakServerConfigBuilder;
import org.keycloak.testframework.server.KeycloakServerConfigInterceptor;

/**
 * 为 {@link InjectSysLogServer} 注入 {@link SysLogServer} 并配置 Keycloak syslog 输出的供应器。
 * <p>
 * 将服务器日志与 {@code org.keycloak.events} 事件重定向到测试 syslog 端点。
 */
public class SysLogServerSupplier implements Supplier<SysLogServer, InjectSysLogServer>, KeycloakServerConfigInterceptor<SysLogServer, InjectSysLogServer> {

    /** {@inheritDoc} 创建并启动 {@link SysLogServer}。 */
    @Override
    public SysLogServer getValue(InstanceContext<SysLogServer, InjectSysLogServer> instanceContext) {
        try {
            return new SysLogServer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** {@inheritDoc} 默认 {@link LifeCycle#GLOBAL} 生命周期。 */
    @Override
    public LifeCycle getDefaultLifecycle() {
        return LifeCycle.GLOBAL;
    }

    /** {@inheritDoc} 停止 syslog 服务器。 */
    @Override
    public void close(InstanceContext<SysLogServer, InjectSysLogServer> instanceContext) {
        SysLogServer server = instanceContext.getValue();
        try {
            server.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** {@inheritDoc} 所有 syslog 服务器实例互相兼容。 */
    @Override
    public boolean compatible(InstanceContext<SysLogServer, InjectSysLogServer> a, RequestedInstance<SysLogServer, InjectSysLogServer> b) {
        return true;
    }

    /** {@inheritDoc} 配置 syslog handler 与 events 日志级别。 */
    @Override
    public KeycloakServerConfigBuilder intercept(KeycloakServerConfigBuilder serverConfig, InstanceContext<SysLogServer, InjectSysLogServer> instanceContext) {
        serverConfig.log()
                .handlers(KeycloakServerConfigBuilder.LogHandlers.SYSLOG)
                .syslogEndpoint(instanceContext.getValue().getEndpoint())
                .handlerLevel(KeycloakServerConfigBuilder.LogHandlers.SYSLOG, "INFO");

        serverConfig.option("spi-events-listener-jboss-logging-success-level", "INFO")
                .log().categoryLevel("org.keycloak.events", "INFO");

        return serverConfig;
    }

    /** {@inheritDoc} 在 Keycloak 服务器启动前执行配置。 */
    @Override
    public int order() {
        return SupplierOrder.BEFORE_KEYCLOAK_SERVER;
    }
}
