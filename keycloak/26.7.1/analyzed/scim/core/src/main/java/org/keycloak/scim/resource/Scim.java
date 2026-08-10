package org.keycloak.scim.resource;

import org.keycloak.authorization.fgap.AdminPermissionsSchema;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.Permissions;

/**
 * SCIM 2.0 核心 schema URN、资源类型名称及辅助方法的常量类。
 * <p>提供资源类型到核心 schema 的映射，以及 Discovery 端点权限检查。</p>
 */
public final class Scim {

    // 核心 Schema URN
    public static final String ENTERPRISE_USER_SCHEMA = "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User";
    public static final String USER_CORE_SCHEMA = "urn:ietf:params:scim:schemas:core:2.0:User";
    public static final String GROUP_CORE_SCHEMA = "urn:ietf:params:scim:schemas:core:2.0:Group";
    public static final String SERVICE_PROVIDER_CONFIG_CORE_SCHEMA = "urn:ietf:params:scim:schemas:core:2.0:ServiceProviderConfig";
    public static final String SCHEMA_CORE_SCHEMA = "urn:ietf:params:scim:schemas:core:2.0:Schema";
    public static final String RESOURCE_TYPE_CORE_SCHEMA = "urn:ietf:params:scim:schemas:core:2.0:ResourceType";
    public static final String PATCH_OP_CORE_SCHEMA = "urn:ietf:params:scim:api:messages:2.0:PatchOp";

    // 核心资源类型名称
    public static final String USER_RESOURCE_TYPE = "User";
    public static final String GROUP_RESOURCE_TYPE = "Group";
    public static final String SERVICE_PROVIDER_CONFIG_RESOURCE_TYPE = "ServiceProviderConfig";
    public static final String RESOURCE_TYPE_RESOURCE_TYPE = "ResourceType";
    public static final String SCHEMA_RESOURCE_TYPE = "Schema";

    /**
     * 根据资源表示类返回对应的核心 SCIM schema URN。
     *
     * @param resourceType 资源表示类
     * @return 核心 schema URN
     * @throws IllegalArgumentException 未知资源类型
     */
    public static String getCoreSchema(Class<? extends ResourceTypeRepresentation> resourceType) {
        return switch (resourceType.getSimpleName()) {
            case USER_RESOURCE_TYPE -> USER_CORE_SCHEMA;
            case GROUP_RESOURCE_TYPE -> GROUP_CORE_SCHEMA;
            case SERVICE_PROVIDER_CONFIG_RESOURCE_TYPE -> SERVICE_PROVIDER_CONFIG_CORE_SCHEMA;
            case RESOURCE_TYPE_RESOURCE_TYPE -> RESOURCE_TYPE_CORE_SCHEMA;
            case SCHEMA_RESOURCE_TYPE -> SCHEMA_CORE_SCHEMA;
            default -> throw new IllegalArgumentException("Unknown resource type " + resourceType);
        };
    }

    /**
     * 判断当前会话是否具备访问 SCIM Discovery 端点的权限。
     * <p>拥有 Users 或 Groups 的 QUERY 权限即视为允许。</p>
     *
     * @param session Keycloak 会话
     * @return 有权限返回 true
     */
    public static boolean hasDiscoveryEndpointPermission(KeycloakSession session) {
        Permissions permissions = session.getContext().getPermissions();

        return permissions.hasPermission(AdminPermissionsSchema.USERS_RESOURCE_TYPE, AdminPermissionsSchema.QUERY)
                || permissions.hasPermission(AdminPermissionsSchema.GROUPS_RESOURCE_TYPE, AdminPermissionsSchema.QUERY);
    }
}
