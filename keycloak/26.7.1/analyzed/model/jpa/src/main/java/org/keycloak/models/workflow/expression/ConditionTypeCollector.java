package org.keycloak.models.workflow.expression;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.workflow.ResourceType;
import org.keycloak.models.workflow.WorkflowConditionProvider;

import static org.keycloak.models.workflow.Workflows.getConditionProvider;
import static org.keycloak.models.workflow.expression.ConditionParserUtil.extractParameter;

/**
 * 遍历布尔条件解析树，求各 {@code conditionCall} 支持资源类型的交集。
 * <p>
 * 用于推断表达式整体适用的 {@link ResourceType} 集合。
 */
public class ConditionTypeCollector extends BooleanConditionParserBaseVisitor<Void> {

    private final KeycloakSession session;

    /** 各条件约束后的资源类型集合；首次访问 conditionCall 时初始化为全部类型。 */
    private Set<ResourceType> resourceTypes;

    public ConditionTypeCollector(KeycloakSession session) {
        this.session = session;
    }

    /**
     * 返回收集到的条件支持资源类型集合（不可变视图）。
     *
     * @throws IllegalStateException 若尚未遍历任何 conditionCall
     */
    public Set<ResourceType> getConditionTypes() {
        if (resourceTypes == null) {
            throw new IllegalStateException("ConditionTypeCollector has not been initialized");
        }
        return Collections.unmodifiableSet(resourceTypes);
    }

    // --- 收集逻辑 ---

    @Override
    public Void visitConditionCall(BooleanConditionParser.ConditionCallContext ctx) {
        if (resourceTypes == null) {
            resourceTypes = EnumSet.allOf(ResourceType.class);
        }

        String conditionName = ctx.Identifier().getText();
        WorkflowConditionProvider conditionProvider = getConditionProvider(session, conditionName, extractParameter(ctx.parameter()));
        // 与当前条件的支持类型取交集，逐步收窄
        resourceTypes.retainAll(List.of(conditionProvider.getSupportedResourceType()));

        // 无需继续访问 parameter 等子节点
        return null;
    }

}
