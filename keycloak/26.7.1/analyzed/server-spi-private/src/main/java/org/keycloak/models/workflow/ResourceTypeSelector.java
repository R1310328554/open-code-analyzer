package org.keycloak.models.workflow;

import java.util.List;

/**
 * 资源类型选择器：筛选 realm 中符合指定 {@link Workflow} 条件的现有资源。
 * <p>{@link ResourceTypeSelector} 实现负责将工作流条件映射为可查询的资源 ID 集合。</p>
 *
 * @see WorkflowProvider#getResourceTypeSelector(ResourceType)
 */
public interface ResourceTypeSelector {

    /**
     * 查找符合工作流首步条件的全部资源 ID。
     *
     * @return 符合条件的资源 ID 列表
     */
    List<String> getResourceIds(Workflow workflow);

    /**
     * 按 ID 解析为领域资源对象。
     * @param resourceId 资源 ID
     * @return 解析后的资源
     */
    Object resolveResource(String resourceId);
}
