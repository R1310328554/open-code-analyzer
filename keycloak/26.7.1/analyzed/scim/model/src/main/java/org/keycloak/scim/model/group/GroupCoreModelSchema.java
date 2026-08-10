package org.keycloak.scim.model.group;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.keycloak.authorization.fgap.AdminPermissionsSchema;
import org.keycloak.common.util.TriConsumer;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelValidationException;
import org.keycloak.models.Permissions;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.scim.protocol.ForbiddenException;
import org.keycloak.scim.resource.group.Group;
import org.keycloak.scim.resource.group.Member;
import org.keycloak.scim.resource.schema.AbstractModelSchema;
import org.keycloak.scim.resource.schema.attribute.Attribute;
import org.keycloak.utils.KeycloakSessionUtil;

import static org.keycloak.utils.StringUtil.isBlank;

/**
 * Group 核心 SCIM schema，映射 {@link GroupModel} 与 {@link Group} 表示。
 * <p>定义 displayName、externalId、members 及 meta 时间戳等属性，并集成 FGAP 成员权限校验。</p>
 */
public final class GroupCoreModelSchema extends AbstractModelSchema<GroupModel, Group> {

    /** 当前 Keycloak 会话，用于权限与成员查询。 */
    private final KeycloakSession session;

    /** 以 Group 核心 schema URN 构造。 */
    public GroupCoreModelSchema(KeycloakSession session) {
        super(Group.SCHEMA);
        this.session = session;
    }

    @Override
    public String getId() {
        return Group.SCHEMA;
    }

    @Override
    public String getName() {
        return "Group";
    }

    @Override
    public String getDescription() {
        return getName();
    }

    /** 返回 Group 模型侧可映射的属性名集合。 */
    @Override
    protected Set<String> getModelAttributeNames() {
        return Set.of("name", "externalId", "members");
    }

    /** 从 {@link GroupModel} 读取指定属性值，members 受 VIEW_MEMBERS 权限过滤。 */
    @Override
    protected Object getAttributeValue(GroupModel model, String name) {
        return switch (name) {
            case "name" -> model.getName();
            case "externalId" -> model.getFirstAttribute("externalId");
            case "members" -> {
                KeycloakSession session = KeycloakSessionUtil.getKeycloakSession();
                RealmModel realm = session.getContext().getRealm();
                Permissions permissions = session.getContext().getPermissions();
                Stream<UserModel> members = Stream.empty();

                if (permissions.hasPermission(model, AdminPermissionsSchema.GROUPS_RESOURCE_TYPE, AdminPermissionsSchema.VIEW_MEMBERS)) {
                    members = session.users().getGroupMembersStream(realm, model)
                            .filter(this::canViewUser);
                }

                yield members.toList();
            }
            default -> null;
        };
    }

    /** 将模型属性名映射为 SCIM schema 属性名。 */
    @Override
    protected String getAttributeSchemaName(String name) {
        return switch (name) {
            case "name" -> "displayName";
            case "externalId" -> name;
            case "members" -> "members";
            default -> null;
        };
    }

    /** 构建 displayName、externalId、meta 与 members 等属性映射器。 */
    @Override
    protected Map<String, Attribute<GroupModel, Group>> getAttributeMappers() {
        List<Attribute<GroupModel, Group>> attributes = new ArrayList<>(Attribute.<GroupModel, Group>simple("displayName")
                    .notCaseExact()
                    .modelAttributeResolver((attribute) -> {
                        if (attribute.getName().equals("displayName")) {
                            return "name";
                        }
                        return null;
                    })
                    .withModelSetter((m, name) -> {
                        if (name != null) {
                            m.setName(name);
                        }
                    })
                    .build());
        attributes.addAll(Attribute.<GroupModel, Group>simple("externalId")
                .immutable()
                .string()
                .withModelSetter(GroupModel::setSingleAttribute)
                .build());
        attributes.addAll(Attribute.<GroupModel, Group>simple("meta.created")
                .timestamp()
                .immutable()
                .modelAttributeResolver(attribute -> "createdTimestamp")
                .build());
        attributes.addAll(Attribute.<GroupModel, Group>simple("meta.lastModified")
                .timestamp()
                .modelAttributeResolver(attribute -> "lastModifiedTimestamp")
                .build());
        attributes.addAll(Attribute.<GroupModel, Group>complex("members", Member.class)
                .multivalued()
                .returned(Attribute.RETURNED_REQUEST)
                .modelAttributeResolver(Attribute::getName)
                .withModelSetter((TriConsumer<GroupModel, String, Set<Member>>) (model, name, values) -> {
                    if (!Optional.ofNullable(values).orElse(Set.of()).isEmpty()) {
                        // PUT 更新不支持直接管理成员，客户端应使用 PATCH
                        throw new ModelValidationException("Managing members on updates are not supported");
                    }
                }, (BiConsumer<Group, Collection<UserModel>>) (group, users) -> {
                    for (UserModel user : Optional.ofNullable(users).orElse(List.of())) {
                        if (!canViewUser(user)) {
                            throw new ModelValidationException("User with id " + user.getId() + " not found");
                        }

                        Member member = new Member();
                        member.setValue(user.getId());
                        member.setDisplay(user.getUsername());
                        member.setType("User");
                        group.addMember(member);
                    }
                })
                .withModelRemover((TriConsumer<GroupModel, String, Set<Member>>) (model, name, values) -> {
                    KeycloakSession session = KeycloakSessionUtil.getKeycloakSession();
                    RealmModel realm = session.getContext().getRealm();
                    checkGroupMembershipPermission(session.getContext().getPermissions(), model);

                    for (Member member : values) {
                        UserModel user = session.users().getUserById(realm, member.getValue());
                        if (user == null || !canViewUser(user)) {
                            throw new ModelValidationException("User with id " + member.getValue() + " not found");
                        }
                        checkRequireManageGroupMembership(session.getContext().getPermissions(), user);
                        user.leaveGroup(model);
                    }
                })
                .withModelAdder((TriConsumer<GroupModel, String, Set<Member>>) (model, name, values) -> {
                    KeycloakSession session = KeycloakSessionUtil.getKeycloakSession();
                    RealmModel realm = session.getContext().getRealm();
                    checkGroupMembershipPermission(session.getContext().getPermissions(), model);

                    for (Member member : values) {
                        UserModel user = session.users().getUserById(realm, member.getValue());
                        if (user == null || !canViewUser(user)) {
                            throw new ModelValidationException("User with id " + member.getValue() + " not found");
                        }
                        checkRequireManageGroupMembership(session.getContext().getPermissions(), user);
                        user.joinGroup(model);
                    }
                })
                .build());
        return attributes.stream().collect(Collectors.toMap(Attribute::getName, Function.identity()));
    }

    @Override
    public void populate(Group resource, GroupModel model) {
        super.populate(resource, model);
        setTimestamps(resource, model);
    }

    @Override
    public void populate(Group resource, GroupModel model, List<String> requestedAttributes, List<String> excludedAttributes) {
        super.populate(resource, model, requestedAttributes, excludedAttributes);
        setTimestamps(resource, model);
    }

    /** 校验 displayName 必填。 */
    @Override
    public void validate(Group representation) throws ModelValidationException {
        if (isBlank(representation.getDisplayName())) {
            throw new ModelValidationException("Display name is required");
        }
    }

    /** 将模型时间戳写入 SCIM 表示的内部字段。 */
    private void setTimestamps(Group resource, GroupModel model) {
        Long createdTimestamp = model.getCreatedTimestamp();
        if (createdTimestamp != null) {
            resource.setCreatedTimestamp(createdTimestamp);
        }
        Long lastModified = model.getLastModifiedTimestamp();
        if (lastModified != null) {
            resource.setLastModifiedTimestamp(lastModified);
        }
    }

    /** 校验当前会话是否具备管理组成员的权限。 */
    private static void checkGroupMembershipPermission(Permissions permissions, GroupModel group) {
        if (GroupModel.Type.ORGANIZATION.equals(group.getType()) && group.getOrganization() != null) {
            throw new ModelValidationException("Cannot access organization related group via non Organization API.");
        }
        if (permissions.isAdminGroup(group)) {
            throw new ForbiddenException();
        }
        if (!permissions.hasPermission(group, AdminPermissionsSchema.GROUPS_RESOURCE_TYPE, AdminPermissionsSchema.MANAGE_MEMBERSHIP)) {
            throw new ForbiddenException();
        }
    }

    private void checkRequireManageGroupMembership(Permissions permissions, UserModel model) {
        if (!permissions.hasPermission(model, AdminPermissionsSchema.USERS_RESOURCE_TYPE, AdminPermissionsSchema.MANAGE_GROUP_MEMBERSHIP)) {
            throw new ForbiddenException();
        }
    }

    /** 判断当前会话是否可查看指定用户。 */
    private boolean canViewUser(UserModel u) {
        Permissions permissions = session.getContext().getPermissions();
        return permissions.hasPermission(u, AdminPermissionsSchema.USERS_RESOURCE_TYPE, AdminPermissionsSchema.VIEW);
    }
}
