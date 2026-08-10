package org.keycloak.testframework.realm;


import java.util.LinkedList;
import java.util.List;

import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.UserRepresentation;

/**
 * 收集并在测试结束后执行用户相关的清理任务。
 * <p>
 * 支持自定义清理回调及将用户表示还原为快照。
 */
public class ManagedUserCleanup {

    /** 待执行的清理任务队列。 */
    private final List<UserCleanup> cleanupTasks = new LinkedList<>();


    /**
     * 追加一条测试结束后执行的用户清理任务。
     *
     * @param userCleanup 清理回调
     * @return 当前收集器（便于链式调用）
     */
    public ManagedUserCleanup add(ManagedUserCleanup.UserCleanup userCleanup) {
        this.cleanupTasks.add(userCleanup);
        return this;
    }

    /** 若尚未注册，则追加将用户还原为给定表示快照的任务。 */
    void resetToOriginalRepresentation(UserRepresentation rep) {
        if (cleanupTasks.stream().noneMatch(c -> c instanceof ManagedUserCleanup.ResetUser)) {
            UserRepresentation clone = RepresentationUtils.clone(rep);
            cleanupTasks.add(new ManagedUserCleanup.ResetUser(clone));
        }
    }

    /** 依次执行所有清理任务并清空队列。 */
    void runCleanupTasks(UserResource user) {
        cleanupTasks.forEach(t -> t.cleanup(user));
        cleanupTasks.clear();
    }

    /** 针对 {@link UserResource} 的单次清理操作。 */
    public interface UserCleanup {

        /**
         * 执行清理。
         *
         * @param user 目标用户 Admin 资源
         */
        void cleanup(UserResource user);

    }

    /** 将用户更新回创建时的表示快照。 */
    private record ResetUser(UserRepresentation rep) implements UserCleanup {

        /** {@inheritDoc} 调用 Admin API 写回快照表示。 */
        @Override
        public void cleanup(UserResource user) {
            user.update(rep);
        }
    }
}
