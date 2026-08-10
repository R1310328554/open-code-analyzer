package org.keycloak.models;

/**
 * 权限评估器：检查当前用户对 Realm 资源是否具备指定操作权限。
 * A permissions evaluator that can be used to check if the current user has permissions to perform an action on realm resources.
 */
public interface Permissions {

    /**
     * 若当前用户对指定资源类型与作用域具备权限则返回 {@code true}。
     * Returns {@code true} if the current user has permissions to perform an action on realm resources with the given {@code resourceType} and with the given {@code scope}.
     *
     * @param resourceType the realm resource type
     * @param scope the scope
     * @return {@code true} if the current user has permissions to perform an action on a realm resource type with the given scope, {@code false} otherwise
     */
    boolean hasPermission(String resourceType, String scope);

    /**
     * 针对特定模型实例检查当前用户是否具备指定资源类型与作用域的权限。
     * Returns {@code true} if the current user has permissions to perform an action on a realm resource type with the given scope
     *
     * @param resourceType the realm resource type
     * @param scope the scope
     * @return {@code true} if the current user has permissions to perform an action on a realm resource type with the given scope, {@code false} otherwise
     */
    boolean hasPermission(Model model, String resourceType, String scope);

    /**
     * 若用户（直接或通过组/复合角色）拥有任意管理员角色则返回 {@code true}。
     * Returns {@code true} if the given user has any admin role assigned, either directly, via group membership, or via composite roles.
     *
     * @param user the user to check
     * @return {@code true} if the user has any admin role, {@code false} otherwise
     */
    boolean isAdminUser(UserModel user);

    /**
     * 若组（直接或通过父组/复合角色）拥有任意管理员角色则返回 {@code true}。
     * Returns {@code true} if the given group has any admin role assigned, either directly, via parent groups, or via composite roles.
     *
     * @param group the group to check
     * @return {@code true} if the group has any admin role, {@code false} otherwise
     */
    boolean isAdminGroup(GroupModel group);
}
