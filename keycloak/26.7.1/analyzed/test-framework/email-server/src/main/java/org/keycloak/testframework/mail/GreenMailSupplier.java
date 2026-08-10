package org.keycloak.testframework.mail;

import org.keycloak.testframework.injection.InstanceContext;
import org.keycloak.testframework.injection.RequestedInstance;
import org.keycloak.testframework.injection.Supplier;
import org.keycloak.testframework.injection.SupplierOrder;
import org.keycloak.testframework.mail.annotations.InjectMailServer;
import org.keycloak.testframework.realm.RealmBuilder;
import org.keycloak.testframework.realm.RealmConfigInterceptor;

/**
 * 为 {@link InjectMailServer} 注入 {@link MailServer} 并配置 Realm SMTP 设置的供应器。
 * <p>
 * 使用固定本地主机与端口启动 GreenMail，并将发件地址写入 Realm 配置。
 */
public class GreenMailSupplier implements Supplier<MailServer, InjectMailServer>, RealmConfigInterceptor<MailServer, InjectMailServer> {

    /** GreenMail SMTP 监听主机。 */
    private final String HOSTNAME = "localhost";
    /** GreenMail SMTP 监听端口。 */
    private final int PORT = 3025;
    /** Realm SMTP 配置中的默认发件人地址。 */
    private final String FROM = "auto@keycloak.org";

    /** {@inheritDoc} 创建并启动 {@link MailServer}。 */
    @Override
    public MailServer getValue(InstanceContext<MailServer, InjectMailServer> instanceContext) {
        return new MailServer(HOSTNAME, PORT);
    }

    /** {@inheritDoc} 停止 GreenMail 服务器。 */
    @Override
    public void close(InstanceContext<MailServer, InjectMailServer> instanceContext) {
        instanceContext.getValue().stop();
    }

    /** {@inheritDoc} 所有邮件服务器实例互相兼容。 */
    @Override
    public boolean compatible(InstanceContext<MailServer, InjectMailServer> a, RequestedInstance<MailServer, InjectMailServer> b) {
        return true;
    }

    /** {@inheritDoc} 将 SMTP 主机、端口与发件人写入 Realm 配置。 */
    @Override
    public RealmBuilder intercept(RealmBuilder realm, InstanceContext<MailServer, InjectMailServer> instanceContext) {
        return realm.smtp(HOSTNAME, PORT, FROM);
    }

    /** {@inheritDoc} 在 Realm 创建前执行 SMTP 配置。 */
    @Override
    public int order() {
        return SupplierOrder.BEFORE_REALM;
    }
}
