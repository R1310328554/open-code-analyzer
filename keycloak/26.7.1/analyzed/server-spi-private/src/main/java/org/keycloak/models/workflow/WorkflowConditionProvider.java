package org.keycloak.models.workflow;


import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.keycloak.provider.Provider;

/**
 * 工作流条件评估提供者接口。
 * </p>
 * 实现类根据 {@link WorkflowExecutionContext} 判断条件是否满足，并可生成 JPA Criteria API {@link Predicate} 用于批量筛选资源。
 */
public interface WorkflowConditionProvider extends Provider {

    /**
     * 返回本条件实现所支持的 {@link }ResourceType}。
     *
     * @return the supported ResourceType for this condition implementation
     */
    ResourceType getSupportedResourceType();

    /**
     * 针对给定执行上下文评估条件是否成立。
     * </p>
     * 通常基于上下文中的资源对象判断；也可能依赖当前时间等与环境相关的因素。
     *
     * @param context the execution context for the workflow evaluation
     * @return {@code true} if the condition is met, {@code false} otherwise
     */
    boolean evaluate(WorkflowExecutionContext context);

    /**
     * 构造表示本条件的 JPA Criteria API {@link Predicate}，用于资源查询。
     * </p>
     * 实现应基于 {@link CriteriaBuilder}、{@link CriteriaQuery} 与资源实体 {@link Root} 构建谓词。
     *
     * @param cb the CriteriaBuilder used to construct predicates
     * @param query the CriteriaQuery being constructed
     * @param resourceRoot the Root representing the resource entity in the query
     * @return a Predicate representing this condition for use in a CriteriaQuery
     */
    Predicate toPredicate(CriteriaBuilder cb, CriteriaQuery<String> query, Root<?> resourceRoot);

    /**
     * 校验条件提供者的内部配置与状态。
     * </p>
     * 配置无效或无法安全运行时应抛出 {@link WorkflowInvalidStateException}。
     *
     * @throws WorkflowInvalidStateException if the provider is in an invalid state
     */
    void validate() throws WorkflowInvalidStateException;
}
