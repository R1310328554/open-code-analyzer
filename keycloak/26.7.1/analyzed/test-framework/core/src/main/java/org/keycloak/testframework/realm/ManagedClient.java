package org.keycloak.testframework.realm;

import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.testframework.injection.ManagedTestResource;

/**
 * 测试框架创建的托管 OAuth/OIDC 客户端封装。
 * <p>
 * 提供 Admin API 访问、带自动回滚的临时更新，以及测试结束后的清理任务。
 */
public class ManagedClient extends ManagedTestResource {

    /** 创建客户端时使用的表示对象（含 id、clientId 等）。 */
    private final ClientRepresentation createdRepresentation;
    /** 对应客户端的 Admin REST 资源。 */
    private final ClientResource clientResource;

    /** 懒初始化的清理任务收集器。 */
    private ManagedClientCleanup cleanup;

    /**
     * @param createdRepresentation 已持久化的客户端表示
     * @param clientResource Admin 客户端资源
     */
    public ManagedClient(ClientRepresentation createdRepresentation, ClientResource clientResource) {
        this.createdRepresentation = createdRepresentation;
        this.clientResource = clientResource;
    }

    /**
     * 返回客户端内部 UUID。
     *
     * @return 客户端 UUID
     */
    public String getId() {
        return createdRepresentation.getId();
    }

    /**
     * 返回客户端对外标识 {@code clientId}。
     *
     * @return 客户端 clientId
     */
    public String getClientId() {
        return createdRepresentation.getClientId();
    }

    /**
     * 返回客户端密钥（若已配置）。
     *
     * @return 客户端 secret，未设置时为 {@code null}
     */
    public String getSecret() {
        return createdRepresentation.getSecret();
    }

    /**
     * 返回用于查看或修改客户端配置的 Admin 资源。
     * <p>
     * 一般应优先使用 {@link #updateWithCleanup}，避免直接修改导致后续测试状态不一致。
     *
     * @return 客户端 Admin 资源
     */
    public ClientResource admin() {
        return clientResource;
    }

    /**
     * 在测试内更新客户端，测试结束后自动恢复（当前通过标记 dirty 重建实现）。
     *
     * @param updates 要应用的客户端变更
     */
    public void updateWithCleanup(ManagedClient.ClientUpdate... updates) {
        ClientRepresentation rep = admin().toRepresentation();

        // TODO Admin v2：将字段设为 null 更新客户端时会被忽略（例如 adminUrl），
        // 暂无法完整还原原始配置；目前通过标记 dirty 重建客户端
        // cleanup().resetToOriginalRepresentation(rep);
        dirty();

        ClientBuilder configBuilder = ClientBuilder.update(rep);
        for (ManagedClient.ClientUpdate update : updates) {
            configBuilder = update.update(configBuilder);
        }

        ClientRepresentation updated = configBuilder.build();
        admin().update(updated);

        // TODO：可通过空字符串删除属性，但受上述限制影响，
        // 仍不能作为完整的还原方案
        // ClientRepresentation original = cleanup().getOriginalRepresentation();
        // updated.getAttributes().keySet().stream().filter(k -> !original.getAttributes().containsKey(k)).forEach(k -> original.getAttributes().put(k, ""));
    }

    /** 返回（必要时创建）客户端级清理任务收集器。 */
    public ManagedClientCleanup cleanup() {
        if (cleanup == null) {
            cleanup = new ManagedClientCleanup();
        }
        return cleanup;
    }

    /** {@inheritDoc} 执行已注册的客户端清理任务。 */
    @Override
    public void runCleanup() {
        if (cleanup != null) {
            cleanup.runCleanupTasks(clientResource);
            cleanup = null;
        }
    }

    /** 描述对 {@link ClientBuilder} 的单次变更。 */
    public interface ClientUpdate {

        /**
         * 应用变更并返回更新后的构建器。
         *
         * @param client 当前客户端构建器
         * @return 变更后的构建器
         */
        ClientBuilder update(ClientBuilder client);

    }

}
