package org.keycloak.utils;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.keycloak.authorization.fgap.AdminPermissionsSchema;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.Permissions;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.services.resources.admin.fgap.GroupPermissionEvaluator;



/**
 * 组层级构建与成员关系查询工具。
 * <p>支持权限感知的组树填充及组织组简化版本。</p>
 */
public class GroupUtils {

    /** 将 {@link GroupModel} 转换为 {@link GroupRepresentation} 的函数式接口。 */
    @FunctionalInterface
    private interface GroupToRepresentationMapper {
        GroupRepresentation apply(GroupModel group);
    }

    /** 按权限或其他条件过滤组的函数式接口。 */
    @FunctionalInterface
    private interface GroupFilter {
        boolean shouldInclude(GroupModel group);
    }

    /**
     * 从子组向上构建组层级树的核心实现（两个公开方法的共享逻辑）。
     *
     * @param session 当前 Keycloak 会话
     * @param realm 目标领域
     * @param groups 待填充层级的起始组流
     * @param mapper 组模型到表示对象的转换函数
     * @param filter 是否包含某组的过滤函数
     * @param subGroupsCount 是否填充子组计数
     * @return 从根到叶、无多余兄弟节点的组表示流
     */
    private static Stream<GroupRepresentation> buildGroupHierarchy(
            KeycloakSession session,
            RealmModel realm,
            Stream<GroupModel> groups,
            GroupToRepresentationMapper mapper,
            GroupFilter filter,
            boolean subGroupsCount) {
        return buildGroupHierarchy(session, realm, groups, mapper, filter, subGroupsCount, null);
    }

    private static Stream<GroupRepresentation> buildGroupHierarchy(
            KeycloakSession session,
            RealmModel realm,
            Stream<GroupModel> groups,
            GroupToRepresentationMapper mapper,
            GroupFilter filter,
            boolean subGroupsCount,
            String stopAtParentId) {

        Map<String, GroupRepresentation> groupIdToGroups = new HashMap<>();

        groups.forEach(group -> {
            // 通过过滤器进行权限检查
            if (!filter.shouldInclude(group)) {
                return;
            }

            GroupRepresentation currGroup = mapper.apply(group);

            if (subGroupsCount) {
                populateSubGroupCount(group, currGroup);
            }

            groupIdToGroups.putIfAbsent(currGroup.getId(), currGroup);

            while (currGroup.getParentId() != null && !currGroup.getParentId().equals(stopAtParentId)) {
                GroupModel parentModel = session.groups().getGroupById(realm, currGroup.getParentId());

                // 对父组进行权限检查
                if (!filter.shouldInclude(parentModel)) {
                    groupIdToGroups.remove(currGroup.getId());
                    break;
                }

                GroupRepresentation parent = groupIdToGroups.computeIfAbsent(
                    currGroup.getParentId(),
                    id -> mapper.apply(parentModel)
                );

                if (subGroupsCount) {
                    populateSubGroupCount(parentModel, parent);
                }

                GroupRepresentation finalCurrGroup = currGroup;

                // 若父组已有相同子组则合并，否则追加
                Optional<GroupRepresentation> duplicateGroup = parent.getSubGroups() == null ?
                    Optional.empty() :
                    parent.getSubGroups().stream()
                        .filter(g -> g.equals(finalCurrGroup))
                        .findFirst();

                if (duplicateGroup.isPresent()) {
                    duplicateGroup.get().merge(currGroup);
                } else {
                    parent.getSubGroups().add(currGroup);
                }

                groupIdToGroups.remove(currGroup.getId());
                currGroup = parent;
            }
        });

        return groupIdToGroups.values().stream()
            .sorted(Comparator.comparing(GroupRepresentation::getName));
    }

    /**
     * 从给定子组向上加载父组直至根，构建完整层级树。
     * <p>结果流中每个 {@link GroupRepresentation} 包含与原始组相关的子组结构。</p>
     *
     * @param session 当前 Keycloak 会话
     * @param realm 目标领域
     * @param groups 待填充层级的起始组流
     * @param full 是否生成完整表示（含全部属性）
     * @param groupEvaluator 组权限评估器
     * @param subGroupsCount 是否填充子组计数
     * @return 从根到叶、无多余兄弟节点的组表示流
     */
    public static Stream<GroupRepresentation> populateGroupHierarchyFromSubGroups(KeycloakSession session, RealmModel realm, Stream<GroupModel> groups, boolean full, GroupPermissionEvaluator groupEvaluator, boolean subGroupsCount) {
        return buildGroupHierarchy(
            session,
            realm,
            groups,
            // 带权限信息的表示映射
            group -> toRepresentation(groupEvaluator, group, full),
            // 带权限检查的过滤器
            group -> {
                if (AdminPermissionsSchema.SCHEMA.isAdminPermissionsEnabled(realm)) {
                    return true; // FGAP v2 以不同方式处理权限
                }
                //TODO GROUPS do permissions work in such a way that if you can view the children you can definitely view the parents?
                return groupEvaluator.canView() || groupEvaluator.canView(group);
            },
            subGroupsCount
        );
    }

    /**
     * {@link #populateGroupHierarchyFromSubGroups(KeycloakSession, RealmModel, Stream, boolean, GroupPermissionEvaluator, boolean)} 的简化版，不做权限检查。
     * <p>适用于组织组等由组织层控制访问的场景。</p>
     *
     * @param stopAtParentId 非空时，遍历到 parentId 等于该值的组即停止，不包含其父组；用于排除内部组织根组
     */
    public static Stream<GroupRepresentation> populateGroupHierarchyFromSubGroups(KeycloakSession session, RealmModel realm, Stream<GroupModel> groups, boolean full, boolean subGroupsCount, String stopAtParentId) {
        return buildGroupHierarchy(
            session,
            realm,
            groups,
            // 不含权限信息的简单映射
            group -> full ?
                ModelToRepresentation.toRepresentation(group, true) :
                ModelToRepresentation.groupToBriefRepresentation(group),
            // 不过滤，始终包含所有组
            group -> true,
            subGroupsCount,
            stopAtParentId
        );
    }

    /**
     * 查询组的子组数量并写入表示对象。
     * <p>与 {@link #toRepresentation} 分离，避免在纯转换逻辑中触发数据库查询。</p>
     *
     * @param group 组模型
     * @param representation 组表示对象
     * @return 填充子组计数后的表示对象
     */
    public static GroupRepresentation populateSubGroupCount(GroupModel group, GroupRepresentation representation) {
        representation.setSubGroupCount(group.getSubGroupsCount());
        return representation;
    }

    // 源自 org.keycloak.admin.ui.rest.GroupsResource：为树中每组设置细粒度访问权限
    /**
     * 将组模型转为表示对象并附加细粒度访问信息。
     *
     * @param groupsEvaluator 组权限评估器
     * @param groupTree 组模型
     * @param full 是否生成完整表示
     * @return 含 access 字段的组表示
     */
    public static GroupRepresentation toRepresentation(GroupPermissionEvaluator groupsEvaluator, GroupModel groupTree, boolean full) {
        GroupRepresentation rep = ModelToRepresentation.toRepresentation(groupTree, full);
        rep.setAccess(groupsEvaluator.getAccess(groupTree));
        return rep;
    }

    /** 获取给定组的全部成员关系（含间接父组），默认 {@code direct=true}。 */
    public static Set<GroupMembership> getAllMemberships(KeycloakSession session, Collection<GroupModel> groups) {
        return getAllMemberships(session, groups, true);
    }

    /**
     * 递归收集给定组的成员关系集合。
     *
     * @param session 当前会话
     * @param groups 起始组集合
     * @param direct 当前层级是否为直接成员关系
     * @return 去重后的成员关系集合
     */
    public static Set<GroupMembership> getAllMemberships(KeycloakSession session, Collection<GroupModel> groups, boolean direct) {
        Set<GroupMembership> memberships = new HashSet<>();
        Permissions permissions = session.getContext().getPermissions();

        for (GroupModel group : groups) {
            if (!permissions.hasPermission(group, AdminPermissionsSchema.GROUPS_RESOURCE_TYPE, AdminPermissionsSchema.VIEW)) {
                continue;
            }

            GroupMembership membership = new GroupMembership(group, direct);

            if (!memberships.add(membership)) {
                continue;
            }

            if (group.getParentId() != null) {
                RealmModel realm = session.getContext().getRealm();
                GroupModel parent = session.groups().getGroupById(realm, group.getParentId());

                if (parent != null) {
                    memberships.addAll(getAllMemberships(session, List.of(parent), false));
                }
            }
        }

        return memberships;
    }

    /** 组成员关系记录：{@code direct} 表示是否为直接成员。 */
    public record GroupMembership(GroupModel group, boolean direct) {

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof GroupMembership that)) return false;
            return Objects.equals(group, that.group);
        }

        @Override
        public int hashCode() {
            return Objects.hash(group);
        }
    }
}
