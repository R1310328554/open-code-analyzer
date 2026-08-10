package org.keycloak.representations.workflows;

import java.util.Collections;
import java.util.List;

import static java.util.Optional.ofNullable;

import static org.keycloak.representations.workflows.WorkflowConstants.CONFIG_ERROR;

/**
 * 工作流运行时状态表示，主要承载执行过程中产生的错误信息列表。
 */
public class WorkflowStateRepresentation {

    /** 运行时错误消息列表。 */
    private List<String> errors = Collections.emptyList();

    /** 默认构造函数。 */
    public WorkflowStateRepresentation() {}

    /**
     * 从工作流表示中提取错误配置构造状态。
     *
     * @param workflow 工作流表示
     */
    public WorkflowStateRepresentation(WorkflowRepresentation workflow) {
        this.errors = ofNullable(workflow.getConfigValues(CONFIG_ERROR)).orElse(Collections.emptyList());
    }

    /** @return 错误消息列表 */
    public List<String> getErrors() {
        return errors;
    }
}
