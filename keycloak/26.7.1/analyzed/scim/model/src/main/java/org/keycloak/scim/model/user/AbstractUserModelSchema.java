package org.keycloak.scim.model.user;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.keycloak.authorization.fgap.AdminPermissionsSchema;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.Permissions;
import org.keycloak.models.UserModel;
import org.keycloak.scim.resource.schema.AbstractModelSchema;
import org.keycloak.scim.resource.schema.attribute.Attribute;
import org.keycloak.scim.resource.user.User;
import org.keycloak.userprofile.AttributeMetadata;
import org.keycloak.userprofile.Attributes;
import org.keycloak.userprofile.UserProfile;
import org.keycloak.userprofile.UserProfileContext;
import org.keycloak.userprofile.UserProfileProvider;

import static java.util.Optional.ofNullable;

import static org.keycloak.scim.resource.schema.attribute.Attribute.getSchema;

/**
 * User SCIM schema 的抽象基类，基于 User Profile 动态解析属性映射。
 * <p>集成 FGAP 权限，控制 groups 等敏感属性的可见性与读写。</p>
 */
public abstract class AbstractUserModelSchema extends AbstractModelSchema<UserModel ,User> {

    /** User Profile 元数据中 SCIM 属性名的注解键。 */
    public static final String ANNOTATION_SCIM_SCHEMA_ATTRIBUTE = "kc.scim.schema.attribute";
    /** 当前 Keycloak 会话。 */
    private final KeycloakSession session;

    /** 以 schema URN 构造抽象 User schema。 */
    public AbstractUserModelSchema(KeycloakSession session, String name) {
        super(name);
        this.session = session;
    }

    /** 从 User Profile 收集可映射模型属性名（含 enabled、groups）。 */
    @Override
    protected Set<String> getModelAttributeNames() {
        UserProfile profile = getUserProfile();
        Attributes attributes = profile.getAttributes();
        Set<String> names = new HashSet<>(attributes.nameSet());

        names.add(UserModel.ENABLED);
        names.add("groups");

        return names;
    }

    /** 通过 Profile 注解将模型属性名解析为 SCIM 属性名。 */
    @Override
    protected String getAttributeSchemaName(String name) {
        if ("groups".equals(name)) {
            return name;
        }

        Object schema = getAttributeAnnotations(name).get(ANNOTATION_SCIM_SCHEMA_ATTRIBUTE);

        if (schema == null) {
            return null;
        }

        return String.valueOf(schema);
    }

    /** 读取 User 模型属性值，groups 受 VIEW 权限过滤。 */
    @Override
    protected Object getAttributeValue(UserModel model, String name) {
        if (UserModel.ENABLED.equals(name)) {
            return String.valueOf(model.isEnabled());
        }
        if ("groups".equals(name)) {
            Permissions permissions = session.getContext().getPermissions();

            if (permissions.hasPermission(model, AdminPermissionsSchema.USERS_RESOURCE_TYPE, AdminPermissionsSchema.VIEW)) {
                return model.getGroupsStream()
                        .filter(this::canViewGroup)
                        .toList();
            }

            return List.of();
        }
        if (UserModel.EMAIL.equals(name)) {
            return model.getEmail() == null ? List.of() : List.of(model.getEmail());
        }
        UserProfile profile = session.getProvider(UserProfileProvider.class).create(UserProfileContext.SCIM, model);
        Attributes attributes = profile.getAttributes();
        return attributes.getFirst(name);
    }

    private Map<String, Object> getAttributeAnnotations(String name) {
        AttributeMetadata metadata = getProfileAttributes().getMetadata(name);

        if (metadata == null) {
            return Map.of();
        }

        return ofNullable(metadata.getAnnotations()).orElse(Map.of());
    }

    private Attributes getProfileAttributes() {
        UserProfile profile = session.getProvider(UserProfileProvider.class).create(UserProfileContext.SCIM, Map.of());
        return profile.getAttributes();
    }

    /** 按 SCIM 路径在 Profile 属性中查找对应模型属性名。 */
    protected String createModelAttributeResolver(Attribute<UserModel, User> attribute) {
        for (String name : getModelAttributeNames()) {
            String scimName = getAttributeSchemaName(name);

            if (hasPath(attribute, scimName)) {
                return name;
            }
        }

        return null;
    }

    /** 判断属性是否属于当前 schema URN。 */
    protected boolean hasSchema(String attributeName) {
        return getId().equals(getSchema(attributeName));
    }

    protected UserProfile getUserProfile() {
        return session.getProvider(UserProfileProvider.class).create(UserProfileContext.SCIM, Map.of());
    }

    /** 判断当前会话是否可查看指定组。 */
    protected boolean canViewGroup(GroupModel group) {
        return session.getContext().getPermissions().hasPermission(group, AdminPermissionsSchema.GROUPS_RESOURCE_TYPE, AdminPermissionsSchema.VIEW);
    }
}
