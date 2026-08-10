package org.keycloak.testframework.realm;

import java.util.LinkedList;
import java.util.List;

import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.RealmRepresentation;

/**
 * 收集并在测试结束后执行 Realm 相关的清理任务。
 * <p>
 * 支持自定义清理回调及将 Realm 表示还原为快照。
 */
public class ManagedRealmCleanup {

    /** 待执行的清理任务队列。 */
    private final List<RealmCleanup> cleanupTasks = new LinkedList<>();

    /**
     * 追加一条测试结束后执行的 Realm 清理任务。
     *
     * @param realmCleanup 清理回调
     * @return 当前收集器（便于链式调用）
     */
    public ManagedRealmCleanup add(RealmCleanup realmCleanup) {
        this.cleanupTasks.add(realmCleanup);
        return this;
    }

    /** 若尚未注册，则追加将 Realm 还原为给定表示快照的任务。 */
    void resetToOriginalRepresentation(RealmRepresentation rep) {
        if (cleanupTasks.stream().noneMatch(c -> c instanceof ResetRealm)) {
            RealmRepresentation clone = RepresentationUtils.clone(rep);
            cleanupTasks.add(new ResetRealm(clone));
        }
    }

    /** 依次执行所有清理任务并清空队列。 */
    void runCleanupTasks(RealmResource realm) {
        cleanupTasks.forEach(t -> t.cleanup(realm));
        cleanupTasks.clear();
    }

    /** 针对 {@link RealmResource} 的单次清理操作。 */
    public interface RealmCleanup {

        /**
         * 执行清理。
         *
         * @param realm 目标 Realm Admin 资源
         */
        void cleanup(RealmResource realm);

    }

    /** 将 Realm 更新回创建时的表示快照。 */
    private record ResetRealm(RealmRepresentation rep) implements RealmCleanup {

        /** {@inheritDoc} 调用 Admin API 写回快照表示。 */
        @Override
        public void cleanup(RealmResource realm) {
            realm.update(rep);
        }

    }

}
