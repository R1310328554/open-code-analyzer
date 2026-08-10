package org.keycloak.testframework.realm;

import java.util.Optional;

import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.testframework.injection.ManagedTestResource;

/**
 * 测试框架创建的托管 Keycloak 用户封装。
 * <p>
 * 提供 Admin API 访问、密码读取、带自动回滚的临时更新及测试结束清理。
 */
public class ManagedUser extends ManagedTestResource {

    /** 创建用户时使用的表示对象。 */
    private final UserRepresentation createdRepresentation;
    /** 对应用户的 Admin REST 资源。 */
    private final UserResource userResource;

    /** 懒初始化的用户级清理任务收集器。 */
    private ManagedUserCleanup cleanup;

    /**
     * @param createdRepresentation 已持久化的用户表示
     * @param userResource Admin 用户资源
     */
    public ManagedUser(UserRepresentation createdRepresentation, UserResource userResource) {
        this.createdRepresentation = createdRepresentation;
        this.userResource = userResource;
    }

    /** 返回用户内部 UUID。 */
    public String getId() {
        return createdRepresentation.getId();
    }

    /** 返回用户名。 */
    public String getUsername() {
        return createdRepresentation.getUsername();
    }

    /** 从创建表示中读取密码凭证值。 */
    public String getPassword() {
        return getPassword(createdRepresentation);
    }

    /** 返回用户 Admin REST 资源。 */
    public UserResource admin() {
        return userResource;
    }

    /**
     * 从用户表示的凭证列表中提取密码类型凭证值。
     *
     * @param userRepresentation 用户表示
     * @return 密码明文，未配置时为 {@code null}
     */
    public static String getPassword(UserRepresentation userRepresentation) {
        Optional<CredentialRepresentation> password = userRepresentation.getCredentials().stream().filter(c -> c.getType().equals(CredentialRepresentation.PASSWORD)).findFirst();
        return password.map(CredentialRepresentation::getValue).orElse(null);
    }

    /**
     * 在测试内更新用户，测试结束后自动还原为快照。
     *
     * @param updates 要应用的用户变更
     */
    public void updateWithCleanup(UserUpdate... updates) {
        UserRepresentation rep = admin().toRepresentation();
        cleanup().resetToOriginalRepresentation(rep);

        UserBuilder configBuilder = UserBuilder.update(rep);
        for (ManagedUser.UserUpdate update : updates) {
            configBuilder = update.update(configBuilder);
        }

        admin().update(configBuilder.build());
    }

    /** 返回（必要时创建）用户级清理任务收集器。 */
    public ManagedUserCleanup cleanup() {
        if (cleanup == null) {
            cleanup = new ManagedUserCleanup();
        }
        return cleanup;
    }

    /** {@inheritDoc} 执行已注册的用户清理任务。 */
    @Override
    public void runCleanup() {
        if (cleanup != null) {
            cleanup.runCleanupTasks(userResource);
            cleanup = null;
        }
    }

    /** 描述对 {@link UserBuilder} 的单次变更。 */
    public interface UserUpdate {

        /**
         * 应用变更并返回更新后的构建器。
         *
         * @param user 当前用户构建器
         * @return 变更后的构建器
         */
        UserBuilder update(UserBuilder user);

    }
}
