package org.keycloak.testframework;

import java.util.List;
import java.util.Map;

import org.keycloak.testframework.admin.AdminClientFactorySupplier;
import org.keycloak.testframework.admin.AdminClientSupplier;
import org.keycloak.testframework.crypto.CryptoHelper;
import org.keycloak.testframework.crypto.CryptoHelperSupplier;
import org.keycloak.testframework.database.DevFileDatabaseSupplier;
import org.keycloak.testframework.database.DevMemDatabaseSupplier;
import org.keycloak.testframework.database.RemoteDatabaseSupplier;
import org.keycloak.testframework.database.TestDatabase;
import org.keycloak.testframework.events.AdminEventsSupplier;
import org.keycloak.testframework.events.EventsSupplier;
import org.keycloak.testframework.events.SysLogServerSupplier;
import org.keycloak.testframework.http.HttpClientSupplier;
import org.keycloak.testframework.http.HttpServerSupplier;
import org.keycloak.testframework.http.SimpleHttpSupplier;
import org.keycloak.testframework.https.CertificatesSupplier;
import org.keycloak.testframework.https.ManagedCertificates;
import org.keycloak.testframework.injection.Supplier;
import org.keycloak.testframework.realm.ClientSupplier;
import org.keycloak.testframework.realm.RealmSupplier;
import org.keycloak.testframework.realm.UserSupplier;
import org.keycloak.testframework.server.DistributionKeycloakServerSupplier;
import org.keycloak.testframework.server.EmbeddedKeycloakServerSupplier;
import org.keycloak.testframework.server.KeycloakServer;
import org.keycloak.testframework.server.KeycloakUrlsSupplier;
import org.keycloak.testframework.server.RemoteKeycloakServerSupplier;

/**
 * 核心测试框架扩展，注册服务器、数据库、Realm/用户、HTTP、证书等默认 Supplier。
 */
public class CoreTestFrameworkExtension implements TestFrameworkExtension {

    /** 返回核心集成测试所需的全部 Supplier。 */
    @Override
    public List<Supplier<?, ?>> suppliers() {
        return List.of(
                new AdminClientSupplier(),
                new AdminClientFactorySupplier(),
                new ClientSupplier(),
                new RealmSupplier(),
                new UserSupplier(),
                new DistributionKeycloakServerSupplier(),
                new EmbeddedKeycloakServerSupplier(),
                new RemoteKeycloakServerSupplier(),
                new KeycloakUrlsSupplier(),
                new DevMemDatabaseSupplier(),
                new DevFileDatabaseSupplier(),
                new RemoteDatabaseSupplier(),
                new SysLogServerSupplier(),
                new EventsSupplier(),
                new AdminEventsSupplier(),
                new HttpClientSupplier(),
                new HttpServerSupplier(),
                new SimpleHttpSupplier(),
                new CertificatesSupplier(),
                new CryptoHelperSupplier()
        );
    }

    /** 为常用值类型提供简短别名（如 server、database）。 */
    @Override
    public Map<Class<?>, String> valueTypeAliases() {
        return Map.of(
                KeycloakServer.class, "server",
                TestDatabase.class, "database",
                ManagedCertificates.class, "certificates",
                CryptoHelper.class, "crypto"
        );
    }

    /** {@link CryptoHelper} 在所有测试中始终启用。 */
    @Override
    public List<Class<?>> alwaysEnabledValueTypes() {
        return List.of(CryptoHelper.class);
    }
}
