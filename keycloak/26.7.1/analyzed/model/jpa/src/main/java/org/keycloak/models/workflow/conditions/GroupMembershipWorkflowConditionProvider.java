package org.keycloak.models.workflow.conditions;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.jpa.entities.UserGroupMembershipEntity;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.models.workflow.ResourceType;
import org.keycloak.models.workflow.WorkflowConditionProvider;
import org.keycloak.models.workflow.WorkflowExecutionContext;
import org.keycloak.models.workflow.WorkflowInvalidStateException;
import org.keycloak.utils.StringUtil;

/**
 * 组成员关系工作流条件：判断用户是否为指定路径组的成员。
 * <p>
 * 支持运行时 {@link #evaluate} 与批量查询 {@link #toPredicate} 两种评估方式。
 */
public class GroupMembershipWorkflowConditionProvider implements WorkflowConditionProvider {

    /** 期望的组路径（如 {@code /parent/child}）。 */
    private final String expectedGroup;
    private final KeycloakSession session;

    public GroupMembershipWorkflowConditionProvider(KeycloakSession session,String expectedGroup) {
        this.session = session;
        this.expectedGroup = expectedGroup;
    }

    @Override
    public ResourceType getSupportedResourceType() {
        return ResourceType.USERS;
    }

    @Override
    public boolean evaluate(WorkflowExecutionContext context) {
        validate();

        RealmModel realm = session.getContext().getRealm();
        UserModel user = session.users().getUserById(realm, context.getResourceId());
        if (user == null) {
            return false;
        }

        GroupModel group = KeycloakModelUtils.findGroupByPath(session, realm, expectedGroup);
        return user.isMemberOf(group);
    }

    @Override
    public Predicate toPredicate(CriteriaBuilder cb, CriteriaQuery<String> query, Root<?> path) {
        validate();

        GroupModel group = KeycloakModelUtils.findGroupByPath(session, session.getContext().getRealm(), expectedGroup);
        if (group == null) {
            return cb.disjunction(); // 恒假
        }

        Subquery<Integer> subquery = query.subquery(Integer.class);
        Root<UserGroupMembershipEntity> from = subquery.from(UserGroupMembershipEntity.class);

        subquery.select(cb.literal(1));
        subquery.where(
                cb.and(
                        cb.equal(from.get("user").get("id"), path.get("id")),
                        cb.equal(from.get("groupId"), group.getId())
                )
        );

        return cb.exists(subquery);
    }

    @Override
    public void validate() {
        if (StringUtil.isBlank(this.expectedGroup)) {
            throw new WorkflowInvalidStateException("Expected group path not set.");
        }
        if (KeycloakModelUtils.findGroupByPath(session, session.getContext().getRealm(), expectedGroup) == null) {
            throw new WorkflowInvalidStateException(String.format("Group with name %s does not exist.", expectedGroup));
        }
    }

    @Override
    public void close() {

    }
}
