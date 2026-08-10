package org.keycloak.testframework.infinispan;

import java.util.Map;

/**
 * 测试框架托管的 Infinispan 服务器生命周期抽象。
 * <p>
 * 实现类负责启动/停止 Infinispan 进程（或等价环境），并向 Keycloak 提供 {@link #serverConfig()} 选项。
 */
public interface InfinispanServer {

    /** 启动 Infinispan 测试服务器。 */
    void start();

    /** 停止 Infinispan 测试服务器并释放资源。 */
    void stop();

    /**
     * 返回应注入 Keycloak 服务器的 Infinispan 相关 CLI/配置项。
     *
     * @return 键值对形式的服务器配置
     */
    Map<String, String> serverConfig();
}
