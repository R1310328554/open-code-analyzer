package org.keycloak.testframework.realm;

import java.util.LinkedList;
import java.util.List;

import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.representations.idm.ClientRepresentation;

/**
 * 收集并在测试结束后执行客户端相关的清理任务。
 * <p>
 * 支持自定义清理回调及将客户端表示还原为快照。
 */
public class ManagedClientCleanup {

    /** 待执行的清理任务队列。 */
    private final List<ClientCleanup> cleanupTasks = new LinkedList<>();

    /**
     * 追加一条测试结束后执行的客户端清理任务。
     *
     * @param clientCleanup 清理回调
     * @return 当前收集器（便于链式调用）
     */
    public ManagedClientCleanup add(ClientCleanup clientCleanup) {
        this.cleanupTasks.add(clientCleanup);
        return this;
    }

    /** 若尚未注册，则追加将客户端还原为给定表示快照的任务。 */
    void resetToOriginalRepresentation(ClientRepresentation rep) {
        if (cleanupTasks.stream().noneMatch(c -> c instanceof ResetClient)) {
            ClientRepresentation clone = RepresentationUtils.clone(rep);
            cleanupTasks.add(new ResetClient(clone));
        }
    }

    /** 返回已注册的快照表示，若无则返回 {@code null}。 */
    ClientRepresentation getOriginalRepresentation() {
        ResetClient clientCleanup = (ResetClient) cleanupTasks.stream().filter(c -> c instanceof ResetClient).findFirst().orElse(null);
        return clientCleanup != null ? clientCleanup.rep() : null;
    }

    /** 依次执行所有清理任务并清空队列。 */
    void runCleanupTasks(ClientResource client) {
        cleanupTasks.forEach(t -> t.cleanup(client));
        cleanupTasks.clear();
    }

    /** 针对 {@link ClientResource} 的单次清理操作。 */
    public interface ClientCleanup {

        /**
         * 执行清理。
         *
         * @param client 目标客户端 Admin 资源
         */
        void cleanup(ClientResource client);

    }

    /** 将客户端更新回创建时的表示快照。 */
    private record ResetClient(ClientRepresentation rep) implements ClientCleanup {

        /** {@inheritDoc} 调用 Admin API 写回快照表示。 */
        @Override
        public void cleanup(ClientResource client) {
            client.update(rep);
        }

    }

}
