package org.keycloak.testframework.remote.providers.runonserver;

import java.net.MalformedURLException;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

/**
 * {@link RunOnServerRealmResourceProvider} 的 SPI 工厂。
 * <p>
 * 按 {@code executionId} 缓存 {@link TestClassLoader}，以便反序列化测试侧提交的类。
 */
public class RunOnServerRealmResourceProviderFactory implements RealmResourceProviderFactory {

    /** Realm 资源提供者标识符。 */
    private static final String ID = "testing-run-on-server";

    private String executionId;
    private ClassLoader testClassLoader;

    /** {@inheritDoc} 创建绑定当前会话的 {@link RunOnServerRealmResourceProvider}。 */
    @Override
    public RealmResourceProvider create(KeycloakSession session) {
        return new RunOnServerRealmResourceProvider(session, this);
    }

    /** {@inheritDoc} 无后置初始化逻辑。 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    /** {@inheritDoc} 无持久资源需关闭。 */
    @Override
    public void close() {
    }

    /** {@inheritDoc} 返回 {@link #ID}。 */
    @Override
    public String getId() {
        return ID;
    }

    /**
     * 获取与给定执行 ID 关联的测试类加载器，必要时创建新实例。
     *
     * @param executionId 测试执行标识
     * @return 用于反序列化远程任务的 {@link TestClassLoader}
     */
    public ClassLoader getTestClassLoader(String executionId) {
        if (!executionId.equals(this.executionId)) {
            try {
                testClassLoader = new TestClassLoader();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
            this.executionId = executionId;
        }
        return testClassLoader;
    }

    /** {@inheritDoc} 初始化默认 {@link TestClassLoader}。 */
    @Override
    public void init(org.keycloak.Config.Scope config) {
        try {
            testClassLoader = new TestClassLoader();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

}
